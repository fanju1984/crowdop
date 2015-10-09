package sg.edu.nus.comp.crowdop.core.model;

import java.util.List;
import java.util.Set;

import sg.edu.nus.comp.datastore.schema.Predicate;

public class LinearCrowdPricing implements CrowdPricing {
	double CSELECT_BASE;
	double CSELECT_INC;
	
	double CJOIN_BASE;
	double CJOIN_INC;
	
	double CFILL_BASE;
	double CFILL_INC;
	
	public LinearCrowdPricing (double cselectBase, double cselectInc, 
			double cjoinBase, double cjoinInc, 
			double cfillBase, double cfillInc) {
		CSELECT_BASE = cselectBase;
		CSELECT_INC = cselectInc;
		CJOIN_BASE = cjoinBase;
		CJOIN_INC = cjoinInc;
		CFILL_BASE = cfillBase;
		CFILL_INC = cfillInc;
	}
	
	public double unitCSelectCost (String tableName, 
			List<Predicate> predicates) {
		double price = CSELECT_BASE + 
				CSELECT_INC * predicates.size();
		return price; // example: 0.005 + 0.005 * predicates.size();
	}
	
	public double unitCJoinCost (String leftTable, String rightTable, 
			List<Predicate> predicates) {
		if (predicates == null) {
			return CJOIN_BASE;
		}
		double price = CJOIN_BASE + CJOIN_INC * predicates.size();
		return price; // example: 0.015
	}
	
	public double unitCFillCost(String tableName, 
			String attributeName, Set<String> attributeDomain) {
		if (attributeDomain == null) {
			return CFILL_BASE;
		}
		double price = CFILL_BASE + CFILL_INC * attributeDomain.size();
		return price;//Example: 0.01 + 0.002 * attributeDomain.size();
	}
}
