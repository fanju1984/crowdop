package sg.edu.nus.comp.crowdop.core.op.crowdop;

import java.util.*;

public class DTreeNode {
	public int dtid;
	public Integer attrIndex = null; // CFill Attribute	
	public Map<String, DTreeNode> children = 
			new HashMap<String, DTreeNode> (); // child nodes
	
	/**
	 * Basic Functions
	 */
	public DTreeNode (int dtId) {
		dtid = dtId;
	}
	
	public boolean equals (Object obj) {
		if (obj instanceof DTreeNode) {
			DTreeNode node = (DTreeNode) obj;
			return dtid == node.dtid;
		}
		return false;
	}
	
	public int hashCode () {
		return new Integer (dtid).hashCode();
	}
	
	public void setFillAttr (int attributeIndex) {
		attrIndex = attributeIndex;
	}
}
