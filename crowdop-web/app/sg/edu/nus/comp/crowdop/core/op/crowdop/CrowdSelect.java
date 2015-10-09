package sg.edu.nus.comp.crowdop.core.op.crowdop;

import java.util.*;

import org.json.*;

import sg.edu.nus.comp.annotation.*;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.datastore.DataTable;
import sg.edu.nus.comp.datastore.schema.*;

public class CrowdSelect extends CrowdOp {
	public static String type = "CSelect";
	String tableName;
	List<Predicate> predicates;
	
	public CrowdSelect (int id, String table) {
		super (id);
		tableName = table;
		predicates = new ArrayList<Predicate> ();
	}
	
	/*****************************************
	 * Basic Operations
	 *
	 *****************************************/
	public void addPredicate (Predicate predicate) {
		predicates.add(predicate);
	}
	
	public void setPredicates (List<Predicate> preds) {
		predicates = preds;
	}
	
	public void clearPredicates () {
		predicates.clear();
	}
	
	public List<Predicate> getPredicates () {
		return predicates;
	}
	
	public String getTableName () {
		return tableName;
	}
	
	public String toString () {
		return "CrowdSelect [" + predicates + "]";
	}
	
	public Op cloneOp () {
		CrowdSelect selectOp = new CrowdSelect (this.oid, this.tableName);
		for (Predicate pred : this.predicates) {
			selectOp.addPredicate(pred);
		}
		return selectOp;
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject jobj = super.toJSON();
		jobj.put("relation-name", this.tableName);
		JSONArray jpreds = new JSONArray ();
		for (Predicate pred : predicates) {
			JSONObject jpred = new JSONObject ();
			jpred.put("left", pred.getLeft().getAttributeName());
			jpred.put("type", pred.getLeft().getType());
			jpred.put("pred-op", pred.getComparisonOp());
			String vstr =  ((Value)pred.getRight()).getValue() + "";
			jpred.put("right", vstr);
			jpreds.put(jpred);
		}
		jobj.put("predicates", jpreds);
		return jobj;
	}
	
	public static Op parse(JSONObject jop) {
		CrowdSelect sop = new CrowdSelect (jop.getInt("id"), 
				jop.getString("relation-name"));
		JSONArray jpreds = jop.getJSONArray("predicates");
		for (int i = 0; i < jpreds.length(); i ++) {
			JSONObject jpred = jpreds.getJSONObject(i);
			String attr = jpred.getString("left");
			VariableType type = VariableType.parse(jpred.getString("type"));
			ComparisonOp cop = ComparisonOp.parseComparison(jpred.getString("pred-op"));
			String vstr = jpred.getString("right");
			
			Attribute left = new Attribute (jop.getString("relation-name"), attr, type);
			Value right = new Value (vstr, type);
			Predicate pred = new Predicate (left, cop, right);
			sop.addPredicate(pred);
		}
		return sop;
	}
	
	public String getType () {
		return type;
	}
	
//	private Tuple setTupleValue (List<Field> fields, Tuple tuple) {
//		String[] values = new String[tuple.getValues().length];
//		for (int i = 0; i < fields.size(); i ++) {
//			Field field = fields.get(i);
//			String valueStr = tuple.getValue(i);
//			for (Predicate pred : predicates) { // for each predicate
//				String attr = pred.getLeft().getAttributeName();
//				if (attr.equals(field.getName())) {
//					if (valueStr.equalsIgnoreCase("null")) { // missing value 
//						valueStr = pred.getRight().toString();
//					} 
//				}
//			}
//			values[i] = valueStr;
//		}
//		Tuple newTuple = new Tuple (values, tuple.kindex);
//		return newTuple;
//	}
//
//	private Boolean evaluate(List<Field> fields, Tuple tuple) {
//		boolean matched = false;
//		for (int i = 0; i < fields.size(); i ++) {
//			Field field = fields.get(i);
//			String valueStr = tuple.getValue(i);
//			for (Predicate pred : predicates) { // for each predicate
//				String attr = pred.getLeft().getAttributeName();
//				if (attr.equals(field.getName())) {
//					matched = true;
//					if (valueStr.equalsIgnoreCase("null")) { // missing value 
//						return null; 
//					} else {
//						valueStr = valueStr.toLowerCase();
//						Value value = new Value (valueStr, field.getType());
//						boolean passed = pred.isInRange(value);
//						if (!passed) return false; // we consider AND logic
//					}
//					
//				}
//			}
//		}
//		if (matched) return true;
//		else return false;
//	}

//	@Override
//	public List<Microtask> crowdsource (List<DataTable> inTables,
//			DataTable opTmpTable, DataStore tmpStore, 
//			JSONObject objTemplate) throws Exception {
//		// Step 1: Prepare the template
//		String opTemplate = loadOpTemplate (type);
//		String predStr = printPredicates (this.predicates);
//		Map<String, String> choices = new HashMap<String, String> ();
//		choices.put("1", "Yes, it is");
//		choices.put("0", "No, it isn't");
//		
//		if (inTables.size() != 1) throw new Exception ("CSelect `" + this.getID() + 
//				"` has invalid number of inputs");
//		DataTable inTable = inTables.get(0);
//		List<Field> fields = inTable.getFields();
//		inTable.openRowScan(tmpStore);
//		Tuple tuple = inTable.getNextRow();
//		List<Tuple> batch = new ArrayList<Tuple> ();
//		List<Microtask> mtasks = new ArrayList<Microtask> ();
//		
//		while (tuple != null) {
//			Boolean passed = evaluate (fields, tuple);
//			if (passed == null) {
//				Microtask mtask = genMicrotask (objTemplate, tuple, fields, 
//						predStr, opTemplate, choices);
//				mtasks.add(mtask);
//			} else if (passed) {
//				batch.add(tuple);
//				if (batch.size() == DATA_LOAD_BATCH_SIZE) {
//					tmpStore.insertBatch(opTmpTable, batch);
//					batch.clear();
//				}
//			}
//			tuple = inTable.getNextRow();
//		}
//		if (!batch.isEmpty()) {
//			tmpStore.insertBatch(opTmpTable, batch);
//		}
//		inTable.closeRowScan();
//		return mtasks;
//	}
//
//	private Microtask genMicrotask(JSONObject objTemplate, Tuple tuple,
//			List<Field> fields, String predStr, String opTemplate, 
//			Map<String, String> choices) {
//		JSONObject relTemplate = 
//				objTemplate.getJSONObject(this.tableName); // get template
//		String tupleStr = this.renderTuple(fields, tuple, relTemplate);
//		String stem = String.format(opTemplate, predStr, tupleStr);
//		String tupleID = tuple.getKey();
//		Microtask mtask = new Microtask (tupleID, stem, choices, 
//				Microtask.TYPE_SINGLE, true);
//		return mtask;
//	}



	@Override
	public String getExplain() {
		return "Crowdsource CSelect with predicates " + this.predicates;
	}

//	@Override
//	public List<String> getDataTables() {
//		List<String> dataTables = new ArrayList<String> ();
//		dataTables.add(tableName);
//		return dataTables;
//	}
//
//	@Override
//	public Map<String, Tuple> crowdsource(DataStore tmpStore,
//			List<String> inTableNames) throws Exception {
//		// Step 1: Prepare the template
//		
//		if (inTableNames.size() != 1) throw new Exception ("CSelect `" + this.getID() + 
//				"` has invalid number of inputs");
//		
//		DataTable inTable = tmpStore.getTable(inTableNames.get(0));
//		List<Field> fields = inTable.getFields();
//		inTable.openRowScan(tmpStore);
//		Tuple tuple = inTable.getNextRow();
//		Map<String, Tuple> rowToTuple = new HashMap<String, Tuple>();
//		int row = 0;
//		while (tuple != null) {
//			Boolean passed = evaluate (fields, tuple);
//			if (passed == null) { // the value if unknown
//				rowToTuple.put(String.valueOf(row), tuple);
//			} 
//			row ++;
//			tuple = inTable.getNextRow();
//		}
//		inTable.closeRowScan();
//		return rowToTuple;
//
//	}
//
//	@Override
//	public void execute(DataStore inStore, List<String> inTableNames,
//			DataStore outStore, String outTableName, 
//			Map<String, String> rowToAnswer) throws Exception {
//		if (inTableNames.size() != 1) throw new Exception ("CSelect `" + this.getID() + 
//				"` has invalid number of inputs");
//		
//		DataTable inTable = inStore.getTable(inTableNames.get(0));
//		inTable.openRowScan(inStore);
//		List<Field> fields = inTable.getFields();
//		Tuple tuple = inTable.getNextRow();
//		int row = 0;
//		List<Tuple> tuples = new ArrayList<Tuple>();
//		while (tuple != null) {
//			Boolean passed = evaluate (fields, tuple);
//			if (passed == null) { // the value if unknown
//				String answer = rowToAnswer.get(String.valueOf(row));
//				answer = answer.trim();
//				if (answer.equals("1")) {
//					tuple = setTupleValue(fields, tuple);
//					tuples.add(tuple);
//				}
//			} else if (passed) {
//				tuples.add(tuple);
//			}
//			row ++;
//			tuple = inTable.getNextRow();
//		}
//		inTable.closeRowScan();
//		outStore.dropTable(outTableName);
//		outStore.createTable(outTableName, fields);
//		outStore.insertBatch(outTableName, tuples);
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public List<Annotation> seekForAnnotation(
			List<DataTable> inTables) throws Exception {
		if (inTables.size() != 1) throw new Exception ("CSelect `" + this.getID() + "`" + 
				": mismatched argument number " + inTables.size() + "(1)");
		DataTable inTable = inTables.get(0);
		List<Predicate> isNulls = new ArrayList<Predicate>();
		
		for (Predicate pred: predicates) { // for each predicate
			Predicate isNull = new Predicate(pred.getLeft(), 
					ComparisonOp.EQUALS, null);
			isNulls.add(isNull);
		}
		List<Tuple> tuples = inTable.filter(isNulls, ComparisonOp.LOGIC_OR, null);
		List<Annotation> annos = new ArrayList<Annotation> ();
		for (Tuple tuple : tuples) {
			Annotation anno = new FilterAnnotation (tuple);
			annos.add(anno);
		}
		return annos;
	}

	@Override
	public void execute(List<DataTable> inTables, DataTable outTable,
			List<Annotation> annos) throws Exception {
		if (inTables.size() != 1) throw new Exception ("CSelect `" + this.getID() + "`" + 
				": mismatched argument number " + inTables.size() + "(1)");
		DataTable inTable = inTables.get(0);
		List<Field> newFields = new ArrayList<Field>();
		for (Field field : inTable.getFields()) newFields.add(field);
		outTable.alterFields (newFields);
		List<Tuple> tuples = inTable.filter(predicates, ComparisonOp.LOGIC_AND, annos);
		List<List<String>> valueTuples = Tuple.toValueTuples(tuples);
		outTable.insert(valueTuples);
	}
}
