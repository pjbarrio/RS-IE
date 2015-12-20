package edu.cs.columbia.iesrcsel.model.extractor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.nc2.iosp.fysat.UnsupportedDatasetException;

import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;

public class DeepWebInformationExtractionSystem extends
InformationExtractionSystem {

	private String suffix;
	private DeepWebLuceneCollection collection;
	private Map<String, String> params;

	public DeepWebInformationExtractionSystem(String id, String extractor, String relation, DeepWebLuceneCollection collection) {
		super(id, relation, extractor);
		suffix="-"+extractor+"-"+relation;
		params = new HashMap<String,String>();
		params.put("extractor", extractor);
		this.collection = collection;
		
	}

	@Override
	protected List<Tuple> extractTuples(Document document) {

		if (!document.getCollection().equals(collection)){
			try {
				throw new UnsupportedDatasetException("Requesting tuples for documents in the wrong collection!");
			} catch (UnsupportedDatasetException e) {
				e.printStackTrace();
				return null;
			}
		} else{
			
			String tuples = collection.getField(document,"Tuples-" + collection.getId() + suffix);

			return createTuples(tuples);
		}

	}

	private List<Tuple> createTuples(String tuples) {

		List<String> tups = Arrays.asList(tuples.substring(1,tuples.length()-1).split(", "));

		List<Tuple> t = new ArrayList<Tuple>();

		for (String tup : tups) {

			if (!tup.trim().isEmpty())
				t.add(generateTuple(tup));

		}

		return t;

	}

	public static Tuple generateTuple(String line) {

		//Tuples are stored: field:value;field:value;field:value

		Tuple t = new Tuple();

		String[] pairs = line.split(";");

		for (int i = 0; i < pairs.length; i++) {

			String[] fv = pairs[i].split(":");

			if (fv.length > 1)
				t.addFieldValue(fv[0], fv[1]);

		}

		return t;

	}

	@Override
	public Map<String, String> getParams() {
		return params;
	}

}
