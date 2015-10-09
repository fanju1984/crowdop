package sg.edu.nus.comp.crowdop.sql;

import java.util.*;

import sg.edu.nus.comp.datastore.schema.*;

public class SelectCommand extends Command {
	private Vector<Attribute> fields = new Vector<Attribute>();
	private Vector<CommandPredicate> predicates = new Vector<CommandPredicate>();
	private Vector<CommandPredicate> joins = new Vector<CommandPredicate>();
	private Vector<String> tables = new Vector<String>();

	public SelectCommand() {
		super(CommandType.SELECT);
	}
	
	public synchronized void addField(Attribute field) {
		fields.add(field);
	}
	
	public synchronized void addPredicate(CommandPredicate predicate) {
		predicates.add(predicate);
	}
	
	public synchronized void addJoin(CommandPredicate join) {
		joins.add(join);
	}
	
	public synchronized void addTable(String table) {
		tables.add(table);
	}
	
	public Attribute[] getFields() {
		return fields.toArray(new Attribute[]{});
	}
	
	public Predicate[] getPredicates() {
		Vector<Predicate> retVect = new Vector<Predicate>();
		
		for (CommandPredicate curr: predicates) {
			retVect.add(curr.getPredicate());
		}
		
		return retVect.toArray(new Predicate[]{});
	}
	
	public Predicate[] getJoins() {
		Vector<Predicate> retVect = new Vector<Predicate>();
		
		for (CommandPredicate curr: joins) {
			retVect.add(curr.getPredicate());
		}
		
		return retVect.toArray(new Predicate[]{});
	}
	
	public String[] getTables() {
		return tables.toArray(new String[]{});
	}
}
