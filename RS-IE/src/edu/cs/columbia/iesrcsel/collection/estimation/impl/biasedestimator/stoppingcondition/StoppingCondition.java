package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition;

import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.impl.Query;

public abstract class StoppingCondition implements Parametrizable{

	private Map<String, String> params;

	public StoppingCondition(Map<String, String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("stopping.condition", this.getClass().getSimpleName());
	}
	
	public abstract boolean stopSamplingProcess();

	public abstract void informSampledQuery(Query query);

	public abstract void informEstimate(double currentEstimate);

	public abstract void reset();

	public Map<String,String> getParams(){
		return params;
	}
	
}
