package edu.cs.columbia.iesrcsel.ranking.generator.impl.classification;

import java.util.List;

import edu.cs.columbia.iesrcsel.model.impl.Descriptor;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public abstract class ResourceSelectionAlgorithm {

	public abstract double calculateScore(Query query, Descriptor<TextCollection> e);

	public abstract int calculateStandardDeviation(List<Double> scores);

	public abstract int calculateMean(List<Double> scores);

	public abstract double calculateScore(String[] words, double[] assignedProbabilities,
			Descriptor<TextCollection> descriptor, double collectionSize);
	// TODO score that resourceSelectionAlgorithm assigns for p(wk|D) = dk/|D|
	
}
