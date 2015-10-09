package sg.edu.nus.comp.datastore.schema;


public class Value extends Variable implements Comparable<Object> {
	@SuppressWarnings("rawtypes")
	private Comparable value;
	
	public Value (String value, VariableType type) {
		super(type);
		if (super.variableType == VariableType.INT) {
			try {
				this.value = new Integer(Integer.parseInt(value));
			} catch (NumberFormatException e) {
				System.out.println("Invalid Integer format '" + value + "'");
			}
		} else if (super.variableType == VariableType.STRING) {
			this.value = value;
		} else {
			try {
				this.value = new Double (Double.parseDouble(value));
			} catch (NumberFormatException e) {
				System.out.println ("Invalid Double format '" + value +"'");
			}
		}
	}
	
	public Object getValue () {
		return value;
	}
	
	public int compareTo (Object v) {
		Value value = (Value)v;
		return this.value.compareTo(value.getValue());
	}
	
	public boolean equals (Value v) {
		return (this.value.equals(v.value)) &&
			super.getType().equals(v.getType());
	}
	
	public String toString () {
		return value.toString();
	}
}
