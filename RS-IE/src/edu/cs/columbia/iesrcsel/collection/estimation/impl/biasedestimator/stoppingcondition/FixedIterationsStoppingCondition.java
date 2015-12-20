package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.impl.Query;

public class FixedIterationsStoppingCondition extends StoppingCondition {

	private int maxIter;
	private int currentIter;
	private Set<Query> queries;
	public FixedIterationsStoppingCondition(Map<String,String> params,int maxIter){
		super(params);
		this.maxIter = maxIter;
		this.queries = new HashSet<Query>();
		reset();
	}
	
	@Override
	public boolean stopSamplingProcess() {
		return (currentIter > maxIter);
	}

	@Override
	public void informSampledQuery(Query query) {
		if (queries.add(query))
			currentIter++;
	}

	@Override
	public void informEstimate(double currentEstimate) {
		;
	}

	@Override
	public void reset() {
		this.queries.clear();
		this.currentIter = 0;
	}

}
