package edu.cs.columbia.iesrcsel.collection.estimation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.ExactCollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class CollectionSizeEstimatorBuilder extends Builder{

	public static Collection<? extends CollectionSizeEstimator> create(
			CollectionSizeEstimatorEnum value, Configuration config) {
		
		List<CollectionSizeEstimator> ret = new ArrayList<CollectionSizeEstimator>();
		
		switch (value) {
		case ExactCollectionSizeEstimator:
			
			ret.add(new ExactCollectionSizeEstimator(new HashMap<String, String>(0)));

		default:
			break;
		}
		
		
		
		return ret;
		
	}


}
