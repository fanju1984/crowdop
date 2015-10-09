package sg.edu.nus.comp.util;

public class Pair<S> {
	S mFormmer;
	S mLatter;
	
	public Pair (S formmer, S latter) {
		mFormmer = formmer;
		mLatter = latter;
	}
	
	public String toString () {
		return "<" + mFormmer + "," + mLatter + ">";
	}
	
	public S getFormmer () {
		return mFormmer;
	}
	
	public S getLatter () {
		return mLatter;
	}
	
}
