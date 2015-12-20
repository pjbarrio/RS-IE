package edu.cs.columbia.iesrcsel.dataset.generation.utils;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DirectoryTree implements Serializable{

	private static DirectoryTree dummyDirectoryTree = new DirectoryTree();
	
	private Map<String,DirectoryTree> map = new HashMap<String,DirectoryTree>();
	
	
	public void addNode(String site, String[] parents, int index) {
				
		if (index == parents.length){ //last node
			
			map.put(site, dummyDirectoryTree);
			
		}else{ //intermediate node
		
			DirectoryTree dt = map.get(parents[index]);
			
			if (dt == null){
				dt = new DirectoryTree();
				map.put(parents[index], dt);
			}
			
			dt.addNode(site, parents, index+1);
		}
	}

	@Override
	public String toString() {
		
		return getString("");

	}

	private String getString(String prefix) {
		
		StringBuilder sb = new StringBuilder();
		
		for (Entry<String,DirectoryTree> entry : map.entrySet()) {
			
			if (entry.getValue() == dummyDirectoryTree){
				
				sb.append(prefix + "SITE:" + entry.getKey() + "\n");
				
			}else{
				
				sb.append(prefix + "CATEGORY:" + entry.getKey() + "\n");
				sb.append(entry.getValue().getString(prefix+"\t"));

			}
		}
		
		return sb.toString();
		
	}

	public Set<String> getNodes() {
		return map.keySet();
	}

	public DirectoryTree getNode(String node) {
		return map.get(node);
	}
	
}
