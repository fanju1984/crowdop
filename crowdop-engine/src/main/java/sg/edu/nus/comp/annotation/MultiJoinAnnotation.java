package sg.edu.nus.comp.annotation;

import java.util.*;

import sg.edu.nus.comp.datastore.schema.Tuple;

public class MultiJoinAnnotation extends Annotation{
	public MultiJoinAnnotation (Tuple leftTuple, List<Tuple> rightTuples) {
		super (true);
		this.token = String.valueOf(leftTuple.tid);
		this.contentTuples.add(leftTuple);
		this.optionTuples.addAll(rightTuples);
	}
	
	public static Set<Integer> getJoinedRightTuples (
			List<String> selections) throws Exception {
		Set<Integer> rids = new HashSet<Integer>();
		for (String sel : selections) {
			rids.add(Integer.parseInt(sel));
		}
		return rids;
	}
}
