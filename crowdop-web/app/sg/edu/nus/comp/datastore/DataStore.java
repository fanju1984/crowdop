package sg.edu.nus.comp.datastore;

import java.util.*;

import sg.edu.nus.comp.datastore.plugin.CSVDataStore;
import sg.edu.nus.comp.datastore.schema.*;

/**
 * DataStore abstracts basic data operations over a database
 * 
 * @author Ju Fan
 *
 */


public interface DataStore {
	/*
	 * Get all tables in the data store
	 */
	public abstract List<DataTable> getAllDataTables() throws Exception;

	/*
	 * Get a specified data table
	 */
	public abstract DataTable getTable (String tableName);
	
	/* 
		Load all data values into main memory, i.e., data instance in the `datatable`
	*/
	public abstract void loadDataInstance (DataTable datatable, Integer sampleSize);

	/* 
		Get an iterator for scanning rows of the `datatable`
	*/
	public abstract Iterator<Tuple> getRowIterator(String tableName);

	/*
		Create a new table
	*/
	public abstract void createTable(String destTableName, 
			List<Field> fields) throws Exception ;

	/*
		Drop a table if it exists
	*/
	public abstract void dropTable(String tableName) throws Exception;
	
	
	/*
		Insert a batch of tuples into a destination table
	*/
	public abstract void insertBatch(String destTableName, 
			List<List<String>> batch) throws Exception;

	public abstract List<Tuple> rowsInRange(String tableName, 
			int start, int end) throws Exception ;

	public abstract double count(String tableName);

	public abstract List<Tuple> filterTable(String tableName,
			Map<Integer, Object> indexToValue, ComparisonOp logicOp);

	public abstract List<Tuple> findByIds(String tableName, Set<Integer> keySet);
	public abstract Map<Integer, Tuple> findMapByIds (String tableName, Set<Integer> keySet);

	public abstract void joinTables(String leftTableName, String rightTableName,
			Map<Integer, Object> indexToJoin, Map<Integer, Set<Integer>> joinMap);	

}
