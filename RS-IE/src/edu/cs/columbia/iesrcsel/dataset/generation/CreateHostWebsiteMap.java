package edu.cs.columbia.iesrcsel.dataset.generation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cs.columbia.iesrcsel.utils.extracting.Statistics;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class CreateHostWebsiteMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Set<String> set = (Set<String>) SerializationHelper.deserialize("data/stats/valid_collections1000.ser");
		
		Map<String,String> hostwebmap = new HashMap<String, String>();
		
		int added = 0;
		
		for (int i = 0; i < Statistics.files.length; i++) {
			
			String host = Statistics.files[i].substring(0, Statistics.files[i].lastIndexOf('-'));
			
			String prefix = "/proj/db-files2/NoBackup/pjbarrio/Dataset/crawl-" + host; 
			
			String path = prefix + "/apache-solr-3.1.0/example/multicore/";
			
			List<String> lines = new ArrayList<String>(0);
			try {
				lines = FileUtils.readLines(new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/crawlSplits/" + Statistics.files[i]));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for (int j = 0; j < lines.size(); j++) {
				
				String website = lines.get(j).replaceAll("\\p{Punct}", "");
				
				if (new File(path + website + "/data/index/").exists()){
					String val_website = lines.get(j).endsWith("/") ? lines.get(j) : lines.get(j) + "/";
					
					hostwebmap.put(val_website,host);
					
					if (set.contains(val_website))
						added++;
					
				}
				
			}
			
		}
		
		System.out.println(hostwebmap.toString());
		
		SerializationHelper.serialize("data/stats/host_web_map.ser", hostwebmap);
		
		System.out.println(added);
		
	}

}
