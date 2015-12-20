package edu.cs.columbia.iesrcsel.collection.estimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.CollisionCounter;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler.DocumentSampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler.DocumentSamplerBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler.DocumentSamplerEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler.RandomDocumentSampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator.GeometricMeanInverseDocumentDegreeEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator.InverseDocumentDegreeEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator.InverseDocumentDegreeEstimatorBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator.InverseDocumentDegreeEstimatorEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator.GeometricMeanPoolSizeFractionEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator.PoolSizeFractionEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator.PoolSizeFractionEstimatorBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator.PoolSizeFractionEstimatorEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.PlainExternalCollectionQuerySampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySamplerBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySamplerEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition.FixedIterationsStoppingCondition;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition.StoppingCondition;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition.StoppingConditionBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition.StoppingConditionEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator.PlainQueryUsefulnessCalculator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator.QueryUsefulnessCalculator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator.QueryUsefulnessCalculatorBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator.QueryUsefulnessCalculatorEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.TermSelector;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.TermSelectorBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.TermSelectorEnum;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.UsefulDocumentCountBiasedEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.UsefulDocumentCountBiasedEstimatorRB;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.UsefulDocumentCountBiasedEstimatorRBAverage;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.UsefulDocumentCountEstimatorPoolBasedBY;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.UsefulDocumentCountEstimatorPoolBasedHeuristicAvgTermFactors;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.UsefulDocumentCountEstimatorPoolBasedHeuristicTermSetFactor;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.UsefulDocumentCountStratifiedEstimatorUsefulSize;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolfree.UsefulDocumentCountEstimatorPoolFreeUnweighted;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolfree.UsefulDocumentCountEstimatorPoolFreeWeighted;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.scorebased.ScoreBasedUsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.collection.loader.CollectionLoader;
import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.query.generator.QueryGeneratorBuilder;
import edu.cs.columbia.iesrcsel.query.generator.QueryGeneratorEnum;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGeneratorBuilder;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGeneratorEnum;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculatorBuilder;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculatorEnum;
import edu.cs.columbia.iesrcsel.utils.Builder;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class UsefulDocumentCountEstimatorBuilder extends Builder {

	public static List<UsefulDocumentCountEstimator> create(String configFile, InformationExtractionSystem ie) {

		if (configFile.equals("empty")){

			List<UsefulDocumentCountEstimator> ret = new ArrayList<UsefulDocumentCountEstimator>();

			ret.add(new UsefulDocumentCountEstimator(new HashMap<String,String>()) {

				@Override
				public void reset() {
					;
				}

				@Override
				public double getNumberOfUsefulDocuments(TextCollection collection,
						InformationExtractionSystem ie,  CollisionCounter collisionCounter, CostLogger cl) {
					return 0;
				}

				@Override
				public double getCurrentNumberOfUsefulDocuments() {
					return 0;
				}
			});

			return ret;
		}

		if (configFile.equals("default")){


			List<UsefulDocumentCountEstimator> ret = new ArrayList<UsefulDocumentCountEstimator>();

			double biasedWeight = 100;
			String trainingCollection = "TREC";
			String prefixFolder = "/proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/biasedestimator/";
			QuerySampler qs = new PlainExternalCollectionQuerySampler(new HashMap<String, String>(), prefixFolder , trainingCollection , biasedWeight, false);
			int maxIter = 10000;
			StoppingCondition sc = new FixedIterationsStoppingCondition(new HashMap<String, String>(),maxIter) ;
			QueryUsefulnessCalculator quc = new PlainQueryUsefulnessCalculator(new HashMap<String, String>());

			long maxNumberOfDocuments = 1000;

			InverseDocumentDegreeEstimator idee = new GeometricMeanInverseDocumentDegreeEstimator(new HashMap<String, String>(),maxNumberOfDocuments);

			DocumentSampler ds = new RandomDocumentSampler(new HashMap<String, String>());

			long maxNumberOfQueries = 1000;

			PoolSizeFractionEstimator psfe = new GeometricMeanPoolSizeFractionEstimator(new HashMap<String, String>(), maxNumberOfQueries);

			Map<String,String> params = new HashMap<String, String>();

			ret.add(new UsefulDocumentCountBiasedEstimator(params, qs, sc, quc, idee, ds, psfe, maxNumberOfDocuments, false, 0,0));

			return ret;

		}else{

			Parameters params = new Parameters();

			FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class).configure(params.properties().setFileName(configFile));

			try{

				Configuration config = builder.getConfiguration();

				String name = config.getString("estimator.name");

				switch (UsefulDocumentEstimatorCountEnum.valueOf(name)) {
				case UsefulDocumentCountEstimatorPoolFreeUnweighted:

					return createUsefulDocumentCountEstimatorPoolFree(config,false);

				case UsefulDocumentCountEstimatorPoolFreeWeighted:

					return createUsefulDocumentCountEstimatorPoolFree(config,true);

				case UsefulDocumentCountEstimatorPoolBasedHeuristicAvgTermFactors:

					return createUsefulDocumentCountEstimatorPoolBased(config, ie, true,false);

				case UsefulDocumentCountEstimatorPoolBasedHeuristicTermSetFactor:

					return createUsefulDocumentCountEstimatorPoolBased(config, ie, false,false);

				case UsefulDocumentCountEstimatorPoolBasedBY:

					return createUsefulDocumentCountEstimatorPoolBased(config, ie, false,true);

				case UsefulDocumentCountBiasedEstimator:

					return createUsefulDocumentCountBiasedEstimator(config,ie);

				case UsefulDocumentCountStratifiedEstimatorUsefulSize:

					return createUsefulDocumentCountStratifiedEstimatorUsefulSize(config);

				case ScoreBasedUsefulDocumentCountEstimator:

					return createScoreBasedUsefulDocumentCountEstimator(config,ie);

				default:

					return null;

				}

			} catch (ConfigurationException cex){
				System.err.println("loading the configuration file failed...");
			}

		}

		return null;

	}



	private static List<UsefulDocumentCountEstimator> createScoreBasedUsefulDocumentCountEstimator(
			Configuration config, InformationExtractionSystem ie) {

		List<UsefulDocumentCountEstimator> ret = new ArrayList<UsefulDocumentCountEstimator>();

		String[] sampleGenerators = config.getString("sampleGenerator").split(SEPARATOR);

		List<SampleGenerator> sgs = new ArrayList<SampleGenerator>();

		for (int i = 0; i < sampleGenerators.length; i++) {

			sgs.addAll(SampleGeneratorBuilder.create(SampleGeneratorEnum.valueOf(sampleGenerators[i]), config, ie));
		
		}	

		String[] queryGenerators = config.getString("queryGenerator").split(SEPARATOR);
		
		List<QueryGenerator> qgs = new ArrayList<QueryGenerator>();
		
		for (int i = 0; i < queryGenerators.length; i++) {
			
			qgs.addAll(QueryGeneratorBuilder.create(QueryGeneratorEnum.valueOf(queryGenerators[i]),config,ie));
			
		}
		
		String[] collectionLoaders = config.getString("collection.loader.set").split(SEPARATOR);
		
		String[] scoreCalculators = config.getString("scoreCalculator").split(SEPARATOR);
		
		List<ScoreCalculator> scs = new ArrayList<ScoreCalculator>();
		
		for (int i = 0; i < scoreCalculators.length; i++) {
			
			scs.addAll(ScoreCalculatorBuilder.create(ScoreCalculatorEnum.valueOf(scoreCalculators[i]),config,ie));
			
		}
		
		for (String colLoadSet : collectionLoaders) {
			
			CollectionLoader cl = new CollectionLoader(colLoadSet);
			
			for (SampleGenerator sampleGenerator : sgs) {
				
				for (QueryGenerator queryGenerator : qgs) {
					
					for (ScoreCalculator<Sample, Sample> calculator : scs) {
						
						Map<String,String> params = new HashMap<String,String>();

						params.putAll(sampleGenerator.getParams());
						params.putAll(queryGenerator.getParams());
						params.put("collection.loader.set", colLoadSet);
						params.putAll(calculator.getParams());
						ret.add(new ScoreBasedUsefulDocumentCountEstimator(params, calculator, sampleGenerator, queryGenerator, cl));

						
					}
					
				}
				
				
			}

			
		}
		
		
		return ret;
	}



	private static List<UsefulDocumentCountEstimator> createUsefulDocumentCountStratifiedEstimatorUsefulSize(
			Configuration config) {

		String[] correlatedQueriesCollection = config.getString("correlated.queries.collection").split(SEPARATOR);

		Integer[] corrQueriesLimit = ToInteger(config.getString("correlated.queries.limit").split(SEPARATOR));

		String[] uncorrelatedQueries = config.getString ("uncorrelated.queries").split(SEPARATOR);

		Integer[] stratum = ToInteger(config.getString("stratas").split(SEPARATOR));

		Integer[] r1s = ToInteger(config.getString("pilot.queries.r1").split(SEPARATOR));

		Integer[] r2s = ToInteger(config.getString("pilot.queries.r2").split(SEPARATOR));

		List<Long> maxNumberOfDocumentss = createList(ToLong(config.getString("max.number.of.documents").split(SEPARATOR)));

		String[] globalCollections = config.getString("global.collection.cardinalities").split(SEPARATOR);

		List<UsefulDocumentCountEstimator> ret = new ArrayList<UsefulDocumentCountEstimator>();

		for (String correlatedQuery : correlatedQueriesCollection) {
			for (Integer h : corrQueriesLimit) {
				for (String uncorrelatedQuery : uncorrelatedQueries) {
					for (Integer L : stratum) {
						for (Integer r1 : r1s) {
							for (Integer r2 : r2s) {
								for (Long k : maxNumberOfDocumentss) {
									for (String globalCollection : globalCollections) {

										Map<String,String> params = new HashMap<String,String>();

										params.put("correlated.queries.collection", correlatedQuery);
										params.put("uncorrelated.queries", uncorrelatedQuery);
										params.put("stratas", Integer.toString(L));
										params.put("pilot.queries.r1", Integer.toString(r1));
										params.put("pilot.queries.r2", Integer.toString(r2));
										params.put("max.number.of.documents",Long.toString(k));
										params.put("correlated.queries.limit", Integer.toString(h));
										params.put("global.collection", globalCollection);
										ret.add(new UsefulDocumentCountStratifiedEstimatorUsefulSize(params, correlatedQuery, uncorrelatedQuery, L, r1, r2, k, h, globalCollection));

									}
								}
							}
						}
					}
				}
			}
		}




		return ret;

	}



	private static List<UsefulDocumentCountEstimator> createUsefulDocumentCountBiasedEstimator(
			Configuration config, InformationExtractionSystem ie) {

		Integer[] splits = ToInteger(config.getString("split").split(SEPARATOR));

		List<Long> maxNumberOfDocumentss = createList(ToLong(config.getString("max.number.of.documents").split(SEPARATOR)));

		List<UsefulDocumentCountEstimator> ret = new ArrayList<UsefulDocumentCountEstimator>();

		String[] querySamplers = config.getString("query.sampler").split(SEPARATOR);
		List<QuerySampler> qss = new ArrayList<QuerySampler>();

		for (int i = 0; i < querySamplers.length; i++) {
			qss.addAll(QuerySamplerBuilder.create(QuerySamplerEnum.valueOf(querySamplers[i]), config, ie));
		}

		String[] stoppingConditions = config.getString("stopping.condition").split(SEPARATOR);
		List<StoppingCondition> scs = new ArrayList<StoppingCondition>();

		for (int i = 0; i < stoppingConditions.length; i++) {
			scs.addAll(StoppingConditionBuilder.create(StoppingConditionEnum.valueOf(stoppingConditions[i]), config, ie));
		}

		String[] poolSizeFractionEstimators = config.getString("pool.size.fraction.estimator").split(SEPARATOR);
		List<PoolSizeFractionEstimator> psfs = new ArrayList<PoolSizeFractionEstimator>();

		for (int k = 0; k < poolSizeFractionEstimators.length; k++) {
			psfs.addAll(PoolSizeFractionEstimatorBuilder.create(PoolSizeFractionEstimatorEnum.valueOf(poolSizeFractionEstimators[k]), config, ie));

		}

		Boolean[] rawb = ToBoolean(config.getString("raw.blackwellization").split(SEPARATOR));

		Boolean[] removeOutliers = ToBoolean(config.getString("removeOutliers").split(SEPARATOR));

		List<double[]> paramOutlierTrue = createPairList(config.getString("outlierLims").split(SEPARATOR));

		List<double[]> paramOutlierFalse = new ArrayList<double[]>();

		List<double[]> paramOutlier;

		String[] documentSamplers = config.getString("document.sampler").split(SEPARATOR);
		List<DocumentSampler> dss = new ArrayList<DocumentSampler>();

		for (int i = 0; i < documentSamplers.length; i++) {
			dss.addAll(DocumentSamplerBuilder.create(DocumentSamplerEnum.valueOf(documentSamplers[i]), config, ie));
		}

		for (int i = 0; i < maxNumberOfDocumentss.size(); i++) {
			long maxNumberOfDocuments = maxNumberOfDocumentss.get(i);


			for (int rb=0; rb<rawb.length; rb++){

				Boolean[] avg = ToBoolean(config.getString("avg.estimate").split(SEPARATOR));

				for (int av = 0; av < avg.length; av++) {

					String[] queryUsefulnessCalculators = {"PlainQueryUsefulnessCalculator"};

					if (!rawb[rb] || avg[av])
						queryUsefulnessCalculators = config.getString("query.usefulness.calculator").split(SEPARATOR);

					List<QueryUsefulnessCalculator> qus = new ArrayList<QueryUsefulnessCalculator>();

					for (int iqu = 0; iqu < queryUsefulnessCalculators.length; iqu++) {
						qus.addAll(QueryUsefulnessCalculatorBuilder.create(QueryUsefulnessCalculatorEnum.valueOf(queryUsefulnessCalculators[iqu]), config, ie));

					}

					String[] inverseDegreeEstimators = {"PredictedInverseDocumentDegreeEstimator"}; 

					if (!rawb[rb])
						inverseDegreeEstimators = config.getString("inverse.degree.estimator").split(SEPARATOR);


					List<InverseDocumentDegreeEstimator> idees = new ArrayList<InverseDocumentDegreeEstimator>();

					for (int j = 0; j < inverseDegreeEstimators.length; j++) {
						idees.addAll(InverseDocumentDegreeEstimatorBuilder.create(InverseDocumentDegreeEstimatorEnum.valueOf(inverseDegreeEstimators[j]), config, ie, maxNumberOfDocuments));

					}

					for (DocumentSampler ds : dss) {

						for (InverseDocumentDegreeEstimator idee : idees) {

							for (StoppingCondition sc : scs) {

								for (QuerySampler qs : qss) {

									for (int k = 0; k < removeOutliers.length; k++) {

										if (removeOutliers[k]){
											paramOutlier = paramOutlierTrue;
										}else{
											paramOutlier = paramOutlierFalse;
										}

										for (double[] po : paramOutlier) {

											for (int spl=0; spl<splits.length; spl++){

												for (PoolSizeFractionEstimator psf : psfs) {

													for (QueryUsefulnessCalculator quc : qus) {

														Map<String,String> params = new HashMap<String, String>();

														params.put("max.number.of.documents",Long.toString(maxNumberOfDocuments));
														params.put("remove.outliers", Boolean.toString(removeOutliers[k]));
														params.put("remove.outliers.flimlow", Double.toString(po[0]));
														params.put("remove.outliers.flimhigh", Double.toString(po[1]));

														params.put("raw.blacwellization", Boolean.toString(rawb[rb]));
														params.put("split",Integer.toString(splits[spl]));
														params.putAll(ds.getParams());
														params.putAll(idee.getParams());
														params.putAll(quc.getParams());
														params.putAll(sc.getParams());
														params.putAll(qs.getParams());
														params.putAll(psf.getParams());

														if (rawb[rb]){


															if (avg[av]){
																ret.add(new UsefulDocumentCountBiasedEstimatorRBAverage(params, qs, sc, idee, ds, maxNumberOfDocuments, removeOutliers[k], po[0],po[1]));
															}else{
																ret.add(new UsefulDocumentCountBiasedEstimatorRB(params, qs, sc, quc, idee, ds,psf, maxNumberOfDocuments, removeOutliers[k], po[0],po[1]));
															}




														} else {

															ret.add(new UsefulDocumentCountBiasedEstimator(params, qs, sc, quc, idee, ds,psf, maxNumberOfDocuments, removeOutliers[k], po[0],po[1]));
														}

													}
												}
											}
										}
									}
								}
							}

						}
					}

				}

			}

		}

		return ret;

	}



	private static List<UsefulDocumentCountEstimator> createUsefulDocumentCountEstimatorPoolBased(
			Configuration config, InformationExtractionSystem ie, boolean avg, boolean BarYoseff) {


		String[] sampleGenerators = config.getString("sampleGenerator").split(SEPARATOR);
		String[] termSelectors = config.getString("termSelector").split(SEPARATOR);
		Boolean[] removeOutliers = ToBoolean(config.getString("removeOutliers").split(SEPARATOR));
		List<double[]> paramOutlierTrue = createPairList(config.getString("outlierLims").split(SEPARATOR));
		List<double[]> paramOutlierFalse = new ArrayList<double[]>();
		paramOutlierFalse.add(new double[]{0.0,0.0});
		List<UsefulDocumentCountEstimator> ret = new ArrayList<UsefulDocumentCountEstimator>();

		//I want to create a unique copy of the samples.

		List<SampleGenerator> sgs = new ArrayList<SampleGenerator>();

		for (int i = 0; i < sampleGenerators.length; i++) {

			sgs.addAll(SampleGeneratorBuilder.create(SampleGeneratorEnum.valueOf(sampleGenerators[i]), config, ie));
		}

		//I want to create a unique copy of the term selectors.

		List<TermSelector> ts = new ArrayList<TermSelector>();

		for (int j = 0; j < termSelectors.length; j++) {

			ts.addAll(TermSelectorBuilder.create(TermSelectorEnum.valueOf(termSelectors[j]), config, ie));

		}		

		List<double[]> paramOutlier;

		for (SampleGenerator sampleGenerator : sgs) {

			for (TermSelector termSelector : ts) {

				for (int k = 0; k < removeOutliers.length; k++) {

					if (removeOutliers[k]){
						paramOutlier = paramOutlierTrue;
					}else{
						paramOutlier = paramOutlierFalse;
					}

					for (double[] ds : paramOutlier) {


						Map<String,String> params = new HashMap<String,String>();

						params.putAll(sampleGenerator.getParams());
						params.putAll(termSelector.getParams());
						params.put("remove.outliers", Boolean.toString(removeOutliers[k]));
						params.put("remove.outliers.flimlow", Double.toString(ds[0]));
						params.put("remove.outliers.flimhigh", Double.toString(ds[1]));

						if (BarYoseff){
							ret.add(new UsefulDocumentCountEstimatorPoolBasedBY(sampleGenerator, termSelector, removeOutliers[k],ds[0], ds[1], params));
						}else{
							if (avg){
								ret.add(new UsefulDocumentCountEstimatorPoolBasedHeuristicAvgTermFactors(sampleGenerator, termSelector, removeOutliers[k],ds[0], ds[1],params));							
							}else{
								ret.add(new UsefulDocumentCountEstimatorPoolBasedHeuristicTermSetFactor(sampleGenerator, termSelector,params));
							}
						}


					}

				}

			}

		}

		return ret;
	}

	private static List<double[]> createPairList(String[] split) {

		List<double[]> ret = new ArrayList<double[]>(split.length);

		for (int i = 0; i < split.length; i++) {

			String[] aux = split[i].split(SEPARATOR_PAIR);

			double[] l = new double[aux.length];

			for (int j = 0; j < aux.length; j++) {

				l[j] = Double.parseDouble(aux[j]);

			}

			ret.add(l);

		}

		return ret;
	}



	private static List<UsefulDocumentCountEstimator> createUsefulDocumentCountEstimatorPoolFree(
			Configuration config, boolean weighted) {

		List<Integer> positiveQueryLinkWeight = new ArrayList<Integer>();

		long randomseed=config.getLong("randomseed");
		//		String[] startupSeedQuery = config.getString("startupSeedQuery").split(SEPARATOR);
		Integer[] startupPathLength = ToInteger(config.getString("startupPathLength").split(SEPARATOR));
		//		String[] sampleSeedQuery = config.getString ("sampleSeedQuery").split(SEPARATOR);

		String[] hgramsQueriesFor = config.getString ("hgrams.queries.for").split(SEPARATOR);

		Integer[] sampleBurnin = ToInteger(config.getString("sampleBurnin").split(SEPARATOR));
		List<Integer> sampleSize = createList(ToInteger(config.getString("sampleSize").split(SEPARATOR)));
		List<Integer> hgramLength = createList(ToInteger(config.getString("hgramLength").split(SEPARATOR)));

		Integer[] splits = ToInteger(config.getString("split").split(SEPARATOR));

		Boolean[] removeOutliers = ToBoolean(config.getString("removeOutliers").split(SEPARATOR));

		List<double[]> paramOutlierTrue = createPairList(config.getString("outlierLims").split(SEPARATOR));
		List<double[]> paramOutlierFalse = new ArrayList<double[]>();
		paramOutlierFalse.add(new double[]{0.0,0.0});

		Integer[] maxDocsPerQueries = ToInteger(config.getString("maxDocsPerQuery").split(SEPARATOR));
		Integer[] minDocsPerQueries = ToInteger(config.getString("minDocsPerQuery").split(SEPARATOR));
		if (weighted){
			positiveQueryLinkWeight.addAll(Arrays.asList(ToInteger(config.getString("positiveQueryLinkWeight").split(SEPARATOR))));
		}

		List<UsefulDocumentCountEstimator> ret = new ArrayList<UsefulDocumentCountEstimator>();

		Map<String,String> params = new HashMap<String,String>();

		List<double[]> paramOutlier;

		for (String hgramsQueriesFo : hgramsQueriesFor) {
			params.put("hgrams.queries.for", hgramsQueriesFo);
			List<Query> hgramsQueriesF = (List<Query>)SerializationHelper.deserialize(hgramsQueriesFo);

			for (Boolean removeOutlier : removeOutliers) {
				params.put("remove.outliers", Boolean.toString(removeOutlier));

				if (removeOutlier){
					paramOutlier = paramOutlierTrue;
				}else{
					paramOutlier = paramOutlierFalse;
				}

				for (double[] ds : paramOutlier) {

					params.put("remove.outliers.flimlow", Double.toString(ds[0]));
					params.put("remove.outliers.flimhigh", Double.toString(ds[1]));

					for (Integer hgramLengt : hgramLength) {
						params.put("hgramlength", Integer.toString(hgramLengt));
						for (Integer sampleSiz : sampleSize) {
							params.put("sampleSize", Integer.toString(sampleSiz));
							for (Integer sampleBurni : sampleBurnin) {
								params.put("burnin", Integer.toString(sampleBurni));
								//						for (String sampleSeedQuer : sampleSeedQuery) {
								//							params.put("sampleseedquery", sampleSeedQuer);

								for (Integer startupPathLengt : startupPathLength) {
									params.put("startuppathlength", Integer.toString(startupPathLengt));
									//								for (String startupSeedQuer : startupSeedQuery) {
									//									params.put("startupseedquery", startupSeedQuer);
									for (Integer maxDocPerQuery : maxDocsPerQueries) {
										params.put("maxDocsPerQuery", Integer.toString(maxDocPerQuery));
										for (Integer minDocPerQuery : minDocsPerQueries) {
											params.put("minDocsPerQuery", Integer.toString(minDocPerQuery));

											for (int spl=0; spl < splits.length ; spl++){
												params.put("split",Integer.toString(splits[spl]));
												if (weighted){
													for (Integer positiveQueryLinkWeigh : positiveQueryLinkWeight) {

														params.put("positivequerylinkweight",Integer.toString(positiveQueryLinkWeigh));

														ret.add(new UsefulDocumentCountEstimatorPoolFreeWeighted(randomseed, hgramsQueriesF, startupPathLengt, sampleBurni, sampleSiz, hgramLengt, positiveQueryLinkWeigh, removeOutlier, ds[0], ds[1], maxDocPerQuery, minDocPerQuery, params));

													}
												}else{

													params.put("positivequerylinkweight","1");

													ret.add(new UsefulDocumentCountEstimatorPoolFreeUnweighted(randomseed, hgramsQueriesF, startupPathLengt, sampleBurni, sampleSiz, hgramLengt, removeOutlier, ds[0], ds[1], maxDocPerQuery, minDocPerQuery, params));

												}
											}

										}

									}
									//								}
								}


							}

							//						}
						}
					}

				}

			}

		}

		return ret;

	}

	public static void main(String[] args) {
		System.out.println(UsefulDocumentCountEstimatorBuilder.create("conf/poolBasedAvgTermFactors.conf",null).size());
	}

}
