package edu.cs.columbia.iesrcsel.utils.extracting.impl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import opennlp.tools.util.InvalidFormatException;

import com.google.gdata.util.common.base.Pair;

import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.Segment;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.constraint.role.impl.EntityTypeConstraint;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.entity.Entity;
import edu.columbia.cs.ref.model.re.Model;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;


public class REELRelationExtractionSystem{

	private Model ri;
	private StructureConfiguration structureConfiguration;
	private Set<String> tags;
	private Set<RelationshipType> relationshipTypes;
	private CandidateSentenceGenerator generator;
	private Map<Integer, String> entitiesTable;
	private EntityExtractor[] extractors;
	private Map<String, Integer> entityTypeTable;
	private String relation;
	private String extractor;

	public REELRelationExtractionSystem(String pathModels, String entityModels, int informationExtraction, int relationConf) throws InvalidFormatException, IOException{

		//Extractors: Shortest-Path (1), Sub-sequences (2), Bag of N-Grams (3), Dependency-Graph (4)
		//Relations: PersonCareer (1), NaturalDisaster (2), ManMadeDisaster (3), PersonTravel (4), VotingResult (5), Indictment-Arrest-Trial (6)

		ri = RelationConfiguration.generateRelationExtractionSystem(pathModels,informationExtraction,relationConf);

		structureConfiguration = RelationConfiguration.generateStructureConfiguration(informationExtraction);

		tags = RelationConfiguration.getTags(relationConf);

		relationshipTypes = getRelationshipType(relationConf,tags);

		generator = new CandidateSentenceGenerator(relationConf,relationshipTypes,tags);

		entityTypeTable = RelationConfiguration.getTagsTable();

		entitiesTable = reverse(entityTypeTable);

		extractors = getExtractors(entityModels,relationConf);		

		relation = RelationConfiguration.getRelationName(relationConf);

		extractor = fillExtractor(informationExtraction);

	}

	public List<Tuple> extractTuplesFrom(String content) throws IOException{

		List<Tuple> tuples = execute(content);
			
		return tuples;
	}
	
	public String toString(){
		return getPlanString();
	}
	
	public String fillExtractor(int i){
		switch (i) {
		case 1:
			return "Shortest-Path";
		case 2:
			return "Sub-sequences";
		case 3:
			return "N-Grams";
		case 4:
			return "Dependency-Graph";
		default:
			return "None";
		}
	}

	private <A, B> Map<A, B> reverse(
			Map<B, A> table) {

		Map<A,B> ret = new HashMap<A, B>();

		for (Entry<B, A> entry : table.entrySet()) {

			ret.put(entry.getValue(), entry.getKey());

		}

		return ret;

	}

	private EntityExtractor[] getExtractors(String entityModels, int relationConf) {

		int[][] entities = RelationConfiguration.getEntities(relationConf);

		List<Integer> entExp = new ArrayList<Integer>(entities.length); 

		for(int i = 0; i < entities.length ; i++){

			int exp = RelationConfiguration.getForExtractor(entities[i][1]);

			System.out.println("Experiment: " + exp);

			if (!entExp.contains(exp))
				entExp.add(exp);
		}

		EntityExtractor[] ret = new EntityExtractor[entExp.size()];

		for(int i = 0; i < ret.length ; i++){
			try {
				ret[i] = RelationConfiguration.createEntityExtractor(entityModels,entExp.get(i));
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return ret;
	}

	public static Set<RelationshipType> getRelationshipType(int relationConf, Set<String> tags) {

		Set<RelationshipType> ret = new HashSet<RelationshipType>(1);

		RelationshipType relationshipType = new RelationshipType(RelationConfiguration.getType(relationConf),tags.toArray(new String[tags.size()]));

		for (String tag : tags) {

			relationshipType.setConstraints(new EntityTypeConstraint(tag), tag);

		}

		ret.add(relationshipType);

		return ret;

	}

	public List<Tuple> execute(String docContent) {

		List<edu.columbia.cs.ref.model.Segment> text = new ArrayList<Segment>();
		
		text.add(new Segment(docContent, 0));
		
		Document doc = new Document(text);

		//prepares Internal Tuple

		//System.out.println("About to generate OS");

		List<Tuple> ret = new ArrayList<Tuple>();

		Set<CandidateSentence> candidateSentences = getCandidateSentences(doc);
		int numCandidates = candidateSentences.size();
		int i=0;
		for(CandidateSentence sent : candidateSentences){
			i++;
			if(numCandidates>100){
				System.out.println(((double)i)/numCandidates);
			}
			OperableStructure operableStructure = structureConfiguration.getOperableStructure(sent);
			System.out.println(operableStructure.getCandidateSentence().getSentence().getValue());
			if (ri.predictLabel(operableStructure).contains(relation)){
				ret.add(generateTuple(operableStructure));
			}
		}

		return ret;
	}

	private Tuple generateTuple(OperableStructure operableStructure) {

		Tuple ret = new Tuple();

		Entity[] entities = operableStructure.getCandidateSentence().getEntities();

		for (int i = 0; i < entities.length; i++) {

			ret.addFieldValue(entities[i].getEntityType(), entities[i].getValue());
			
		}

		return ret;
	}



	private Set<CandidateSentence> getCandidateSentences(Document doc) {

		try {

			//System.out.println("About to generate Ent");

			Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> entitiesMap = createEntitiesMap(doc);

			//System.out.println("About to generate Ent");

			Set<CandidateSentence> ret = new HashSet<CandidateSentence>(0);

			//System.out.println("Generating CS");

			Runnable t =  new CachCandidateSentenceRunnable(doc,generator, entitiesMap, entitiesTable, ret);
			int numEntities = 0;

			t.run();

			return ret;

		} catch (ClassCastException e) {
			e.printStackTrace();
		} 

		return null;



	}

	private Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> createEntitiesMap(
			Document doc) {

		Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> ret = new HashMap<Integer, List<Pair<Long, Pair<Integer, Integer>>>>();

		for (int i = 0 ; i < extractors.length ; i++){

			Runnable t = new Thread(new EntityExtractorRunnable(doc,extractors[i],ret,entityTypeTable));

			t.run();

		}

		return ret;

	}

	public String getPlanString() {
		return relation;
	}

	public String getRelationship(){
		return relation;
	}

	public String getExtractor(){
		return extractor;
	}


}
