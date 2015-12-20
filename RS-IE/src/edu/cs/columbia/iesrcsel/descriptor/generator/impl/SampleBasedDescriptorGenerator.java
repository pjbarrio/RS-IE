package edu.cs.columbia.iesrcsel.descriptor.generator.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.cs.columbia.iesrcsel.descriptor.generator.DescriptorGenerator;
import edu.cs.columbia.iesrcsel.model.impl.Descriptor;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class SampleBasedDescriptorGenerator extends DescriptorGenerator<TextCollection,Sample>{

	@Override
	protected Descriptor<TextCollection> _generateDescriptor(Sample sample){

		Descriptor<TextCollection> descriptor = new Descriptor<TextCollection>(sample.getCollection(), this);
		
		Map<String,Integer> termsMap  = new HashMap<String, Integer>();
		
		for (Document document : sample) {
			Set<String> added = document.getTerms();
			for (String term : added) {
				Integer freq = termsMap.get(term);
				if (freq == null){
					freq = 0;
				}
				termsMap.put(term, freq+1);
			}
		}
		
		for (Entry<String,Integer> entry : termsMap.entrySet()) {
			descriptor.setProbabilityOfWord(entry.getKey(), (double) entry.getValue() / (double) sample.size());
		}
		
		return descriptor;
	}
	

	
}


