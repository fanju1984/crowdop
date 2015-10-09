package sg.edu.nus.comp.datastore.schema;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Predicate implements Comparable{
	private ComparisonOp operator;

	private Attribute left;

	private Variable right;

	/**
	 * the constructor for predicate at a attribute
	 * 
	 * @param lt: the name of left table
	 * @param la: the name of left attribute
	 * @param v:  the value
	 */
	public Predicate(Attribute left, ComparisonOp op, Variable right) {
		this.operator = op;
		this.left = left;
		this.right = right;
	}
	
	public Predicate () {}

	public void setLeft(Attribute left) {
		this.left = left;
	}

	public Attribute getLeft() {
		return left;
	}

	public void setRight(Variable right) {
		this.right = right;
	}

	public Variable getRight() {
		return right;
	}
	
	public void setComparisonOp (ComparisonOp op) {
		this.operator = op;
	}
	
	public ComparisonOp getComparisonOp ()
	{
		return this.operator;
	}
	
	public boolean hasSameLeft (Predicate predicate) {
		return this.left.equals(predicate.getLeft());
	}
	
	
	public Predicate[] intersect(Predicate predicate, boolean flag)
	{
		Value value1, value2;
		value1 = (Value)this.right;
		value2 = (Value)predicate.getRight();
		
		int comparison = value1.compareTo(value2); 
		if (value1.getType() == VariableType.STRING) {
			if (comparison != 0)
				return null;
			
			Predicate ret[] = new Predicate[1];
			ret[0] = this;
			return ret;
		} else if (value1.getType() == VariableType.INT ||
				value1.getType() == VariableType.DOUBLE) {
			
			ComparisonOp op = predicate.getComparisonOp();
			if (this.operator == ComparisonOp.EQUALS) {			//=
				/* equals is easy to check, just check that this predicate's
				 * values is in the range and return this predicate because it
				 * is in the intersection */
				if (op == ComparisonOp.EQUALS && comparison == 0) {
					return new Predicate[] { this };
				} else if (op == ComparisonOp.GREATER_THAN && comparison > 0) {
					return new Predicate[] { this };
				} else if (op == ComparisonOp.GREATER_THAN_OR_EQUAL && comparison >=0) {
					return new Predicate[] { this };
				}else if (op == ComparisonOp.LOWER_THAN && comparison < 0) {
					return new Predicate[] { this };
				} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL && comparison <=0) {
					return new Predicate[] {this };
				}else if (op == ComparisonOp.NOT_EQUAL && comparison != 0 ) {
					return new Predicate[] { this };
				}else {
					return null;
				}
			} else if (this.operator == ComparisonOp.GREATER_THAN) {			//>
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison < 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL ) {
					if (comparison < 0) {
						return new Predicate[] { predicate };
					} else {
						return new Predicate[] { this };
					}
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison <0 ) {
						return new Predicate[] {predicate,this};
					} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL && comparison == 0) {
						return new Predicate[] {new Predicate(left,ComparisonOp.EQUALS,right)};
					} else {
						return null;	//id>3; id<=2 
					}
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if (comparison < 0) {
						return new Predicate[] {predicate, this};
					} else {
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.GREATER_THAN_OR_EQUAL) {			//>=
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison <= 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL ) {
					if (comparison <= 0) {
						return new Predicate[] { predicate };
					} else {
						return new Predicate[] { this };
					}
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison <0 ) {
						return new Predicate[] {predicate,this};
					} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL && comparison == 0) {
						return new Predicate[] {new Predicate(left,ComparisonOp.EQUALS,right)};
					} else {
						return null;
					}
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if (comparison < 0) {
						return new Predicate[] {predicate, this};
					} else if (comparison == 0){
						return new Predicate[] {new Predicate(left, ComparisonOp.GREATER_THAN, right)};
					} else {
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.LOWER_THAN) {			// <
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison > 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison <= 0) {
						return new Predicate[] { this };
					} else {
						return new Predicate[] {  predicate };
					}
				} else if ((op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL)&& comparison > 0) {
					return new Predicate[] { this, predicate };
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if ( comparison >0) {
						return new Predicate[] { this, predicate }; 
					} else {
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.LOWER_THAN_OR_EQUAL) {			// <=
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison >= 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison < 0) {
						return new Predicate[] { this };
					} else {
						return new Predicate[] {  predicate };
					}
				} else if ((op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL)) {
					if (comparison > 0) {
						return new Predicate[] { this, predicate };
					} else if (op == ComparisonOp.GREATER_THAN_OR_EQUAL && comparison == 0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.EQUALS, right)};
					} else {
						return null;
					}
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if ( comparison >0) {
						return new Predicate[] { this, predicate }; 
					} else if (comparison == 0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.LOWER_THAN,right)};
					} else {		// <0
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.NOT_EQUAL) {		//<>
				if (op == ComparisonOp.EQUALS && comparison != 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.GREATER_THAN) {
					if (comparison <= 0) {
						return new Predicate[] {predicate};
					} else {
						return new Predicate[] { this, predicate }; 
					}
				} else if (op == ComparisonOp.GREATER_THAN_OR_EQUAL) {
					if (comparison >0){
						return new Predicate[] { this, predicate }; 
					} else if (comparison ==0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.GREATER_THAN, right)};
					} else {
						return new Predicate[] { predicate };
					}
				}else if (op == ComparisonOp.LOWER_THAN) {
					if (comparison < 0){
						return new Predicate[] { this, predicate }; 
					} else {
						return new Predicate[] { predicate };
					}
				} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL ) {
					if (comparison <0){
						return new Predicate[] { this, predicate }; 
					} else if (comparison ==0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.LOWER_THAN, right)};
					} else {
						return new Predicate[] { predicate };
					}
				}else if (op == ComparisonOp.NOT_EQUAL ) {
					if (comparison == 0) {
						return new Predicate[] { this };
					} else {
						return new Predicate[] { this, predicate }; 
					}
				}else {
					return null;
				}
			}else {
				return null;
			}
			
		} else {
			return null;
		}
	}
	public Predicate[] intersect(Predicate predicate){
		Value value1, value2;
		value1 = (Value)this.right;
		value2 = (Value)predicate.getRight();
		
		int comparison = value1.compareTo(value2); 
		if (value1.getType() == VariableType.STRING) {
			if (comparison != 0)
				return null;
			
			Predicate ret[] = new Predicate[1];
			ret[0] = this;
			return ret;
		} else if (value1.getType() == VariableType.INT ||
				value1.getType() == VariableType.DOUBLE) {
			
			ComparisonOp op = predicate.getComparisonOp();
			if (this.operator == ComparisonOp.EQUALS) {			//=
				/* equals is easy to check, just check that this predicate's
				 * values is in the range and return this predicate because it
				 * is in the intersection */
				if (op == ComparisonOp.EQUALS && comparison == 0) {
					return new Predicate[] { this };
				} else if (op == ComparisonOp.GREATER_THAN && comparison > 0) {
					return new Predicate[] { this };
				} else if (op == ComparisonOp.GREATER_THAN_OR_EQUAL && comparison >=0) {
					return new Predicate[] { this };
				}else if (op == ComparisonOp.LOWER_THAN && comparison < 0) {
					return new Predicate[] { this };
				} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL && comparison <=0) {
					return new Predicate[] {this };
				}else if (op == ComparisonOp.NOT_EQUAL && comparison != 0 ) {
					return new Predicate[] { this };
				}else {
					return null;
				}
			} else if (this.operator == ComparisonOp.GREATER_THAN) {			//>
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison < 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL ) {
					if (comparison < 0) {
						return new Predicate[] { predicate };
					} else {
						return new Predicate[] { this };
					}
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison <0 ) {
						return new Predicate[] {predicate,this};
					} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL && comparison == 0) {
						return new Predicate[] {new Predicate(left,ComparisonOp.EQUALS,right)};
					} else {
						return null;	//id>3; id<=2 
					}
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if (comparison < 0) {
						return new Predicate[] {predicate, this};
					} else {
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.GREATER_THAN_OR_EQUAL) {			//>=
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison <= 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL ) {
					if (comparison <= 0) {
						return new Predicate[] { predicate };
					} else {
						return new Predicate[] { this };
					}
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison <0 ) {
						return new Predicate[] {predicate,this};
					} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL && comparison == 0) {
						return new Predicate[] {new Predicate(left,ComparisonOp.EQUALS,right)};
					} else {
						return null;
					}
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if (comparison < 0) {
						return new Predicate[] {predicate, this};
					} else if (comparison == 0){
						return new Predicate[] {new Predicate(left, ComparisonOp.GREATER_THAN, right)};
					} else {
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.LOWER_THAN) {			// <
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison > 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison <= 0) {
						return new Predicate[] { this };
					} else {
						return new Predicate[] {  predicate };
					}
				} else if ((op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL)&& comparison > 0) {
					return new Predicate[] { this, predicate };
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if ( comparison >0) {
						return new Predicate[] { this, predicate }; 
					} else {
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.LOWER_THAN_OR_EQUAL) {			// <=
				/* with greater than we have to check which range applies */
				if (op == ComparisonOp.EQUALS && comparison >= 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.LOWER_THAN || op == ComparisonOp.LOWER_THAN_OR_EQUAL) {
					if (comparison < 0) {
						return new Predicate[] { this };
					} else {
						return new Predicate[] {  predicate };
					}
				} else if ((op == ComparisonOp.GREATER_THAN || op == ComparisonOp.GREATER_THAN_OR_EQUAL)) {
					if (comparison > 0) {
						return new Predicate[] { this, predicate };
					} else if (op == ComparisonOp.GREATER_THAN_OR_EQUAL && comparison == 0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.EQUALS, right)};
					} else {
						return null;
					}
				} else if (op == ComparisonOp.NOT_EQUAL) {
					if ( comparison >0) {
						return new Predicate[] { this, predicate }; 
					} else if (comparison == 0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.LOWER_THAN,right)};
					} else {		// <0
						return new Predicate[] {this};
					}
				} else {
					return null;
				}
			} else if (this.operator == ComparisonOp.NOT_EQUAL) {		//<>
				if (op == ComparisonOp.EQUALS && comparison != 0) {
					return new Predicate[] { predicate };
				} else if (op == ComparisonOp.GREATER_THAN) {
					if (comparison <= 0) {
						return new Predicate[] {predicate};
					} else {
						return new Predicate[] { this, predicate }; 
					}
				} else if (op == ComparisonOp.GREATER_THAN_OR_EQUAL) {
					if (comparison >0){
						return new Predicate[] { this, predicate }; 
					} else if (comparison ==0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.GREATER_THAN, right)};
					} else {
						return new Predicate[] { predicate };
					}
				}else if (op == ComparisonOp.LOWER_THAN) {
					if (comparison < 0){
						return new Predicate[] { this, predicate }; 
					} else {
						return new Predicate[] { predicate };
					}
				} else if (op == ComparisonOp.LOWER_THAN_OR_EQUAL ) {
					if (comparison <0){
						return new Predicate[] { this, predicate }; 
					} else if (comparison ==0) {
						return new Predicate[] {new Predicate(left, ComparisonOp.LOWER_THAN, right)};
					} else {
						return new Predicate[] { predicate };
					}
				}else if (op == ComparisonOp.NOT_EQUAL ) {
					if (comparison == 0) {
						return new Predicate[] { this };
					} else {
						return new Predicate[] { this, predicate }; 
					}
				}else {
					return null;
				}
			}else {
				return null;
			}
			
		} else {
			return null;
		}
	}
	
	/** 
	 * compare the given p with current object to see whether there's some value belongs to both the two predicate
	 * @param p
	 * @return
	 */
	public boolean contains(Predicate p) {
		if (!this.hasSameLeft(p)) {
			return false;		// no same left, no need to compare
		}
		Value thisValue = (Value) this.right;
		ComparisonOp op = this.getComparisonOp();
		ComparisonOp pOP = p.getComparisonOp();
		Value pRight = (Value)p.getRight();
		if ((op == ComparisonOp.GREATER_THAN) || (op == ComparisonOp.GREATER_THAN_OR_EQUAL)) {		//>, >=
			if ((op == ComparisonOp.GREATER_THAN) && (pOP == ComparisonOp.LOWER_THAN)) {	//one is > another is  <
				if (thisValue.compareTo(pRight)==0) return false;
				else return isInRange(pRight);
			}
			if ((pOP == ComparisonOp.GREATER_THAN) || (pOP == ComparisonOp.GREATER_THAN_OR_EQUAL)) {		//>, >=
				return true;
			} else {			//=, <, <=
				return isInRange(pRight);
			}
		} else if (op == ComparisonOp.EQUALS) {
			if ((pOP == ComparisonOp.GREATER_THAN) || (pOP == ComparisonOp.LOWER_THAN)) {
				if (thisValue.compareTo(pRight) == 0) return false;
			} else {
				return isInRange(pRight);
			}
		} else {		//this pre: <, <=
			if ((op == ComparisonOp.LOWER_THAN) && (pOP == ComparisonOp.GREATER_THAN)) {	//one is > another is  <
				if (thisValue.compareTo(pRight)==0) return false;
				else return isInRange(pRight);
			}
			if ((pOP == ComparisonOp.LOWER_THAN) || (pOP == ComparisonOp.LOWER_THAN_OR_EQUAL)) {		//>, >=
				return true;
			} else {			//=, >, >=
				return isInRange(pRight);
			}
		}
		return false;
	}
	
	public boolean isInRange(Value v) {
		Value value = (Value)this.right;
		if (!value.getType().equals(v.getType())) {
			return false;
		} else {
			int comparison = v.compareTo(value);		//2008.1.2 by songxm
			if (this.operator.equals(ComparisonOp.EQUALS)) {
				return (comparison == 0);
			} else if (this.operator.equals(ComparisonOp.GREATER_THAN)) {
				return (comparison == 1);
			} else if (this.operator.equals(ComparisonOp.LOWER_THAN)) {
				return (comparison == -1);
			} else if (this.operator.equals(ComparisonOp.LOWER_THAN_OR_EQUAL)) {
				return (comparison == -1 || comparison == 0);
			} else if (this.operator.equals(ComparisonOp.GREATER_THAN_OR_EQUAL)) {
				return (comparison == 1 || comparison == 0);
			} else if (this.operator.equals(ComparisonOp.NOT_EQUAL)) {
				return (comparison != 0);
			} else {
				return false;
			}
		}
	}
	
	public boolean isInRange(String tableName, String attName, String valueString) 
		throws ClassCastException {
		if (tableName.equals(this.left.getTableName()) &&
				attName.equals(this.left.getAttributeName())) {
			Value value = new Value(valueString, this.left.getType());
			return this.isInRange(value);
		} else {
			throw new ClassCastException ();
		}
		
	}
	
	public boolean equals (Predicate p) {
		return left.equals(p.left) &&
			operator.equals(p.operator) &&
			right.equals(p.right);
	}
	
	public String toString() {
		String op = operator.toString();
		if (this.right instanceof Value) {
			String added = new String("");
			if (left.getType().equals(VariableType.STRING)) {
				Value value = (Value)this.right;
				String valueString = String.valueOf(value.getValue());
				if (valueString.indexOf("'") == -1 &&
						valueString.indexOf("\"") == -1) {
					added = "\"";
				}
			}
			return left.toString().toLowerCase() + " " + op + " " + added+ right.toString()+ added;
		} else {
			return left.toString().toLowerCase() + " " + op + " " + right.toString();
		}
	}

	public int compareTo(Object arg0) {
		double rnd = Math.random();
		if (rnd <= 0.5) {
			return 1;
		} else {
			return -1;
		}
	}
	
/*
	public static Predicate parseStringType (String predStr) {
		Pattern strpredRegex = Pattern.compile(CommandParser.comparisonRegex);
		Matcher m = strpredRegex.matcher(predStr);
		if (m.matches()) {
			String tableAttr = m.group(1);
			String parts[] = tableAttr.split("\\.");
			Attribute attr = new Attribute(parts[0], parts[1],
					VariableType.STRING);
			String opStr = m.group(3);
			ComparisonOp op = ComparisonOp.parseComparison(opStr);
			String vstr = m.group(4);
			
			Predicate pred = null;
			if (vstr.matches(".+\\..+")) {
				parts = vstr.split("\\.");
				Attribute rightAttr = new Attribute(parts[0], parts[1],
						VariableType.STRING);
				pred = new Predicate (attr, op, rightAttr);
			} else {
				vstr = vstr.replace('\"', ' ').trim();
				Value value = new Value(vstr, VariableType.STRING);
				pred = new Predicate(attr, op, value);
			}
			return pred;
		} 
		return null;
	}
	*/
	
	public static String printPredicates(List<Predicate> predicates) {
		String predStr = "";
		for (int i = 0; i < predicates.size(); i ++) {
			predStr += predicates.get(i);
			if (i < predicates.size() - 1) {
				predStr += " AND ";
			}
		}
		return predStr;
	}
}
