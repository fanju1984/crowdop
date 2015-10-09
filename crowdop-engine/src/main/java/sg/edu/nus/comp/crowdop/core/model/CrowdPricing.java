package sg.edu.nus.comp.crowdop.core.model;

import java.util.*;

import sg.edu.nus.comp.datastore.schema.*;

/**
 * A generic model for pricing strategy in crowdsourcing environment
 * @author Ju Fan
 *
 */
public interface CrowdPricing {
	/**
	 * The unit price of a CSELECT operation
	 * @param tableName: the underlying data table of the CSELECT
	 * @param predicates: the predicates over the data table
	 * @return
	 */
	public abstract double unitCSelectCost (String tableName, List<Predicate> predicates);
	
	/**
	 * The unit price of a CJOIN operation
	 * @param leftTable: the left table
	 * @param rightTable: the right table
	 * @param predicates: the join predicates
	 * @return
	 */
	public abstract double unitCJoinCost (String leftTable, String rightTable, 
			List<Predicate> predicates);
	
	/**
	 * The unit price of CFill operation
	 * @param tableName
	 * @param attributeName
	 * @param attributeDomain
	 * @return
	 */
	public abstract double unitCFillCost(String tableName, 
			String attributeName, Set<String> attributeDomain);
}
