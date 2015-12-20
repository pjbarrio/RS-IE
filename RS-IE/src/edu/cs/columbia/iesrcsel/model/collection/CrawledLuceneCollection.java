package edu.cs.columbia.iesrcsel.model.collection;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.NoDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.RemoveDuplicatesTokenFilter;
import org.apache.solr.analysis.WordDelimiterFilterFactory;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class CrawledLuceneCollection extends TextCollection {

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

	private static Set<String> stop_words = null;

	private WordDelimiterFilterFactory t;

	public CrawledLuceneCollection(String website, String host) {
		super(website);

		try {

			String prefix = "/proj/db-files2/NoBackup/pjbarrio/Dataset/crawl-" + host;

			String path = prefix + "/apache-solr-3.1.0/example/multicore/";

			Directory directory = FSDirectory.open(new File(path + website + "/data/index/"), new NoLockFactory());
			IndexReader indexReader = IndexReader.open(directory,true);
			indexSearcher = new IndexSearcher(indexReader);

			t = new WordDelimiterFilterFactory(); //XXX see parameters, specially catenateWords

			Map<String, String> params = new HashMap<String, String>();

			params.put("generateWordParts","1");
			params.put("generateNumberParts","1");
			params.put("catenateWords","1");
			params.put("catenateNumbers","1");
			params.put("catenateAll","0");
			params.put("splitOnCaseChange","1");

			t.init(params);

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

	private static final String CONTENT_FIELD = "content";

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
			b.add(new TermQuery(new Term("content", t)), Occur.MUST);
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

			if (aux==null){ //XXX term vectors have not been indexed



				//				StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_CURRENT);
				//
				//				TokenStream ts = sa.tokenStream(CONTENT_FIELD, new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD)));


				Reader reader = new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD));

				TokenStream ts = new RemoveDuplicatesTokenFilter(t.create(new LowerCaseFilter(Version.LUCENE_31, new StopFilter(Version.LUCENE_31, new WhitespaceTokenizer(Version.LUCENE_31, reader) , getStopWords(), true))));;

				CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);

				ts.reset();

				Set<String> ret = new HashSet<String>();

				while (ts.incrementToken()) {
					if (!ret.add(charTermAttribute.toString()))
						;//System.err.println("Duplicate removal in CrawledLuceneCollection.getTerms() failed");
				}

				ts.close();

				return ret;

			}				

			return new HashSet<String>(Arrays.asList(aux.getTerms()));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	//	@Override
	//	public Set<String> getStopWords() {
	//
	//		if (stop_words == null){
	//			try {
	//				stop_words = new HashSet<String>(FileUtils.readLines(new File("data/stopWords.txt")));
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	////			for (Object stop : StandardAnalyzer.STOP_WORDS_SET){
	////				stop_words.add(new String((char[])stop));
	////			}
	//
	//		}
	//
	//		return stop_words;
	//
	//	}

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



	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void close() {
		try {
			indexSearcher.getIndexReader().close();
			indexSearcher.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getHGrams(Document doc, int h) {

		Set<String> ret = new HashSet<String>();

		try {

			//			Reader reader = new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD));
			//			TokenStream tokenizer = new LowerCaseFilter(Version.LUCENE_CURRENT, new StandardTokenizer(Version.LUCENE_CURRENT, reader));

			Reader reader = new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD));

			TokenStream tokenizer = t.create(new LowerCaseFilter(Version.LUCENE_31, new WhitespaceTokenizer(Version.LUCENE_31, reader)));


			//if I want to remove stopwords and other things, then I can add new filters.
			//See if they are removed (e.g., "house of cards" -> "house cards") or ignored completely (e.g., "house of cards"-> [])

			ShingleFilter stokenizer = new ShingleFilter(tokenizer, h, h);
			stokenizer.setOutputUnigrams(false);
			CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

			while (stokenizer.incrementToken()) {

				ret.add(charTermAttribute.toString());

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
	public List<String> getTokenizedTerms(Document doc) {

		try {

			TermFreqVector aux = indexSearcher.getIndexReader().getTermFreqVector(doc.getId(),CONTENT_FIELD);

			if (aux != null){

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
				
			} else {

				Reader reader = new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD));

				TokenStream ts = t.create(new LowerCaseFilter(Version.LUCENE_31, new StopFilter(Version.LUCENE_31, new WhitespaceTokenizer(Version.LUCENE_31, reader) , getStopWords(), true)));

				CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);

				ts.reset();

				List<String> terms = new ArrayList<String>();

				while (ts.incrementToken()) {

					terms.add(charTermAttribute.toString());

				}

				ts.close();

				return terms;
			}
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ArrayList<String>(0);

	}

	@Override
	public Map<String, Integer> getTermFreqMap(Document doc) {

		Map<String, Integer> map = new HashMap<String, Integer>();

		try {

			TermFreqVector aux = indexSearcher.getIndexReader().getTermFreqVector(doc.getId(),CONTENT_FIELD);

			if (aux==null){ //XXX term vectors have not been indexed

				//				StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_CURRENT);
				//
				//				TokenStream ts = sa.tokenStream(CONTENT_FIELD, new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD)));

				Reader reader = new StringReader(indexSearcher.doc(doc.getId()).get(CONTENT_FIELD));

				TokenStream ts = t.create(new LowerCaseFilter(Version.LUCENE_31, new StopFilter(Version.LUCENE_31, new WhitespaceTokenizer(Version.LUCENE_31, reader) , getStopWords(), true)));

				CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);

				ts.reset();

				while (ts.incrementToken()) {

					String term = charTermAttribute.toString();

					Integer freq = map.get(term);

					if (freq == null){
						freq = 0;
					}

					map.put(term, freq + 1);

				}

				ts.close();

				return map;

			} else {

				int size = aux.getTerms().length;

				for (int i = 0; i < size; i++) {

					map.put(aux.getTerms()[i],aux.getTermFrequencies()[i]);

				}

				return map;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;

	}

}