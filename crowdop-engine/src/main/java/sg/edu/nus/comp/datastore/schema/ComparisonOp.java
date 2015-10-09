package sg.edu.nus.comp.datastore.schema;

public enum ComparisonOp {
	EQUALS					("="),
	LOWER_THAN				("<"),
	LOWER_THAN_OR_EQUAL		("<="),
	GREATER_THAN			(">"),
	GREATER_THAN_OR_EQUAL	(">="),
	NOT_EQUAL				("<>"),
	LOGIC_OR				("||"),
	LOGIC_AND				("&&");
	private String id;
	
	private ComparisonOp(String id) {
		this.id = id;
	}
	
	public String toString() {
		return id;
	}
	
	public static ComparisonOp parseComparison(String id) {
		for (ComparisonOp co: ComparisonOp.values()) {
			if (id.equals(co.id))
				return co;
		}
		
		throw new RuntimeException("Invalid comparison operator identifier '" + id + "'");
	}
}