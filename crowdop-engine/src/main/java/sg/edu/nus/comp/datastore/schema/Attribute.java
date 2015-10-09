package sg.edu.nus.comp.datastore.schema;


public class Attribute extends Variable {
	private String tableName;
	private String attributeName;
	
	public Attribute(String tableName, String attributeName, VariableType type) {
		super(type);
		this.tableName = tableName;
		this.attributeName = attributeName;
	}
	
//	public Attribute (String tableName, String attributeName) {
//		super(VariableType.UNDEFINED);
//		this.setTableName(tableName);
//		this.attributeName = attributeName;
//	}
	
	public Attribute (String attributeName) {
		super(VariableType.UNDEFINED);
		this.attributeName = attributeName;
		this.tableName = null;
	}
	
	public boolean hasTableName() {
		return (tableName != null && !tableName.equals(""));
	}
	
	public void setTableName(String tableName) {
		if (tableName == null) {
			this.tableName = "";
		} else {
			this.tableName = tableName;
		}
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String getAttributeName() {
		return attributeName;
	}
	
	public boolean equals (Attribute attribute) {
		return (tableName.equals(attribute.getTableName())) &&
				(attributeName.equals(attribute.getAttributeName()));
	}
	
	public int hashCode () {
		return new String (tableName + attributeName).hashCode();
	}
	
	public String toString() {
		if (tableName.trim().equals(""))
			return attributeName;
		else
			return tableName + "." + attributeName; 
	}
}
