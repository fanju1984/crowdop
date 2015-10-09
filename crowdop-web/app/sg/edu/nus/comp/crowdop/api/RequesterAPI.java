package sg.edu.nus.comp.crowdop.api;

import org.json.JSONObject;

import java.util.*;

import sg.edu.nus.comp.crowdop.core.execute.PlanExec;
import sg.edu.nus.comp.crowdop.core.model.CrowdPricing;
import sg.edu.nus.comp.crowdop.core.plangen.CrowdPlan;
import sg.edu.nus.comp.crowdop.core.plangen.OptOption;
import sg.edu.nus.comp.crowdop.core.plangen.PlanGen;
import sg.edu.nus.comp.datastore.*;

public class RequesterAPI {
	static RequesterAPI ins = null;
	
	public static RequesterAPI getIns () {
		if (ins == null) {
			ins = new RequesterAPI ();
		}
		return ins;
	}
	
	PlanGen planGen;
	PlanExec planExec;
	
	private RequesterAPI () {
		planGen = new PlanGen ();
		planExec = new PlanExec ();
	}
	
	
	
	/**
	 * This function can be used for requester to submit a new crowdsourcing job request.
	 * The Crowdsourcing engine will optimize and generate execution plan, 
	 *     which will be then materialized. 
	 *     
	 * @param srcStore: the source storing the data to be crowdsourced. 
	 * @param summaryStore: the source storing summary information, e.g., samples
	 * @param sqlStr: the sql-like statement for declarative crowdsourcing. 
	 * @param budget: the monetary budget (NULL if there is no budget constraint).
	 * @param cprice: the model for crowdsourcing pricing. 
	 * @param optOption: the strategy for optimization
	 * @return: the job identifier
	 */
	public CrowdPlan submitJobRequest (DataStore srcStore, DataStore summaryStore, 
			String sqlStr, Double budget, 
			CrowdPricing cprice) {
		try {
			CrowdPlan cplan = PlanGen.generatePlan(srcStore, summaryStore,
					sqlStr, budget, cprice);
			return cplan; // return ID of the planned job
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void initCrowdsourcing (DataStore datasource, 
			String jobID, JSONObject template) {
		try {
			planExec.initJobExec (datasource, jobID, template);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean crowdsourceBatch (String jobID) {
		try {
			planExec.crowdsourceBatch(jobID);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * This function returns a summary of current progress of requested jobID
	 * @param jobID: the assigned jobID
	 * @return: A summary of job progress
	 */
	public JSONObject checkProgress (String jobID) {
		return null;
	}
	
	/**
	 * Load the crowdsourcing result to a destination data-store
	 * @param jobID: the identifier of a job
	 * @param destStore: the destination store for the result
	 * @param tableName: the output table for the result
	 * @return: true/false: if the result has been generated and loaded
	 * @throws Exception
	 */
	public boolean loadResult (String jobID, DataStore destStore, 
			String tableName) throws Exception {
		if (!planExec.isJobCompleted (jobID)) return false;
		planExec.loadResult (jobID, destStore, tableName);
		return true;
	}
	
}
