package sg.edu.nus.comp.annotation;

import java.util.*;
import sg.edu.nus.comp.datastore.schema.Tuple;

public class Annotation {
	public String token; // annotation signature
	public boolean multiLabel; // if the answer(s) can be multiple
	public List<Tuple> contentTuples; // the content to be annotated
	public List<Tuple> optionTuples; // the options to annotate
	public List<String> selections;
	/**
	 * This constructor is used for generate annotations
	 * @param type
	 * @param multiLabel
	 */
	protected Annotation(boolean multiLabel) {
		this.multiLabel = multiLabel;
		contentTuples = new ArrayList<Tuple> ();
		optionTuples = new ArrayList<Tuple> ();
	}
	
	/**
	 * This constructor is used for collection results
	 */
	public Annotation (String token, boolean multiLabel, 
			List<String> selections) {
		this.token = token;
		this.selections = selections;
	}
	
	
	public static Map<String, List<Annotation>> groupByToken (List<Annotation> annos) {
		Map<String, List<Annotation>> groups = 
				new HashMap<String, List<Annotation>> ();
		for (Annotation anno : annos) {
			List<Annotation> group = groups.get(anno.token);
			if (group == null) {
				group = new ArrayList<Annotation>();
			}
			group.add(anno);
			groups.put(anno.token, group);
		}
		return groups;
	}
}
