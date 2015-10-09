package sg.edu.nus.comp.crowdagg;

import java.util.*;

import sg.edu.nus.comp.annotation.Annotation;


public class MajorityVotingAggregator {
	public static Map<Long, Annotation> aggregate (
			Map<Long, Map<String, Annotation>> task2worker2answer) {
		Map<Long, Annotation> task2result = 
				new HashMap<Long, Annotation>();
		for (Long task : task2worker2answer.keySet()) {
			Map<String, Annotation> worker2answer = 
					task2worker2answer.get(task);
			Map<String, Integer> answer2count = 
					new HashMap<String, Integer> ();
			for (String worker : worker2answer.keySet()) {
				Annotation anno = worker2answer.get(worker);
				for (String answer : anno.selections) {
					Integer count = answer2count.get(answer);
					if (count == null) {
						count = 0;
					}
					count ++;
					answer2count.put(answer, count);
				}
			}
			List<String> aggSels = new ArrayList<String>();
			for (String answer : answer2count.keySet()) {
				int count = answer2count.get(answer);
				if (count >= worker2answer.size() / 2) {
					aggSels.add(answer);
				}
			}
			Annotation aggAnno = new Annotation (null, false, aggSels);
			task2result.put(task, aggAnno);
			
		}
		return task2result;
	}
}
