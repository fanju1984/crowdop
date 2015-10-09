package sg.edu.nus.comp.crowdop.core.execute;

import java.io.*;
import java.util.*;

import org.json.*;

import sg.edu.nus.comp.config.Config;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.crowdop.core.op.crowdop.CrowdSelect;
import sg.edu.nus.comp.crowdop.core.op.machineop.Relation;
import sg.edu.nus.comp.crowdop.core.plangen.*;
import sg.edu.nus.comp.crowdop.crowdconn.CrowdConnector;
import sg.edu.nus.comp.datastore.*;
import sg.edu.nus.comp.datastore.plugin.CSVDataStore;
import sg.edu.nus.comp.datastore.schema.*;

/**
 * This class implements the iterative crowd execution model
 * 
 * @author Ju Fan
 * 
 */
public class PlanExec {
	CrowdConnector crowdConn;
	
	
	
	public PlanExec () {
		crowdConn = new CrowdConnector ();
	}
	
	public void initJobExec (DataStore srcStore, String jobID, JSONObject template) throws Exception {
		// Step 1: Load the execution plan
		CrowdPlan cplan = loadJobPlan (jobID);
		
		// Step 2: Initialize the store engines and data loader
		String path = Config.execDir + jobID + "/data-tmp/";
		DataStore tmpStore = new CSVDataStore (path);		
		// Step 3: Execute the first batch, i.e., loading data
		List<CrowdUnit> firstBatch = cplan.getFirstBatch ();
		for (int i = 0; i < firstBatch.size(); i ++) {
			CrowdUnit cunit = firstBatch.get(i);
			Op op = cunit.getOperator ();
			if (op instanceof Relation) {
				Relation rop = (Relation) op;
				String opName = "op-" + op.getID();
				String relationName = rop.tableName;
				//loader.loadTable(relationName, opName);
			}
			cunit.setUnitState(CrowdUnit.STATE_COMP); // the unit becomes completed. 
		}
		materializeJobPlan (jobID, cplan);
		
		path = Config.execDir + jobID + "/template.json";
		BufferedWriter w = new BufferedWriter (new FileWriter (path));
		w.write(template.toString());
		w.close();
	}
	
	/**
	 * Publish new batches of microtasks to the crowdsourcing market
	 * @param jobID
	 * @param market
	 * @param template
	 */
	public void crowdsourceBatch (String jobID) throws Exception {
		// Step 0: Load the crowdsourcing properties
		String path = Config.execDir + jobID + "/template.json";
		BufferedReader r = new BufferedReader (new FileReader (path));
		String line = r.readLine();
		r.close();
		JSONObject template = new JSONObject (line);
		
		
		// Step 1: Load the execution plan
		CrowdPlan cplan = loadJobPlan (jobID);
		
		// Step 2: Initialize the data store
		path = Config.execDir + jobID + "/data-tmp/";
		DataStore tmpStore = new CSVDataStore (path);
		List<CrowdUnit> cunits = cplan.getRunnableUnits();
		for (CrowdUnit cunit : cunits) {
			Op op = cunit.getOperator();
			List<Integer> inIds = cunit.getInputIDs ();
			List<Microtask> mtasks = null;
			if (op instanceof CrowdSelect) {
				mtasks = genMicrotasks (cunit, inIds, tmpStore, template);
			}
			if (mtasks != null && !mtasks.isEmpty()) {
				double price = cunit.getCrowdPrice ();
				cunit.setTotalTaskNum (mtasks.size());
				crowdConn.publish (jobID, op.getID(), mtasks, price);
				cunit.setUnitState(CrowdUnit.STATE_ONGO); // change the state to ongoing
			} else {
				cunit.setUnitState(CrowdUnit.STATE_COMP);
			}
			
		}
		this.materializeJobPlan(jobID, cplan); // materialize the updated plan
	}

	/**
	 * Execute CSELECT operator to publish crowdsourcing tasks
	 * @param jop
	 * @param tmpStore
	 * @param template
	 */
	private List<Microtask> genMicrotasks (CrowdUnit cunit, List<Integer> inIds,
			DataStore tmpStore, JSONObject template) throws Exception {
		CrowdSelect op = (CrowdSelect) cunit.getOperator();
		List<DataTable> inTables = new ArrayList<DataTable> ();
		List<Field> outFields = new ArrayList<Field> ();
		for (int inId : inIds) {
			DataTable inTable = tmpStore.getTable("op-" + inId);
			outFields.addAll(inTable.getFields());
			inTables.add(inTable);
		}		
		DataTable opTmpTable = tmpStore.getTable("op-" + op.getID());
		if (opTmpTable == null) {
			tmpStore.createTable("op-" + op.getID(), outFields);
			opTmpTable = tmpStore.getTable("op-" + op.getID());
		}
		
		List<Microtask> mtasks = null;
		//op.crowdsource(inTables, opTmpTable, tmpStore, template);
		return mtasks;
	}

	private void materializeJobPlan(String jobID, CrowdPlan cplan) throws Exception {
		String path = Config.execDir + jobID + "/exec-plan.json";
		BufferedWriter w = new BufferedWriter (new FileWriter (path));
		//w.write(cplan.toJSONString());
		w.close();
	}
	
	private CrowdPlan loadJobPlan(String jobID) throws Exception {
		String path = Config.execDir + jobID + "/exec-plan.json";
		BufferedReader r = new BufferedReader (new FileReader (path));
		String line = r.readLine();
		r.close();
		CrowdPlan cplan = null;
		//CrowdPlan cplan = CrowdPlan.parse(line);
		return cplan;
	}

	public boolean isJobCompleted(String jobID) {
		// TODO Auto-generated method stub
		return false;
	}

	public void loadResult(String jobID, DataStore destStore, String tableName) {
		// TODO Auto-generated method stub
		
	}
}