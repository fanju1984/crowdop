package sg.edu.nus.comp.datastore.schema;
import java.util.*;



/**
 * this class is designed to store the structure 
 * and fragmentation information of a table
 * @author FanJu
 *
 */
public class Table {
	private String name;//the table's name
	private Map<String, Field> fields; //all fields belonging to the table
	
	private int size;			//for how many records it contains
	
	
	public Table (String name) {
		this.name = name;
		fields = new Hashtable <String, Field> ();
	}
	
	Map<String, Map<String, Double>> domains;
	
	public void setFieldDomains (Map<String, Map<String, Double>> fieldDomains) {
		domains = fieldDomains;
	}
	
	
	
	public Map<String, Double> getFieldDomains (String attributeName) {
		return domains.get(attributeName);
	}
	
	public void setSize(int s) {
		size = s;
	}
	
	public int getSize() {
		return this.size;
	}
		
	
	public String getTableName () {
		return this.name;
	}
	
	public Field[] getFields() {
		Field allFields[] = new Field[fields.size()];
		int count = 0;
		for (String key : fields.keySet()) {
			allFields[count] = this.fields.get(key);
			count ++;
		}
		return allFields;
	}

	
	public void addField (Field field) throws Exception{
		String key = field.getName();
		if (!fields.containsKey(key)) {
			this.fields.put(key, field);
		} else {
			throw new Exception ("duplicated field name '" + key +"'");
		}
	}
	
	public boolean fieldExists (String name) {
		return fields.containsKey(name);
	}
	
	public int fieldNumber () {
		return fields.size();
	}
	
	public Field getField (String name) throws Exception {
		if (fields.containsKey(name)) {
			return fields.get(name);
		} else {
			throw new Exception ("the field's name '" + name +"' is not found");
		}
	}
	
	public Attribute[] getAttributes () {
		Attribute [] attributes = new Attribute[this.fields.size()];
		
		for (String key : fields.keySet()) {
			Field field = this.fields.get(key);
			attributes [field.getOrder()] = new Attribute (
											this.name,
											field.getName(),
											field.getType());
		}
		return attributes;
	}
	
	
	public Attribute getPrimaryKey () {
		for (String key : fields.keySet()) {
			Field field = this.fields.get(key);
			if (field.isKey()) {
				String tableName = this.name ;
				VariableType type = field.getType();
				String attributeName = field.getName();
				Attribute a = new Attribute (tableName, attributeName, type);
				return a;
			}
		}
		return null;
	}
	
	public String toString () {
		StringBuffer buffer = new StringBuffer ();
		buffer.append(this.name +" : ");
		
		for (String fieldName : fields.keySet()) {
			buffer.append(this.fields.get(fieldName) + " , ");
		}
		buffer.append("\n");
		
		return buffer.toString();
	}
}
