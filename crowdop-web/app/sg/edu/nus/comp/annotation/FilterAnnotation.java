package sg.edu.nus.comp.annotation;

import java.util.List;

import sg.edu.nus.comp.datastore.schema.*;

public class FilterAnnotation extends Annotation{
	public FilterAnnotation (Tuple tuple) {
		super(false);
		this.token = String.valueOf(tuple.tid);
		this.contentTuples.add(tuple);
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
