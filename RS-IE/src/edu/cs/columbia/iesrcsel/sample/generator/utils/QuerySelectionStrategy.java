package edu.cs.columbia.iesrcsel.sample.generator.utils;

import java.util.List;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;

public abstract class QuerySelectionStrategy {

	private String _nextQuery;

	public abstract void initialize(List<Query> initialQueries);
	
	public boolean hasMoreQueries() {
		_nextQuery = _getNextQueryText();
		return _nextQuery != null;
	}

	protected abstract String _getNextQueryText();

	/**
	 * This requires to call {@link #hasMoreQueries() hasMoreQueries()} before
	 * even the first getNextQuery() call!
	 * @return the next query if there is one, null otherwise
	 */
	public Query getNextQuery() {
		return new Query(_nextQuery);
	}

	public abstract void update(Document document);

}
