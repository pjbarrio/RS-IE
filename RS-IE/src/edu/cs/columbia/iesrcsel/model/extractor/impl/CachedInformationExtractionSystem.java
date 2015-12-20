package edu.cs.columbia.iesrcsel.model.extractor.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lemurproject.indri.ParsedDocument;
import lemurproject.indri.ParsedDocument.TermExtent;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import ucar.nc2.iosp.fysat.UnsupportedDatasetException;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class CachedInformationExtractionSystem extends
		InformationExtractionSystem {

	public Map<String, List<Tuple>> tupleData;
	private Map<String, String> params;
	private TextCollection collection;

	public CachedInformationExtractionSystem(String id, String relation, String extractor, TextCollection collection) {
		super(id, relation, extractor);
		
		params = new HashMap<String,String>();
		
		params.put("extractor", extractor);
		
		String extr = getExtractor() == null? "default" : getExtractor();
		
		this.collection = collection;
		
		tupleData = (Map<String,List<Tuple>>) SerializationHelper.deserialize("data/extractions/" + getRelation() + "." + extr + "." + collection.getId());		
		
	}

	@Override
	protected List<Tuple> extractTuples(Document document) {
		return getTupleData(document.getCollection()).get(document.getPath());
	}

	private Map<String, List<Tuple>> getTupleData(TextCollection collection) {
		
		if (!this.collection.equals(collection)){
			try {
				throw new UnsupportedDatasetException("Requesting tuples for documents in the wrong collection!");
			} catch (UnsupportedDatasetException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return tupleData;
		
	}

	@Override
	public Map<String, String> getParams() {
		return params;
	}
	
	
}
