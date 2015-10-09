package sg.edu.nus.comp.datastore.schema;

public enum VariableType {
	INT				("int"),
	STRING			("string"),
	DOUBLE			("double"),
	UNDEFINED		("undefined");
	
	private String id;
	
	private VariableType (String id) {
		this.id = id;
	}
	
	public String toString () {
		return this.id;
	}
	
	public static VariableType parse (String str) {
		if (str.equalsIgnoreCase("int")) return INT;
		else if (str.equalsIgnoreCase("string")) return STRING;
		else if (str.equalsIgnoreCase("double")) return DOUBLE;
		return UNDEFINED;
	}
}
