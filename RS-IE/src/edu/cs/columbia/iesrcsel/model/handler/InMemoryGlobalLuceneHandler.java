package edu.cs.columbia.iesrcsel.model.handler;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import ucar.nc2.iosp.fysat.UnsupportedDatasetException;
import edu.columbia.cs.cg.prdualrank.index.analyzer.TokenBasedAnalyzer;
import edu.cs.columbia.iesrcsel.model.Queryable;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class InMemoryGlobalLuceneHandler implements Queryable, Serializable {

	private class TokenBasedTokenizer extends Tokenizer{

		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
		private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
		private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		
		private List<String> tokenizedTerms;
		private int currentIndex;
		private int previousOffset;

		public TokenBasedTokenizer(List<String> tokenizedTerms) {
			this.tokenizedTerms = tokenizedTerms;
			currentIndex = 0;
			previousOffset = 0;
		}

		@Override
		public boolean incrementToken() throws IOException {
			
			clearAttributes();
			
			if (currentIndex >= tokenizedTerms.size())
				return false;
			
			termAtt.append(tokenizedTerms.get(currentIndex));
			
			int end = previousOffset + tokenizedTerms.get(currentIndex).length();
			
			offsetAtt.setOffset(previousOffset, end);
			
			previousOffset += end + 1; //+1 is to simulate a space
			
			posIncrAtt.setPositionIncrement(1);
			
			currentIndex++;
			
			return true;
			
		}
		
	}
	
	private static final String CONTENT_FIELD = "contents";
	private RAMDirectory idx;
	private IndexWriter writer;
	private IndexSearcher searcher;
	private Map<String,TextCollection> collections;
	
	public InMemoryGlobalLuceneHandler(){
		
		collections = new HashMap<String, TextCollection>();
		
		idx = new RAMDirectory();
		
		IndexWriterConfig idc = new IndexWriterConfig(Version.LUCENE_31, new TokenBasedAnalyzer(null));
		
		try {
			writer = new IndexWriter(idx,idc);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	@Override
	public List<ScoredDocument> search(Query query) {
		
		return search(query, Integer.MAX_VALUE);
		
	}

	private BooleanQuery getQuery(Query query) {
		BooleanQuery b = new BooleanQuery();

		for (String t : query.getTerms()) {
			b.add(new TermQuery(new Term(CONTENT_FIELD, t)), Occur.MUST);
		}

		return b;
	}
	
	private IndexSearcher getSearcher() {
		
		if (searcher == null){
			
			try {
				writer.close();
				searcher = new IndexSearcher(idx);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return searcher;
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {

		BooleanQuery b = getQuery(query);

		try {
			TopDocs result = getSearcher().search(b, (int)maxNumberOfDocuments);
			List<ScoredDocument> ret = new ArrayList<ScoredDocument>(result.scoreDocs.length);
			for (ScoreDoc d : result.scoreDocs) {
				ret.add(new ScoredDocument(new Document(getCollection(getSearcher().doc(d.doc).get("IdCollection")), Integer.valueOf(getSearcher().doc(d.doc).get("myId"))), d.score));
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	
	private TextCollection getCollection(String collectionId) {
		
		return collections.get(collectionId);
		
	}


	@Override
	public long getDocumentFrequency(String term) {
		try {
			return getSearcher().docFreq(new Term(CONTENT_FIELD, term));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public double matchingItems(Query query) {
		
		BooleanQuery b = getQuery(query);

		try {
			TopDocs result = getSearcher().search(b, Integer.MAX_VALUE);

			return result.totalHits;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return -1;
		
	}

	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void addDocument(Document document) {
		
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
		
		doc.add(new Field("myId", Integer.toString(document.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		doc.add(new Field("IdCollection", document.getCollection().getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		saveCollection(document.getCollection());
		
		doc.add(new Field(CONTENT_FIELD, new LowerCaseFilter(Version.LUCENE_31, new TokenBasedTokenizer(document.getTokenizedTerms())),TermVector.YES));
		
		try {
			writer.addDocument(doc);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	private void saveCollection(TextCollection collection) {
		
		TextCollection t = collections.get(collection.getId());
		
		if (t == null){
			collections.put(collection.getId(),collection);
		}
		
		
	}
	
}
