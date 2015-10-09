package sg.edu.nus.comp.datastore.schema;

import java.util.*;

public class GDD {
	
	static GDD gdd = null;
	public static GDD loadGDD () {
		if (gdd == null) {
			gdd = new GDD ();
		}
		return gdd;
	}
	
	private String currentDatabase = null;
	private Map<String, Map<String, Table>> databases =
		new HashMap<String, Map<String, Table>>();
	
	public GDD () {}
	
	
	public String getCurrentDatabase (){
		return this.currentDatabase;
	}
	
	public Map<String, Map<String, Table>> getDatabases () {
		return this.databases;
	}
	
	public boolean databaseExists(String name) {
		return databases.containsKey(name);
	}
	
	public void addDatabase (String dbName) {
		databases.put(dbName, new HashMap<String, Table> ());
	}
	

	public void selectDatabase(String name) throws Exception {
		if (!databaseExists(name)) {
			throw new Exception ("The database with name '"+name+"' is nonexsitent");
		}
		currentDatabase = name;
	}
	
	public boolean tableExists (String dbName, String tableName) {
		if (databases.containsKey(dbName)) {
			if (databases.get(dbName).containsKey(tableName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean tableExists(String name) {
		if (currentDatabase == null)
			return false;
		
		return databases.get(currentDatabase).containsKey(name);
	}
	
	public void addTable (String dbName, String tableName) {
		databases.get(dbName).put(tableName, new Table (tableName));
	}
	
	public void addField (String dbName, String tableName, 
			String fieldName, int fieldOrder) {
		try {
			Table table = databases.get(dbName).get(tableName);
			boolean isKey = false;
			if (fieldName.equals("id")) isKey = true;
			Field field = new Field (fieldName, VariableType.STRING, isKey);
			field.setOrder(fieldOrder);
			table.addField(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean fieldExists(String[] tables, String name) throws Exception {
		String table, field;
		int idx;

		if (currentDatabase == null) {
			throw new Exception ("Select a database first");
		}
		
		if ((idx = name.indexOf('.')) >= 0) {
			table = name.substring(0, idx);
			field = name.substring(idx+1, name.length());
		} else {
			table = null;
			field = name;
		}
		
		if (table != null) {
			boolean tableInList = false;
			for (String ct: tables) {
				if (table.equals(ct)) {
					tableInList = true;
				}
			}
			if (!tableInList) {
				/* the field is in format table.field and the table can't be found from list */
				return false;
			}
			
			Table tb = databases.get(currentDatabase).get(table);
			if (tb == null) {
				/* table of table.field format doesn't exist */
				return false;
			}

			if (!tb.fieldExists(field)) {
				return false;
			}
		} else {
			int instances = 0;
			
			for (String tab: tables) {
				Table tb = databases.get(currentDatabase).get(tab);
				if (tb == null) {
					/* table in table list doesn't exist */
					return false;
				}
				
				if (tb.fieldExists(field)) {
					instances++;
				}
			}
			
			/* if table is not specified, there can be only one instance of certain field name */
			if (instances != 1) {
				return false;
			}
		}
		
		return true;
	}
	
	
	
	private Table getTable (String tableName) throws Exception {
		if (this.currentDatabase == null) {
			throw new Exception ("Select a database first");
		}
		Map<String, Table> database = this.databases.get(currentDatabase);
		if (!database.containsKey(tableName)) {
			throw new Exception ("The table with name '" + tableName +"' isn't found");
		}
		Table table = database.get(tableName);
		return table;
	}
	
	/**
	 * 
	 * @param db by songxm
	 * @return
	 */
	public List<Table> getTablesByDBName(String db) throws Exception{
		if (!this.databaseExists(db)) {
			throw new Exception("Database " + db + " not exist");
		}
		ArrayList<Table> aTables = new ArrayList<Table>();
		Map <String, Table> database = this.databases.get(db);
		for (String name : database.keySet()) {
			aTables.add(database.get(name));
		}
		return aTables;
	}
	
	public Table getTable(String db, String name) {
		Map<String, Table> database = this.databases.get(db);
		Table table = database.get(name);
		return table;
	}
	

	
	public Attribute[] getAttributes (String tableName) throws Exception{
		Table table = this.getTable(tableName);
		return table.getAttributes();
	}
	
	
	public Attribute getPrimaryKey (String tableName) throws Exception {
		Table table = this.getTable(tableName);
		return table.getPrimaryKey();
	}
	
	
	public String getCurrentDBName () throws Exception{
		if (this.currentDatabase == null) {
			throw new Exception ("Cannot read from the current database");
		}
		return this.currentDatabase;
	}
	
	public String toString () {
		StringBuffer buffer = new StringBuffer();
		buffer.append("************  GDD  ***************\n");
		buffer.append("current database: " + this.currentDatabase+"\n");
		for (String dbName : databases.keySet()){
			buffer.append("database: " + dbName + "\n");
			for (String tableName : databases.get(dbName).keySet()) {
				Table table = this.databases.get(dbName).get(tableName);
				buffer.append(tableName+" : " + table+"\n");
			}
			buffer.append("\n");
			
		}
		
		buffer.append("*******************************\n");
		return buffer.toString();
	}
}
