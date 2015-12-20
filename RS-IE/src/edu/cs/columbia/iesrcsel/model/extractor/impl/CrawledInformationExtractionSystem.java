package edu.cs.columbia.iesrcsel.model.extractor.impl;

import java.io.EOFException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.nc2.iosp.fysat.UnsupportedDatasetException;

import edu.cs.columbia.iesrcsel.model.collection.CrawledLuceneCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class CrawledInformationExtractionSystem extends
		InformationExtractionSystem {

	private Map<Integer,List<Tuple>>  extractions;
	private Map<String, String> params;
	private TextCollection collection;
	
	public CrawledInformationExtractionSystem(String id, String relation, String extractor, TextCollection collection) {
		super(id, relation,extractor);
		
		params = new HashMap<String,String>();
		
		params.put("extractor", extractor);
		
		this.collection = collection;
		
		String website = collection.getId();
		
		extractions = new HashMap<Integer, List<Tuple>>();
		
		int currentSplit = 1;
		
		String extr = getExtractor() == "SSK"? "Sub-sequences" : "N-Grams";
		
		String file = "data/extraction/crawl/" + website + "/" + extr + "_" + getRelation() + "-" + currentSplit+ ".data";
		
		while (new File(file).exists()){
			
			try{
				
				Map<Integer,List<Tuple>> aux = (Map<Integer,List<Tuple>>)SerializationHelper.deserialize(file);
				
				extractions.putAll(aux);
				
			} catch (NullPointerException e){
				
				System.err.println("Broken extraction file: " + file);
				
			}
			
			
			currentSplit++;
			
			file = "data/extraction/crawl/" + website + "/" + extr + "_" + getRelation() + "-" + currentSplit+ ".data";
			
		}
		
	}

	@Override
	protected List<Tuple> extractTuples(Document document) {
		return getExtractions(document.getCollection()).get(document.getId());
	}

	private Map<Integer, List<Tuple>> getExtractions(TextCollection textCollection) {
		
		if (!textCollection.equals(collection)){
				try {
					throw new UnsupportedDatasetException("Requesting tuples for documents in the wrong collection!");
				} catch (UnsupportedDatasetException e) {
					e.printStackTrace();
					return null;
				}
			}
		
		return extractions;
		
		
	}

	@Override
	public Map<String, String> getParams() {
		return params;
	}

}
