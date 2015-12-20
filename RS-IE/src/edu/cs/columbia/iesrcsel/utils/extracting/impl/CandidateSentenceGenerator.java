package edu.cs.columbia.iesrcsel.utils.extracting.impl;
import java.io.FileNotFoundException;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gdata.util.common.base.Pair;

import edu.columbia.cs.ref.algorithm.CandidatesGenerator;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.columbia.cs.ref.tool.document.splitter.impl.OpenNLPMESplitter;
import edu.columbia.cs.ref.tool.tagger.entity.EntityTagger;
import edu.columbia.cs.ref.tool.tagger.entity.impl.MapBasedEntityTagger;


public class CandidateSentenceGenerator {

	private int relationConfigurationId;
	private CandidatesGenerator generator;
	private Set<RelationshipType> relationshipTypes;
	private Set<String> tags;

	public CandidateSentenceGenerator(int relationConfigurationId, Set<RelationshipType> relationshipTypes, Set<String> tags) throws FileNotFoundException {
		
		this.relationConfigurationId = relationConfigurationId;
		
		this.relationshipTypes = relationshipTypes;
		
		OpenNLPMESplitter splitter = new OpenNLPMESplitter("en-sent.bin");
		
		Class klass = CandidatesGenerator.class;

	    CodeSource codeSource = klass.getProtectionDomain().getCodeSource();

	    if ( codeSource != null) {

	        System.out.println("Location:" + codeSource.getLocation());

	    }

		
		generator = new CandidatesGenerator(splitter);
		
		this.tags = tags;
		
	}

	public int getRelationConfigurationId() {
		return relationConfigurationId;
	}

	public Set<CandidateSentence> generateCandidateSentences(Document doc,
			Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>> entitiesMap, Map<Integer,String> entityTable) {
		
		if (entitiesMap.size() <= 1){
			return new HashSet<CandidateSentence>(0);
		}
		
		EntityTagger tagger = new MapBasedEntityTagger(tags,entitiesMap,entityTable);
		tagger.enrich(doc);
				
		return generator.generateCandidates(doc, relationshipTypes);
		
	}

}
