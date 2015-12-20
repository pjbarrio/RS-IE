package edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.ParsedDocument.TermExtent;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;

import edu.cs.columbia.iesrcsel.utils.SearchableUtils;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class IndexingNewYorkTimes {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

//		createFiles();

		boolean stemming = true;
		
		System.out.println(stemming);
		
		createIndexes(stemming);

		System.out.println("Done.");
	}

	private static void createIndexes(boolean stemming) throws IOException {

		String suffix = "_stemming";
		
		if (!stemming)
			suffix = "_no" + suffix;
		
		String[] stopWords = FileUtils.readLines(new File("data/stopWords.txt")).toArray(new String[0]);

		Map<String,Set<File>> map = (Map<String,Set<File>>)SerializationHelper.deserialize("data/cleanToIndex.ser");

		TokenizerME tokenizer = new TokenizerME(new TokenizerModel(new File("models/en-token.bin")));

		for (Entry<String,Set<File>> entry : map.entrySet()) {

			String name = generateName(entry.getKey());
			
			System.out.println("Creating: " + name);
			
			if (new File("data/indexes/tr_" + name + "_full"+suffix+".idx").exists())	
				continue;
			
			createIndex("data/indexes/tr_" + name + "_full"+suffix+".idx",entry.getValue() , stopWords,tokenizer,stemming);

		}

	}

	private static void createFiles() {

//		String folder = "/local/pjbarrio/Files/Downloads/NYTValidationSplitPlain/";

		String folder = "C:\\Users\\Pablo\\Downloads\\2002\\";
		
		File[] files = new File(folder).listFiles();

		NYTCorpusDocumentParser parser = new NYTCorpusDocumentParser();

		Set<String> classes = new HashSet<String>();

		Map<String,Integer> freqMap = new HashMap<String,Integer>();

		Map<String,List<File>> filesMap = new HashMap<String, List<File>>();

		for (int i = 0; i < files.length; i++) {

			if (i % 10000 == 0)
				System.out.print(".");

			NYTCorpusDocument document = parser.parseNYTCorpusDocumentFromFile(files[i], false);

			classes.addAll(document.getTaxonomicClassifiers());
			//			
			for (String classi : document.getTaxonomicClassifiers()) {

				String normalizedClass = getName(classi);

				List<File> filesM = filesMap.get(normalizedClass);

				if (filesM == null){

					filesM = new ArrayList<File>();

					filesMap.put(normalizedClass, filesM);

				}

				filesM.add(files[i]);

				Integer freq = freqMap.get(classi);

				if (freq == null){
					freq = 0;
				}

				freqMap.put(classi, freq+1);
			}

		}

		SerializationHelper.serialize("data/toIndex_tr.ser", filesMap);

		SerializationHelper.serialize("data/classes_tr.ser", classes);
		SerializationHelper.serialize("data/classesFreq_tr.ser", freqMap);
		//		
		//		System.out.println(classes.size() + " - " + classes.toString());

	}

	public static String getName(String Class) {

		int lastIndex = 0;

		for (int i = 0; i < 3; i++) {

			if (i == 0 || lastIndex > 0)
				lastIndex = Class.indexOf('/', lastIndex) + 1;

		}

		if (lastIndex > 0)
			return Class.substring(0, lastIndex-1);

		return Class;
	}

	private static void createIndex(String indexName, Set<File> files, String[] stopWords, TokenizerME tokenizer, boolean stemming) {

		System.out.println(indexName);
		
		try {

			IndexEnvironment env = new IndexEnvironment();
			env.setStoreDocs(true);
			env.setStopwords(stopWords);
			if (stemming)
				env.setStemmer("porter");
			env.setIndexedFields(new String[]{"doc"});
			env.setMetadataIndexedFields(new String[]{"doc"}, new String[0]);
			env.create(indexName);

			NYTCorpusDocumentParser np = new NYTCorpusDocumentParser();

			int i = 0;
			
			Set<String> set = new HashSet<String>();
			
			for (File file : files) {

				if (i++ % 1000 == 0)
					System.err.print(".");
				
				ParsedDocument p_doc = createParsedDocument(np,file,tokenizer);
				
				env.addParsedDocument(p_doc);

				set.addAll(Arrays.asList(SearchableUtils.filterSearchableTerms(p_doc.terms, 0)));
				
			}

			System.err.print("\n");
			
			env.close();
		
			Map<String,Integer> index = new HashMap<String,Integer>();
			
			i = 0;
			
			for (String string : set) {
				
				index.put(string, i++);
				
			}
			
			SerializationHelper.serialize(indexName+"_termsId", index);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
	}

	public static ParsedDocument createParsedDocument(NYTCorpusDocumentParser np, File NYTdocFile, TokenizerME tokenizer) throws InvalidFormatException, IOException {

		String name = NYTdocFile.getName();
		
		NYTCorpusDocument NYTdoc = np.parseNYTCorpusDocumentFromFile(NYTdocFile, false);
		
		ParsedDocument doc = new ParsedDocument();

		StringBuilder sb = new StringBuilder();

		if (NYTdoc.getHeadline() != null)
			sb.append(NYTdoc.getHeadline() + "\n");
		
		for (int i = 0; i < NYTdoc.getTitles().size(); i++) {
			
			if (NYTdoc.getTitles().get(i) != null)
				sb.append(NYTdoc.getTitles().get(i)  + "\n" );
		}

		if (NYTdoc.getArticleAbstract() != null)
			sb.append(NYTdoc.getArticleAbstract() + "\n");

//		if (NYTdoc.getLeadParagraph() != null)
//			sb.append(NYTdoc.getLeadParagraph() + "\n");

		if (NYTdoc.getBody() != null)
			sb.append(NYTdoc.getBody());

		doc.content = sb.toString();

		Map<String,String> map = new HashMap<String, String>();

		map.put("doc", name);

		doc.metadata = map;

		doc.text = sb.toString();

		String[] terms = tokenizer.tokenize(sb.toString());

		Span[] spans = tokenizer.tokenizePos(sb.toString());

		spans = SearchableUtils.filterSearchableTerms(terms, 0, spans);
		
		TermExtent[] termsE = new TermExtent[spans.length];

		for (int i = 0; i < spans.length; i++) {

			termsE[i] = new TermExtent(spans[i].getStart(), spans[i].getEnd());

		}

		terms = SearchableUtils.filterSearchableTerms(terms, 0);
		
		doc.terms = terms;

		doc.positions = termsE;

		return doc;
	}

	public static String generateName(String key) {
		return key.replace(" ", "").replace("'", "").replace(".", "").replace("/", "").toLowerCase();
	}

}
