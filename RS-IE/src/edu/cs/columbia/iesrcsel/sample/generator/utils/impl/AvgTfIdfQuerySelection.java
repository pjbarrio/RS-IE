package edu.cs.columbia.iesrcsel.sample.generator.utils.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;

public class AvgTfIdfQuerySelection extends QuerySelectionStrategy {

	private int itsInitialIndex;
	private List<Query> itsInitialQueries;

	private Map<String, Long> itsTermCountOverAllDocuments;
	private HashMap<String, Long> itsTermDocumentFrequency;
	private Set<String> itsReturnedTerms;
	private int itsSeed;
	
	public AvgTfIdfQuerySelection(int seed) {
		this.itsSeed = seed;
	}

	/**
	 * Next query will be the word with maximal avg_tf.
	 * If no documents have been processed yet, the initial queries will
	 * be used (see {@link #initialize(List) initialize()}). As soon as a document has been
	 * processed (through {@link #update(Document) update()}), the initial queries will
	 * be ignored.
	 */
	@Override
	protected String _getNextQueryText() {
		if (itsTermCountOverAllDocuments.isEmpty()) {
			if (itsInitialIndex >= itsInitialQueries.size()) {
				return null;
			}
			String word = itsInitialQueries.get(itsInitialIndex++).toString(); // TODO This seems to assume that queries are single word queries?
			markAsReturned(word);
			return word;
		} else {
			double max = -1;
			String word = null;
			
			for (Entry<String, Long> entry : itsTermDocumentFrequency.entrySet()) {
				Long ctf = itsTermCountOverAllDocuments.get(entry.getKey());
				final double avg_tf = (double) ctf / (double) entry.getValue();
				if (avg_tf > max){
					max = avg_tf;
					word = entry.getKey();
				}
			}
			if (word != null) {
				markAsReturned(word);
			}
			return word;
		}
	}

	private void markAsReturned(String word) {
		itsReturnedTerms.add(word);	
		itsTermDocumentFrequency.remove(word);
		itsTermCountOverAllDocuments.remove(word);
	}
	
	@Override
	public void update(Document document) {
		Map<String,Integer> words = document.getTermFreqMap();
		
		for (Entry<String,Integer> entry : words.entrySet()) {
			if (itsReturnedTerms.contains(entry.getKey())) {
				continue;
			}
			
			Long freq = itsTermDocumentFrequency.get(entry.getKey());
			if (freq == null){
				freq = new Long(0);
			}
			itsTermDocumentFrequency.put(entry.getKey(), freq + 1);
			
			Long oldFreq = itsTermCountOverAllDocuments.get(entry.getKey());
			if (oldFreq == null){
				oldFreq = new Long(0);
			}
			itsTermCountOverAllDocuments.put(entry.getKey(), oldFreq + entry.getValue());
			
		}
		// newlyAddedWords.clear(); // TODO Why? This is a local variable, will be discarded anyway?
	}

	/**
	 *  Needs single word queries as initial ones; these will only be used as long
	 *  as no document has been processed yet (see {@link #_getNextQueryText() getNextQueryText()})
	 */
	@Override
	public void initialize(List<Query> initialQueries) {
		this.itsInitialQueries = new ArrayList<Query>(initialQueries.size());
		
		//Create queries in random order by seed
		
		MersenneTwister rg = new MersenneTwister(itsSeed);
		
		Set<Integer> chosenIndex = new HashSet<Integer>();
		
		while(itsInitialQueries.size() < initialQueries.size()){
			
			int ch = rg.nextInt(initialQueries.size());
			
			if (chosenIndex.add(ch))			
				itsInitialQueries.add(initialQueries.get(ch));
			
		}
		
		chosenIndex.clear();
		
		this.itsInitialIndex = 0;
		itsTermCountOverAllDocuments = new HashMap<String,Long>();
		itsTermDocumentFrequency = new HashMap<String,Long>();
		itsReturnedTerms = new HashSet<String>();
		
	}

	
	
}