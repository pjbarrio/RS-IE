package edu.cs.columbia.iesrcsel.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import edu.cs.columbia.iesrcsel.model.impl.Document;

public class HGramGenerator {

	private int itsH;
	
	
	public HGramGenerator(int h){
		itsH = h;

	}
	
	public static void main(String[] args) throws IOException {
		
		Reader reader = new StringReader("This is a test string");
		TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_CURRENT, reader);
		tokenizer = new ShingleFilter(new StopFilter(Version.LUCENE_CURRENT, tokenizer, StandardAnalyzer.STOP_WORDS_SET), 2, 4);
		CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

		while (tokenizer.incrementToken()) {
		    String token = charTermAttribute.toString();
		    System.out.println(token);
		}
		
	}
	
	public Set<String> generateHGrams(Document doc) {

		// TODO remove stop words?
		// TODO remove words containing digits?
		
		if (itsH == 1){ //1-grams are the terms
			
			Set<String> hgrams = new HashSet<String>();
			
			for (String t: doc.getTerms()) {
				if (!t.matches("[0-9]+")) { // remove numbers
					hgrams.add(t);
				}
			}
			
			return hgrams;
			
		}else{
			
			return doc.getHGrams(itsH);
			
		}
		
		

	}

}
