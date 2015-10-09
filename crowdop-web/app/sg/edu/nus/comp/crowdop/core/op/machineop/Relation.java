package sg.edu.nus.comp.crowdop.core.op.machineop;

import org.json.JSONObject;

import sg.edu.nus.comp.annotation.Annotation;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.datastore.DataTable;
import sg.edu.nus.comp.datastore.schema.*;

import java.util.*;

public class Relation extends Op {
	public String tableName;

	public static String type = "Relation";

	public Relation (int oid, String tname) {
		super (oid);
		tableName = tname;
	}
	
	public String toString () {
		return "Relation [" + tableName + "]: ";
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jobj = super.toJSON();
		jobj.put("relation-name", this.tableName);
		return jobj;
	}
	
	public static Op parse(JSONObject jop) {
		Relation rop = new Relation (jop.getInt("id"), 
				jop.getString("relation-name"));
		return rop;
		
	}
	
	public String getType () {
		return type;
	}
	
	public void execute(List<DataTable> inTables, DataTable outTable, 
			List<Annotation> annos) throws Exception{
		if (inTables.size() != 1) throw new Exception ("CSelect `" + this.getID() + "`" + 
				": mismatched argument number " + inTables.size() + "(1)");
		DataTable inTable = inTables.get(0);
		
		Iterator<Tuple> it = inTable.getRowIterator();
		List<Tuple> tuples = new ArrayList<Tuple> ();
		while (it.hasNext()) {
			Tuple tuple = it.next();
			tuples.add(tuple);
		}
		outTable.alterFields(inTable.getFields());
		outTable.insert(Tuple.toValueTuples(tuples));
	}

	@Override
	public String getExplain() {
		return "Fetch records from data table `" + this.tableName + "'";
	}

	@Override
	public List<Annotation> seekForAnnotation(List<DataTable> inTables)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
