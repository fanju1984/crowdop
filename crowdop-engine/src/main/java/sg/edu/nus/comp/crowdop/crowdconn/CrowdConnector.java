package sg.edu.nus.comp.crowdop.crowdconn;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import sg.edu.nus.comp.crowdop.core.execute.Microtask;

public class CrowdConnector {

	/**
	 * Publish the microtasks to a crowdsourcing platform via JSON format
	 * @param jobID: the identifier of the job
	 * @param opId: the corresponding operator ID
	 * @param mtasks: a list of microtasks
	 * @param price: the price of the task
	 * @throws Exception
	 */
	public void publish(String jobID, int opId, List<Microtask> mtasks,
			double price) throws Exception {
		JSONObject jtaskgroup = new JSONObject ();
		jtaskgroup.put("task-group-id", jobID + "_" + opId);
		jtaskgroup.put("price", price);
		JSONArray jtasks = new JSONArray ();
		jtaskgroup.put("mtasks", jtasks);
		
		for (Microtask mtask: mtasks) {
			JSONObject jtask = new JSONObject ();
			jtask.put("task-id", mtask.tid);
			jtask.put("stem", mtask.stem);
			jtask.put("choices", mtask.choices);
			jtask.put("question-type", mtask.type);
			jtask.put("is-close", mtask.isClose);
			jtasks.put(jtask);
		}
		
		
		
	}
	
	public void collectAnswers () throws Exception {
		
	}
	
	

}
