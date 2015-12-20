package edu.cs.columbia.iesrcsel.execution.logger.impl;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.cs.columbia.iesrcsel.execution.logger.LogSaver;
import edu.cs.columbia.iesrcsel.model.CostAware;

public class PrintStreamLogger extends LogSaver {

	private PrintStream outD;
	private Set<StringBuilder> headers;
	private boolean changeOnly = true;
	private double currentEstimate;
	private String currentStatus;
	private Map<PrintStream, StringBuilder> tempString;
	
	public PrintStreamLogger(PrintStream out) {
		this(out,true);
	}

	public PrintStreamLogger(PrintStream out, boolean changeOnly) {
		this.outD = out;
		headers = new HashSet<StringBuilder>();
		this.currentEstimate = Double.MIN_VALUE;
		this.currentStatus = "Started";
		this.changeOnly = changeOnly;
		tempString = new HashMap<PrintStream, StringBuilder>();
	}
	
	@Override
	public void log( String status,Map<String,String> estimatorParams, Map<String, CostAware> costAwares, Map<String,Map<String,String>> costAwaresParams, double estimation, boolean forced) {
		
		log(outD,status, estimatorParams,costAwares,costAwaresParams, estimation, forced);
				
	}

	@Override
	public void log(PrintStream outStream, String status, Map<String, String> estimatorParams,
			Map<String, CostAware> costAwares,
			Map<String, Map<String, String>> costAwaresParams, double estimation, boolean forced) {
	
		StringBuilder out = getStringBuilder(outStream);
		
		if (!forced){
		
			if (currentStatus.equals(status) && (Double.isNaN(estimation) || (changeOnly && currentEstimate == estimation)))
				return;
			
		}

		currentEstimate = estimation;
		currentStatus = status;
		
		if (!headers.contains(out)){
			
			headers.add(out);
			
			out.append("\n status, ");
			
			for (Entry<String,String> estimator : estimatorParams.entrySet()) {
				out.append("estimator." + estimator.getKey() + ",");
			}
			
			for (Entry<String,CostAware> cost_aware : costAwares.entrySet()) {
				
				for (Entry<String,String> entry_param : cost_aware.getValue().getParams().entrySet()) {
					out.append(cost_aware.getKey() + "." + entry_param.getKey() + ",");
				}
				
				for (Entry<String,Integer> entry_cost : cost_aware.getValue().getCost().entrySet()) {
					out.append(entry_cost.getKey() + ",");
				}
				
			}
			
			out.append("estimated.number.of.useful.documents");
		}
		
		out.append("\n" + status + ",");
		
		for (Entry<String,String> estimator : estimatorParams.entrySet()) {
			out.append(estimator.getValue() + ",");
		}

		
		for (Entry<String,CostAware> cost_aware : costAwares.entrySet()) {
			
			for (Entry<String,String> entry_param : cost_aware.getValue().getParams().entrySet()) {
				out.append(entry_param.getValue() + ",");
			}
			
			for (Entry<String,Integer> entry_cost : cost_aware.getValue().getCost().entrySet()) {
				out.append(entry_cost.getValue() + ",");
			}
			
			
		}

		out.append(estimation);
		
		out.append("\n");
		
	}

	private StringBuilder getStringBuilder(PrintStream outStream) {
		
		StringBuilder sb = tempString.get(outStream);
		
		if (sb == null){
			sb = new StringBuilder();
			tempString.put(outStream, sb);
		}
		
		return sb;
		
	}

	@Override
	public void dump() {
		dump(outD);
	}

	public void dump(PrintStream out) {
		StringBuilder sb = getStringBuilder(out);
		out.print(sb.toString());
		clearStringBuilder(out);
	}

	private void clearStringBuilder(PrintStream out) {
		tempString.remove(out);		
	}

}
