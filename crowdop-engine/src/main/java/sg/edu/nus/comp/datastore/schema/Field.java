package sg.edu.nus.comp.datastore.schema;



public class Field {
	private String name;
	private VariableType type;
	private boolean isKey;
	private int order;
	
	public Field (String name, VariableType type, boolean isKey) {
		this.type = type;
		this.name = name;
		this.isKey = isKey;
		this.order = 0;
	}
	
	public String getName () {
		return this.name;
	}
	
	public boolean isKey () {
		return isKey;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public int getOrder() {
		return order;
	}
	
	public VariableType getType () {
		return this.type;
	}
	
	public boolean equals (Field field) {
		return (this.name.equals(field.name));
	}
	public String toString () {
		return "(" + this.name + "," + 
			type.toString()+ "," + isKey + ")";
	}
}
