package trial;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lemurproject.indri.ParsedDocument;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class RelationStats {

	public static Map<Integer,String> pathMap = new HashMap<Integer,String>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Map<String,Set<File>> map = (Map<String,Set<File>>)SerializationHelper.deserialize("data/cleanToIndex.ser");

		InformationExtractionSystem ie = new CachedInformationExtractionSystem("natDis", "NaturalDisaster", null, "NYT");
		
		TextCollection tc = new TextCollection("myCol") {
			
			@Override
			public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<ScoredDocument> search(Query query) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public double matchingItems(Query query) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public long getDocumentFrequency(String term) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public long size() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean isStopWord(String term) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public String[] getTerms(Document doc) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Set<String> getStopWords() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getPath(Document doc) {
				return pathMap.get(doc.getId());
			}
			
			@Override
			public boolean containsTerm(Document document, String term) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public Set<String> getHGrams(Document doc, int h) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public double matchingItems(Query query, Set<Document> documents,
					int docsPerQuery) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String[] getTokenizedTerms(Document document) {
				return null;
			}

			@Override
			public Map<String, Integer> getTermFreqMap(Document document) {
				return null;
			}
			
		};
		
		int i = 0;
		
		for (Entry<String,Set<File>> entry : map.entrySet()) {
			
			int count = 0;
			
			for (File f : entry.getValue()) {
			
				i++;
				
				pathMap.put(i,f.getName());
				
				Document d = new Document(tc, i);
				
				if (ie.extract(d).size() > 0)
					count++;
				
			}
			
			System.out.println("TOPIC: " + entry.getKey() + " - Count: " + count + " - Fraction: " + (double)count / (double)entry.getValue().size());
			
		}
	}

}
