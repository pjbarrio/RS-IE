package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator;

import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.CollisionLogger;
import edu.cs.columbia.iesrcsel.model.impl.Document;

public class CollisionCounter {

	Map<Document, Integer> docFreq;
	private long collisions;
	private CollisionLogger logger;
	
	public CollisionCounter(CollisionLogger logger){
		this.logger = logger;
		reset();
	}
	
	private void reset() {
		collisions = 0;
		docFreq = new HashMap<Document, Integer>();
	}

	public void addDocument(Document doc) {
		
		Integer freq = docFreq.get(doc);
		
		if (freq == null){
			freq = 0;
		}
		
		docFreq.put(doc, freq+1);
		
//		collisions -= ((freq - 1) * (freq))/2;
//		
//		collisions += (freq * (freq+1))/2; 

		collisions += freq;
		
		logger.log(getUniqueDocuments(), getCollisions());
		
	}

	public long getCollisions(){
		return collisions;
	}
	
	public long getUniqueDocuments(){
		return docFreq.size();
	}
}
