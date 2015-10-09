package sg.edu.nus.comp.crowdop.core.plangen.optimize;

import java.util.*;

import sg.edu.nus.comp.crowdop.core.datasummary.DataSummary;
import sg.edu.nus.comp.crowdop.core.model.CrowdPricing;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdSelect;
import sg.edu.nus.comp.crowdop.core.plangen.*;
import sg.edu.nus.comp.crowdop.core.plangen.OpTree.OpTreeNode;
import sg.edu.nus.comp.datastore.*;
import sg.edu.nus.comp.datastore.schema.*;
import sg.edu.nus.comp.util.ScoredItem;


public class SelectOptimizer {
	public static String RANDOM_OPT = "Random";
	public static String SORT_RANDOM_OPT = "Sort+Random";
	public static String SORT_GREEDY_OPT = "Sort+Greedy";
	public static String CROWDOP_OPT = "CrowdOp";
	public static String PARALLEL_OPT = "Parallel";
	public static String SEQ_OPT = "Seq";
	
	DataSummary dataSummary;
	CrowdPricing cprice;
	
	public SelectOptimizer (DataSummary summary, CrowdPricing crowdPrice) {
		dataSummary = summary;
		cprice = crowdPrice;
	}
	
	/**
	 * Generate "optimization plan" as well as it monetary cost
	 * @param cselectRoot: the top of the CSelect path
	 * @param table: The corresponding table in the leaf
	 * @param latencyBound: The latency bound
	 * @param selectOpt: the select-optimization strategy
	 * @return: the optimization plan with its cost
	 */
	public OpTree genOptimizedPlan (OpTreeNode leaf, OpTreeNode cselectRoot, 
			String tableName, Integer latencyBound, String selectOpt) throws Exception {
		List<List<ScoredItem<Predicate>>> packs = null;
		double tableSize = dataSummary.tableCardinality (tableName);
		
		// Generate a query plan
		if (selectOpt.equals(CROWDOP_OPT)) {
			List<ScoredItem<Predicate>> sorted = sortCSelectOpNodes (cselectRoot, false);
			packs =	repackCSelectOpNodes (cselectRoot, sorted, latencyBound, tableName, tableSize);
		} else if (selectOpt.equals(SORT_RANDOM_OPT)) {
			List<ScoredItem<Predicate>> sorted = sortCSelectOpNodes (cselectRoot, true);
			packs = this.randompackCSelectOpNodes(cselectRoot, sorted, latencyBound, tableName, tableSize);
		} else if (selectOpt.equals(SORT_RANDOM_OPT)) {
			List<ScoredItem<Predicate>> sorted = sortCSelectOpNodes (cselectRoot, false);
			packs = this.randompackCSelectOpNodes(cselectRoot, sorted, latencyBound, tableName, tableSize);
		} else if (selectOpt.equals(SORT_GREEDY_OPT)) {
			List<ScoredItem<Predicate>> sorted = sortCSelectOpNodes (cselectRoot, false);
			packs = this.greedypackCSelectOpNodes(cselectRoot, sorted, latencyBound, tableName, tableSize);
		} else if (selectOpt.equals(PARALLEL_OPT)) {
			List<ScoredItem<Predicate>> sorted = sortCSelectOpNodes (cselectRoot, true);
			packs =	new ArrayList<List<ScoredItem<Predicate>>> ();
			packs.add(sorted);
		} else if (selectOpt.equals(SEQ_OPT)) {
			List<ScoredItem<Predicate>> sorted = sortCSelectOpNodes (cselectRoot, false);
			Collections.shuffle(sorted);
			packs = this.greedypackCSelectOpNodes(cselectRoot, sorted, latencyBound, tableName, tableSize);
		}
		
		// Estimate the cost of the generated query plan
		OpTreeNode node = leaf;
		OpTreeNode newNode = new OpTreeNode (leaf.mOp, null);
		OpTree opt = new OpTree ();
		double estCost = 0.0;
		double inputSize = tableSize;
		for (int i = 0; i < packs.size(); i ++) {
			List<Predicate> preds = new ArrayList<Predicate> ();
			for (ScoredItem<Predicate> item : packs.get(i)) {
				preds.add(item.getItem());
			}
			double price = cprice.unitCSelectCost(tableName, preds);
			estCost += inputSize * price;
			double sel = dataSummary.selectivity (tableName, preds);
			inputSize *= sel;
			
			// Clone
			OpTreeNode oldParent = node.mParent;
			CrowdSelect newOp = (CrowdSelect) oldParent.mOp.cloneOp ();
			newOp.setPredicates(preds);
			OpTreeNode newParent = new OpTreeNode (newOp, null);
			// Link the pointers
			newParent.children.add(newNode);
			newNode.mParent = newParent;
			// Update for next loop
			node = oldParent;
			newNode = newParent;
		}
		opt.mRoot = newNode;
		opt.estimatedCost = estCost;
		opt.estimatedCard = inputSize;
		return opt;
	}
	
	/**
	 * Execute the optimization and reshape the CSelect path in the OP-Tree
	 * @param cselectRoot: The original top
	 * @param leaf: The leaf node
	 * @param packs: The optimization plan
	 */
	public void execOptimization (OpTreeNode cselectRoot, OpTreeNode leaf, List<List<ScoredItem<Predicate>>> packs) {
		// Step 3: Re-structure the tree
		OpTreeNode node = leaf;
		for (int i = 0; i < packs.size(); i ++) {
			node = node.mParent;
			List<ScoredItem<Predicate>> preds = packs.get(i);
			CrowdSelect crowdSelect = (CrowdSelect) node.mOp;
			crowdSelect.clearPredicates();
			for (ScoredItem<Predicate> item : preds) {
				crowdSelect.addPredicate(item.getItem());
			}
		}
		OpTreeNode originTop = cselectRoot;
		OpTreeNode newTop = node;
		
		if (originTop == newTop) return;
		// Replace cselectPath with newTop
		OpTreeNode newParent = originTop.mParent;
		OpTreeNode oldParent = newTop.mParent;
		
		oldParent.children.remove(newTop);
		newParent.children.remove(originTop);
		newParent.children.add(newTop);
		
		newTop.mParent = newParent;
		originTop.mParent = null;
	}
	
	
	/**
	 * The 1st step of Multi-CSelect optimization
	 * @param root: the tree consisting with CSelects
	 * @return
	 */
	private List<ScoredItem<Predicate>> sortCSelectOpNodes (OpTreeNode root, boolean isRandom) throws Exception {
		List<ScoredItem<Predicate>> sorted = new ArrayList<ScoredItem<Predicate>> ();
		OpTreeNode node = root;
		while (node != null) {
			Op op = node.mOp;
			if (op instanceof CrowdSelect) {
				CrowdSelect crowdSelect = (CrowdSelect) op;
				Predicate predicate = crowdSelect.getPredicates().get(0);
				String tableName = predicate.getLeft().getTableName();
				List<Predicate> predicates = new ArrayList<Predicate> ();
				predicates.add (predicate);
				double sortScore = -1;
				if (isRandom) {
					sortScore = Math.random();
				} else{
					double sel = dataSummary.selectivity(tableName, predicates);
					sortScore = sel;
				}
				ScoredItem<Predicate> item = new ScoredItem<Predicate> (predicate, sortScore);
				sorted.add(item);
			}
			// Check the child node
			if (node.children.isEmpty()) {
				node = null;
			} else {
				node = node.children.get(0);
			}
		}
		Collections.sort(sorted);
		return sorted;
	}
	
	/**
	 * The 2nd step of Multi-CSelect optimization
	 * @param root
	 * @param sorted
	 * @param maxLatency
	 */
	private List<List<ScoredItem<Predicate>>> repackCSelectOpNodes (OpTreeNode root, 
			List<ScoredItem<Predicate>> sorted, int maxLatency, String tableName, double tableSize) {
		// re-pack the CSelects in the reverse order
		int partitionNum = maxLatency - 1;
		Map<Integer, Map<Integer, Integer>> index = 
				new HashMap<Integer, Map<Integer, Integer>> ();
		// sorted-index --> partition-number --> partition-index
		double [][] costMatrix = new double[sorted.size()][partitionNum + 1];
		for (int i = 0; i < sorted.size(); i ++) {

			// Compute input size
			double inputSize = tableSize;
			for (int k = i + 1; k < sorted.size(); k ++) {
				inputSize *= sorted.get(k).getScore();
			}
			// Find the predicates;
			List<Predicate> predicates = new ArrayList<Predicate> ();
			for (int l = 0 ; l <= i; l ++) {
				predicates.add(sorted.get(l).getItem());
			}
			for (int j = 0; j <= partitionNum; j ++) { // # of partition
				double cost = -1.0;
				if (j == 0 || i == 0) {
					cost = inputSize * cprice.unitCSelectCost(tableName, predicates);
				} else {
					// Compute the cost of non-partition
					double nonCost = 
							inputSize * cprice.unitCSelectCost(tableName, predicates);
					
					// Find the partition-index that minimizes the cost
					double minCost = Double.MAX_VALUE;
					int minL = -1;
					for (int l = 0; l <= i - 1; l ++) {
						double cost1 = costMatrix[l][j - 1];
						List<Predicate> subPreds = predicates.subList(l+1, predicates.size());
						double cost2 = inputSize * cprice.unitCSelectCost(tableName, subPreds);
						cost = cost1 + cost2;
						if (cost < minCost) {
							minCost = cost;
							minL = l;
						}
					}
					
					if (cost < nonCost) {
						Map<Integer, Integer> map = index.get(i);
						if (map == null) map = new HashMap<Integer, Integer> ();
						map.put(j, minL);
						index.put(i, map);
					}
				}
				costMatrix[i][j] = cost;
			}
		}
		
		List<List<ScoredItem<Predicate>>> parts = 
				new ArrayList<List<ScoredItem<Predicate>>> ();
		int lastIndex = sorted.size() - 1;
		for (int j = partitionNum; j > 0; j --) {
			if (!index.containsKey(lastIndex) || 
					!index.get(lastIndex).containsKey(j)) {
				break;
			}
			int partitionIndex = index.get(lastIndex).get(j);
			parts.add( sorted.subList(partitionIndex + 1, lastIndex + 1));
			lastIndex = partitionIndex;
		}
		if (lastIndex >= 0) {
			parts.add(sorted.subList(0, lastIndex + 1));
		}
		
		return parts;
	}
	
	private List<List<ScoredItem<Predicate>>> greedypackCSelectOpNodes (OpTreeNode root, 
			List<ScoredItem<Predicate>> sorted, int maxLatency, String tableName, double tableSize) {
		List<List<ScoredItem<Predicate>>> parts = new ArrayList<List<ScoredItem<Predicate>>> ();
		int count;
		int latency = 0;
		for (count = sorted.size(); count > 0; count --) {
			if (latency == maxLatency - 1) break;
			parts.add(sorted.subList(count - 1, count));
			latency ++;
		}
		if (count > 0) { // there are unpacked predicates
			parts.add(sorted.subList(0, count ));
		}
		return parts;
	}
	
	/**
	 * Pack the predicates randomly
	 */
	private List<List<ScoredItem<Predicate>>> randompackCSelectOpNodes (OpTreeNode root, 
			List<ScoredItem<Predicate>> sorted, int maxLatency, String tableName, double tableSize) {
		List<List<ScoredItem<Predicate>>> parts = new ArrayList<List<ScoredItem<Predicate>>> ();
		
		Set<Integer> indexes = new HashSet<Integer> ();
		while (indexes.size() < maxLatency - 1) {
			int index = (int) (sorted.size() * Math.random());
			indexes.add(index);
		}
		
		int start = 0;
		for (int index : indexes) {
			parts.add(sorted.subList(start, index + 1));
			start = index + 1;
		}
		
		if (start < sorted.size()) {
			parts.add(sorted.subList(start, sorted.size()));
		}
		
		return parts;
	}
}
