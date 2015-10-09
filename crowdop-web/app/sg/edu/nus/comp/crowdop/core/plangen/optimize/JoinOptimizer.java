package sg.edu.nus.comp.crowdop.core.plangen.optimize;

import java.util.*;

import sg.edu.nus.comp.crowdop.core.datasummary.DataSummary;
import sg.edu.nus.comp.crowdop.core.model.CrowdPricing;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdJoin;
import sg.edu.nus.comp.crowdop.core.op.crowdop.DTreeNode;
import sg.edu.nus.comp.crowdop.core.op.crowdop.DecisionTree;
import sg.edu.nus.comp.crowdop.core.plangen.OpTree;
import sg.edu.nus.comp.crowdop.core.plangen.OpTree.OpTreeNode;
import sg.edu.nus.comp.datastore.schema.*;

public class JoinOptimizer {
	public static String CJOIN_ONLY_OPT = "CJoin-Only";
	public static String CROWDOP_APP_OPT = "CrowdOp-App";
	public static String CROWDOP_EXACT_OPT = "Crowd-Exact";
	public static String CFILL_FULL_OPT = "CFill-Full";
	public static String QURK_OPT = "Qurk";
	
	DataSummary dataSummary;
	CrowdPricing cprice;
	
	public JoinOptimizer (DataSummary summary, CrowdPricing crowdPrice) {
		dataSummary = summary;
		cprice = crowdPrice;
	}
	
	public OpTree genOptimizedPlan (CrowdJoin cjoin, 
			double leftInputCard, double rightInputCard, 
			Integer latencyBound, String joinOpt) throws Exception {
		DecisionTree dtree = new DecisionTree (cjoin);
		////////////////////////////////////////////
		// Strategies for building decision tree  //
		////////////////////////////////////////////
		if (joinOpt.equals(CJOIN_ONLY_OPT)) {
			dtree.mRoot = dtree.newNode(); // A trivial tree which only has the root
			
		} else if (joinOpt.equals(CFILL_FULL_OPT)) {
			List<Integer> cfillIndexes = new ArrayList<Integer> ();
			for (int i = 0; i < dtree.leftAttrs.size(); i ++) {
				cfillIndexes.add(i);
			}
			optFullFill (dtree, latencyBound, cfillIndexes);
		} else if (joinOpt.equals(QURK_OPT)) {
			optQurk (dtree, latencyBound, 0.5);
		} else if (joinOpt.equals(CROWDOP_APP_OPT)) {
			crowdOpAppConstruct (dtree, leftInputCard, rightInputCard, latencyBound);
		} else if (joinOpt.equals(CROWDOP_EXACT_OPT)) {
			crowdOpExactConstruct (dtree, leftInputCard, rightInputCard, latencyBound);
		}
		
		// Estimate the cost of the generated decision tree
		List<Integer> attrStack = new ArrayList<Integer> ();
		List<String> valueStack = new ArrayList<String> ();
		
		double estCost = estimateCost (dtree.mRoot, leftInputCard, rightInputCard, 
				attrStack, valueStack, dtree);
		CrowdJoin newCJoin = (CrowdJoin) cjoin.cloneOp();
		newCJoin.setDecisionTree(dtree);
		
		
		OpTreeNode newNode = new OpTreeNode (newCJoin, null);
		OpTree plan = new OpTree ();
		plan.mRoot = newNode;
		plan.estimatedCost = estCost;
		
		return plan;
	}
	
	
	/**
	 * Estimate the cost of the subtree rooted at "root"
	 * 
	 */
	public Double estimateCost (DTreeNode root, double leftInputCard, double rightInputCard, 
			List<Integer> attrStack, List<String> valueStack, DecisionTree dtree) throws Exception {
		if (root.children.isEmpty()) { // CJoin
			double cjoinCost = estimateCJoinCost(dtree, leftInputCard, 
					rightInputCard, attrStack, valueStack);
			return cjoinCost;
		} else {
			double cost = 0.0;
			double cfillCost = estimateCFillCost(dtree, leftInputCard, rightInputCard, 
					root.attrIndex,	attrStack, valueStack);
			cost += cfillCost;
			
			List<Integer> newAttrStack = forkNewList (attrStack, root.attrIndex);

			for (String value : root.children.keySet()) {
				DTreeNode child = root.children.get(value);
				List<String> newValueStack = forkNewList (valueStack, value); 
				
				double childCost = estimateCost (child, leftInputCard, rightInputCard, 
						newAttrStack, newValueStack, dtree);
				cost += childCost;
			}
			return cost;
		}
	}

	/**
	 * Optimization Strategy 1: Apply CFills to all the CFill attributes under latency bound
	 * @param dtree: The decision tree to be constructed
	 * @param latencyBound: The latency bound
	 */
	private void optFullFill (DecisionTree dtree, Integer latencyBound, 
			List<Integer> attrIndexes) throws Exception {
		dtree.mRoot = dtree.newNode();
		if (latencyBound == null) latencyBound = Integer.MAX_VALUE;
		List<DTreeNode> frontNodes = new ArrayList<DTreeNode> ();
		frontNodes.add(dtree.mRoot);
		
		for (int i = 0; i < attrIndexes.size() && i < latencyBound - 1; i ++) {
			int attrIndex = attrIndexes.get(i);
			List<DTreeNode> newFrontNodes = new ArrayList<DTreeNode> ();
			for (DTreeNode front : frontNodes) {
				front.setFillAttr(attrIndex); // Set CFill Attribute
				
				front.children = new HashMap<String, DTreeNode> ();
				Set<String> attrDomain = dtree.valueDomain(attrIndex, dataSummary);
				for (String value : attrDomain) {
					DTreeNode child = dtree.newNode();
					front.children.put(value, child);
					newFrontNodes.add(child);
				}
			}
			frontNodes = newFrontNodes;
		}
	}
	
	/**
	 * Optimization Strategy 2: 
	 * 			Apply Qurk algorithm to select CFill attribute having low selectivity
	 * @param dtree: The decision tree to be constructed
	 * @param latencyBound: The latency bound
	 */
	private void optQurk (DecisionTree dtree, Integer latencyBound, double selThreshold) throws Exception {
		// Step 1: Select attributes based on selectivity
		List<Integer> attrIndexes = new ArrayList<Integer> ();
		for (int i = 0; i < dtree.leftAttrs.size(); i ++) {
			double joinSel = 0.0;
			Set<String> attrDomain = dtree.valueDomain(i, dataSummary);
			for (String value : attrDomain) {
				
				double leftSel = estimateSelectivity (dtree.leftTable, 
						dtree.leftAttrs.get(i), value);
				double rightSel = estimateSelectivity (dtree.rightTable, 
						dtree.rightAttrs.get(i), value);
				
				joinSel += leftSel * rightSel;
			}
			//System.out.println ("$: " + joinSel);
			if (joinSel <= selThreshold) { // Only consider the attributes with low selectivity
				attrIndexes.add(i);
			} 
		}
		System.out.println ("Qurk: " + attrIndexes);
		// Step 2: Construct the tree from the selected attributes.  
		optFullFill (dtree, latencyBound, attrIndexes);
	}
	
	/**
	 * Optimization Strategy 3: 
	 * 			Apply a greedy-based algorithm to construct the decision tree
	 * @param dtree: The decision tree to be constructed
	 * @param latencyBound: The latency bound
	 */
	private void crowdOpAppConstruct (DecisionTree dtree, double leftInputCard, double rightInputCard, 
			Integer latencyBound) throws Exception {
		
		if (latencyBound == null) latencyBound = Integer.MAX_VALUE;
		List<Integer> attrStack = new ArrayList<Integer> ();
		List<String> valueStack = new ArrayList<String> ();
		
		dtree.mRoot = dtree.newNode();
		int latency = 1;
		appConstruct (dtree.mRoot, leftInputCard, rightInputCard, 
				attrStack, valueStack, latency, latencyBound, dtree);
	}
	
	public void appConstruct (DTreeNode root, double leftInputCard, double rightInputCard,
			List<Integer> attrStack, List<String> valueStack,  
			int latency, int latencyBound, DecisionTree dtree) throws Exception {
		// In the following cases, there is no need for further CFill
		double cjoinCost = estimateCJoinCost (dtree, leftInputCard, rightInputCard, attrStack, valueStack);
		if (latency == latencyBound) return; // Latency-bound
		if (latency == dtree.leftAttrs.size() + 1) return; // No more CFill attributes
		if (cjoinCost == 0.0) return; // If fill, no benefit for reducing join cost.

		double minCost = cjoinCost; 
		Integer selectedAttrIndex = null;
		
		for (int attrIndex = 0; attrIndex < dtree.leftAttrs.size(); attrIndex ++) {
			if (attrStack.contains(attrIndex)) continue; // attrIndex has been used
			
			// Compute CFill cost for 'attrIndex'
			double cfillCost = estimateCFillCost (dtree, leftInputCard, rightInputCard, 
					attrIndex, attrStack, valueStack);	
			
			// Compute CJoin cost
			double subCjoinCost = 0.0;
			List<Integer> newAttrStack = forkNewList (attrStack, attrIndex);
			Set<String> attrDomain = dtree.valueDomain(attrIndex, dataSummary);

			for (String value : attrDomain) {
				List<String> newValueStack = forkNewList (valueStack, value);
				subCjoinCost += estimateCJoinCost (dtree, leftInputCard, rightInputCard, 
						newAttrStack, newValueStack);
			}
			double cost = cfillCost + subCjoinCost;	
			if (cost < minCost) {
				minCost = cost;
				selectedAttrIndex = attrIndex;
			}
		}
		if (selectedAttrIndex == null) {
			return; // no need to fill
		}
		// Execute the CFill operation
		Set<String> valueDomain = dtree.valueDomain(selectedAttrIndex, dataSummary);
		root.setFillAttr(selectedAttrIndex);
		List<Integer> newAttrStack = forkNewList (attrStack, selectedAttrIndex);
					
		for (String value : valueDomain) {
			DTreeNode child = dtree.newNode();
			root.children.put(value, child);
			List<String> newValueStack = forkNewList (valueStack, value);
			// Construct the decision tree recursively
			appConstruct (child, leftInputCard, rightInputCard, newAttrStack, newValueStack,  
					latency + 1, latencyBound, dtree);
		}
	}
	
	

	/**
	 * Optimization Strategy 4: 
	 * 			Apply a DP-based algorithm to construct the decision tree
	 * @param dtree: The decision tree to be constructed
	 * @param latencyBound: The latency bound
	 */
	private void crowdOpExactConstruct (DecisionTree dtree, double leftInputCard, double rightInputCard, 
			Integer latencyBound) throws Exception {
		if (latencyBound == null) latencyBound = Integer.MAX_VALUE;
		
		List<Integer> attrStack = new ArrayList<Integer> ();
		List<String> valueStack = new ArrayList<String> ();
		List<Double> costs = new ArrayList<Double> ();
		int latency = 1;
		dtree.mRoot = doExactConstruct (dtree, leftInputCard, rightInputCard, attrStack, valueStack, 
				latency, latencyBound, costs);
		System.out.println ("Estimated Cost: " + costs.get(0));
	}
	
	public DTreeNode doExactConstruct (DecisionTree dtree, double leftInputCard, double rightInputCard, 
			List<Integer> attrStack, List<String> valueStack,  
			int latency, int latencyBound, List<Double> costs) throws Exception {
		DTreeNode root = dtree.newNode();
		double cjoinCost = estimateCJoinCost(dtree, leftInputCard, rightInputCard, attrStack, valueStack);
		if (latency == latencyBound || latency == dtree.leftAttrs.size() + 1 || cjoinCost == 0) {
			costs.add(cjoinCost);
			return root;
		}
		 
		Double minCost = cjoinCost;
		Integer selectedAttrIndex = null;
		Map<String, DTreeNode> selectedChildren = null;
		
		for (int attrIndex = 0; attrIndex < dtree.leftAttrs.size(); attrIndex ++) { // Examine every non-visited attribute
			if (attrStack.contains(attrIndex)) continue;
			// Compute CFill cost for 'cfillIndex'
			double cfillCost = estimateCFillCost (dtree, leftInputCard, rightInputCard, 
					attrIndex, attrStack, valueStack);	
			
			double subCJoinCost = 0.0;
			
			List<Integer> newAttrStack = forkNewList (attrStack, attrIndex);
			Set<String> attrDomain = dtree.valueDomain(attrIndex, dataSummary);
			
			Map<String, DTreeNode> children = new HashMap<String, DTreeNode> ();
			for (String value : attrDomain) {
				List<String> newValueStack = forkNewList (valueStack, value);
				List<Double> newCosts = new ArrayList<Double> ();
				DTreeNode child = doExactConstruct (dtree, leftInputCard, rightInputCard, 
						newAttrStack, newValueStack, latency + 1, latencyBound, newCosts);
				subCJoinCost += newCosts.get(0);
				children.put(value, child);
			}
			
			double cost = cfillCost + subCJoinCost;
			if (cost < minCost) {
				minCost = cost;
				selectedAttrIndex = attrIndex;
				selectedChildren = children;
			}
		}	
		
		if (selectedAttrIndex == null) {
			costs.add(cjoinCost);
		} else {
			root.setFillAttr(selectedAttrIndex);
			root.children = selectedChildren;
			costs.add(minCost);
		}
		return root;
	}
	
	private static List<Integer> forkNewList (List<Integer> list, int newElement) {
		List<Integer> newList = new ArrayList<Integer> ();
		newList.addAll(list);
		newList.add(newElement);
		return newList;
	}
	
	private static List<String> forkNewList (List<String> list, String newElement) {
		List<String> newList = new ArrayList<String> ();
		newList.addAll(list);
		newList.add(newElement);
		return newList;
	}
	
	
	/***********************************************************
	 * Estimation Functions
	 * 
	 *********************************************************/
	
	/**
	 * Estimate the cost of CJoin
	 * 
	 */
	private double estimateCFillCost (DecisionTree dtree, double leftInputSize, double rightInputSize, 
			int attrIndex, List<Integer> attrStack, List<String> valueStack) throws Exception {
		double cfillCost = 0.0;
		
		String leftAttr = dtree.leftAttrs.get(attrIndex).getAttributeName();
		double leftSel = estimateSelectivity (dtree.leftTable, dtree.leftAttrs, attrStack, valueStack);
		Set<String> leftDomain = dataSummary.valueDomain(dtree.leftTable, leftAttr);
		double leftPrice = cprice.unitCFillCost(dtree.leftTable, leftAttr, leftDomain);
		
		
		String rightAttr = dtree.rightAttrs.get(attrIndex).getAttributeName();
		double rightSel = estimateSelectivity (dtree.rightTable, dtree.rightAttrs, attrStack, valueStack);
		Set<String> rightDomain = dataSummary.valueDomain(dtree.rightTable, rightAttr);
		double rightPrice = cprice.unitCFillCost(dtree.rightTable, rightAttr, rightDomain);
		
		
		cfillCost += leftPrice * leftSel * leftInputSize;
		cfillCost += rightPrice * rightSel * rightInputSize;
		
		return cfillCost;
	}
	
	/**
	 * Estimate the cost of CJoin
	 * 
	 */
	private double estimateCJoinCost (DecisionTree dtree, double leftInputCard, double rightInputCard,  
			List<Integer> attrStack, List<String> valueStack) throws Exception {
		double leftSel = estimateSelectivity (dtree.leftTable, dtree.leftAttrs, attrStack, valueStack);
		double leftCard = leftInputCard * leftSel;
		double rightSel = estimateSelectivity (dtree.rightTable, dtree.rightAttrs, attrStack, valueStack);
		double rightCard = rightInputCard * rightSel;
		double cost = leftCard * rightCard * cprice.unitCJoinCost(null, null, null);
		return cost;
	}
	
	/**
	 * Generate predicates for obtaining selectivity
	 * 
	 */
	
	private Double estimateSelectivity (String tableName, Attribute attr, 
			String valueStr) throws Exception {
		List<Predicate> preds = new ArrayList<Predicate> ();
		Value value = new Value (valueStr, VariableType.STRING);
		
		Predicate pred = new Predicate (attr, ComparisonOp.EQUALS, value);
		preds.add(pred);
		double sel = dataSummary.selectivity(tableName, preds);
		return sel;
	}
	
	
	private Double estimateSelectivity (String tableName, List<Attribute> allAttrs, 
			List<Integer> attrIndexes, List<String> values) throws Exception {
		List<Predicate> preds = new ArrayList<Predicate> ();
		for (int i = 0; i < attrIndexes.size(); i ++) {
			int attrIndex = attrIndexes.get(i);
			Attribute attr = allAttrs.get(attrIndex);
			
			String valueStr = values.get(i);
			Value value = new Value (valueStr, VariableType.STRING);
			
			Predicate pred = new Predicate (attr, ComparisonOp.EQUALS, value);
			preds.add(pred);
		}
		double sel = dataSummary.selectivity(tableName, preds);
		
		return sel;
	}
}

