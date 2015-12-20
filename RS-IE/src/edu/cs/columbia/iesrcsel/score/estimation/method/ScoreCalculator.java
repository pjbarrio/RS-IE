package edu.cs.columbia.iesrcsel.score.estimation.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

/**
 * @param <T> Class of items that get scored (e.g., pairs of collection samples)
 * @param <S> Class of items that we need to initialize from (e.g., samples themselves)
 */
public abstract class ScoreCalculator<T,S> implements Parametrizable{

	private Map<T,Double> scoresMap = new HashMap<T, Double>();
	private Map<String,String> params;
	
	public ScoreCalculator(Map<String,String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("score.calculator", this.getClass().getSimpleName());
	}
	
	public double estimateScore(T source, QueryGenerator queryGenerator) {
		Double score = scoresMap.get(source);		
		if (score == null){
			score = _estimateScore(source, queryGenerator);
			scoresMap.put(source, score);
		}
		return score;
	}

	public abstract List<PairUnordered<Query, Double>> estimateQueryScores(T source, QueryGenerator queryGenerator);
	
	protected abstract Double _estimateScore(T source, QueryGenerator queryGenerator);

	public abstract void initialize(List<S> samples, QueryGenerator queryGenerator);

	public void reset(){
		scoresMap.clear();
		_reset();
	}

	protected abstract void _reset();

	@Override
	public Map<String, String> getParams() {
		return params;
	}
	
}
