package sg.edu.nus.comp.launchers;

import java.util.*;


import sg.edu.nus.comp.crowdop.api.RequesterAPI;
import sg.edu.nus.comp.crowdop.core.model.*;
import sg.edu.nus.comp.crowdop.core.plangen.*;
import sg.edu.nus.comp.datastore.DataStore;
import sg.edu.nus.comp.datastore.plugin.*;

public class RequesterAPITest {
	public static void main (String args[]) throws Exception {
		RequesterAPI reqApi = RequesterAPI.getIns();
		String sqlStr = null;
		
		String selectSqlStr = "select * from image "
				+ "where image.make = 'audi' and "
				+ "image.body_style = 'suvs' and "
				+ "image.year = 2014 ";
		
		String joinSqlStr = "select * from vehicle, image "
				+ "where vehicle.make = image.make and "
				+ "vehicle.model = image.model ";
		
		String fillSqlStr = "select image.color from image";
		
		sqlStr = selectSqlStr;
		
		DataStore srcStore = new CSVDataStore (
				"/Users/fanj/Documents/Projects/foodlog/crowdop/crowdop-web/.datasources/.data/");
		DataStore summaryStore = new CSVDataStore (
				"/Users/fanj/Documents/Projects/foodlog/crowdop/crowdop-web/.datasources/.summary/");

		CrowdPricing cprice = new LinearCrowdPricing (0.005, 0.005, 0.015, 0.00, 0.01, 0.002);
		CrowdPlan cplan = reqApi.submitJobRequest(srcStore, summaryStore, 
				sqlStr, null, cprice);
		for (List<CrowdUnit> cunits : cplan.batchlist) {
			System.out.println("######## BATCH ############");
			for (CrowdUnit cunit : cunits) {
				System.out.println (cunit.op.toJSON());
			}
		}
		
//		String templateStr = FileUtil.file2String("example/yahooauto_template.json");
//		JSONObject template = new JSONObject (templateStr);
//		
//		reqApi.initCrowdsourcing(srcStore, jobID, template);
//		reqApi.crowdsourceBatch(jobID);
	}
}
