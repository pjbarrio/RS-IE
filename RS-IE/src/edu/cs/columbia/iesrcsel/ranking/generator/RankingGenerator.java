package edu.cs.columbia.iesrcsel.ranking.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

public abstract class RankingGenerator {

	public final List<PairUnordered<Query, TextCollection>> getRankedList(Set<TextCollection> collections, InformationExtractionSystem extractionSystem, QueryGenerator queryGenerator){
		Map<PairUnordered<Query, TextCollection>, Double> scoresMap = _estimateQueryCollectionScores(collections,extractionSystem,queryGenerator);
		
		List<PairUnordered<Query, TextCollection>> list = new ArrayList<PairUnordered<Query,TextCollection>>(scoresMap.keySet());
		
		Collections.sort(list, new MapBasedComparator<PairUnordered<Query, TextCollection>>(scoresMap, true));
		
		return list;
	}
	
	protected abstract Map<PairUnordered<Query, TextCollection>, Double> _estimateQueryCollectionScores(
			Set<TextCollection> collections,
			InformationExtractionSystem extractionSystem,
			QueryGenerator queryGenerator);

	public static Map<PairUnordered<Query, TextCollection>,Integer> getRankedMap(List<PairUnordered<Query, TextCollection>> list){
		
		Map<PairUnordered<Query, TextCollection>,Integer> map = new HashMap<PairUnordered<Query, TextCollection>, Integer>(list.size());
		
		Integer i = 0;
		for (PairUnordered<Query, TextCollection> s: list) {			
			map.put(s, ++i);			
		}
		
		return map;
	
	}
	
}
