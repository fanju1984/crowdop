package sg.edu.nus.comp.crowdop.core.datasummary;

import java.util.*;

import sg.edu.nus.comp.datastore.DataStore;
import sg.edu.nus.comp.datastore.DataTable;
import sg.edu.nus.comp.datastore.schema.Predicate;
import sg.edu.nus.comp.datastore.schema.Tuple;
import sg.edu.nus.comp.datastore.schema.Value;

/**
 * This class implements a simple version of data summary for estimation
 *  - The summary store contains tables of triples, i.e., (attribute, value, selectivity)
 * @author Ju Fan
 *
 */

public class DataSummary {
	DataStore dataStore;
	DataStore summaryStore;
	public DataSummary(DataStore dataStore, DataStore summaryStore) {
		this.dataStore = dataStore;
		this.summaryStore = summaryStore;
	}

	public Set<String> valueDomain(String tableName, 
			String attributeName) throws Exception {
		Set<String> domains = new HashSet<String> ();
		DataTable dataTable = summaryStore.getTable(tableName);
		if (dataTable == null) return domains;
		Iterator<Tuple> it = dataTable.getRowIterator();
		while (it.hasNext()) {
			Tuple tuple = it.next();
			String attr = tuple.getValue(0);
			if (attributeName.equals(attr)) {
				String value = tuple.getValue(1);
				domains.add(value);
			}			
		}
		return domains;
	}

	public double tableCardinality(String tableName) throws Exception {
		DataTable dataTable = dataStore.getTable(tableName);
		return dataTable.count();
	}

	public double selectivity(String tableName, List<Predicate> preds) {
		Double sel = null;
		DataTable dataTable = summaryStore.getTable(tableName);
		if (dataTable == null) return 0.0; // no selectivity
		Iterator<Tuple> it = dataTable.getRowIterator();
		while (it.hasNext()) {
			Tuple tuple = it.next();
			String attr = tuple.getValue(0);
			String value = tuple.getValue(1);
			Double s = Double.parseDouble(tuple.getValue(2));
 			for (Predicate pred : preds) {
 				String left = pred.getLeft().getAttributeName();
 				String right = ((Value)pred.getRight()).getValue().toString();
 				if (left.equalsIgnoreCase(attr) && 
 						right.equalsIgnoreCase(value)) {
 					if (sel == null) sel = 1.0;
 					sel *= s;
 				}
			}
		}
		if (sel == null) return 1.0;
		return sel;
	}

}
