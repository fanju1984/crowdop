package sg.edu.nus.comp.annotation;

import java.util.List;

import sg.edu.nus.comp.datastore.schema.*;

public class JoinAnnotation extends Annotation {
	
	public JoinAnnotation (Tuple leftTuple, Tuple rightTuple) {
		super (false);
		this.token = leftTuple.tid + "_" + rightTuple.tid;
		this.contentTuples.add(leftTuple);
		this.contentTuples.add(rightTuple);
		this.optionTuples.add(new Tuple(new String[0], 0));
		this.optionTuples.add(new Tuple(new String[0], 1));
	}
	
	public static Boolean isPassed (List<String> selections) throws Exception {
		if (selections.size() > 1) 
			throw new Exception ("invalid result number for Filter Annotation");
		if (selections.isEmpty()) return null;
		return selections.get(0).equals("1");
	}
}
