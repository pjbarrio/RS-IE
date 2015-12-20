package edu.cs.columbia.iesrcsel.sample.generator;

import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.CostAware;
import edu.cs.columbia.iesrcsel.model.CostAwareTextCollection;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public abstract class SampleGenerator implements Parametrizable{

	private Map<TextCollection, Sample> map = new HashMap<TextCollection,Sample>();
	private Map<TextCollection,Map<String,CostAware>> costs = new HashMap<TextCollection,Map<String,CostAware>>();
	private Map<String, String> params;
	
	public SampleGenerator(Map<String,String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("sample.generator", this.getClass().getSimpleName());
	}
	
	public Sample generateSample(TextCollection textCollection){
		Sample sample = map.get(textCollection);
		
		if (sample == null){
			
			if (textCollection instanceof CostAwareTextCollection){ //XXX if collection is not cost aware but ie is, we miss it...
				((CostAwareTextCollection)textCollection).getCostLogger().startIndividualLogging("sample.generation");
			}
			
			sample = _generateSample(textCollection);
			map.put(textCollection, sample);
		
			if (textCollection instanceof CostAwareTextCollection){
				costs.put(textCollection,((CostAwareTextCollection)textCollection).getCostLogger().stopIndividualLogging("sample.generation"));
			}
			
			return sample;
			
		}
		
		if (textCollection instanceof CostAwareTextCollection){
			
			((CostAwareTextCollection)textCollection).getCostLogger().addCachedCosts(costs.get(textCollection));
			sample.setCollection(textCollection);
			
		}
		
		return sample;
	}

	protected abstract Sample _generateSample(TextCollection textCollection);

	public Map<String,String> getParams(){
		return params;
	}
	
}
