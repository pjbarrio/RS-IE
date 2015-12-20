package edu.cs.columbia.iesrcsel.classification.data.generation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lemurproject.indri.ParsedDocument;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.InvalidFormatException;
import edu.berkeley.compbio.jlibsvm.ImmutableSvmParameterPoint;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationProblemImpl;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationSVM;
import edu.berkeley.compbio.jlibsvm.binary.BinaryModel;
import edu.berkeley.compbio.jlibsvm.binary.C_SVC;
import edu.berkeley.compbio.jlibsvm.kernel.GaussianRBFKernel;
import edu.berkeley.compbio.jlibsvm.kernel.LinearKernel;
import edu.berkeley.compbio.jlibsvm.oneclass.OneClassModel;
import edu.berkeley.compbio.jlibsvm.oneclass.OneClassProblemImpl;
import edu.berkeley.compbio.jlibsvm.oneclass.OneClassSVC;
import edu.berkeley.compbio.jlibsvm.regression.RegressionModel;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.SearchableUtils;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.IndexingNewYorkTimes;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.NYTCorpusDocumentParser;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class GenerateClassifiersForProbes {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		//train("hello", null,null);

//		createFromIndex();
	
		createOneClass();
		
	}

	private static void createOneClass() throws InvalidFormatException, IOException {
		
		Map<String,Set<File>> map = (Map<String,Set<File>>)SerializationHelper.deserialize("data/cleanToIndex_tr.ser");
		
		//Load Indexes
		
		Map<String,IndriCollection> indexes = new HashMap<String,IndriCollection>();
		
		for (String entry : map.keySet()) {
			
			indexes.put(entry,new IndriCollection(IndexingNewYorkTimes.generateName(entry), "data/indexes/tr_" + IndexingNewYorkTimes.generateName(entry) + "_full_stemming.idx"));
			
		}
	
		for (Entry<String,IndriCollection> entry : indexes.entrySet()) {
			
			train(entry.getKey(),entry.getValue(), map.get(entry.getKey()));
			
		}
		
		System.out.println("Done.");
		
	}

	private static void train(String directories, IndriCollection index, Set<File> files) throws InvalidFormatException, IOException {
		
		Map<String,Integer> termIds = (Map<String,Integer>)SerializationHelper.deserialize("data/indexes/tr_" + IndexingNewYorkTimes.generateName(directories) + "_full_no_stemming.idx_termsId");
		
		//Create termfreq
		
		Map<Integer,Long> termFreq = new HashMap<Integer,Long>(termIds.size());
		
		for (Entry<String,Integer> entry : termIds.entrySet()) {
			
			long freq = index.getDocumentFrequency(entry.getKey());
			
			termFreq.put(entry.getValue(), freq);
			
		}
		
		double min_freq = 0.003;
		double max_freq = 0.97;
		
		Map<String,Integer> f_termIds = FileterTermsByFreq(termIds,termFreq, min_freq, max_freq, termIds.size());
		
		Map<File,Set<String>> searchableTerms = new HashMap<File,Set<String>>((int)index.size());
		
		System.err.println(index.size());
		
		String folder = "C:\\Users\\Pablo\\Downloads\\2002\\";
		
		int count = 0;
		
		Document d;
		
		for (int i = 1; i <= index.size(); i++) {
			
			if (count++ % 1000 == 0)
				System.out.print(".");
			
			d = new Document(index, i);
			
			searchableTerms.put(new File(folder,index.getPath(d)), index.getTerms(d));
			
		}
		
		System.out.println("\nParsing done...");
		
		Map<File,SparseVector> mapVec = new HashMap<File,SparseVector>();
		
		count = 0;
		
		SparseVector sv;
		
		for (File file : files){
			
			if (count++ % 1000 == 0)
				System.out.print(".");
			
			sv = generateSparseVector(searchableTerms.get(file),f_termIds);
			
			if (sv != null)
				mapVec.put(file, sv);

		}
		
		System.out.println("Vectors done...");
		
		train("data/qprober/model/OneClass" + IndexingNewYorkTimes.generateName(directories), files,null, mapVec);

		SerializationHelper.serialize("data/qprober/queries/termIdsMap"+IndexingNewYorkTimes.generateName(directories)+".ser", f_termIds);
		
	}

	private static void createFromIndex() throws InvalidFormatException, IOException {

		Map<String,Integer> termIds = (Map<String,Integer>)SerializationHelper.deserialize("data/indexes/full_training_no_stemming.idx_termsId");
		
		TextCollection tr_col = new IndriCollection("training", "data/indexes/full_training_stemming.idx");
		
		Map<String,Set<File>> map = (Map<String,Set<File>>)SerializationHelper.deserialize("data/cleanToIndex_tr.ser");

		//Create termfreq
		
		Map<Integer,Long> termFreq = new HashMap<Integer,Long>(termIds.size());
		
		for (Entry<String,Integer> entry : termIds.entrySet()) {
			
			long freq = tr_col.getDocumentFrequency(entry.getKey());
			
			termFreq.put(entry.getValue(), freq);
			
		}
		
		double min_freq = 0.003;
		double max_freq = 0.97;
		
		Map<String,Integer> f_termIds = FileterTermsByFreq(termIds,termFreq, min_freq, max_freq, termIds.size());
		
		Map<File,Set<String>> searchableTerms = new HashMap<File,Set<String>>((int)tr_col.size());
		
		System.err.println(tr_col.size());
		
		String folder = "C:\\Users\\Pablo\\Downloads\\2002\\";
		
		File[] files = new File(folder).listFiles();
				
		int count = 0;
		
		Document d;
		
		for (int i = 1; i <= tr_col.size(); i++) {
			
			if (count++ % 1000 == 0)
				System.out.print(".");
			
			d = new Document(tr_col, i);
			
			searchableTerms.put(new File(folder,tr_col.getPath(d)), tr_col.getTerms(d));
			
		}
		
		System.out.println("\nParsing done...");
		
		Map<File,SparseVector> mapVec = new HashMap<File,SparseVector>();
		
		count = 0;
		
		SparseVector sv;
		
		for (int i = 0; i < files.length; i++){
			
			if (count++ % 1000 == 0)
				System.out.print(".");
			
			sv = generateSparseVector(searchableTerms.get(files[i]),f_termIds);
			
			if (sv != null)
				mapVec.put(files[i], sv);

		}
		
		System.out.println("Vectors done...");
		
		for (Entry<String,Set<File>> pos : map.entrySet()) {

			System.out.println(" Training: " + pos.getKey());

			Set<File> totalNeg = new HashSet<File>();

			for (Entry<String,Set<File>> neg : map.entrySet()) {

				if (!pos.getKey().equals(neg.getKey())){

					totalNeg.addAll(neg.getValue());

				}

			}

			totalNeg.removeAll(pos.getValue());

			train("data/qprober/model/" + IndexingNewYorkTimes.generateName(pos.getKey()), pos.getValue(),totalNeg, mapVec);

		}

		SerializationHelper.serialize("data/qprober/queries/termIdsMap.ser", f_termIds);

	}

	private static SparseVector generateSparseVector(Set<String> strings,
			Map<String, Integer> f_termIds) {
		
		List<Integer> index = new ArrayList<Integer>();		
		
		for (String t : strings) {
			Integer ind = f_termIds.get(t.toLowerCase());
			if (ind != null){
				index.add(ind);
			}
		}
		
		SparseVector sv = new SparseVector(index.size());

		int i = 0;
		
		if (index.size() == 0)
			return null;
		
		for (Integer integer : index) {

			sv.indexes[i] = integer;
			sv.values[i++] = 1.0f;

		}

		return sv;
	
		
	}

	private static Map<String, Integer> FileterTermsByFreq(
			Map<String, Integer> termIds, Map<Integer, Long> termFreq,
			double min_freq, double max_freq, int size) {
		
		Map<String, Integer> ret = new HashMap<String,Integer>();
		
		for (Entry<String,Integer> t_f : termIds.entrySet()) {
			
			double fr = (double)termFreq.get(t_f.getValue()) / (double)size; 
			
			if (fr >= min_freq && fr <= max_freq){
				ret.put(t_f.getKey(), t_f.getValue());
			}
			
		}
		
		return ret;
	}

	private static String[] generateTermsVector(File file,
			NYTCorpusDocumentParser np, TokenizerME tokenizer,
			Map<String, Integer> termIds, Map<Integer, Integer> termFreq) throws InvalidFormatException, IOException{
		
		ParsedDocument p_doc = IndexingNewYorkTimes.createParsedDocument(np, file, tokenizer);

		String[] p_terms = SearchableUtils.filterSearchableTerms(p_doc.terms, 0);
		
		Set<Integer> created = new HashSet<Integer>();

		for (int i = 0; i < p_terms.length; i++) {

			Integer index = termIds.get(p_terms[i].toLowerCase());

			Integer freq;
			
			if (index == null){
				index = termIds.size();
				termIds.put(p_terms[i].toLowerCase(), index);
				freq = 0;
			}else{
				freq = termFreq.get(index);
			}

			if (created.add(index)){
				termFreq.put(index, freq+1);
			}

		}
		
		return p_terms;
	}


	private static void train(String name, Set<File> pos,
			Set<File> neg, Map<File, SparseVector> mapVec) throws InvalidFormatException, IOException {

		Map<SparseVector,Boolean> trainExamples = createExamples(pos,neg,mapVec);

		int i = 0;

		Map<SparseVector,Integer> trainIds = new HashMap<SparseVector,Integer>(trainExamples.size());

		for (SparseVector value : trainExamples.keySet()) {
			trainIds.put(value,i++);
		}

		if (neg != null && !neg.isEmpty()){ //binary classification
		
			BinaryClassificationSVM<Boolean, SparseVector> svm = new C_SVC<Boolean, SparseVector>();

            BinaryClassificationProblemImpl<Boolean, SparseVector> artificialProblem = new BinaryClassificationProblemImpl<Boolean, SparseVector>(Boolean.class, trainExamples, trainIds);

            ImmutableSvmParameterPoint.Builder<Boolean,SparseVector> svmParamBuilder = new ImmutableSvmParameterPoint.Builder<Boolean, SparseVector>();

            svmParamBuilder.kernel = new LinearKernel();

            svmParamBuilder.eps = 0.1f;

            BinaryModel<Boolean, SparseVector> model = svm.train(artificialProblem, svmParamBuilder.build());

            Map<Integer,Double> weights = getWeightVector(model.SVs,model.alphas);

            model.save(name + ".model");
            
            SerializationHelper.serialize(name + "_weights.ser", weights);
			
		} else {//One class
			
			OneClassSVC<Boolean, SparseVector> svm = new OneClassSVC<Boolean, SparseVector>();
			
			//I think the float is good for nothing
			
			Map<SparseVector,Float> trainExamplesFloat = new HashMap<SparseVector,Float>(trainExamples.size());
			
			for (Entry<SparseVector,Boolean> entry : trainExamples.entrySet()) {
				trainExamplesFloat.put(entry.getKey(), 1.0f);
			}
			
			OneClassProblemImpl<Boolean, SparseVector> artificialProblem = new OneClassProblemImpl<Boolean, SparseVector>(trainExamplesFloat, trainIds, true);
			
			ImmutableSvmParameterPoint.Builder<Float,SparseVector> svmParamBuilder = new ImmutableSvmParameterPoint.Builder<Float, SparseVector>();
	
			svmParamBuilder.kernel = new LinearKernel();//new GaussianRBFKernel(1.0f / trainExamples.size());
	
			svmParamBuilder.eps = 0.1f;
	
			RegressionModel<SparseVector> model = svm.train(artificialProblem, svmParamBuilder.build());
	
			Map<Integer,Double> weights = getWeightVector(model.SVs,model.alphas);
	
			model.save(name + ".model");
			
			SerializationHelper.serialize(name + "_weights.ser", weights);
			
		}
		
	}

	private static Map<Integer, Double> getWeightVector(Object[] obj,
			double[] alphas) {

		Map<Integer, Double> weights = new HashMap<Integer,Double>();

		SparseVector[] sVs = new SparseVector[obj.length];

		for (int i = 0; i < sVs.length; i++) {
			sVs[i] = (SparseVector)obj[i];
		}

		for (int i = 0; i < sVs.length; i++) {

			for (int j = 0; j < sVs[i].indexes.length; j++) {

				int index = sVs[i].indexes[j];
				double value = sVs[i].values[j];
				Double wFeature=weights.get(index);
				if(wFeature==null){
					wFeature=0.0;
				}
				wFeature+=alphas[i]*value;
				weights.put(index, wFeature);

			}

		}

		return weights;

	}

	private static Map<SparseVector, Boolean> createExamples(
			Set<File> pos, Set<File> neg, Map<File, SparseVector> mapVec) throws InvalidFormatException, IOException {

		Map<SparseVector,Boolean> ret = new HashMap<SparseVector,Boolean>();

		SparseVector sv;
		
		for (File file : pos) {
			
			sv = mapVec.get(file);
			if (sv!=null)
				ret.put(sv, true);
		}
		
		if (neg != null){
		
			for (File file : neg) {
				sv = mapVec.get(file);
				if (sv!=null)
					ret.put(mapVec.get(file), false);
			}
		}
		
		return ret;

		//		Map<SparseVector, Boolean> ret = new HashMap<SparseVector, Boolean>();
		//		
		//		for (int i = 0; i < 100; i++) {
		//			
		//			ret.put(createMap(),Math.floor(Math.random() * 10) % 2 == 0);
		//			
		//		}
		//		
		//		return ret;
	}



	private static SparseVector createMap() {

		SparseVector ret = new SparseVector(50);

		for (int i = 0; i < 50; i++) {
			ret.indexes[i] = i * (int) Math.floor(Math.random() * 50);
			ret.values[i] = (float)Math.floor(Math.random() * 50);
		}

		return ret;
	}

}
