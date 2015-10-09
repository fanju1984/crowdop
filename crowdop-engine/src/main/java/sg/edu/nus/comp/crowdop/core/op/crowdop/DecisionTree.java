package sg.edu.nus.comp.crowdop.core.op.crowdop;

/**
 * The decision tree is used organize the data items into a tree. 
 * @author Ju Fan
 *
 */

import java.util.*;

import sg.edu.nus.comp.crowdop.core.datasummary.DataSummary;
import sg.edu.nus.comp.datastore.schema.*;

public class DecisionTree {	
	/////////////////////////////////////////////////////////////////////////
	int maxID = -1;
	public DTreeNode newNode () {
		maxID ++;
		DTreeNode node = new DTreeNode (maxID);
		return node;
	}
	//////////////////////////////////////////////////////////////////////////
	
	public String leftTable; 
	public List<Attribute> leftAttrs = new ArrayList<Attribute> ();
	
	public String rightTable;
	public List<Attribute> rightAttrs = new ArrayList<Attribute> ();
	
	public DTreeNode mRoot;
	
	public DecisionTree (CrowdJoin cjoin) {
		leftTable = cjoin.leftRelation;
		rightTable = cjoin.rightRelation;
		for (Predicate predicate : cjoin.getConditions()) {
			leftAttrs.add(predicate.getLeft());
			rightAttrs.add((Attribute)predicate.getRight());
		}
	}
	
	public Set<String> valueDomain (int attrIndex, DataSummary dbstate) throws Exception {
		Set<String> attrDomains = new HashSet<String> ();
		Set<String> leftDomain = dbstate.valueDomain(leftTable, 
				leftAttrs.get(attrIndex).getAttributeName());
		Set<String> rightDomain = dbstate.valueDomain(rightTable, 
				rightAttrs.get(attrIndex).getAttributeName());
		attrDomains.addAll(leftDomain);
		attrDomains.addAll(rightDomain);
		return attrDomains;
	}
	
	/**
	 * Get the CFill Attribute
	 * @param node
	 * @param tableIndex
	 * @return
	 */
	public Attribute getCFillAttribute (DTreeNode node, int tableIndex) {
		Integer attrIndex = node.attrIndex;
		if (attrIndex == null) return null;
		List<Attribute> attributes = null; 
		if (tableIndex == 0) attributes = leftAttrs;
		else if (tableIndex == 1) attributes = rightAttrs;
		Attribute attribute = attributes.get(attrIndex);
		return attribute;
	}
	
	
	/********************************************
	 * Typical tree functions 
	 * 
	 ************************************************/
	
	/**
	 * Get the depth of the tree
	 */
	public int getDepth () {
		return doGetDepth (mRoot);
	}
	
	int doGetDepth (DTreeNode root) {
		if (root.children.isEmpty()) return 1;
		int maxChildDepth = -1;
		for (String value : root.children.keySet()) {
			int childDepth = doGetDepth (root.children.get(value));
			if (childDepth > maxChildDepth) maxChildDepth = childDepth;
		}
		return maxChildDepth + 1;
	}
	
	public String toString () {
		StringBuffer buffer = new StringBuffer ();
		doString (mRoot, buffer, 0, "Root");
		return buffer.toString();
	}
	
	public void doString (DTreeNode root, StringBuffer buffer, int depth, String parentValue) {
		//if (root.partitionAttrIndex == null) return;
		if (root == null) return;
		for (int i = 0; i < depth; i ++) buffer.append("  ");
		if (root.attrIndex == null) {
			buffer.append(parentValue + " >>> leaf: " + root.dtid);
		} else{
			buffer.append(parentValue + " >>> " + root.dtid);
		}
		buffer.append("\n");
		for (String value : root.children.keySet()) {
			doString (root.children.get(value), buffer, depth + 1, value);
		}
	}
}
