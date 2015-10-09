package sg.edu.nus.comp.datastore.plugin;

import java.io.*;
import java.util.*;

import sg.edu.nus.comp.datastore.*;
import sg.edu.nus.comp.datastore.schema.*;

import org.apache.commons.csv.*;

/**
 * This class implments a data stores based on CSV files
 * 
 * @author Ju Fan
 *
 */


public class CSVDataStore implements DataStore {
	class CSVDataIterator implements Iterator<Tuple> {
		Iterator<CSVRecord> csvIter;
		Integer keyIndex;
		public CSVDataIterator (String datafile) throws Exception { //, Integer kindex) throws Exception {
			Reader in = new FileReader(datafile);
			csvIter = CSVFormat.EXCEL.parse(in).iterator();
			csvIter.hasNext();
			csvIter.next();
			
		}

		public boolean hasNext() {
			try {
				return csvIter.hasNext();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return false;
		}
		
		public void remove() {
			
		}
		
		public Tuple next() {
			CSVRecord record = csvIter.next();
			int tid = Integer.parseInt(record.get(0));
			
			String vstrs[] = new String[record.size() - 1];
			for (int i = 1; i < record.size(); i ++) {
				vstrs[i - 1] = record.get(i);
			}
			Tuple tuple = new Tuple (vstrs, tid);//, keyIndex);
			return tuple;
		}
		
		public void finalize () {
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	String storePath;
	
	/**
	 * Create/Specify a directory file store in a given path
	 * @param path: the path storing the temporary files
	 */
	public CSVDataStore (String path) {
		try {
			File dir = new File (path);
			if (!dir.exists()) dir.mkdirs();
			storePath = path;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<DataTable> getAllDataTables() throws Exception {
		List<DataTable> dataTables = new ArrayList<DataTable> ();
		File folder = new File (storePath);
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.getAbsolutePath().endsWith(".csv")) {
				String tableName = file.getName().substring(0, 
						file.getName().length() - 4);
				DataTable dataTable = getTable (tableName);
				if (dataTable != null) {
					dataTables.add(dataTable);
				}
			}
		}
		return dataTables;
	}


	@Override
	public DataTable getTable(String tableName) {
		try {
			String filename = storePath + tableName + ".csv";
			DataTable datatable = parseMeta(tableName, filename);
			return datatable;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private DataTable parseMeta (String tableName, String filename) throws Exception {
		//System.out.println("Parse Meta: " + tableName + "\t" + filename);
		File file = new File (filename);
		if (!file.exists()) return null;
		BufferedReader r = new BufferedReader (new FileReader (filename));
		String line = r.readLine();
		r.close();
		Reader in = new StringReader(line);
		CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
		List<CSVRecord> list = parser.getRecords();
		parser.close();

		CSVRecord title = list.get(0);

		List<Field> fields = new ArrayList<Field> (); 

		for (int i = 1; i < title.size(); i ++) { 
			// Note: the index 0 is reserved for `tid`
			String value = title.get(i); // get the value
			String[] tmps = value.split ("::");
			String fieldName = tmps[0]; // field name
			String fieldType = tmps[1]; // field type
			Boolean isKey = Boolean.parseBoolean (tmps[2]); // is key?
			Field field = new Field (fieldName, 
					VariableType.parse(fieldType), isKey);
			fields.add(field);
		}
		DataTable datatable = new DataTable (tableName, fields, this);
		return datatable;
	}

	@Override
	public void createTable(String destTableName, 
			List<Field> fields) throws Exception {
		String filename = storePath + destTableName + ".csv";
		File file = new File (filename);
		if (file.exists()) throw 
			new Exception ("Table `" + destTableName + "` already exists");
		FileWriter out = new FileWriter(filename);
		CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.EXCEL);
		List<String> values = new ArrayList<String> ();
		values.add("tid");
		for (Field field : fields) {
			String value = field.getName() + "::" +
				field.getType() + "::" +
				field.isKey();
			values.add (value);
		}
		csvFilePrinter.printRecord(values);
		csvFilePrinter.close();
		out.close();
	}

	public void insertBatch(String tableName, List<List<String>> batch) throws Exception{
		String filename = storePath + tableName + ".csv";
		File file = new File (filename);
		if (!file.exists()) throw 
			new Exception ("Table `" + tableName + "` does not exists");
		int maxId = -1;
		Iterator<Tuple> it = this.getRowIterator(tableName);
		while (it.hasNext()) {
			Tuple tuple = it.next();
			maxId = tuple.tid;
		}
		maxId ++;
		
		FileWriter out = new FileWriter(filename, true);
		CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.EXCEL);
		
		for (List<String> values : batch) {
			List<String> newValues = new ArrayList<String>();
			newValues.add(String.valueOf(maxId));
			newValues.addAll(values);
			
			csvFilePrinter.printRecord(newValues);
			maxId ++;
		}
		csvFilePrinter.close();
		out.close();
	}

	@Override
	public void dropTable(String tableName) throws Exception {
		// TODO Auto-generated method stub
		String filename = storePath + tableName + ".csv";
		File file = new File (filename);
		if (file.exists()) {
			file.delete();
		}
	}

	@Override
	public void loadDataInstance(DataTable datatable, Integer sampleSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Tuple> getRowIterator(String tableName) {
		try {
			String filename = storePath + tableName + ".csv";
			File file = new File (filename);
			if (!file.exists()) throw 
				new Exception ("Table `" + tableName + "` does not exists");
			
			CSVDataIterator it = new CSVDataIterator (filename);//, datatable.getKeyIndex());
			return it;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Tuple> rowsInRange(String tableName, 
			int start, int end) throws Exception {
		// TODO: We only implment the simple scan-based method now
		List<Tuple> tuples = new ArrayList<Tuple> ();
		Iterator<Tuple> it = this.getRowIterator(tableName);
		int row = 0;
		while (it.hasNext()) {
			Tuple tuple = it.next();
			if (row >= start && row < end) {
				tuples.add(tuple);
			}
			row ++;
		}
		it = null; // destroy the iterator
		return tuples;
	}

	@Override
	public double count(String tableName) {
		int count = 0;
		Iterator<Tuple> it = this.getRowIterator(tableName);
		while (it.hasNext()) {
			count ++;
			it.next();
		}
		return count;
	}

	@Override
	public List<Tuple> filterTable(String tableName, 
			Map<Integer, Object> indexToPredValue, ComparisonOp logicOp) {
		List<Tuple> tuples = new ArrayList<Tuple> ();
		Iterator<Tuple> it = this.getRowIterator(tableName);
		//System.out.println("Start to Filter: " + tableName + "\t" + indexToPredValue);
		while (it.hasNext()) {
			Tuple tuple = it.next();
			Boolean flag = null;
			for (int index : indexToPredValue.keySet()) {
				Boolean passed = null;
				String tupleValue = tuple.getValue(index); // tuple value 
				String predValue = (String)indexToPredValue.get(index); // predicate value
				if (predValue == null) {
					if (tupleValue.equalsIgnoreCase("null")) 
						passed = true;
					else passed = false;
				} else {
					passed = tupleValue.equalsIgnoreCase(predValue);
				}
				if (flag == null) flag = passed;
				else if (logicOp == ComparisonOp.LOGIC_AND) flag = flag && passed;
				else flag = flag || passed;
			}
			if (flag) tuples.add(tuple);
		}
		return tuples;
	}

	@Override
	public List<Tuple> findByIds(String tableName, Set<Integer> keySet) {
		List<Tuple> tuples = new ArrayList<Tuple> ();
		Iterator<Tuple> it = this.getRowIterator(tableName);
		while (it.hasNext()) {
			Tuple tuple = it.next();
			if (keySet.contains(tuple.tid)) {
				tuples.add(tuple);
			}
		}
		return tuples;
	}

	@Override
	public void joinTables(String leftTableName, String rightTableName,
			Map<Integer, Object> indexToJoin, Map<Integer, Set<Integer>> joinMap) {
		Iterator<Tuple> lIt = this.getRowIterator(leftTableName);
		while (lIt.hasNext()) {
			Tuple lTuple = lIt.next();
			Set<Integer> rightIds = joinMap.get(lTuple.tid);
			if (rightIds == null) {
				rightIds = new HashSet<Integer> ();
				joinMap.put(lTuple.tid, rightIds);
			}
			Iterator<Tuple> rIt = this.getRowIterator(rightTableName);
			while (rIt.hasNext()) {
				Tuple rTuple = rIt.next();
				Boolean flag = null;
				for (int lf : indexToJoin.keySet()) {
					int rf = (Integer) indexToJoin.get(lf);
					if (lTuple.getValue(lf).equalsIgnoreCase("null") 
							|| rTuple.getValue(rf).equalsIgnoreCase("null")) {
						flag = null; // unknown
						break;
					} else if (lTuple.getValue(lf).equalsIgnoreCase(
								rTuple.getValue(rf))) {
							flag = true; // partially satisfied
					} else {
						flag = false;
						break; // not satisfied
					}
				}
				if (flag != null && flag) {
					rightIds.add(rTuple.tid); // add the right id
				}
			}
		}
		
	}

	@Override
	public Map<Integer, Tuple> findMapByIds(String tableName,
			Set<Integer> keySet) {
		Map<Integer, Tuple> tupleMap = new HashMap<Integer, Tuple>();
		Iterator<Tuple> it = this.getRowIterator(tableName);
		while (it.hasNext()) {
			Tuple tuple = it.next();
			if (keySet.contains(tuple.tid)) {
				tupleMap.put(tuple.tid, tuple);
			}
		}
		return tupleMap;
	}

	
}
