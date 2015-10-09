package sg.edu.nus.comp.datastore;

import java.util.*;

import sg.edu.nus.comp.annotation.*;
import sg.edu.nus.comp.datastore.schema.*;
import sg.edu.nus.comp.importer.FileImporter;

/**
 * This is the main class of this package.
 * This class abstracts the basic structure of a relational table, which typically contains
 * 1) Meta-data, which is represented in class TableMeta
 * 2) Data values, which is contained in the 2-dimension data values
 *
 * @author Ju Fan
 */

public class DataTable {
	String tableName;
	List<Field> fields; //all fields belonging to the table
	DataStore store;
	List<Tuple> tuples; // the tuples caching in the memory
	
    public DataTable(String name, List<Field> fs, DataStore store) throws Exception{
    	tableName = name;
    	fields = fs;
    	this.store = store;
    }
    
    /**
     * Get the table name
     * @return table name
     */
    public String getName() {
		return this.tableName;
	}

    /**
     * Get list of fields in the table
     * @return list of table fields
     */
	public List<Field> getFields() {
		return this.fields;
	}
	
	public Field getFieldByName(String attribute) {
		for (Field field : fields) {
			if (field.getName().equals(attribute)) return field;
		}
		return null;
	}
	
	public int getFieldIndex (String attribute) {
		for (int i = 0; i < fields.size(); i ++) {
			if (fields.get(i).getName().equals(attribute)) return i;
		}
		return -1;
	}
	
	public void alterFields(List<Field> newFields) throws Exception {
		// TODO Auto-generated method stub
		store.dropTable(this.tableName);
		store.createTable(tableName, newFields);
	}
	
	public void importFromFile (String filename) throws Exception {
		FileImporter importer = FileImporter.create(filename); // create an importer
		List<Field> fields = new ArrayList<Field>();
		List<List<String>> tuples = new ArrayList<List<String>>();
		importer.runImport(fields, tuples); // collect fields & tuples from the file
		// TODO: we only consider the replace mode now
		this.alterFields(fields);
		this.insert(tuples);
	}
	
	/**
	 * Get the name of all fields
	 * @return a list of field names
	 */
	public List<String> getFieldNames () {
		List<String> fieldNames = new ArrayList<String>();
		for (Field field : fields) {
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}
	


	/**
	 * Get iterator for fetch the rows in the table
	 * @return
	 */
	public Iterator<Tuple> getRowIterator() {
		return store.getRowIterator(tableName);
	}
 
	/**
	 * Random access the tuples within a range
	 * @param start: the starting row number
	 * @param end: the ending row number
	 * @return: a list of tuple strings
	 */
	public List<List<String>> fetch (int start, int end) throws Exception {
		List<Tuple> tuples = store.rowsInRange(this.tableName, start, end);
		List<List<String>> tupleStrs = new ArrayList<List<String>> ();
		for (Tuple tuple : tuples) {
			List<String> tupleStr = Arrays.asList(tuple.getValues());
			tupleStrs.add(tupleStr);
		}
		return tupleStrs;
	}

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tableName + "\n");
        
        for (Field field : fields) {
            buffer.append(field.getName() + "\t");
        }
        buffer.append("\n");

        int num = 50;
        int row = 0;
        Iterator<Tuple> it = this.getRowIterator();
        while (it.hasNext()) {
        	Tuple tuple = it.next();
        	buffer.append("ROW " + row + "\t");
            for (String cell : tuple.getValues()) {
                buffer.append(cell + "\t");
            }
            buffer.append("\n");
        	row ++;
        	if (row >= num) break;
        }
        return buffer.toString();
    }
    
	public void insert(List<List<String>> batch) throws Exception {
		this.store.insertBatch(tableName, batch);
	}
	
	private Map<Integer, Object> extractPredicates (List<Predicate> predicates, 
			DataTable joinTable) {
		Map<Integer, Object> indexToValue = new HashMap<Integer, Object>();
		for (Predicate pred : predicates) {
			int fieldIndex = this.getFieldIndex(
					pred.getLeft().getAttributeName());
			Object value = null;
			if (pred.getRight() != null) {
				if (joinTable != null) { // the right is an attribute
					String rightAttrName = ((Attribute)pred.getRight()).getAttributeName();
					int rightIndex = joinTable.getFieldIndex(rightAttrName);
					value = rightIndex;
				} else { // the right is a value
					value = ((Value)pred.getRight()).toString();
				}
			}	
			indexToValue.put(fieldIndex, value);
		}
		return indexToValue;
	}

	public List<Tuple> filter(List<Predicate> predicates, ComparisonOp logicOp, 
			List<Annotation> annos) throws Exception {
		
		// Derive the value from predicates
		List<Tuple> tuples = new ArrayList<Tuple>();
		if (predicates == null && logicOp == null && annos == null) {
			// Special case: Fetch all;
			Iterator<Tuple> it = this.getRowIterator();
			while (it.hasNext()) {
				tuples.add(it.next());
			}
			return tuples;
		}
		Map<Integer, Object> indexToPredValue = this.extractPredicates(predicates, null);
		
		if (annos != null) {
			// Find the annotated tuples
			Map<String, List<Annotation>> annoGroups = 
					Annotation.groupByToken(annos);
			Set<Integer> annoIds = new HashSet<Integer>();
			for (String token : annoGroups.keySet()) {
				annoIds.add(Integer.parseInt(token));
			}
			List<Tuple> annoTuples = store.findByIds(tableName, annoIds);
			for (Tuple annoTuple : annoTuples) {
				List<Annotation> tupleAnnos = annoGroups.get(String.valueOf(annoTuple.tid));
				Boolean passed = FilterAnnotation.isPassed(tupleAnnos.get(0).selections);
				if (passed != null && passed) {
					if (logicOp == ComparisonOp.LOGIC_AND)
						annoTuple.fill (indexToPredValue); 
					// fill the tuple based on the derived values
					tuples.add(annoTuple);
				}
			}
		}
		List<Tuple> knownTuples = store.filterTable(tableName, indexToPredValue, logicOp);
		tuples.addAll(knownTuples);
		return tuples;
	}

	public List<Tuple> join(DataTable rightTable, List<Predicate> conditions,
			List<Annotation> annos) throws Exception {
		List<Tuple> tuples = new ArrayList<Tuple>();
		Map<Integer, Set<Integer>> joinMap = new HashMap<Integer, Set<Integer>> ();
		if (annos != null) { // find the join map based on annotations
			Map<String, List<Annotation>> annoGroups = 
					Annotation.groupByToken(annos);
			for (String token : annoGroups.keySet()) {
				String[] tmps = token.split("_");
				int tid = Integer.parseInt(tmps[0]);
				Set<Integer> joinIds = joinMap.get(tid);
				if(joinIds == null) {
					joinIds = new HashSet<Integer>();
					joinMap.put(tid, joinIds); // initialize the join map
				}
				for (Annotation anno: annoGroups.get(token)) {
					if (tmps.length == 2) {
						int joinId = Integer.parseInt(tmps[1]);
						Boolean passed = JoinAnnotation.isPassed(anno.selections);
						if (passed != null && passed) 
							joinIds.add(joinId); // put into the join
					} else {
						Set<Integer> rids = MultiJoinAnnotation.
								getJoinedRightTuples(anno.selections);
						
						joinIds.addAll(rids);
					}
				}
			}
		}
		
		Set<Integer> rightIds = new HashSet<Integer>();
		for (int lId : joinMap.keySet()) rightIds.addAll(joinMap.get(lId));
		Map<Integer, Object> indexToJoin = extractPredicates(conditions, rightTable);
		store.joinTables (tableName, rightTable.getName(), indexToJoin, joinMap);
		List<Tuple> leftTuples = store.findByIds(tableName, joinMap.keySet());
		Map<Integer, Tuple> rightTupleMap = store.findMapByIds(rightTable.getName(), rightIds);
		for (Tuple leftTuple : leftTuples) {
			for (int rId : joinMap.get(leftTuple.tid)) {
				Tuple rightTuple = rightTupleMap.get(rId);
				Tuple newTuple = Tuple.createByJoin(leftTuple, 
						rightTuple, indexToJoin, null);
				tuples.add(newTuple);
			}
		}
		
		return tuples;
	}

	public List<Tuple> fill(List<Annotation> annos, 
			List<String> attrMap) throws Exception {
		List<Tuple> tuples = new ArrayList<Tuple>();
		Map<Integer, List<Annotation>> annoGroups = 
				new HashMap<Integer, List<Annotation>> ();
		for (Annotation anno : annos) {
			String[] tmps = anno.token.split("_");
			int tid = Integer.parseInt(tmps[1]);
			List<Annotation> annoGroup = annoGroups.get(tid);
			if (annoGroup == null) annoGroup = new ArrayList<Annotation>();
			annoGroup.add(anno);
			annoGroups.put(tid, annoGroup);
		}
		Iterator<Tuple> it = this.getRowIterator();
		while (it.hasNext()) {
			Tuple tuple = it.next();
			List<Annotation> tupleAnnos = annoGroups.get(tuple.tid);
			if (tupleAnnos != null) {
				Map<Integer, Object> indexToFValue = 
						new HashMap<Integer, Object>();
				for (Annotation anno : tupleAnnos) {
					String fvalue = LabelAnnotation.getFilledValue(anno.selections);
					int attrIndex = Integer.parseInt(anno.token.split("_")[0]);
					int fieldIndex = this.getFieldIndex(attrMap.get(attrIndex));
					indexToFValue.put(fieldIndex, fvalue);
				}
				tuple.fill(indexToFValue);
			}
			tuples.add(tuple);
		}
		
		return tuples;
	}

	public double count() {
		return this.store.count(tableName);
	}

	

	
}
