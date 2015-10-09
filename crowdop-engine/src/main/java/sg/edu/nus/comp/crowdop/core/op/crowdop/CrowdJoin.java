package sg.edu.nus.comp.crowdop.core.op.crowdop;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import sg.edu.nus.comp.annotation.*;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.datastore.DataTable;
import sg.edu.nus.comp.datastore.schema.*;

public class CrowdJoin extends CrowdOp {
	public String leftRelation;
	public String rightRelation;
	public List<Predicate> conditions;
	public DecisionTree dtree = null;
	
	public static String type = "CJoin";

	
	public CrowdJoin (int oid, String left, String right) {
		super (oid);
		leftRelation = left;
		rightRelation = right;
		conditions = new ArrayList<Predicate> ();
	}
	
	public void addCondition (Predicate condition) {
		conditions.add(condition);
	}
	
	public List<Predicate> getConditions () {
		return conditions;
	}
	
	public void setDecisionTree (DecisionTree decisionTree) {
		dtree = decisionTree;
	}
	
	public String toString () {
		return "CrowdJoin " + conditions ;
	}
	
	public Op cloneOp () {
		CrowdJoin joinOp = new CrowdJoin (this.oid, this.leftRelation, this.rightRelation);
		for (Predicate pred : this.conditions) {
			joinOp.addCondition(pred);
		}
		return joinOp;
	}
	
	public String getType () {
		return type;
	}

	@Override
	public String getExplain() {
		return "Crowdsource CJoin between `" + 
				this.leftRelation + "` and `" + this.rightRelation + "` " + 
				"on predicates " + this.conditions;
	}

//	@Override
//	public List<String> getDataTables() {
//		List<String> dataTables = new ArrayList<String>();
//		dataTables.add(this.leftRelation);
//		dataTables.add(this.rightRelation);
//		return dataTables;
//	}
//
//	@Override
//	public Map<String, Tuple> crowdsource(DataStore tmpStore,
//			List<String> inTableNames) throws Exception {
//		Map<String, Tuple> rowToTuple = 
//				new HashMap<String, Tuple>();
//		if (inTableNames.size() != 2) 
//			throw new Exception ("CJoin `" + this.getID() + 
//				"` has invalid number of inputs");
//		DataTable leftTable = tmpStore.getTable(inTableNames.get(0));
//		List<Field> leftFields = leftTable.getFields();
//		DataTable rightTable = tmpStore.getTable(inTableNames.get(1));
//		List<Field> rightFields = rightTable.getFields();
//		
//		leftTable.openRowScan(tmpStore);
//		Tuple leftTuple = leftTable.getNextRow();
//		int leftRow = 0;
//		while (leftTuple != null) {
//			rightTable.openRowScan(tmpStore);
//			Tuple rightTuple = rightTable.getNextRow();
//			int rightRow = 0;
//			while (rightTuple != null) {
//				Boolean passed = evaluate (leftFields, leftTuple, 
//						rightFields, rightTuple);
//				if (passed == null) { // the value if unknown
//					Tuple tuple = Tuple.concat(leftTuple, 
//							rightTuple, leftTuple.kindex);
//					rowToTuple.put(leftRow + "::" + rightRow, tuple);
//				} 
//				rightTuple = rightTable.getNextRow();
//				rightRow ++;
//			}
//			rightTable.closeRowScan();
//			
//			leftTuple = leftTable.getNextRow();
//			leftRow ++;
//		}
//		leftTable.closeRowScan();
//		
//		return rowToTuple;
//	}
//
//	private Boolean evaluate(List<Field> leftFields, Tuple leftTuple,
//			List<Field> rightFields, Tuple rightTuple) {
//		for (Predicate pred : this.conditions) { // for each prdicate
//			String leftAttr = pred.getLeft().getAttributeName();
//			String rightAttr = 
//					((Attribute)pred.getRight()).getAttributeName();
//			// Check left index
//			int leftIndex = -1;
//			for (int i = 0; i < leftFields.size(); i ++) {
//				if (leftFields.get(i).getName().equals(leftAttr))
//					leftIndex = i;
//			}
//			// Check right index
//			int rightIndex = -1;
//			for (int i = 0; i < rightFields.size(); i ++) {
//				if (rightFields.get(i).getName().equals(rightAttr))
//					rightIndex = i;
//			}
//			String leftValue = leftTuple.getValue(leftIndex);
//			String rightValue = rightTuple.getValue(rightIndex);
//			// need crowdsource
//			if (leftValue.equalsIgnoreCase("null")) return null;
//			if (rightValue.equalsIgnoreCase("null")) return null;
//			// the predicates cannot be satisfied
//			if (!leftValue.equalsIgnoreCase(rightValue)) {
//				return false;
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
//		if (inTableNames.size() != 2) 
//			throw new Exception ("CJoin `" + this.getID() + 
//				"` has invalid number of inputs");
//		DataTable leftTable = inStore.getTable(inTableNames.get(0));
//		List<Field> leftFields = leftTable.getFields();
//		DataTable rightTable = inStore.getTable(inTableNames.get(1));
//		List<Field> rightFields = rightTable.getFields();
//		
//		
//		
//		// assemble the fields & create the new table
//		outStore.dropTable(outTableName);
//		List<Field> fields = new ArrayList<Field>();
//		for (Field f : leftFields) {
//			Field field = new Field(leftRelation + "." + f.getName(), 
//					f.getType(), f.isKey());
//			fields.add(field);
//		}
//		for (Field f : rightFields) {
//			Field field = new Field (rightRelation + "." + f.getName(),
//					f.getType(), f.isKey());
//			fields.add(field);
//		}
//		outStore.createTable(outTableName, fields);
//		
//		// Dump the tuples
//		List<Tuple> tuples = new ArrayList<Tuple>();
//		leftTable.openRowScan(inStore);
//		Tuple leftTuple = leftTable.getNextRow();
//		int leftRow = 0;
//		while (leftTuple != null) {
//			rightTable.openRowScan(inStore);
//			Tuple rightTuple = rightTable.getNextRow();
//			int rightRow = 0;
//			while (rightTuple != null) {
//				Boolean passed = evaluate (leftFields, leftTuple, 
//						rightFields, rightTuple);
//				Tuple tuple = null;
//				if (passed == null) { 
//					String answer = rowToAnswer.get(leftRow + "::" + rightRow);
//					if (answer.trim().equals("1")) {
//						tuple = Tuple.concat(leftTuple, 
//								rightTuple, leftTuple.kindex);
//					}
//				} else if (passed) {
//					tuple = Tuple.concat(leftTuple, 
//							rightTuple, leftTuple.kindex);
//				}
//				if (tuple != null) {
//					tuples.add(tuple);
//				}
//				rightTuple = rightTable.getNextRow();
//				rightRow ++;
//			}
//			rightTable.closeRowScan();
//			
//			leftTuple = leftTable.getNextRow();
//			leftRow ++;
//		}
//		leftTable.closeRowScan();
//		outStore.insertBatch(outTableName, tuples);
//		
//	}
	
	
	public JSONObject toJSON() {
		JSONObject jobj = super.toJSON();
		jobj.put("left-relation", this.leftRelation);
		jobj.put("right-relation", this.rightRelation);
		JSONArray jpreds = new JSONArray ();
		for (Predicate pred : conditions) {
			JSONObject jpred = new JSONObject ();
			jpred.put("left", pred.getLeft().getAttributeName());
			jpred.put("type", pred.getLeft().getType());
			jpred.put("pred-op", pred.getComparisonOp());
			jpred.put("right", ((Attribute)pred.getRight()).getAttributeName());
			jpreds.put(jpred);
		}
		jobj.put("predicates", jpreds);
		return jobj;
	}
	
	public static Op parse(JSONObject jop) {
		CrowdJoin sop = new CrowdJoin (jop.getInt("id"), 
				jop.getString("left-relation"),
				jop.getString("right-relation"));
		
		JSONArray jpreds = jop.getJSONArray("predicates");
		for (int i = 0; i < jpreds.length(); i ++) {
			JSONObject jpred = jpreds.getJSONObject(i);
			String attr = jpred.getString("left");
			VariableType type = VariableType.parse(jpred.getString("type"));
			ComparisonOp cop = ComparisonOp.parseComparison(jpred.getString("pred-op"));
			String vstr = jpred.getString("right");
			
			Attribute left = new Attribute (jop.getString("left-relation"), attr, type);
			Attribute right = new Attribute (jop.getString("right-relation"), vstr, type);
			Predicate pred = new Predicate (left, cop, right);
			sop.addCondition(pred);
		}
		return sop;
	}
	
	public String printPredicates() {
		String predStr = "";
		for (int i = 0; i < conditions.size(); i ++) {
			predStr += conditions.get(i);
			if (i < conditions.size() - 1) {
				predStr += " AND ";
			}
		}
		return predStr;
	}

	@Override
	public List<Annotation> seekForAnnotation(List<DataTable> inTables)
			throws Exception {
		if (inTables.size() != 2) throw new Exception ("CJoin `" + this.getID() + "`" + 
				": mismatched argument number " + inTables.size() + "(2)");
		DataTable leftTable = inTables.get(0);
		DataTable rightTable = inTables.get(1);
		
		// Step 1: Find tuples in the left table with null join attributes
		List<Predicate> leftNulls = new ArrayList<Predicate>();
		for (Predicate cond : conditions) {
			Predicate leftNull = new Predicate(cond.getLeft(), ComparisonOp.EQUALS, null);
			leftNulls.add(leftNull);
		}
		List<Tuple> leftNullTuples = leftTable.filter(leftNulls, 
				ComparisonOp.LOGIC_OR, null);
		
		// Step 2: Find tuples in the right table with null join attributes
		List<Predicate> rightNulls = new ArrayList<Predicate>();
		for (Predicate cond : conditions) {
			Predicate rightNull = new Predicate(
					(Attribute)cond.getRight(), ComparisonOp.EQUALS, null);
			rightNulls.add(rightNull);
		}
		List<Tuple> rightNullTuples = rightTable.filter(rightNulls, 
				ComparisonOp.LOGIC_OR, null);
		
		// Step 3: Generate annotations
		List<Annotation> annos = new ArrayList<Annotation>();
		// TODO: we did not optimize for the right table
		if (!leftNullTuples.isEmpty() && rightNullTuples.isEmpty()) { 
			// case 1: candidates = left null * right all
			List<Tuple> rightAllTuples = rightTable.filter(null, null, null); // find all tuples
			for (Tuple leftNullTuple : leftNullTuples) {
				Annotation anno = new MultiJoinAnnotation (leftNullTuple, rightAllTuples);
				annos.add(anno);
			}
		} else {
			// TODO: the algorithm below is naive (enumeration) and could be improved further
			List<Tuple> leftAllTuples = leftTable.filter(null, null, null);
			List<Tuple> rightAllTuples = rightTable.filter(null, null, null);
			for (Tuple leftTuple : leftAllTuples) {
				for (Tuple rightTuple : rightAllTuples) {
					if (leftNullTuples.contains(leftTuple) || 
							rightNullTuples.contains(rightTuple)) {
						Annotation anno = new JoinAnnotation (leftTuple, rightTuple);
						annos.add(anno);
					}
				}
			}
		}
		return annos;
	}

	@Override
	public void execute(List<DataTable> inTables, DataTable outTable,
			List<Annotation> annos) throws Exception {
		if (inTables.size() != 2) throw new Exception ("CJoin `" + this.getID() + "`" + 
				": mismatched argument number " + inTables.size() + "(2)");
		DataTable leftTable = inTables.get(0);
		DataTable rightTable = inTables.get(1);
		List<Field> newFields = new ArrayList<Field>();
		newFields.addAll(leftTable.getFields());
		newFields.addAll(rightTable.getFields());
		outTable.alterFields(newFields);
		
		List<Tuple> tuples = leftTable.join(rightTable, conditions, annos);
		outTable.insert(Tuple.toValueTuples(tuples));
	}
}
