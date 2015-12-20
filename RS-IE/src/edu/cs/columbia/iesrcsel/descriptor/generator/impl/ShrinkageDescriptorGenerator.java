package edu.cs.columbia.iesrcsel.descriptor.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.descriptor.generator.DescriptorGenerator;
import edu.cs.columbia.iesrcsel.model.DatabasesHierarchy;
import edu.cs.columbia.iesrcsel.model.impl.Category;
import edu.cs.columbia.iesrcsel.model.impl.Descriptor;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;

public class ShrinkageDescriptorGenerator extends DescriptorGenerator<TextCollection,TextCollection> {

	private DatabasesHierarchy dbH;
	private DescriptorGenerator<TextCollection,Sample> descriptorGenerator;
	private DescriptorGenerator<Category,Category> categoryDescriptorGenerator;
	private SampleGenerator sampleGenerator;

	public ShrinkageDescriptorGenerator(DatabasesHierarchy dbH, DescriptorGenerator<TextCollection,Sample> descriptorGenerator, SampleGenerator sampleGenerator, DescriptorGenerator<Category,Category> categoryDescriptorGenerator) {
		this.dbH = dbH;
		this.descriptorGenerator = descriptorGenerator;
		this.sampleGenerator = sampleGenerator;
		this.categoryDescriptorGenerator = categoryDescriptorGenerator;
	}

	@Override
	protected Descriptor<TextCollection> _generateDescriptor(TextCollection textCollection) {
		
		Descriptor<TextCollection> RD;
		
		Descriptor<TextCollection> descriptor = descriptorGenerator.generateDescriptor(sampleGenerator.generateSample(textCollection));
		
		List<Category> categories = dbH.getHigherCategories(textCollection); //Does not include 
		
		//Initialization step
		
		double[] lambdas = new double[categories.size()+2];
		
		double value = 1.0 / (2.0 + lambdas.length);
		
		for (int i = 0; i < lambdas.length; i++) {
			lambdas[i] = value;
		}
		
		Set<String> terms = new HashSet<String>();
		
		List<Descriptor<Category>> descriptors = new ArrayList<Descriptor<Category>>();
		
		for (int j = 0; j < categories.size(); j++) {
			
			Descriptor<Category> desc = categoryDescriptorGenerator.generateDescriptor(categories.get(j));
			
			descriptors.add(desc);
			
			terms.addAll(desc.getTerms());
			
		}
		
		double pwC0 = 1.0 / (double)terms.size();
		
		int iter = 0;
		
		do{
		
			//Calculating R(D)
			
			double val;
			
			//XXX need sample instead of textCollection...
			
			RD = new Descriptor<TextCollection>(textCollection, this);
			
			for (String term : terms) {
				
				val = lambdas[lambdas.length-1]*descriptor.getProbabilityOfWord(term) + lambdas[0]*pwC0;
				
				for (int i = 1; i < descriptors.size(); i++) {
					
					val+=lambdas[i]*descriptors.get(i).getProbabilityOfWord(term);						
					
				}
				
				RD.setProbabilityOfWord(term, val);
				
			}
			
			//Expectation Step:
			
			double[] Bs = new double[categories.size() + 2];
			
			//calculating B0
			
			double deno = 0.0;
			
			for (String term : descriptor.getTerms()) {
				
				deno += 1.0/(RD.getProbabilityOfWord(term));
				
			}
			
			Bs[0] = descriptor.getTerms().size() * lambdas[0]*pwC0*deno;
			
			//Calculating other Bs
			
			int i;
			
			for (int k = 0; k < descriptors.size(); k++) { //Optimization can be revert the fors
				
				i = k+1;
				
				Bs[i] = 0.0;
				
				for (String term : descriptor.getTerms()) {
					Bs[i]+=lambdas[i]*descriptor.getProbabilityOfWord(term)/RD.getProbabilityOfWord(term);
				}
				
			}
			
			//Calculating Bm+1
			
			int index = Bs.length-1;
			
			Bs[index] = 0.0;
			
			for (String term : descriptor.getTerms()) {
			
				Bs[index] = lambdas[index]*descriptor.getProbabilityOfWord(term)/RD.getProbabilityOfWord(term);
				
			}
			
			//Maximization Step:
			
			double den = 0.0;
			
			for (int j = 0; j < Bs.length; j++) {
				den += Bs[j];
			}
			for (int j = 0; j < Bs.length; j++) {
				lambdas[j] = Bs[j]/den;
			}
			
			//Termination Check
			
		} while (++iter < 15); //XXX see the expectation maximization convergence, although the original paper says that it takes a dozen iterations. We can measure p(w|D) for each word for epsilon);
		
		return RD;
		
	}
	
	

}
