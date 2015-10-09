package sg.edu.nus.comp.backend;


import java.util.*;
import java.io.*;

/**
 * Do not forget to run the "finishDump" function.
 * @author Ju Fan
 *
 */
public class DBDump {
	SQLBackend sql;
	String prefix = null;
	List<String> tuples = null;
	int batch = -1;
	BufferedWriter w;
	public DBDump (SQLBackend pSql) throws Exception {
		sql = pSql;
		w = new BufferedWriter (new FileWriter ("./dump.log"));
	}
	
	void doInsert() throws Exception {
		String stmt = prefix;
		int bcount = 0;
		for (String tuple : tuples) {
			stmt += tuple;
			if (bcount < tuples.size() - 1) {
				stmt += ",";
			}
			bcount++;
		}
		//try {
			//System.out.println(stmt);
		sql.execute(stmt);
		//} catch (Exception e) {
		//	e.printStackTrace();
		//	System.out.println (stmt);
		//	w.write(stmt);
		//	w.newLine();
		//	w.flush();
		//}
		tuples.clear();
	}
	
	public void initDump (String pPrefix, int pBatch) throws Exception{
		prefix = pPrefix;
		tuples = new LinkedList<String> ();
		batch = pBatch;
	}
	
	public void addTuple (String tuple) throws Exception {
		tuples.add(tuple);
		if (tuples.size() == batch) {
			doInsert ();
		}
	}
	
	public void finishDump () throws Exception {
		if (tuples.size() > 0) {
			doInsert();
		}
		prefix = null;
		tuples = null;
	}
	
	public static void main (String args[]) throws Exception {
		SQLBackend sql = null;
		DBDump dump = new DBDump (sql);
		dump.initDump("INSERT INTO table1 (a1, a2) VALUES ", 100);
		while (true) {
			dump.addTuple("('abc',123)");
			break;
		}
		dump.finishDump();
	}
}
