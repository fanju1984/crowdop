package sg.edu.nus.comp.crowdop.core.execute;

import java.util.*;

public class Microtask {
	public static final String TYPE_SINGLE = "SINGLE";
	public static final String TYPE_MULTIPLE = "MULTIPLE";
	
	public String tid; // the identifier of the microtask
	public String stem; // the microtask (question) stem
	public Map<String, String> choices; // the possible choices
	public String type; // the type of the microtask
	public boolean isClose; // the answer is close w.r.t the choices
	
	
	public Microtask(String taskID, String taskStem, 
			Map<String, String> taskChoices, String taskType, boolean close) {
		tid = taskID;
		stem = taskStem;
		choices = taskChoices;
		type = taskType;
		isClose = close;
	}
}
