package sg.edu.nus.comp.crowdop.core.op.crowdop;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import sg.edu.nus.comp.annotation.*;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.datastore.DataTable;
import sg.edu.nus.comp.datastore.schema.Attribute;
import sg.edu.nus.comp.datastore.schema.ComparisonOp;
import sg.edu.nus.comp.datastore.schema.Field;
import sg.edu.nus.comp.datastore.schema.Predicate;
import sg.edu.nus.comp.datastore.schema.Tuple;

public class CrowdFill extends CrowdOp {
	String tableName; // the table to be filled
	public List<String> attributes; // a list of attributes to be filled
	public List<Set<String>> attrDomains; // value domains of the attributes
	
	public static String type = "CFill";
	public CrowdFill(int oid, String tableName) {
		super(oid);
		this.tableName = tableName;
		attributes = new ArrayList<String>();
		attrDomains = new ArrayList<Set<String>>();
	}
	
	public void addFillAttribute (String attribute, Set<String> domain) {
		attributes.add(attribute);
		attrDomains.add(domain);
	}

	@Override
	public String getType() {
		return type;
	}

	public String getTableName() {
		return this.tableName;
	}

	public List<String> getAttributeNames () {
		return this.attributes;
	}
	public String getAttrName() {
		return this.attributes.get(0);
	}

	public Set<String> getAttrDomain() {
		return this.attrDomains.get(0);
	}
	
	public JSONObject toJSON() {
		JSONObject jobj = super.toJSON();
		jobj.put("relation-name", this.tableName);
		JSONArray jattrs = new JSONArray ();
		for (int i = 0; i < this.attributes.size(); i ++) {
			String attr = attributes.get(i);
			Set<String> domain = attrDomains.get(i);
			
			JSONObject jattr = new JSONObject ();
			jattr.put("attribute", attr);
			JSONArray jdomain = new JSONArray();
			for (String value : domain) {
				jdomain.put(value);
			}
			jattr.put("domain", jdomain);
			
			jattrs.put(jattr);
		}
		jobj.put("fill-attributes", jattrs);
		return jobj;
	}
	
	public static Op parse(JSONObject jop) {
		CrowdFill sop = new CrowdFill (jop.getInt("id"), 
				jop.getString("relation-name"));
		
		JSONArray jattrs = jop.getJSONArray("fill-attributes");
		for (int i = 0; i < jattrs.length(); i ++) {
			JSONObject jattr = jattrs.getJSONObject(i);
			String attr = jattr.getString("attribute");
			JSONArray jdomain = jattr.getJSONArray("domain");
			Set<String> domain = new HashSet<String>();
			for (int j = 0; j < jdomain.length(); j ++) {
				String value = jdomain.getString(j);
				domain.add(value);
			}
			sop.addFillAttribute(attr, domain);
		}
		return sop;
	}

	@Override
	public String getExplain() {
		// TODO Auto-generated method stub
		return "Crowdsource CFill on `" + this.tableName + "` "
				+ " for attribute(s) " + this.attributes;
	}

//	@Override
//	public Map<String, Tuple> crowdsource(DataStore tmpStore,
//			List<String> inTableNames) throws Exception {
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
//			Boolean valueMissing = evaluate (fields, tuple);
//			if (!valueMissing) { // the value if unknown
//				rowToTuple.put(String.valueOf(row), tuple);
//			} 
//			row ++;
//			tuple = inTable.getNextRow();
//		}
//		inTable.closeRowScan();
//		return rowToTuple;
//	}
//
//	/**
//	 * 
//	 * @param fields
//	 * @param tuple
//	 * @return
//	 */
//	private boolean evaluate(List<Field> fields, Tuple tuple) {
//		for (int i = 0; i < fields.size(); i ++) {
//			Field field = fields.get(i);
//			String value = tuple.getValue(i);
//			if (attributes.contains(field.getName())) {
//				if (value.equalsIgnoreCase("null")) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}
//
//	@Override
//	public void execute(DataStore inStore, List<String> inTableNames,
//			DataStore outStore, String outTableName, 
//			Map<String, String> rowToAnswer)
//			throws Exception {
//		if (inTableNames.size() != 1) throw new Exception ("CSelect `" + this.getID() + 
//				"` has invalid number of inputs");
//		System.out.println ("rowToAnswer: " + rowToAnswer);
//		DataTable inTable = inStore.getTable(inTableNames.get(0));
//		List<Field> fields = inTable.getFields();
//		inTable.openRowScan(inStore);
//		Tuple tuple = inTable.getNextRow();
//		int row = 0;
//		List<Tuple> tuples = new ArrayList<Tuple>();
//		while (tuple != null) {
//			Boolean valueMissing = evaluate (fields, tuple);
//			if (!valueMissing) { // the value if unknown
//				String answer = rowToAnswer.get(row+"");
//				String[] tmps = answer.split("\\|\\|");
//				Map<String, String> attr2value = new HashMap<String, String>();
//				for (String tmp : tmps) {
//					String[] parts = tmp.split("\\:\\:");
//					attr2value.put(parts[0], parts[1]);
//				}
//				String[] newValues = new String[tuple.size()];
//				for (int i = 0; i < fields.size(); i ++) {
//					String fieldName = fields.get(i).getName();
//					String value = tuple.getValue(i);
//					if (attr2value.containsKey(fieldName)) {
//						value = attr2value.get(fieldName);
//					}
//					newValues[i] = value;
//				}
//				Tuple newTuple = new Tuple (newValues, tuple.kindex);
//				tuples.add(newTuple);
//			} 
//			row ++;
//			tuple = inTable.getNextRow();
//		}
//		inTable.closeRowScan();
//		outStore.dropTable(outTableName);
//		outStore.createTable(outTableName, fields);
//		outStore.insertBatch(outTableName, tuples);
//	}
	
	public String toString () {
		return "CrowdFill " + this.tableName + "->" + this.attributes + "\t" + this.attrDomains ;
	}

	@Override
	public List<Annotation> seekForAnnotation(List<DataTable> inTables)
			throws Exception {
		if (inTables.size() != 1) throw new Exception ("CSelect `" + this.getID() + "`" + 
				": mismatched argument number " + inTables.size() + "(1)");
		DataTable inTable = inTables.get(0);
		List<Annotation> annos = new ArrayList<Annotation> ();
		for (int i = 0; i < attributes.size(); i ++) {
			String attribute = attributes.get(i);
			Field field = inTable.getFieldByName(attribute);
			Attribute attr = new Attribute(inTable.getName(), 
					attribute, field.getType());
			Predicate predicate = new Predicate(attr, ComparisonOp.EQUALS, null);
			List<Predicate> preds = new ArrayList<Predicate>();
			preds.add(predicate);
			List<Tuple> tuples = inTable.filter(preds, 
					ComparisonOp.LOGIC_AND, null); // retrieve the tuples vith null values
			for (Tuple tuple : tuples) {
				Set<String> domain = this.attrDomains.get(i);
				Annotation anno = new LabelAnnotation (i,tuple, domain);
				annos.add(anno);
			}
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
		List<Tuple> tuples = inTable.fill(annos, this.attributes);
		outTable.insert(Tuple.toValueTuples(tuples));
	}

}
