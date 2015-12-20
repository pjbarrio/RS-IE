package edu.cs.columbia.iesrcsel.utils.extracting;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.extracting.impl.CIMPLEExtractionSystem;


public class NavigateThroughResults {
	
	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, SQLException {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Dataset/crawl-jakarta/"; 
		
		String path = prefix + "/apache-solr-3.1.0/example/multicore/";
		
		String website = "www.bbc.co.uk".replaceAll("\\p{Punct}", "");;
		
		Directory directory = FSDirectory.open(new File(path + website + "/data/index/"));

		IndexReader indexReader = IndexReader.open(directory);

		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		List<Integer> files = ExtractPablosResultsForDatabase.getFiles(indexSearcher);

		for(int i=0; i< files.size(); i++){

				String content = ExtractPablosResultsForDatabase.getContent(indexSearcher,files.get(i));
				
				if (Math.random() > 0.8){
					System.out.println(content);
				}
				
//				System.out.println(content.length());
				
		}

		indexSearcher.close();
		indexReader.close();
		directory.close();
		
	}

}
