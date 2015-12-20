package edu.cs.columbia.iesrcsel.collection.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.cs.columbia.iesrcsel.model.collection.CrawledLuceneCollection;
import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class CollectionLoader {

	private List<TextCollection> collections;
	
	public CollectionLoader(String task) {
		
		collections = new ArrayList<TextCollection>();
		
		boolean deepweb = false;
		
		if (task.equals("deepweb")){
			deepweb=true;
		} 
		
		try{
			Map<String,String> hostwebmap = new HashMap<String, String>();
			Set<String> set = null;
			boolean focusonbroken=false;
			if (deepweb){
				//Deep Web databases
				set = new HashSet<String>(FileUtils.readLines(new File("data/ordereddeepwebdatabases.list")));
								
			}else{
				set = (Set<String>) SerializationHelper.deserialize("data/stats/"+task+"0.15_1000.ser");
				hostwebmap = (Map<String,String>) SerializationHelper.deserialize("data/stats/host_web_map.ser");
			}
			
			for (String website : set) {
				
				String url = website.replaceAll("\\p{Punct}", "");
				
				TextCollection tc;
				
				if (deepweb){
					tc = new DeepWebLuceneCollection(url, "data/indexes/deepweb/tv-"+url+".idx");
				}else{
					tc = new CrawledLuceneCollection(url, hostwebmap.get(website));
				}
				
				collections.add(tc);				
				
			}
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
		
	}
	
	public Iterable<TextCollection> collections() {
		return collections;
	}

}
