package edu.cs.columbia.iesrcsel.descriptor.generator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.descriptor.generator.DescriptorGenerator;
import edu.cs.columbia.iesrcsel.model.DatabasesHierarchy;
import edu.cs.columbia.iesrcsel.model.impl.Category;
import edu.cs.columbia.iesrcsel.model.impl.Descriptor;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;

public class CategoryDescriptorGenerator extends DescriptorGenerator<Category,Category> {

	private DatabasesHierarchy dbH;
	private SampleBasedDescriptorGenerator collectionDescriptorGenerator;
	private CollectionSizeEstimator collectionSizeEstimator;
	private SampleGenerator sampleGenerator;

	public CategoryDescriptorGenerator(DatabasesHierarchy dbH, SampleBasedDescriptorGenerator collectionDescriptorGenerator, SampleGenerator sampleGenerator, CollectionSizeEstimator collectionSizeEstimator){
		this.dbH = dbH;
		this.collectionDescriptorGenerator = collectionDescriptorGenerator;
		this.sampleGenerator = sampleGenerator;
		this.collectionSizeEstimator = collectionSizeEstimator;
	}
	
	@Override
	protected Descriptor<Category> _generateDescriptor(Category category) {
		
		List<TextCollection> collections = dbH.getDatabasesExhaustive(category);
		
		Map<String,Double> termsMap = new HashMap<String, Double>();
		
		for (int i = 0; i < collections.size(); i++) {
			
			double size = collectionSizeEstimator.getCollectionSizeEstimate(collections.get(i));
			
			Descriptor<TextCollection> descr = collectionDescriptorGenerator.generateDescriptor(sampleGenerator.generateSample(collections.get(i)));
			
			for (String term : descr.getTerms()) {
				
				Double freq = termsMap.get(term);
				
				if (freq == null){
					freq = 0.0;
				}
				
				freq += descr.getProbabilityOfWord(term) * size;
				
				termsMap.put(term, freq);
			}
			
		}

		Descriptor<Category> descriptor = new Descriptor<Category>(category, this);

		for (Entry<String,Double> entry : termsMap.entrySet()) {
			
			descriptor.setProbabilityOfWord(entry.getKey(), entry.getValue());
			
		}
		
		return descriptor;
		
	}

}
