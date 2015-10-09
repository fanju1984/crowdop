package sg.edu.nus.comp.crowdop.core.plangen;

import java.util.*;

import sg.edu.nus.comp.crowdop.core.datasummary.DataSummary;
import sg.edu.nus.comp.crowdop.core.model.CrowdPricing;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdFill;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdJoin;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdSelect;
import sg.edu.nus.comp.crowdop.core.plangen.OpTree.OpTreeNode;
import sg.edu.nus.comp.crowdop.core.plangen.optimize.GlobalOptimizer;
import sg.edu.nus.comp.crowdop.sql.CommandParser;
import sg.edu.nus.comp.crowdop.sql.SelectCommand;
import sg.edu.nus.comp.datastore.DataStore;

public class PlanGen {
	public static CrowdPlan generatePlan (DataStore srcStore, 
			DataStore summaryStore, 
			String sqlStr, Double budget, 
			CrowdPricing cprice) throws Exception {
		DataSummary dataSummary = new DataSummary (srcStore, summaryStore);
		CommandParser parser = new CommandParser(); // the parser for SQL statement
		sqlStr = sqlStr.trim().toLowerCase();
		SelectCommand queryCmd = (SelectCommand)parser.parse(sqlStr, 
				srcStore.getAllDataTables()); // command parsing
		OpTree orgOpTree = new OpTree (queryCmd, dataSummary); // generate an initial OP-Tree
		System.out.println ("Original Plan:");
		System.out.println (orgOpTree);
		
		OptOption optOption = OptOption.getDefaultOption();
		GlobalOptimizer globalOptimizer = 
				new GlobalOptimizer (optOption, dataSummary, cprice);
		
		OpTree optOpTree = globalOptimizer.optimize(orgOpTree, budget);
		if (orgOpTree.mRoot.mOp instanceof CrowdFill) { // the projection
			// append the projection node
			orgOpTree.mRoot.children.clear();
			orgOpTree.mRoot.children.add(optOpTree.mRoot);
			optOpTree.mRoot.mParent = orgOpTree.mRoot;
			optOpTree.mRoot = orgOpTree.mRoot;
		}
		System.out.println(optOpTree);
		if (optOpTree == null) return null;
		
		CrowdPlan cplan = new CrowdPlan (optOpTree.estimatedCard, 
				optOpTree.estimatedCard);
		opTree2Batches (optOpTree.mRoot, cplan, cprice);		
//		this.materializePlan (jobID, cplan);
//		return jobID;
		return cplan;
	}
	
//	private void materializePlan(String jobID, CrowdPlan cplan) throws Exception {
//		String path = Config.execDir + jobID + "/";
//		path += "exec-plan.json";
//		FileWriter file = new FileWriter(path);
//		file.write(cplan.toJSONString());
//		file.flush();
//		file.close();
//	}

//	private String generateJobID () {
//		Calendar cal = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat("MMdd-HHmmss");
//        String jobID = sdf.format(cal.getTime());
//		String path = Config.execDir + jobID + "/";
//		File dir = new File (path);
//		if (!dir.exists()) dir.mkdirs();
//		return jobID;
//	}
	
	static int opTree2Batches (OpTreeNode root, CrowdPlan cplan, 
			CrowdPricing cprice) {
		int batchNum = -1;
		if (root.children.isEmpty()) { // the leaf
			batchNum = 0;
		} else {
			for (OpTreeNode child : root.children) {
				int cbn = opTree2Batches (child, cplan, cprice);
				if (cbn > batchNum) batchNum = cbn;
			}
			batchNum ++;
		}
		Op op = root.mOp;
		List<Integer> inIds = new ArrayList<Integer> ();
		for (OpTreeNode child : root.children) {
			inIds.add(child.mOp.getID());
		}
		double crowdPrice = getCrowdPrice (op, cprice);
		cplan.addUnit (op, crowdPrice, inIds, batchNum);
		return batchNum;
	}

	private static double getCrowdPrice(Op op, CrowdPricing cprice) {
		double price = 0.0;
		if (op instanceof CrowdSelect) {
			CrowdSelect cselect = (CrowdSelect) op;
			price = cprice.unitCSelectCost(cselect.getTableName(), 
					cselect.getPredicates());
		} else if (op instanceof CrowdFill) {
			CrowdFill cfill = (CrowdFill) op;
			price = cprice.unitCFillCost(cfill.getTableName(), 
					cfill.getAttrName (), cfill.getAttrDomain ());
		} else if (op instanceof CrowdJoin) {
			CrowdJoin cjoin = (CrowdJoin) op;
			price = cprice.unitCJoinCost(cjoin.leftRelation, 
					cjoin.rightRelation, cjoin.conditions);
		}
		return price;
	}
}
