package sg.edu.nus.comp.crowdop.core.op.machineop;

import java.util.List;

import org.json.JSONObject;

import sg.edu.nus.comp.annotation.Annotation;
import sg.edu.nus.comp.crowdop.core.op.Op;
import sg.edu.nus.comp.datastore.DataTable;

public class PartialResult extends Op {
	public static String type = "PartialResult";

	public PartialResult (int id) {
		super (id);
	}
	
	public String toString () {
		return "PartialResult " ;
	}
	
	
	@Override
	public JSONObject toJSON() {
		JSONObject jobj = super.toJSON();
		return jobj;
	}
	
	public static Op parse(JSONObject jop) {
		PartialResult pop = new PartialResult (jop.getInt("id"));
		return pop;
	}
	
	public String getType () {
		return type;
	}

	@Override
	public String getExplain() {
		// TODO Auto-generated method stub
		return "Return the result";
	}

	@Override
	public List<Annotation> seekForAnnotation(List<DataTable> inTables)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(List<DataTable> inTables, DataTable outTable,
			List<Annotation> annos) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
