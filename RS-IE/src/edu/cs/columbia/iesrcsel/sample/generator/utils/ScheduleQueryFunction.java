package edu.cs.columbia.iesrcsel.sample.generator.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;

public class ScheduleQueryFunction {

	private Map<Query,Double> map = new HashMap<Query,Double>();
	private boolean balance;
	
	public ScheduleQueryFunction(boolean balance){
		this.balance = balance;
	}
	
	public void update(Query query, int numberOfTuples) {
		Double freq = map.get(query);
		
		if (freq == null){
			freq = 0d;
		}
		if (numberOfTuples > 0) {
			// TODO Is there a reason for not using the actual number?
			freq++;
		}
		map.put(query, freq);
	}

	public void reSchedule(List<Query> initialQueries) {
		Collections.sort(initialQueries, new MapBasedComparator<Query>(map, !balance));
	}

}
