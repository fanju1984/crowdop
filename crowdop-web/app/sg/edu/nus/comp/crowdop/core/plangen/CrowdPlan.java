package sg.edu.nus.comp.crowdop.core.plangen;

import java.util.*;

import sg.edu.nus.comp.crowdop.core.op.Op;

public class CrowdPlan {
	public List<List<CrowdUnit>> batchlist;
	Double estCost;
	Double estCard;
	
	public CrowdPlan (Double estimatedCost, Double estimatedCard) {
		batchlist = new ArrayList<List<CrowdUnit>> ();
		estCost = estimatedCost;
		estCard = estimatedCard;
	}
	
//	private CrowdPlan () {}
	
	public void addUnit (Op op, double unitPrice, 
			List<Integer> inputUnitIds, int batchIndex) {
		if (batchlist.size() <= batchIndex) {
			int oldLen = batchlist.size();
			for (int i = oldLen; i <= batchIndex; i ++) {
				batchlist.add(new ArrayList<CrowdUnit> ());
			}
		} 
		List<CrowdUnit> batch = batchlist.get(batchIndex);
		CrowdUnit cunit = new CrowdUnit (op, unitPrice, inputUnitIds);
		batch.add (cunit);
	}

//	public String toJSONString() {
//		JSONObject jplan = new JSONObject ();
//		jplan.put("job-id", this.jobID);
//		jplan.put("est-cost", this.estCost);
//		jplan.put("est-card", this.estCost);
//		JSONArray jbatchAry = new JSONArray ();
//		jplan.put("batches", jbatchAry);
//		for (List<CrowdUnit> batch : batchlist) {
//			JSONArray jbatch = new JSONArray ();
//			jbatchAry.put(jbatch);
//			for (CrowdUnit cunit : batch) {
//				JSONObject junit = cunit.toJSON ();
//				jbatch.put(junit);
//			}
//		}
//		return jplan.toString();
//	}
	
//	public static CrowdPlan parse (String jsonStr) {
//		JSONObject jplan = new JSONObject (jsonStr);
//		CrowdPlan cplan = new CrowdPlan ();
//		cplan.jobID = jplan.getString("job-id");
//		cplan.estCard = jplan.getDouble("est-card");
//		cplan.estCost = jplan.getDouble("est-cost");
//		cplan.batchlist = new ArrayList<List<CrowdUnit>> ();
//		JSONArray jbatchAry = jplan.getJSONArray("batches");
//		for (int i = 0; i < jbatchAry.length(); i ++) {
//			JSONArray jbatch = jbatchAry.getJSONArray(i);
//			List<CrowdUnit> cunits = new ArrayList<CrowdUnit> ();
//			for (int j = 0; j < jbatch.length(); j ++) {
//				JSONObject junit = jbatch.getJSONObject(j);
//				CrowdUnit cunit = CrowdUnit.parse (junit);
//				cunits.add (cunit);
//			}
//			cplan.batchlist.add(cunits);
//		}
//		return cplan;
//	}

	public List<CrowdUnit> getFirstBatch() {
		return batchlist.get(0);
	}
	
	/**
	 * Get the runnable units, whose state is STATE_INIT
	 * We use a batch-level granuality. 
	 * @return
	 */
	public List<CrowdUnit> getRunnableUnits () {
		for (int i = 0; i < batchlist.size(); i ++) {
			List<CrowdUnit> batch = batchlist.get(i);
			List<CrowdUnit> runnable = new ArrayList<CrowdUnit> ();
			boolean batchCompleted = true;
			for (CrowdUnit cunit : batch) {
				if (cunit.state == CrowdUnit.STATE_INIT) {
					batchCompleted = false;
					runnable.add(cunit);
				}
			}
			if (!batchCompleted) return runnable;
		}
		return null;
	}
}
