package edu.cs.columbia.iesrcsel.model.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import weka.core.UnsupportedAttributeTypeException;

import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.execution.logger.impl.DummyCostLogger;
import edu.cs.columbia.iesrcsel.model.CostAware;
import edu.cs.columbia.iesrcsel.model.CostAwareTextCollection;
import edu.cs.columbia.iesrcsel.model.collection.DummyTextCollection;
import edu.cs.columbia.iesrcsel.model.extractor.impl.DummyInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;

public class CostAwareInformationExtractionSystem extends InformationExtractionSystem implements CostAware{

	private InformationExtractionSystem underlyingExtractionSystem;
	private Set<Integer> extractedDocuments;
	private Set<Integer> extractedUsefulDocuments;
	private CostLogger cl;
	private Map<String, CostAwareInformationExtractionSystem> individualLoggers;


	public CostAwareInformationExtractionSystem(InformationExtractionSystem underlyingExtractionSystem, CostLogger cl){
		super(underlyingExtractionSystem.getId(), underlyingExtractionSystem.getRelation(), underlyingExtractionSystem.getExtractor());
		this.cl = cl;
		this.underlyingExtractionSystem = underlyingExtractionSystem;
		individualLoggers = new HashMap<String, CostAwareInformationExtractionSystem>();
		initialize();
	}
	
	@Override
	public CostLogger getCostLogger() {
		return cl;
	}
	
	private void initialize() {
		extractedDocuments=new HashSet<Integer>();
		extractedUsefulDocuments=new HashSet<Integer>();
	}

	@Override
	protected List<Tuple> extractTuples(Document document) {
		//It is always unique calls. No need to save the docuemnt id.
		extractedDocuments.add(document.getId());
		List<Tuple> ret =  underlyingExtractionSystem.extract(document);
		if (!ret.isEmpty())
			extractedUsefulDocuments.add(document.getId());
		cl.log();
		
		for (CostAwareInformationExtractionSystem dummy : individualLoggers.values()) {
			dummy.extractedDocuments.add(document.getId());
			if (!ret.isEmpty())
				dummy.extractedUsefulDocuments.add(document.getId());
		}
		
		return ret;
	}

	public Map<String,Integer> getCost(){
		Map<String,Integer> ret = new HashMap<String,Integer>(2);
		ret.put("Extracted Documents", extractedDocuments.size());
		ret.put("Extracted Useful Documents", extractedUsefulDocuments.size());
		return ret;
	}
	
	public void clearCost(){
		initialize();
	}

	@Override
	public Map<String, String> getParams() {
		
		Map<String, String> ret = new HashMap<String,String>();
		
		ret.putAll(underlyingExtractionSystem.getParams());
		
		ret.put("relation.name", getRelation());
		
		return ret;
		
	}

	@Override
	public String getName() {
		return "Extraction System";
	}

	@Override
	public void startIndividualLogging(String id) {
		
		individualLoggers.put(id, new CostAwareInformationExtractionSystem(new DummyInformationExtractionSystem(), new DummyCostLogger()));
	
	}

	@Override
	public CostAware stopIndividualLogging(String id) {
		
		return individualLoggers.remove(id);
		
	}

	@Override
	public void addCost(CostAware cost) {
		
		if (cost instanceof CostAwareInformationExtractionSystem){
			CostAwareInformationExtractionSystem caie = (CostAwareInformationExtractionSystem)cost;
			
			extractedDocuments.addAll(caie.extractedDocuments);
			extractedUsefulDocuments.addAll(caie.extractedUsefulDocuments);
						
		} else
			try {
				throw new UnsupportedAttributeTypeException("Need to pass a CostAwareInformationExtractionSystem");
			} catch (UnsupportedAttributeTypeException e) {
				e.printStackTrace();
			}
		
	}

	
}
