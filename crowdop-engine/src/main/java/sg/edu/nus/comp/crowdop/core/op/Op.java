package sg.edu.nus.comp.crowdop.core.op;

import java.util.List;

import sg.edu.nus.comp.annotation.Annotation;
import sg.edu.nus.comp.crowdop.core.op.crowdop.*;
import sg.edu.nus.comp.crowdop.core.op.machineop.*;
import sg.edu.nus.comp.datastore.DataTable;

import org.json.*;

public abstract class Op {
	protected int oid; // ID of the OP
	
	public Op (int id) {
		oid = id;
	}
	
	public boolean equals (Object obj) {
		if (obj instanceof Op) {
			Op op = (Op) obj;
			return oid == op.oid;
		}
		return false;
	}
	
	public int hashCode () {
		return new Integer (this.oid).hashCode();
	}	
	
	public Op cloneOp () {
		return null;
	}

	public int getID() {
		return oid;
	}
	
	public abstract List<Annotation> seekForAnnotation (
			List<DataTable> inTables) throws Exception;
	
	public abstract void execute(List<DataTable> inTables, DataTable outTable, 
			List<Annotation> annos) throws Exception;
	
	public abstract String getType ();
	
	public abstract String getExplain();
		
	public JSONObject toJSON () {
		JSONObject jobj = new JSONObject ();
		jobj.put("id", this.oid);
		jobj.put("type", this.getType());
		return jobj;
	}

	public static Op parse(JSONObject jop) {
		String type = jop.getString("type");
		if (type.equals(Relation.type)) {
			return Relation.parse(jop);
		} else if (type.equals(CrowdSelect.type)) {
			return CrowdSelect.parse(jop);
		} else if (type.equals(CrowdJoin.type)) {
			return CrowdJoin.parse(jop);
		} else if (type.equals(CrowdFill.type)) {
			return CrowdFill.parse(jop);
		} else if (type.equals(PartialResult.type)) {
			return PartialResult.parse(jop);
		}
		return null;
	}
	
}
