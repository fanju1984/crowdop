package sg.edu.nus.comp.crowdop.sql;

import java.util.regex.*;
import java.util.*;

import sg.edu.nus.comp.datastore.DataTable;
import sg.edu.nus.comp.datastore.schema.*;

public class CommandParser {
	/* Regexs for database and table names */
	private static final String dbNameRegex = "[a-zA-Z]\\w+";
	private static final String tableNameRegex = "[a-zA-Z]\\w+";
	/* Field name can be in form table.field or field, both are supported */
	private static final String fieldNameRegex = "((" + tableNameRegex + "\\.)?\\w+)";
	/* Supported values are 'string' and int */
	private static final String valueRegex = "('[-\\w]+'|\"[-\\w]+\"|\\d+)";
	/* Supported operations for comparisons */
	private static final String operatorRegex = "(=|<|<=|>|>=|<>)";
	/* Comparison between fields or a field and a value */
	public static final String comparisonRegex =
		fieldNameRegex  + "\\s+" + operatorRegex + "\\s+(" + valueRegex + "|" + fieldNameRegex + ")";
	/* Supported boolean expressions between conditions */
	private static final String booleanOpRegex = "(and)";
	/* Supported conditions, currently only a list of comparisons with ANDs */
	private static final String conditionRegex =
		comparisonRegex + "(\\s+" + booleanOpRegex + "\\s+" + comparisonRegex + ")*";
	
	/* Table of all supported commands in the program */
	private static final String[] commands = new String[] {
		"selectdb\\s+" + dbNameRegex,
		/*
		 * Possible valid inputs checked:
		 * 
		 * SELECT * FROM table;
		 * SELECT field FROM table;
		 * SELECT field1, field2 FROM table;
		 * SELECT * FROM table WHERE field1 = field1;
		 * SELECT * FROM table WHERE field1 = value1 AND field2 = field3;
		 * 
		 * There can be whitespaces before and/or after comma or equality sign, but
		 * they are not required.
		 */
		"select\\s+(\\*|" +
		fieldNameRegex + "(\\s*,\\s+(\\*|(" + fieldNameRegex + ")))*)\\s+" +
		"from\\s+" + tableNameRegex  + "(\\s*,\\s+" + tableNameRegex + ")*" +
		"(\\s+where\\s+" + conditionRegex  + ")?",
	};
	
	/* The dynamic variables of this object */
	private Pattern commandPattern;
	private int index;
	private String error;
	
	public CommandParser() {
		StringBuffer regex = new StringBuffer("(");

		/* Concatenate all the used commands into a single regex */
		for (String c: commands)
			regex.append("(" + c + ")|");

		/* Replace useless '|' character with correct ending */
		regex.replace(regex.length()-1, regex.length(), ")\\s*");

		/* Compile the pattern for more effective handling */
		commandPattern = Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
	}
	
	public boolean validate(String str) {
		return commandPattern.matcher(str).matches();
	}
	
	public Command parse(String str, List<DataTable> datatables) {
		index = 0;

		String[] table = str.split("\\s+");
		
		try {
			if (table[0].equals("selectdb")) {
					index = skipString(str, "selectdb", index);
					if (!table[1].matches(dbNameRegex)) {
						error = "Database name invalid";
						return null;
					}
					index = skipString(str, table[1], index);
					if (table.length != 2) {
						error = "Too many arguments for 'selectdb' command";
						return null;
					}
					
					return new SelectDatabaseCommand(table[1]);
			} else if (table[0].equals("select")) {
				return parseSelectStatement(str, table, datatables);
			} else {
				error = "Unknown command";
				return null;
			}
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			// Unexpected end of command
			error = "Unexpected end of command string";
			return null;
		}
	}
		
	private Command parseSelectStatement(String str, String[] table, 
			List<DataTable> datatables) {
		int i;
		Vector<String> fields, tables;
		int comparisons;

		index = skipString(str, "select", index);
		i = 1;
		fields = new Vector<String>();
		tables = new Vector<String>();
		while (i<table.length) {
			int tmpidx;
			
			if (!table[i].matches("(\\*|" + fieldNameRegex + "),?")) {
				error = "Field name invalid";
				return null;
			}
			tmpidx = skipString(str, table[i++], index);
			
			/* check if we still have more fields separated with comma */
			if (table[i-1].charAt(table[i-1].length()-1) == ',') {
				/* add field to the selected fields */
				fields.add(table[i-1].substring(0, table[i-1].length()-1));
			} else if (table.length > i && table[i].charAt(0) == ',') {
				/* add field to the selected fields */
				fields.add(table[i-1]);
				tmpidx = skipString(str, table[i++], tmpidx);
			} else {
				/* add field to the selected fields */
				fields.add(table[i-1]);
				index = tmpidx;
				break;
			}

			index = tmpidx;
		}
		
		if (!table[i].matches("from")) {
			error = "The 'from' statement of 'select' expected but not found";
			return null;
		}
		index = skipString(str, table[i++], index);
		
		while (i<table.length) {
			int tmpidx;
			
			if (!table[i].matches(tableNameRegex + ",?")) {
				error = "Table name invalid";
				return null;
			}
			tmpidx = skipString(str, table[i++], index);
			
			/* check if we still have more fields separated with comma */
			if (table[i-1].charAt(table[i-1].length()-1) == ',') {
				/* add table to the selected tables */
				tables.add(table[i-1].substring(0, table[i-1].length()-1));
			} else if (table.length > i && table[i].charAt(0) == ',') {
				/* add table to the selected tables */
				tables.add(table[i-1]);
				index = skipString(str, table[i++], index);;
			} else {
				/* add table to the selected tables */
				tables.add(table[i-1]);
				index = tmpidx;
				break;
			}
			
			index = tmpidx;
		}
		
		SelectCommand ret = new SelectCommand();
		for (String f: fields) {
			int tmpIdx = f.indexOf('.');
			Attribute attr;
			
			if (tmpIdx >= 0) {
				String tableName = f.substring(0, tmpIdx);
				String attrName = f.substring(tmpIdx+1);
				VariableType attrType = getAttrType (tableName, attrName, datatables);
				attr = new Attribute(tableName, attrName, attrType);
			} else {
				attr = new Attribute(null, f, VariableType.UNDEFINED);
			}
			
			ret.addField(attr);
		}
		for (String t: tables) {
			ret.addTable(t);
		}
		
		if (i == table.length) {
			/* no where statement at all, this is ok */
			return ret;
		}
		
		if (!table[i].matches("where")) {
			error = "The 'where' statement of 'select' expected but not found";
			return null;
		}
		index = skipString(str, table[i++], index);
		comparisons = parseConditionStatement(str, table, i, tables.toArray(new String[] {}));
		if (comparisons < 0) {
			return null;
		}
		
		if (i + comparisons*3 + comparisons-1 != table.length) {
			error = "Invalid number of parameters to where clause";
			return null;
		}
		
		for (int j=0; j<comparisons; j++) {
			int skip = i + j*4;
			int tmp;
			
			Attribute attribute;
			tmp = table[skip].indexOf('.');
			if (tmp >= 0) {
				String tableName = table[skip].substring(0, tmp);
				String attrName = table[skip].substring(tmp+1);
				VariableType attrType = getAttrType (tableName, attrName, datatables);
				attribute = new Attribute(tableName, attrName, attrType); 
			} else {
				attribute = new Attribute(null, table[skip], VariableType.UNDEFINED);
			}
			skip++;
			
			ComparisonOp comparisonOp = ComparisonOp.parseComparison(table[skip++]);
			
			if (table[skip].matches(valueRegex)) {
				/* FIXME: value type should be parsed? */
				String right = table[skip];
				VariableType type = null;
				if (right.indexOf("\'")>=0 || right.indexOf("\"")>=0) {
					type = VariableType.STRING;
				} else if (right.indexOf(".")>=0){
					type = VariableType.DOUBLE;
				} else {
					type = VariableType.INT;
				}
				Value value = new Value(right, type);
				
				CommandPredicate predicate = new CommandPredicate(attribute, comparisonOp, value);
				ret.addPredicate(predicate);
			} else if (table[skip].matches(fieldNameRegex)) {
				Attribute attr;
				tmp = table[skip].indexOf('.');
				if (tmp >= 0) {
					String tableName = table[skip].substring(0, tmp);
					String attrName = table[skip].substring(tmp+1);
					VariableType attrType = getAttrType (tableName, attrName, datatables);
					attr = new Attribute(tableName, attrName, attrType);
				} else {
					attr = new Attribute(null, table[skip], VariableType.UNDEFINED);
				}
				
				CommandPredicate predicate = new CommandPredicate(attribute, comparisonOp, attr);
				ret.addJoin(predicate);
			} else {
				error = "Predicate right value unknown type";
				return null;
			}
			
			skip++;
		}
		
		
		return ret;
	}
	

	
	private VariableType getAttrType(String tableName, String attrName,
			List<DataTable> datatables) {
		DataTable datatable = null;
		for (DataTable dt : datatables) {
			if (dt.getName().equals(tableName)) {
				datatable = dt;
				break;
			}
		}
		if (datatable == null) return VariableType.UNDEFINED;
		List<Field> fields = datatable.getFields();
		Field field = null;
		for (Field f : fields) {
			if (f.getName().equals(attrName)) {
				field = f;
				break;
			}
		}
		if (field == null) return VariableType.UNDEFINED;
		return field.getType();
	}

	private int parseConditionStatement(String str, String[] table, int tableIdx, String[] dbTables) {
		int i = tableIdx;
		int ret = 0;
		
		while (i < table.length) {
			/* don't check the operator on first run */
			if (i != tableIdx) {
				if (!table[i].matches(booleanOpRegex)) {
					error = "Condition boolean operator invalid";
					return -1;
				}
				index = skipString(str, table[i++], index);
			}
			
			if (!table[i].matches(fieldNameRegex)) {
				error = "Condition left parameter syntax invalid";
				return -1;
			}
			
			index = skipString(str, table[i++], index);
			
			if (!table[i].matches(operatorRegex)) {
				error = "Condition operator invalid";
				return -1;
			}
			index = skipString(str, table[i++], index);
			
			if (!table[i].matches("(" + fieldNameRegex + "|" + valueRegex + ")")) {
				error = "Condition right parameter invalid";
				return -1;
			}
			
			index = skipString(str, table[i++], index);
			
			ret++;
		}	
		
		return ret;
	}
	
	public String getError() {
		return error;
	}
	
	public int getErrorIndex() {
		return index;
	}
	
	private int skipString(String str, String word, int index) {
		int ret;
		
		ret = str.indexOf(word, index) + word.length();
		while (ret < str.length()) {
			if (Character.isWhitespace(str.charAt(ret))) {
				ret++;
				continue;
			}

			break;
		}
		
		return ret;
	}
}
