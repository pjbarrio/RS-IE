package edu.cs.columbia.iesrcsel.execution.logger;

import java.io.PrintStream;
import java.util.Map.Entry;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.execution.logger.impl.PrintStreamCollisionLogger;
import edu.cs.columbia.iesrcsel.execution.logger.impl.PrintStreamLogger;
import edu.cs.columbia.iesrcsel.model.CostAwareTextCollection;
import edu.cs.columbia.iesrcsel.model.extractor.CostAwareInformationExtractionSystem;

public class CollisionLogger {

	private LogCollisionSaver saver;
	private String header;
	private String parameterValues;

	public CollisionLogger(CostAwareTextCollection ctc,
			CostAwareInformationExtractionSystem cie,
			UsefulDocumentCountEstimator estimator,
			int ufuls, LogCollisionSaver logSaver) {
		this.saver = logSaver;
		
		header="";
		parameterValues="";
		
		for (Entry<String,String> params : ctc.getParams().entrySet()) {
			header += params.getKey() + ",";
			parameterValues += params.getValue() + ",";		
		}

		for (Entry<String,String> params : cie.getParams().entrySet()) {
			header += params.getKey() + ",";
			parameterValues += params.getValue() + ",";		
		}

		for (Entry<String,String> params : estimator.getParams().entrySet()) {
			header += params.getKey() + ",";
			parameterValues += params.getValue() + ",";		
		}

		header += "useful.documents";
		parameterValues += Integer.toString(ufuls);
		
		saver.log(header, "uniqueDocuments","Collisions");
		
	}

	public void log(long uniqueDocuments, long collisions) {
		saver.log(parameterValues, Double.toString(uniqueDocuments), Double.toString(collisions));
	}

	public void dump() {
		saver.dump();		
	}

}
