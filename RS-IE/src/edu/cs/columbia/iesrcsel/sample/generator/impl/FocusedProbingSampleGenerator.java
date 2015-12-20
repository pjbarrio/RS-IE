package edu.cs.columbia.iesrcsel.sample.generator.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;

import edu.cs.columbia.iesrcsel.model.DatabasesHierarchy;
import edu.cs.columbia.iesrcsel.model.impl.Category;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;

public class FocusedProbingSampleGenerator extends SampleGenerator {

	private DatabasesHierarchy dbH;
	private double tspecificiy;
	private double tcoverage;
	private int Kdocs;

	public FocusedProbingSampleGenerator(DatabasesHierarchy dbH, double tspecificity, double tcoverage, int Kdocs, Map<String, String> params){
		super(params);
		this.dbH = dbH;
		this.tspecificiy = tspecificity;
		this.tcoverage = tcoverage;
		this.Kdocs = Kdocs;
	}
	
	@Override
	protected Sample _generateSample(TextCollection textCollection) {
		
		Category root = dbH.getRoot();
		
		List<Document> documents = new ArrayList<Document>();
		
		documents.addAll(getContentSummary(root,textCollection,tspecificiy,tcoverage,1.0));
		
		Sample sample = new Sample(textCollection, this);
		
		for (Document document : documents) {
			sample.addDocument(document);
		}
		
		return sample;
	}

	private Set<Document> getContentSummary(Category C,
			TextCollection collection, double tspecificiy,
			double tcoverage, double Especificity) {
		
		//Figure 2 in Classification-Aware Hidden Web Text Databases 2008.
		
		Set<Document> ret = new HashSet<Document>();		
		
		if (dbH.isLeaf(C)) //XXX C is the category the textCollection should get assigned ;)
			return ret;
		
		Set<Category> subcategories = dbH.getSubcategories(C);
		
		double[] Ecoverage = new double[subcategories.size()];
		
		int indexCat = 0;
		
		double totalCoverage = 0.0;
		
		for (Category category : subcategories) {
			
			List<Query> queries = dbH.getQueries(category);
			
			for (Query query : queries) {
				
				List<ScoredDocument> results = collection.search(query);
				
				Ecoverage[indexCat] += collection.matchingItems(query); //XXX only if results contains all ...
				
				totalCoverage += Ecoverage[indexCat];
				
				int added = 0;
				
				for (int i = 0; i < results.size() && added < Kdocs; i++) {
					
					if (ret.add(results.get(i))) //ADDs the unseen
						added++;
					
				}
				
			}
			
			indexCat++;
			
		}

		Matrix Ecover = new Matrix(Ecoverage, 3);
		
		Matrix MInv = dbH.getInvertedConfusionMatrix(C);
		
		Matrix cover = MInv.times(Ecover);
		
		double[] coverage = cover.transpose().getArray()[0];
		
		double[] specificityDC = cover.times(Especificity/totalCoverage).transpose().getArray()[0]; //XXX cover or Ecover? I think cover, because Ecover is not accurate
		
		int indexC = 0;		
				
		for (Category category : subcategories) {
			
			if (specificityDC[indexCat] >= tspecificiy && coverage[indexCat] >=tcoverage ){
				
				ret.addAll(getContentSummary(category, collection, tspecificiy, tcoverage, specificityDC[indexC]));
				
			}
			
			indexC++;
			
		}
		
		return ret;
		
	}


}
