package edu.cs.columbia.iesrcsel.descriptor.generator;

import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.impl.Descriptor;

/**
 * 
 * @author Pablo
 *
 * @param <T> Level of the descriptor the algorithm generates (e.g., sample)
 * @param <S> Source of the descriptor generator algorithm (e.g., collection)
 */

public abstract class DescriptorGenerator<T,S> {

	private Map<S,Descriptor<T>> descriptorMap = new HashMap<S, Descriptor<T>>();
	
	public Descriptor<T> generateDescriptor(S source) {
		Descriptor<T> d = descriptorMap.get(source);
		if (d == null){
			d = _generateDescriptor(source);
			descriptorMap.put(source, d);
		}
		return d;
	}

	protected abstract Descriptor<T> _generateDescriptor(S source);

}
