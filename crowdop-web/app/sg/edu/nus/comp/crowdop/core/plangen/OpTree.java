package sg.edu.nus.comp.crowdop.core.plangen;

import java.util.*;

import sg.edu.nus.comp.crowdop.core.datasummary.DataSummary;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdFill;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdJoin;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdSelect;
import sg.edu.nus.comp.crowdop.core.op.crowdop.DecisionTree;
import sg.edu.nus.comp.crowdop.core.op.machineop.PartialResult;
import sg.edu.nus.comp.crowdop.core.op.machineop.Relation;
import sg.edu.nus.comp.crowdop.sql.*;
import sg.edu.nus.comp.datastore.schema.*;

public class OpTree {
	public static class OpTreeNode {
		public Op mOp; // Operator
		public List<OpTreeNode> children; // Child operator
		public OpTreeNode mParent; // Parent operator
		
		public OpTreeNode (Op op, OpTreeNode parent) {
			mOp = op;
			mParent = parent;
			children = new ArrayList<OpTreeNode> ();
		}
		
		public String toString () {
			String str = mOp.getID () + ": " + mOp.toString();
			return str;
		}
	}
	
	int MAX_OP_ID = -1;
	public int assignOpID () {
		MAX_OP_ID ++;
		return MAX_OP_ID;
	}
	
	public OpTreeNode mRoot;
	
	public Double estimatedCost = null;
	public Double estimatedCard = null;
	
	public Map<String, OpTreeNode> leafIndex;
	
	public OpTree (SelectCommand queryCmd, DataSummary dataSummary) throws Exception {		
		// step1: create the leaf nodes which contain the relation (table)
		//        the list named leaves contains all the leaf nodes
		String[] tables = queryCmd.getTables();// the data from command
		leafIndex = new HashMap<String, OpTreeNode>();
		for (String t : tables) {
			int nodeId = assignOpID();
			Relation r = new Relation(nodeId, t);
			OpTreeNode l = new OpTreeNode(r, null);
			leafIndex.put(r.tableName, l);
		}
		// step2: create the intermediate node which contain join operators.
		//        Then link them to the leaf nodes.
		//        the QueryNode lastJoinNode is the output of this step
		Predicate[] joints = queryCmd.getJoins();// the data from command
		Map<String, OpTreeNode> joinIndex = new HashMap<String, OpTreeNode> (); 
		OpTreeNode lastJoinNode = null;
		for (Predicate current : joints) {
			Attribute left = current.getLeft(); // Left Relation . Attribute
			Attribute right = (Attribute) current.getRight(); // Right Relation . Attribute
			String key = left.getTableName() + "_" + right.getTableName();
			OpTreeNode node = joinIndex.get(key);
			if (node == null) {
				int nodeId = assignOpID ();
				CrowdJoin join = new CrowdJoin (nodeId, 
						left.getTableName(), right.getTableName());
				node = new OpTreeNode (join, null);
				
				// Find left/right child
				OpTreeNode leftRelation = leafIndex.get(left.getTableName());
				OpTreeNode rightRelation = leafIndex.get(((Attribute)right).getTableName());
				OpTreeNode leftJoinNode = getTopAncester(leftRelation);
				OpTreeNode rightJoinNode = getTopAncester(rightRelation);
				// Link the children with node
				node.children.add(leftJoinNode);
				node.children.add(rightJoinNode);
				leftJoinNode.mParent = node;
				rightJoinNode.mParent = node;
				// Set node as the last node
				lastJoinNode = node;

			}
			joinIndex.put(key, node);
			((CrowdJoin)node.mOp).addCondition(current);
		}
		// step 3: create the intermediate node which contain select operators.
		//         Then link them to the lastJoinNode
		Predicate[] predicates = queryCmd.getPredicates();// the data from
		OpTreeNode lastSelectNode = lastJoinNode;
		for (Predicate predicate : predicates) {
			String tableName = predicate.getLeft().getTableName();
			
			CrowdSelect select = new CrowdSelect (assignOpID (), tableName);
			select.addPredicate(predicate);
			OpTreeNode node = new OpTreeNode (select, null);
			if (lastSelectNode != null) {
				node.children.add(lastSelectNode);
				lastSelectNode.mParent = node;
			} else {
				OpTreeNode leaf = leafIndex.get(tableName);
				node.children.add(leaf);
				leaf.mParent = node;
			}
			lastSelectNode = node;
		}
		
		OpTreeNode lastNode = null;
		if (lastSelectNode != null) lastNode = lastSelectNode;
		else {
			lastNode = leafIndex.get(leafIndex.keySet().iterator().next());
		}
		
		// Consider the filling case
		Attribute[] prjAttrs = queryCmd.getFields();
		if (prjAttrs.length > 0 && 
				prjAttrs[0].getTableName() != null) {
			// The user has specify some projection attributes
			String prjTable = prjAttrs[0].getTableName();
			CrowdFill cfill = new CrowdFill(assignOpID (), prjTable);
			for (Attribute prjAttr : prjAttrs) {
				if (!prjAttr.getTableName().equals(prjTable)) {
					throw new Exception ("current version "
							+ "cannot suport projection over multiple tables");
				}
				Set<String> valueDomain = 
						dataSummary.valueDomain(prjTable, prjAttr.getAttributeName());
				cfill.addFillAttribute(prjAttr.getAttributeName(), valueDomain);
			}
			OpTreeNode cfillNode = new OpTreeNode(cfill, null);
			cfillNode.children.add(lastNode);
			lastNode.mParent = cfillNode;
			lastNode = cfillNode;
		}
		
		/*PartialResult prOp = new PartialResult (assignOpID ());
		mRoot = new OpTreeNode (prOp, null);
		

		mRoot.children.add(lastNode);
		lastNode.mParent = mRoot;*/
		mRoot = lastNode;
	}
	
	public OpTree() {
	}

	public OpTreeNode searchForOpNode (OpTreeNode root, int queryOpId) {
		if (root.mOp.getID() == queryOpId) return root;
		for (OpTreeNode child : root.children) {
			OpTreeNode found = searchForOpNode (child, queryOpId);
			if (found != null) return found;
		}
		return null;
	}
	
	/**
	 * Get leaf nodes
	 */
	public void getLeafNodes (List<OpTreeNode> leaves) {
		doGetLeafNodes (leaves, mRoot);
	}
	
	public void doGetLeafNodes (List<OpTreeNode> leaves, OpTreeNode root) {
		if (root == null) return;
		if (root.children.isEmpty()) leaves.add(root);
		for (OpTreeNode child : root.children) {
			doGetLeafNodes (leaves, child);
		}
	}
	
	/**
	 * Get top ancestor of a node
	 * @param node
	 * @return
	 */
	private OpTreeNode getTopAncester(OpTreeNode node) {
		while (node.mParent != null) {
			node = node.mParent;
		}
		return node;
	}
	
	public List<OpTreeNode> getCJoins () {
		List<OpTreeNode> cjoins = new ArrayList<OpTreeNode> ();
		doGetCJoins (mRoot, cjoins);
		return cjoins;
	}
	
	public static void doGetCJoins (OpTreeNode root, List<OpTreeNode> cjoins) {
		if (root == null) return;
		if (root.mOp instanceof CrowdSelect) return;
		if (root.mOp instanceof CrowdJoin) {
			cjoins.add(root);
		}
		for (OpTreeNode child : root.children) {
			doGetCJoins (child, cjoins);
		}
	}
	
	public OpTreeNode getCSelectLeaf (OpTreeNode root) {
		OpTreeNode node = root;
		while (node.children.size() > 0) {
			node = node.children.get(0);
		}
		return node;
	}
	
	/**
	 * This function returns a collection of paths. 
	 *   Each path consists of 1) Relation, and 2) CSelect
	 * @return
	 */
	public List<OpTreeNode> getCSelectPaths () {
		List<OpTreeNode> cselectPaths = new ArrayList<OpTreeNode> ();
		for (String leafRelation : leafIndex.keySet()) {
			OpTreeNode leaf = leafIndex.get(leafRelation);
			OpTreeNode node = leaf.mParent;
			OpTreeNode top = null;
			while (node != null) {
				if (node.mOp instanceof CrowdSelect) {
					top = node;
					node = node.mParent;
				} else {
					break;
				}
			}
			if (top != null) {
				cselectPaths.add(top);
			} else {
				cselectPaths.add(leaf);
			}
		}
		return cselectPaths;
	}
	
	public String toString() {
		return getString(mRoot, 0);
	}
	private static String getString(OpTreeNode root, int depth) {
		if (root == null) return null;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < depth; i ++) buffer.append("  ");
		buffer.append(root.toString());
		buffer.append("\n");
		if (root.mOp instanceof CrowdJoin) {
			DecisionTree dtree = ((CrowdJoin)root.mOp).dtree;
			//buffer.append(dtree != null ? dtree.getDepth() : null);
			if (dtree != null) {
			buffer.append(dtree.toString());
			buffer.append("\n");
			}
		}
		for (OpTreeNode child : root.children) {
			buffer.append(getString (child, depth + 1));
		}
		return buffer.toString();
	}

	public int getCSelectPathLength(OpTreeNode cselectRoot) {
		int length = 0;
		OpTreeNode node = cselectRoot;
		while (!node.children.isEmpty()) {
			length ++;
			node = node.children.get(0);
		}
		return length;
	}	
}
