package sg.edu.nus.comp.util;

public class ScoredItem<T> implements Comparable<Object>{
	T obj;
	double score;
	
	public ScoredItem (T pObj, double pScore) {
		obj = pObj;
		score = pScore;
	}
	
	public ScoredItem (T pObj) {
		obj = pObj;
	}
	
	public T getItem () {
		return obj;
	}
	
	public void setScore (double pScore) {
		score = pScore;
	}
	
	public double getScore () {
		return score;
	}

	/**
	 * This comparison is for descending ranking
	 */
	
	@SuppressWarnings("unchecked")
	public int compareTo(Object arg0) {
		ScoredItem<T> sItem = (ScoredItem<T>) arg0;
		if (sItem.score > score) {
			return 1;
		} else if (sItem.score < score){
			return -1;
		} else {
			return ((Comparable)obj).compareTo(sItem.obj);
			//return -1;
		}
	}
	
	public boolean equals (Object arg0) {
		ScoredItem<T> sItem = (ScoredItem<T>) arg0;
		return sItem.obj.equals(sItem.obj);
	}
	
	public String toString () {
		String ret = new String ();
		ret += "(";
		ret += obj;
		ret += " , ";
		ret += score;
		ret += ")";
		return ret;
	}
}
