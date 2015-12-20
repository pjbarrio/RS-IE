package edu.cs.columbia.iesrcsel.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.descriptor.generator.DescriptorGenerator;

public class Descriptor<S>{

	Map<String,Double> freqsMap = new HashMap<String,Double>();
	private S source;
	private DescriptorGenerator<S,?> descriptorGenerator;
	
	public Descriptor(S source, DescriptorGenerator<S,?> descriptorGenerator) {
		
		this.source = source;
		this.descriptorGenerator = descriptorGenerator;
		
	}

	public double getProbabilityOfWord(String term){
		
		Double freq = freqsMap.get(term);
		
		return freq == null ? 0.0 : freq;
	}

	public void setProbabilityOfWord(String term, double probability){
		
		freqsMap.put(term, probability);
		
	}
	
	public Set<String> getTerms(){
		return freqsMap.keySet();
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Descriptor){
			Descriptor<S> other = (Descriptor<S>) obj;
			if (source.equals(other.source)){
				if (descriptorGenerator.equals(other.descriptorGenerator))
					return true;
			}
			return false;
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return 31*(31 + source.hashCode()) + descriptorGenerator.hashCode();
	}
}
