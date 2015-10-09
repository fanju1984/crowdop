package sg.edu.nus.comp.datastore.schema;

import java.util.*;

/**
 * We consider a row-oriented layout
 * @author Ju Fan
 *
 */
public class Tuple {
	public Integer tid;
	private String [] values;
	//public Integer kindex; // the index in `values` indicating the key
	
	public Tuple(List<String> vlist, Integer tid) {
		values = vlist.toArray(new String[vlist.size()]);
		this.tid = tid;
	}
	
	public Tuple (String [] vs, Integer tid) {//, Integer keyIndex) {
		values = vs;
		this.tid = tid;
		//kindex = keyIndex;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Tuple) {
			return tid.equals(((Tuple)obj).tid);
		}
		return false;
	}
	
	public int hashCode() {
		return tid.hashCode();
	}
	
	public String[] getValues () {
		return values;
	}
	
	public int size() {
		return values.length;
	}
	
	public String getValue (int index) {
		return values[index];
	}
	
	public String toString () {
		return Arrays.asList(values).toString();
	}

//	public String getKey() {
//		if (kindex == null || 
//				kindex >= values.length) {
//			return null;
//		}
//		return values[kindex];
//	}
	
	public static Tuple concat (Tuple tuple1, 
			Tuple tuple2, Integer tid) {//, Integer kIndex) {
		String[] newValues = 
				new String[tuple1.size() + tuple2.size()];
		for (int i = 0; i < tuple1.size(); i ++) {
			newValues[i] = tuple1.getValue(i);
		}
		for (int i = 0; i < tuple2.size(); i ++) {
			newValues[i + tuple1.size()] = 
					tuple2.getValue(i);
		}
		Tuple newTuple = new Tuple (newValues, tid); //, kIndex);
		return newTuple;
	}
	
	public static List<List<String>> toValueTuples (List<Tuple> tuples) {
		List<List<String>> valueTuples = new ArrayList<List<String>> ();
		for (Tuple tuple : tuples) {
			valueTuples.add(Arrays.asList(tuple.getValues()));
		}
		return valueTuples;
	}

	public void fill(Map<Integer, Object> indexToValue) {
		for (int index : indexToValue.keySet()) {
			values[index] = (String)indexToValue.get(index);
		}
	}

	public static Tuple createByJoin(Tuple leftTuple, Tuple rightTuple,
			Map<Integer, Object> indexToJoin, Integer tid) {
		List<String> values = new ArrayList<String>();
		values.addAll(Arrays.asList(leftTuple.getValues()));
		values.addAll(Arrays.asList(rightTuple.getValues()));
		for (int lId : indexToJoin.keySet()) {
			int rId = (Integer)indexToJoin.get(lId);
			if (leftTuple.getValue(lId).equalsIgnoreCase("null")) {
				if (!rightTuple.getValue(rId).equalsIgnoreCase("null")) {
					values.set(lId, rightTuple.getValue(rId));
				}
			} else {
				if (rightTuple.getValue(rId).equalsIgnoreCase("null")) {
					values.set(leftTuple.size() + rId, 
							leftTuple.getValue(lId));
				}
			}
		}
		Tuple newTuple = new Tuple (values, tid);
		return newTuple;
	}
}
