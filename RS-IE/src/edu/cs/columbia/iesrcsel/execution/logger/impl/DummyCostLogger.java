package edu.cs.columbia.iesrcsel.execution.logger.impl;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.CollisionCounter;
import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.execution.logger.LogSaver;
import edu.cs.columbia.iesrcsel.model.CostAware;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class DummyCostLogger extends CostLogger {

	private static UsefulDocumentCountEstimator estimator = new UsefulDocumentCountEstimator(new HashMap<String, String>()){

		@Override
		public double getNumberOfUsefulDocuments(TextCollection collection,
				InformationExtractionSystem ie,  CollisionCounter collisionCounter, CostLogger cl) {
			return 0;
		}

		@Override
		public double getCurrentNumberOfUsefulDocuments() {
			return 0;
		}

		@Override
		public void reset() {
			;
		}
		
	};
	
	private static LogSaver saver = new LogSaver() {
		
		@Override
		public void log(PrintStream out, String status, Map<String, String> estimatorParams,
				Map<String, CostAware> costAwares,
				Map<String, Map<String, String>> costAwaresParams, double estimation) {
			;
		}
		
		@Override
		public void log( String status,Map<String, String> estimatorParams,
				Map<String, CostAware> costAwares,
				Map<String, Map<String, String>> costAwaresParams, double estimation) {
			;
		}

		@Override
		public void log( String status,Map<String, String> estimatorParams,
				Map<String, CostAware> costAwares,
				Map<String, Map<String, String>> costAwaresParams,
				double estimation, boolean forced) {
			;
			
		}

		@Override
		public void log(PrintStream out, String status, Map<String, String> estimatorParams,
				Map<String, CostAware> costAwares,
				Map<String, Map<String, String>> costAwaresParams,
				double estimation, boolean forced) {
			;
			
		}

		@Override
		public void dump() {
			;
			
		}
		
		@Override
		public void dump(PrintStream out) {
			;
			
		}
	};
	
	public DummyCostLogger() {
		super(estimator, saver);
	}

}
