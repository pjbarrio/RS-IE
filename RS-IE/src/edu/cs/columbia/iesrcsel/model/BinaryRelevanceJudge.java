package edu.cs.columbia.iesrcsel.model;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;

public interface BinaryRelevanceJudge {
	public boolean isRelevant(Document d, Query q);
}
