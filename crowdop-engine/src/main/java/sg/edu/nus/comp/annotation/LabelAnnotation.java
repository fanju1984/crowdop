package sg.edu.nus.comp.annotation;

import sg.edu.nus.comp.datastore.schema.Tuple;

import java.util.*;

public class LabelAnnotation extends Annotation {
	
	public LabelAnnotation (int fieldIndex, Tuple tuple, Set<String> labels) {
		super (false);
		this.token = fieldIndex + "_" + tuple.tid;
		this.contentTuples.add(tuple);
		for (String label : labels) {
			String[] tmps = {label};
			this.optionTuples.add(new Tuple(tmps, null));
		}
	}
	
	public static String getFilledValue (List<String> selections) throws Exception {
		if (selections.size() > 1) 
			throw new Exception ("invalid result number for Filter Annotation");
		if (selections.isEmpty()) return null;
		return selections.get(0);
	}
}
