package sg.edu.nus.comp.crowdop.sql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sg.edu.nus.comp.datastore.schema.*;

public class CommandPredicate {
	private Predicate predicate;
	
	public CommandPredicate() {
		predicate = null;
	}
	
	Pattern regex = Pattern.compile("^['\\\"](.+)['\\\"]$");
	public CommandPredicate(Attribute left, ComparisonOp op, Variable right) {
		if (right instanceof Value) {
			String vstr = ((Value) right).getValue() + "";
			if (left.getType() == VariableType.STRING) {
				Matcher m = regex.matcher(vstr);
				if (m.matches()) {
					vstr = m.group(1);
					right = new Value (vstr, left.getType());
				}
			}
		}
		predicate = new Predicate ();
		predicate.setLeft(left);
		predicate.setRight(right);
		predicate.setComparisonOp(op);
		
	}
	
	public CommandPredicate(Predicate predicate) {
		this.predicate = predicate;
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate (Predicate p) {
		this.predicate = p;
	}
}
