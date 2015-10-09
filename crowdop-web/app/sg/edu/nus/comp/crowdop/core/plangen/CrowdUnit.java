package sg.edu.nus.comp.crowdop.core.plangen;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import sg.edu.nus.comp.crowdop.core.op.Op;

public class CrowdUnit {
	public final static int STATE_INIT = 0;
	public final static int STATE_ONGO = 1;
	public final static int STATE_COMP = 2;
	
	public CrowdUnit(Op operator, double unitPrice,
			List<Integer> inputUnitIds) {
		op = operator;
		taskPrice = unitPrice;
		inputIds = inputUnitIds;
		state = STATE_INIT;
		totalTaskNum = 0;
		compTaskNum = 0;
	}
	
	private CrowdUnit () {}
	
	public Op op;
	public List<Integer> inputIds; // the inputs of the unit
	int state; // the state of the unit
	int totalTaskNum;
	int compTaskNum;
	public double taskPrice;

	public Op getOperator() {
		return op;
	}

	public void setUnitState (int unitState) {
		this.state = unitState;
	}

	public List<Integer> getInputIDs() {
		return inputIds;
	}

	

	public double getCrowdPrice() {
		return this.taskPrice;
	}

	public void setTotalTaskNum(int num) {
		this.totalTaskNum = num;
		
	}

	public JSONObject toJSON() {
		JSONObject junit = new JSONObject ();
		junit.put("operator", op.toJSON());
		junit.put("price", this.taskPrice);
		junit.put("state", this.state);
		junit.put("total-task-num", this.totalTaskNum);
		junit.put("comp-task-num", this.compTaskNum);
		JSONArray jary = new JSONArray ();
		for (int inId : inputIds) jary.put(inId);
		junit.put("inputs", jary);
		return junit;
	}

	public static CrowdUnit parse(JSONObject junit) {
		CrowdUnit cunit = new CrowdUnit ();
		cunit.op = Op.parse (junit.getJSONObject("operator"));
		cunit.taskPrice = junit.getDouble("price");
		cunit.state = junit.getInt("state");
		cunit.totalTaskNum = junit.getInt("total-task-num");
		cunit.compTaskNum = junit.getInt("comp-task-num");
		cunit.inputIds = new ArrayList<Integer> ();
		JSONArray jary = junit.getJSONArray("inputs");
		for (int i = 0; i < jary.length(); i ++) {
			cunit.inputIds.add(jary.getInt(i));
		}
		return cunit;
	}
}
