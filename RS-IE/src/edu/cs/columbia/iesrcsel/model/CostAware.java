package edu.cs.columbia.iesrcsel.model;

import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;

public interface CostAware extends Parametrizable{

	public Map<String, Integer> getCost();
	
	public void clearCost();

	public Map<String, String> getParams();
	
	public void addCost(CostAware cost);

	public String getName();
	
	public CostLogger getCostLogger();

	public void startIndividualLogging(String id);

	public CostAware stopIndividualLogging(String id);

}
