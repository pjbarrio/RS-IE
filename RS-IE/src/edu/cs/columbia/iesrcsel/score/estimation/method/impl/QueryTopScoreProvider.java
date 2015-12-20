package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import edu.cs.columbia.iesrcsel.model.impl.Query;

public interface QueryTopScoreProvider {
	public double getTopScore(Query q);
}
