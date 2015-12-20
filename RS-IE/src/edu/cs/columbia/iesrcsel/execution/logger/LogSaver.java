package edu.cs.columbia.iesrcsel.execution.logger;

import java.io.PrintStream;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.CostAware;

public abstract class LogSaver {

	public void log(String status, Map<String,String> estimatorParams, Map<String, CostAware> costAwares, Map<String,Map<String,String>> costAwaresParams, double estimation){
		log(status, estimatorParams, costAwares, costAwaresParams, estimation, false);
	}
	
	public abstract void log(String status, Map<String,String> estimatorParams, Map<String, CostAware> costAwares, Map<String,Map<String,String>> costAwaresParams, double estimation, boolean forced);

	public void log(PrintStream out, String status, Map<String, String> estimatorParams, Map<String, CostAware> costAwares, Map<String, Map<String, String>> costAwaresParams, double estimation){
		log(out, status, estimatorParams, costAwares, costAwaresParams, estimation,false);
	}
	
	public abstract void log(PrintStream out, String status, Map<String, String> estimatorParams, Map<String, CostAware> costAwares, Map<String, Map<String, String>> costAwaresParams, double estimation, boolean forced);

	public abstract void dump();

	public abstract void dump(PrintStream out);
	
}
