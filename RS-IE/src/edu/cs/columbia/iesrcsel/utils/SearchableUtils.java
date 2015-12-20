package edu.cs.columbia.iesrcsel.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

public class SearchableUtils {

	private static TokenizerME tokenizer;
	private static Set<String> _stopWords;

	public static String[] getSearchableTerms(String text){
		String[] terms = getTokenizer().tokenize(text);
		return filterSearchableTerms(terms, 1);
	}
	
	private static TokenizerME getTokenizer(){
		if (tokenizer == null){
			try {
				tokenizer = new TokenizerME(new TokenizerModel(new File("models/en-token.bin")));
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tokenizer;
	}

	public static String[] filterSearchableTerms(String[] terms, int start) {
		return filterSearchableTerms(terms, start, false);
	}

	public static String convertToSearchableTerm(String term){
		if (_stopWords == null){
			try {
				_stopWords = new HashSet<String>(FileUtils.readLines(new File("data/stopWords.txt")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		term = term.replaceAll("\\p{Punct}+", "").trim();
		
		if (_stopWords.contains(term.toLowerCase()))
			return "";
		
		return term;
	}
	
	public static String[] filterSearchableTerms(String[] terms, int start, boolean lowerCase) {
		if (_stopWords == null){
			try {
				_stopWords = new HashSet<String>(FileUtils.readLines(new File("data/stopWords.txt")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int numberOfSearchableTerms = 0;
		// remove punctuation and discard stopwords
		for (int i = start; i < terms.length; i++) {
			terms[i] = terms[i].replaceAll("\\p{Punct}+", "").trim();
			if (!terms[i].isEmpty()){
				if (_stopWords.contains(terms[i].toLowerCase())) {
					terms[i] = "";
				} else {
					numberOfSearchableTerms++;
				}
			}
		}
		// now just keep the searchable terms in the result
		String[] ret = new String[numberOfSearchableTerms];
		int index = 0;
		for (int i = start; i < terms.length; i++) {
			if (!terms[i].isEmpty()) {
				if (lowerCase) {
					ret[index++] = terms[i].toLowerCase();
				} else {
					ret[index++] = terms[i];
				}
			}
		}
		return ret;
		
	}

	public static Span[] filterSearchableTerms(String[] terms, int start,
			Span[] spans) {
		
		if (_stopWords == null){
			try {
				_stopWords = new HashSet<String>(FileUtils.readLines(new File("data/stopWords.txt")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int size = 0;
		
		for (int i = start; i < terms.length; i++) {
			terms[i] = terms[i].replaceAll("\\p{Punct}+", "").trim();
			if (!terms[i].isEmpty()){
				if (_stopWords.contains(terms[i].toLowerCase()))
					terms[i] = "";
				else
					size++;
			}
				
		}
		
		Span[] ret = new Span[size];
		
		int index = 0;
		
		for (int i = start; i < terms.length; i++) {
			
			if (!terms[i].isEmpty())
				ret[index++] = spans[i];
			
		}
		
		return ret;		
		
	}
	
}
