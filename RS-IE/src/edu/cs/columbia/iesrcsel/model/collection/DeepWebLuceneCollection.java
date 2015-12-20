package edu.cs.columbia.iesrcsel.model.collection;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class DeepWebLuceneCollection extends TextCollection {

	private class MyFieldSelector implements FieldSelector{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6669925082703255747L;
		private String MY_FIELD;

		public MyFieldSelector(String myField){
			MY_FIELD=myField;
		}

		@Override
		public FieldSelectorResult accept(String fieldName) {
			if (fieldName.equals(MY_FIELD)) return FieldSelectorResult.LOAD_AND_BREAK;
			return FieldSelectorResult.NO_LOAD;
		}

	}

	private class MyFilter extends Filter{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int docID;

		public MyFilter(int docID){
			this.docID = docID;
		}

		@Override
		public DocIdSet getDocIdSet(IndexReader arg0) throws IOException {

			return new DocIdSet() {

				@Override
				public DocIdSetIterator iterator() throws IOException {
					return new DocIdSetIterator() {


						private boolean asked;

						@Override
						public int nextDoc() throws IOException {
							if (!asked){
								asked = true;
								return docID;
							}
							return NO_MORE_DOCS;
						}

						@Override
						public int docID() {
							if (!asked){
								return docID;
							}
							return NO_MORE_DOCS;
						}

						@Override
						public int advance(int arg0) throws IOException {

							if (!asked && arg0 < docID){
								asked=true;
								return docID;
							}

							return NO_MORE_DOCS;
						}
					};
				}
			};
		}
	}

	private static final  String PATH_FIELD = "path";

	public static FieldSelector f_path =  new FieldSelector() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;



		@Override
		public FieldSelectorResult accept(String fieldName) {
			if (fieldName == PATH_FIELD  ) return FieldSelectorResult.LOAD_AND_BREAK;
			return FieldSelectorResult.NO_LOAD;
		}

	};

	private static Map<String,FieldSelector> fieldSelectorMap = new HashMap<String,FieldSelector>();

	private IndexSearcher indexSearcher;

	private Set<String> stop_words = null;

	public DeepWebLuceneCollection(String id, String file) {
		super(id);

		Similarity s;
		
		if (file.contains("ntfidf")){
			s = new DefaultSimilarity(){
				@Override
				public float tf(float freq) {
					return 1.0f;
				}
				@Override
				public float idf(int docFreq, int numDocs) {
					return 1.0f;
				}
			};
		} else if (file.contains("ntf")){
			s = new DefaultSimilarity(){
				public float tf(float freq) {
					return 1.0f;
				}
			};
		} else if (file.contains("nidf")){
			
			s = new DefaultSimilarity(){
				@Override
				public float idf(int docFreq, int numDocs) {
					return 1.0f;
				}
			};
			
		} else{
			s =  new DefaultSimilarity();
		}
		
		try {

			Directory directory = FSDirectory.open(new File(file), new NoLockFactory());
			IndexReader indexReader = IndexReader.open(directory);
			indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(s);
			
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String CONTENT_FIELD = "contents";

	@Override
	public List<ScoredDocument> search(Query query) {
		return search(query,Integer.MAX_VALUE);
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {

		BooleanQuery b = getQuery(query);

		try {
			TopDocs result = indexSearcher.search(b, (int)maxNumberOfDocuments);
			List<ScoredDocument> ret = new ArrayList<ScoredDocument>(result.scoreDocs.length);
			for (ScoreDoc d : result.scoreDocs) {
				ret.add(new ScoredDocument(new Document(this, d.doc), d.score));
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private BooleanQuery getQuery(Query query) {
		BooleanQuery b = new BooleanQuery();

		for (String t : query.getTerms()) {
			b.add(new TermQuery(new Term("contents", t)), Occur.MUST);
		}

		return b;
	}

	@Override
	public long getDocumentFrequency(String term) {
		try {
			return indexSearcher.docFreq(new Term(CONTENT_FIELD, term));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public double matchingItems(Query query) {

		BooleanQuery b = getQuery(query);

		try {
			TopDocs result = indexSearcher.search(b, Integer.MAX_VALUE);

			return result.totalHits;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return -1;

	}

	@Override
	public long size() {

		return indexSearcher.getIndexReader().numDocs();

	}

	@Override
	public String getPath(Document doc) {

		try {
			return indexSearcher.doc(doc.getId(),f_path).get(PATH_FIELD);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public Set<String> getTerms(Document doc) {
		try {

			TermFreqVector aux = indexSearcher.getIndexReader().getTermFreqVector(doc.getId(),CONTENT_FIELD);

			if (aux==null){

				try {
					throw new Exception("Term vector has not been indexed for " + this.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return new HashSet<String>(0);
			}								

			return new HashSet<String>(Arrays.asList(aux.getTerms()));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Set<String> getStopWords() {

		if (stop_words == null){
			stop_words = new HashSet<String>(StandardAnalyzer.STOP_WORDS_SET.size());
			for (Object stop : StandardAnalyzer.STOP_WORDS_SET){
				stop_words.add(new String((char[])stop));
			}

		}

		return stop_words;


	}

	@Override
	public boolean isStopWord(String term) {
		return getStopWords().contains(term);
	}

	@Override
	public boolean containsTerm(Document document, String term) {

		try {

			return indexSearcher.search(new TermQuery(new Term(CONTENT_FIELD,term)), new MyFilter(document.getId()),Integer.MAX_VALUE).totalHits > 0;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public String getField(Document doc, String field) {
		try {

			return indexSearcher.doc(doc.getId(),getFieldSelector(field)).get(field);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private FieldSelector getFieldSelector(String field) {

		FieldSelector ret = fieldSelectorMap.get(field);

		if (ret == null){

			ret = new MyFieldSelector(field);
			fieldSelectorMap.put(field, ret);

		}

		return ret;

	}

	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Set<String> getHGrams(Document doc, int h) {

		Set<String> ret = new HashSet<String>();

		try {

			Reader reader = new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD));
			TokenStream tokenizer = new LowerCaseFilter(Version.LUCENE_31, new StandardTokenizer(Version.LUCENE_31, reader));

			//if I want to remove stopwords and other things, then I can add new filters.
			//See if they are removed (e.g., "house of cards" -> "house cards") or ignored completely (e.g., "house of cards"-> [])

			ShingleFilter stokenizer = new ShingleFilter(tokenizer, h, h);
			stokenizer.setOutputUnigrams(false);
			CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

			while (stokenizer.incrementToken()) {
				String token = charTermAttribute.toString();
				ret.add(token);
			}

			reader.close();
			tokenizer.close();
			stokenizer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<String> getTokenizedTerms(Document document) {
		
		try {

			TermFreqVector aux = indexSearcher.getIndexReader().getTermFreqVector(document.getId(),CONTENT_FIELD);

			if (aux==null){

				try {
					throw new Exception("Term vector has not been indexed for " + this.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return new ArrayList<String>(0);
			
			} else {

				int size = aux.getTerms().length;
				
				int total = 0;
				
				for (int i = 0; i < size; i++) {

					total += aux.getTermFrequencies()[i];
				
				}
				
				List<String> ret = new ArrayList<String>(total);
				
				for (int i = 0; i < size; i++) {
					
					int freq = aux.getTermFrequencies()[i];
					
					for (int j = 0; j < freq; j++) {
						ret.add(aux.getTerms()[i]);
					}
					
				}
				
				return ret;
			}							

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

		
	}

	@Override
	public Map<String, Integer> getTermFreqMap(Document doc) {

		try {

			TermFreqVector aux = indexSearcher.getIndexReader().getTermFreqVector(doc.getId(),CONTENT_FIELD);

			if (aux==null){

				try {
					throw new Exception("Term vector has not been indexed for " + this.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return new HashMap<String,Integer>(0);
			
			} else {

				Map<String,Integer> map = new HashMap<String, Integer>();
				
				int size = aux.getTerms().length;

				for (int i = 0; i < size; i++) {

					map.put(aux.getTerms()[i],aux.getTermFrequencies()[i]);

				}

				return map;
			}							

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public void close() {
		try {
			indexSearcher.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

}
