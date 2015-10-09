package sg.edu.nus.comp.crowdop.core.plangen.optimize;

import java.util.*;

import sg.edu.nus.comp.crowdop.core.model.CrowdPricing;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdJoin;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdSelect;
import sg.edu.nus.comp.crowdop.core.op.machineop.Relation;
import sg.edu.nus.comp.crowdop.core.plangen.OpTree.OpTreeNode;
import sg.edu.nus.comp.crowdop.core.plangen.*;
import sg.edu.nus.comp.crowdop.core.datasummary.DataSummary;
import sg.edu.nus.comp.util.Pair;



public class GlobalOptimizer {
	public static String NO_OPT = "NoOpt";
	public static String CROWDDB_OPT = "CrowdDbOpt";
	public static String CROWDOP_OPT = "CrowdOpOpt";

	OptOption optOption = null;
	DataSummary dataSummary = null;
	CrowdPricing cprice = null;
	
	public GlobalOptimizer (OptOption optimizeOption, DataSummary databaseState, 
			CrowdPricing crowdPrice) {
		optOption = optimizeOption;
		dataSummary = databaseState;
		cprice = crowdPrice;
	}
	
	public OpTree optimize (OpTree opTree, Double budget) throws Exception {
		if (optOption.glbOpt.equals(NO_OPT)) {
			return opTree; // Do nothing
		} else if (optOption.glbOpt.equals(CROWDDB_OPT)) {
			pushDownSelect(opTree); // Pushing down the Selects
			return opTree;
		} else if (optOption.glbOpt.equals(CROWDOP_OPT)) {
			pushDownSelect(opTree); // Pushing down the Selects
			SelectOptimizer selectOptimizer = new SelectOptimizer (dataSummary, cprice);
			JoinOptimizer joinOptimizer = new JoinOptimizer (dataSummary, cprice);
			List<OpTreeNode> cselectPaths = opTree.getCSelectPaths(); // CSelectPaths
			List<OpTreeNode> cjoinOps = opTree.getCJoins();
			
			int latencyLB = computeLatencyBound (cselectPaths, cjoinOps, false);
			int latencyUB = computeLatencyBound (cselectPaths, cjoinOps, true);
			
			OpTree opt = doOptimize (opTree, cselectPaths, cjoinOps, latencyLB, latencyUB, budget, 
					selectOptimizer, joinOptimizer);
			
			return opt;
		}
		return null;
	}

	/********************************************************************************
	 * Push Down the select operators
	 * @param opTree
	 ********************************************************************************/
	public void pushDownSelect (OpTree opTree) {
		doPushDownSelect (opTree.mRoot, opTree);
	}
	
	private static void doPushDownSelect (OpTreeNode root, OpTree opTree) {
		if (root == null) return;
		for (OpTreeNode child : root.children) {
			doPushDownSelect (child, opTree);
		}
		
		if (!(root.mOp instanceof CrowdSelect)) return;
		CrowdSelect select = (CrowdSelect) root.mOp;
		
		// Find the new place: the parent of the corresponding leaf
		String relation = select.getTableName();
		OpTreeNode leaf = opTree.leafIndex.get(relation);
		
		if (leaf.mParent == root) return; // No need to push
		if (root.children.size() != 1) return; // Invalid select
		if (!(root.children.get(0).mOp instanceof CrowdJoin)) return; 
		// Bottom-up

		// Remove root from the old place
		OpTreeNode oldChild = root.children.get(0);
		OpTreeNode oldParent = root.mParent;
		
		oldChild.mParent = oldParent;
		oldParent.children.remove(root);
		oldParent.children.add(oldChild);
	
		// Add root to the new place
		leaf.mParent.children.remove(leaf);
		leaf.mParent.children.add(root);
		root.children.clear();
		root.children.add(leaf);
		
		root.mParent = leaf.mParent;
		leaf.mParent = root;
	}
	
	/********************************************************************************
	 * Compute the bounds of latency
	 * @param cselectPaths
	 * @param cjoinOps
	 * @param b
	 * @return
	 ********************************************************************************/
	private static int computeLatencyBound(List<OpTreeNode> cselectPaths,
			List<OpTreeNode> cjoinOps, boolean isUB) {
		int bound = 0;
		// Step 1: Consider the CSelect paths
		for (OpTreeNode cselectPath : cselectPaths) {
			int latency = 1; // If computing LB, the latency is 1
			if (isUB) { // If computing UB, the latency is the length of the path
				latency = 0;
				OpTreeNode node = cselectPath;
				while (!node.children.isEmpty()) {
					latency ++;
					node = node.children.get(0);
				}
			}
			if (latency > bound) bound = latency; // Compute MAX
		}
		// Step 2: Consider the CJoin Ops
		int joinBound = 0;
		for (OpTreeNode cjoinOp : cjoinOps) {
			int latencyLB = 1;
			int latencyUB = ((CrowdJoin)cjoinOp.mOp).conditions.size() + 1;
			if (isUB) joinBound += latencyUB;
			else joinBound += latencyLB;
		}
		bound += joinBound;
		return bound;
	}
	
	/********************************************************************************
	 * Do the Optimization
	 * @param opTree
	 * @param cselectPaths
	 * @param cjoinOps
	 * @param latencyLB
	 * @param latencyUB
	 * @param budget
	 * @param selectOptimizer
	 * @param joinOptimizer
	 * @throws Exception
	 ********************************************************************************/
	private OpTree doOptimize(OpTree opTree, List<OpTreeNode> cselectPaths,
			List<OpTreeNode> cjoinOps, int latencyLB, int latencyUB, Double budget,
			SelectOptimizer selectOptimizer, JoinOptimizer joinOptimizer) throws Exception {
		
		if (budget == null) { // Optimize Objective 1: Minimizing monetary cost
			OpTree opt = doNoLatencyBoundOptimize (opTree, cselectPaths, cjoinOps, 
					selectOptimizer, joinOptimizer, latencyUB);
			return opt;
		}
		// Optimize Objective 2: Budget bounded optimization - A binary search strategy
		System.out.println("Latency Bounds: " + latencyLB + "," + latencyUB);
		OpTree opt = null;
		int lb = latencyLB;
		int ub = latencyUB;
		
		OpTree opt1 = doLatencyBoundOptimize (opTree, cselectPaths, cjoinOps, lb, 
				selectOptimizer, joinOptimizer);
		if (opt1.estimatedCost <= budget) { // lb must be the optimized latency
			return opt1;
		} 
		OpTree opt2 = doLatencyBoundOptimize (opTree, cselectPaths, cjoinOps, ub, 
				selectOptimizer, joinOptimizer);
		if (opt2.estimatedCost > budget) { // Even ub cannot meet the budget 
			return null; //no feasible plan!
		}
		opt = opt2;
		while (lb < ub - 1) {
			int cb = (lb + ub) / 2;
			OpTree opt3 = doLatencyBoundOptimize (opTree, cselectPaths, cjoinOps, cb, 
					selectOptimizer, joinOptimizer);
			if (opt3.estimatedCost > budget) { // the solution is within [cb, ub]
				lb = cb + 1;
			} else { // the solution is within [lb, cb]
				ub = cb;
				opt = opt3;
			}
		}
		return opt;
	}
	
	private OpTree doNoLatencyBoundOptimize(OpTree opTree,
			List<OpTreeNode> cselectPaths, List<OpTreeNode> cjoinOps,
			SelectOptimizer selectOptimizer, JoinOptimizer joinOptimizer, int latencyBound) throws Exception{
		Map<Set<String>, Map<Integer, OpTree>> planPool = new HashMap<Set<String>, Map<Integer, OpTree>> ();
		
		Set<String> entireRSet = new HashSet<String> ();
		for (OpTreeNode cselectPath : cselectPaths ) {
			OpTreeNode leaf = opTree.getCSelectLeaf(cselectPath);
			Relation relation = (Relation) leaf.mOp;
			Set<String> rset = new HashSet<String> ();
			rset.add(relation.tableName);
			entireRSet.add(relation.tableName);
			int pathLength = opTree.getCSelectPathLength (cselectPath);
			if (pathLength == 0) { // ONLY containing a Relation
				OpTree plan = new OpTree ();
				OpTreeNode node = new OpTreeNode (leaf.mOp, null);
				plan.mRoot = node;
				plan.estimatedCard = (double)dataSummary.tableCardinality(relation.tableName);
				plan.estimatedCost = 0.0;
				saveToPlanPool (rset, plan, latencyBound, planPool);
			} else {
				OpTree plan = selectOptimizer.genOptimizedPlan(leaf, cselectPath, relation.tableName, 
						latencyBound, optOption.selOpt);
				saveToPlanPool (rset, plan, latencyBound, planPool);
			}
		}
		
		for (int k = 1; k <= cjoinOps.size(); k ++) {
			for (OpTreeNode cjoinNode : cjoinOps) {
				CrowdJoin crowdJoin = (CrowdJoin) cjoinNode.mOp;
				List<Pair<Set<String>>> rsetPairs = genRSetPairs (planPool.keySet(), crowdJoin, k);
				for (Pair<Set<String>> pair : rsetPairs) {
					entireRSet.addAll(pair.getFormmer());
					entireRSet.addAll(pair.getLatter());
					
					OpTree plan1 = getFromPlanPool (pair.getFormmer(), latencyBound, planPool);
					OpTree plan2 = getFromPlanPool (pair.getLatter(), latencyBound, planPool);
					
					int maxJoinLatency = crowdJoin.conditions.size() + 1;
					OpTree plan = joinOptimizer.genOptimizedPlan(crowdJoin, plan1.estimatedCard, plan2.estimatedCard, 
							maxJoinLatency, optOption.joOpt);
					double cost = plan1.estimatedCost + plan2.estimatedCost + plan.estimatedCost;
					plan.estimatedCost = cost;
					plan.mRoot.children.add(plan1.mRoot);
					plan.mRoot.children.add(plan2.mRoot);
					
					Set<String> rset = new HashSet<String> ();
					rset.addAll(pair.getFormmer()); 
					rset.addAll(pair.getLatter());
					this.saveToPlanPool(rset, plan, latencyBound, planPool);
				}
			}
		}
		return this.getFromPlanPool(entireRSet, latencyBound, planPool);
	}

	private OpTree doLatencyBoundOptimize(OpTree opTree,
			List<OpTreeNode> cselectPaths, List<OpTreeNode> cjoinOps,
			int latencyBound, SelectOptimizer selectOptimizer, JoinOptimizer joinOptimizer) throws Exception{
		Map<Set<String>, Map<Integer, OpTree>> planPool = new HashMap<Set<String>, Map<Integer, OpTree>> ();
		if (cjoinOps.isEmpty() && cselectPaths.size() == 1) { // Special case 1: single select path, no join
			OpTreeNode cselectPath = cselectPaths.get(0);
			OpTreeNode leaf = opTree.getCSelectLeaf(cselectPath);
			Relation relation = (Relation) leaf.mOp;
			OpTree plan = selectOptimizer.genOptimizedPlan(leaf, cselectPath, relation.tableName, 
					latencyBound, optOption.selOpt);
			return plan;
		} else if (cjoinOps.size() == 1 && cselectPaths.isEmpty()) { // Special case 2: single join, no select
			CrowdJoin cjoin = (CrowdJoin) cjoinOps.get(0).mOp;
			double leftTableCard = dataSummary.tableCardinality(cjoin.leftRelation);
			double rightTableCard = dataSummary.tableCardinality(cjoin.rightRelation);
			OpTree plan = joinOptimizer.genOptimizedPlan(cjoin, leftTableCard, rightTableCard, 
					latencyBound, optOption.joOpt);
			for (OpTreeNode child : cjoinOps.get(0).children) {
				plan.mRoot.children.add(child);
			}
			return plan;
		}
		// The general case: 
		
		Set<String> entireRSet = new HashSet<String> ();
		for (OpTreeNode cselectPath : cselectPaths ) {
			OpTreeNode leaf = opTree.getCSelectLeaf(cselectPath);
			Relation relation = (Relation) leaf.mOp;
			int pathLength = opTree.getCSelectPathLength (cselectPath);
			if (pathLength == 0) { // ONLY containing a Relation
				OpTree plan = new OpTree ();
				OpTreeNode node = new OpTreeNode (leaf.mOp, null);
				plan.mRoot = node;
				plan.estimatedCard = dataSummary.tableCardinality(relation.tableName);
				plan.estimatedCost = 0.0;
				Set<String> rset = new HashSet<String> ();
				rset.add(relation.tableName);
				saveToPlanPool (rset, plan, 0, planPool);
			} else {
				for (int latency = 1; 
						latency <= Math.min(latencyBound - cjoinOps.size(), pathLength); latency ++) {				
					OpTree plan = selectOptimizer.genOptimizedPlan(leaf, cselectPath, relation.tableName, 
							latency, optOption.selOpt);
					Set<String> rset = new HashSet<String> ();
					rset.add(relation.tableName);
					saveToPlanPool (rset, plan, latency, planPool);
				}
			}
		}
		for (int k = 1; k <= cjoinOps.size(); k ++) {
			for (OpTreeNode cjoinNode : cjoinOps) {
				CrowdJoin crowdJoin = (CrowdJoin) cjoinNode.mOp;
				List<Pair<Set<String>>> rsetPairs = genRSetPairs (planPool.keySet(), crowdJoin, k);
				for (Pair<Set<String>> pair : rsetPairs) {
					entireRSet.addAll(pair.getFormmer());
					entireRSet.addAll(pair.getLatter());
					for (int latency = k; latency <= latencyBound + k - cjoinOps.size(); latency ++) {
						double minCost = Double.MAX_VALUE;
						OpTree optPlan = null;
						
						for (int sublatency = 1; sublatency <= latency ; sublatency ++) {
							OpTree plan1 = getFromPlanPool (pair.getFormmer(), latency - sublatency, planPool);
							OpTree plan2 = getFromPlanPool (pair.getLatter(), latency - sublatency, planPool);
							if (plan1 == null || plan2 == null) continue;
							if (pair.getFormmer().size() + pair.getLatter().size() == 3) {
								//System.out.println (pair.getFormmer() + "\t" + pair.getLatter() + "\t" + sublatency );
							}
							OpTree plan = joinOptimizer.genOptimizedPlan(crowdJoin, 
									plan1.estimatedCard, plan2.estimatedCard, 
									sublatency, optOption.joOpt);
							double cost = plan1.estimatedCost + plan2.estimatedCost + plan.estimatedCost;	
							
							if (cost < minCost ) {
								minCost = cost;
								optPlan = plan;
								optPlan.estimatedCost = cost;
								optPlan.mRoot.children.add(plan1.mRoot);
								optPlan.mRoot.children.add(plan2.mRoot);
							}
						}
						if (optPlan != null) {
							System.out.println("Latency Allocate: " + latency);
							Set<String> rset = new HashSet<String> ();
							rset.addAll(pair.getFormmer()); 
							rset.addAll(pair.getLatter());
							this.saveToPlanPool(rset, optPlan, latency, planPool);
						}
					}
				}
			}
		}
		return this.getFromPlanPool(entireRSet, latencyBound, planPool);
		
	}

	private OpTree getFromPlanPool(Set<String> rset, int latency, 
			Map<Set<String>, Map<Integer, OpTree>> planPool) {
		Map<Integer, OpTree> plans = planPool.get(rset);
		if (plans == null) {
			System.out.println();
		}
		for (int l = latency; l >= 0; l --) {
			if (plans.containsKey(l)) return plans.get(l);
		}
		return null;
	}

	private List<Pair<Set<String>>> genRSetPairs(Set<Set<String>> keySet,
			CrowdJoin crowdJoin, int k) {
		List<Pair<Set<String>>> list = new ArrayList<Pair<Set<String>>> ();
		List<Set<String>> setlist = new LinkedList<Set<String>> ();
		for (Set<String> rset : keySet) setlist.add(rset);
		for (int i = 0; i < setlist.size(); i ++) {
			Set<String> rset1 = setlist.get(i);
			for (int j = i + 1; j < setlist.size(); j ++) {
				Set<String> rset2 = setlist.get(j);				
				if (rset1.size() + rset2.size() != k + 1) continue;
				if (Collections.disjoint(rset1, rset2)) {
					if (rset1.contains(crowdJoin.leftRelation) && rset2.contains(crowdJoin.rightRelation)) {
						Pair<Set<String>> pair = new Pair<Set<String>> (rset1, rset2);
						list.add(pair);
					} else if (rset1.contains(crowdJoin.rightRelation) && rset2.contains(crowdJoin.leftRelation)) {
						Pair<Set<String>> pair = new Pair<Set<String>> (rset2, rset1);
						list.add(pair);
					}
					
				}
			}
		}
		return list;
	}

	private void saveToPlanPool(Set<String> rset, OpTree plan, int latency,
			Map<Set<String>, Map<Integer, OpTree>> planPool) {
		Map<Integer, OpTree> plans = planPool.get(rset);
		if (plans == null) plans = new HashMap<Integer, OpTree> ();
		OpTree oldPlan = plans.get(latency);
		if (rset.size() == 3 && oldPlan != null) {
			return;
		}
		if (oldPlan == null || oldPlan.estimatedCost > plan.estimatedCost) {
			plans.put(latency, plan);
		}
		planPool.put(rset, plans);
	}
	
}
