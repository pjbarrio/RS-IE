package edu.cs.columbia.iesrcsel.sample.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.sample.generator.impl.CyclicQuerySampleGeneration;
import edu.cs.columbia.iesrcsel.sample.generator.impl.QBSSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.ReScheduleSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.sample.generator.utils.ScheduleQueryFunction;
import edu.cs.columbia.iesrcsel.sample.generator.utils.impl.AvgTfIdfQuerySelection;
import edu.cs.columbia.iesrcsel.utils.Builder;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class SampleGeneratorBuilder extends Builder {

	private static List<Integer> splits;
	private static Boolean[] tasw;
	private static Boolean[] balanced;
	private static List<Integer> splitsQBS;

	public static List<SampleGenerator> create(SampleGeneratorEnum samgen,
			Configuration config, InformationExtractionSystem ie) {
		
		List<Integer> sampleSize = createList(ToInteger(config.getString("sampleSize").split(SEPARATOR))); //sampleSize=100,1000,100
		
		List<Integer> numberOfQueries = createList(ToInteger(config.getString("numberOfQueries").split(SEPARATOR))); //numberOfQueries=500
		
		Integer[] MaxdocumentsPerQuery = ToInteger(config.getString("max.documentsPerQuery").split(SEPARATOR));
		
		Integer[] documentsPerQuery = ToInteger(config.getString("documentsPerQuery").split(SEPARATOR)); //documentsPerQuery=100 , this is the round size
		
		List<SampleGenerator> ret = new ArrayList<SampleGenerator>();
		
		Map<String,String> params = new HashMap<String, String>();
		
		for (Integer sampleSiz : sampleSize) {
			
			params.put("sampleSize", Integer.toString(sampleSiz));
			
			for (Integer numberOfQuer : numberOfQueries) {
				
				params.put("numberOfQueries", Integer.toString(numberOfQuer));
				
				for (Integer documentsPerQuer : documentsPerQuery) {
					
					params.put("documentsPerQuery", Integer.toString(documentsPerQuer));
					
					for (int i = 0; i < MaxdocumentsPerQuery.length; i++) {
						
						params.put("max.documentsPerQuery", Integer.toString(MaxdocumentsPerQuery[i]));
						
						switch (samgen) {
						
						case CyclicQuerySampleGeneration:
						case ReScheduleSampleGenerator:	
							
							List<Integer> splits = getSplits(config);
							
							Boolean[] tasw = getTuplesAsStopWords(config);
							
							for (Integer split : splits) {
							
								params.put("split", Integer.toString(split));
								
								for (Boolean tupleAsStopWord : tasw) {
							
									params.put("tuplesAsStopWords", Boolean.toString(tupleAsStopWord));
									
									List<Query> initialQueries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + ie.getRelation() + "." + ie.getExtractor() + "." + split + "." + tupleAsStopWord + ".ser"));
									
									switch (samgen) {
									case CyclicQuerySampleGeneration:
										
										params.put("balanced", "NA");
										
										ret.add(new CyclicQuerySampleGeneration(sampleSiz, numberOfQuer, documentsPerQuer, MaxdocumentsPerQuery[i], initialQueries, params));
										
										break;

									case ReScheduleSampleGenerator:
										Boolean[] balanced = getBalanced(config);
										
										for (Boolean balance : balanced) {
											
											params.put("balanced", Boolean.toString(balance));
											
											ret.add(new ReScheduleSampleGenerator(ie, sampleSiz, documentsPerQuer, MaxdocumentsPerQuery[i], numberOfQuer, new ScheduleQueryFunction(balance), initialQueries, params));
											
										}
										break;
									default:
										
										break;
									}
									
									
									
								}
								
							}
								
							break;
							
						case QBSSampleGenerator:
							
							String[] initialQueries = config.getString("qbs.initialQueries").split(SEPARATOR);
							
							String[] querySelectionStrategy = config.getString("qbs.querySelectionStrategy").split(SEPARATOR);
							
							List<Integer> seeds = getSeedsQBS(config);
							
							for (String initialQuer : initialQueries) {

								params.put("initialQueries",initialQuer);
								
								List<Query> initialQs = (List<Query>)SerializationHelper.deserialize(initialQuer);
								
								for (String querySelectionStrateg : querySelectionStrategy) {
											
									params.put("QuerySelectionStrategy",querySelectionStrateg);
									
									for (Integer seed : seeds) {
										
										QuerySelectionStrategy qss = null;
										
										if (querySelectionStrateg.equals("AvgTfIdfQuerySelection")) {
											qss = new AvgTfIdfQuerySelection(seed);
										} 
										
										params.put("seed", Integer.toString(seed));
										
										ret.add(new QBSSampleGenerator(initialQs, sampleSiz, numberOfQuer, documentsPerQuer, qss, params));
										
									}
									
								}
								
							}
							
							break;
							
						default:
							
							break;
						
						}
						
					}
					
					
					
				}
			}
		}
		
		return ret;
		
	}

	private static List<Integer> getSeedsQBS(Configuration config) {
		
		if (splitsQBS == null){
			splitsQBS = Arrays.asList(ToInteger(config.getString("sample.seed").split(SEPARATOR))); //ie.initialQueries.split=1,5,1 
		}
		
		return splitsQBS;
		
	}

	private static Boolean[] getBalanced(Configuration config) {
		
		if (balanced == null){
			balanced = ToBoolean(config.getString("scheduleFunction.balanced").split(SEPARATOR));
		}
		return balanced;
	}

	private static Boolean[] getTuplesAsStopWords(Configuration config) {
		
		if (tasw == null){
			tasw = ToBoolean(config.getString("ie.initialQueries.tupleAsStopWord").split(SEPARATOR)); //ie.initialQueries.tupleAsStopWord=true,false
		}
		
		return tasw;
	}

	private static List<Integer> getSplits(Configuration config) {

		if (splits == null){
			splits = createList(ToInteger(config.getString("ie.initialQueries.split").split(SEPARATOR))); //ie.initialQueries.split=1,5,1 
		}
		
		return splits;
		
		
		
	}

}
