package edu.cs.columbia.iesrcsel.execution.logger;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.model.CostAware;
import edu.cs.columbia.iesrcsel.model.CostAwareTextCollection;
import edu.cs.columbia.iesrcsel.model.extractor.CostAwareInformationExtractionSystem;

public class CostLogger {

	private UsefulDocumentCountEstimator estimator;
	private LogSaver saver;
	private Map<String,CostAware> costAwares;
	private Map<String,Map<String,String>> costAwaresParams;
	private Map<String, String> estimatorParams;
	private String currentStatus;

	public CostLogger(UsefulDocumentCountEstimator estimator, LogSaver saver){
		this.estimator = estimator;
		this.estimatorParams = estimator.getParams();
		this.saver = saver;
		this.costAwares = new HashMap<String,CostAware>();
		this.costAwaresParams = new HashMap<String,Map<String,String>>();
		this.currentStatus = "estimation";
	}

	public void register(CostAware costAware){
		costAwares.put(costAware.getName(),costAware);
		costAwaresParams.put(costAware.getName(),costAware.getParams());
	}

	public void setCurrentStatus(String status){
		this.currentStatus = status;
	}
	
	public void log(){

		saver.log(currentStatus, estimatorParams, costAwares, costAwaresParams, estimator.getCurrentNumberOfUsefulDocuments(),false);

	}

	public void log(PrintStream out) {
		saver.log(out, currentStatus, estimatorParams,costAwares,costAwaresParams, estimator.getCurrentNumberOfUsefulDocuments(),true);		
	}

	public void startIndividualLogging(String id) {
		for (Entry<String,CostAware> entry : costAwares.entrySet()) {
			entry.getValue().startIndividualLogging(id);
		}		
	}

	public Map<String, CostAware> stopIndividualLogging(String id) {

		Map<String, CostAware> ret = new HashMap<String, CostAware>();

		for (Entry<String,CostAware> entry : costAwares.entrySet()) {
			ret.put(entry.getKey(), entry.getValue().stopIndividualLogging(id));
		}

		return ret;

	}

	public void addCachedCosts(Map<String, CostAware> map) {
		for (Entry<String, CostAware> entry : map.entrySet()) {

			CostAware ca = costAwares.get(entry.getKey());

			if (ca != null)
				ca.addCost(entry.getValue());

		}
	}

	public void dump() {
		saver.dump();
	}

	public void dump(PrintStream out) {
		saver.dump(out);
	}

}
