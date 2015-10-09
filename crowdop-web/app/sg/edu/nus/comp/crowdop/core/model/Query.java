package sg.edu.nus.comp.crowdop.core.model;

import sg.edu.nus.comp.crowdop.sql.*;

/**
 * This class defines the query model of CrowdOP
 * @author fanju
 *
 */
public class Query {
	String dbName;
	String queryStr;
	SelectCommand queryCmd;
	Double budget;
	Integer latencyBound;
	
	public Query(String db, SelectCommand qcmd, String queryString, Double cbudget, Integer latBound) {
		dbName = db;
		budget = cbudget;
		queryCmd = qcmd;
		queryStr = queryString;
		latencyBound = latBound;
	}

	public String getDBName() {
		return dbName;
	}

	public SelectCommand getQueryCmd() {
		return queryCmd;
	}

	public Double getBudget() {
		return budget;
	}

	public String getQueryString() {
		return queryStr;
	}
	
	public String toString () {
		return dbName + "\t" + queryStr + "\t" + budget;
	}

	public Integer getLatencyBound() {
		return latencyBound;
	}
}
