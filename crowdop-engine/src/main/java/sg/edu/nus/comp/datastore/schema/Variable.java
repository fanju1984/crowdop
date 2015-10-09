package sg.edu.nus.comp.datastore.schema;

public abstract class Variable {
	protected VariableType variableType;
	
	public Variable (VariableType type) {
		this.variableType = type;
	}
	
	public VariableType getType () {
		return variableType;
	}
	
	public void setType (VariableType type) {
		this.variableType = type;
	}
}
