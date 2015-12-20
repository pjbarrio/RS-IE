package edu.cs.columbia.iesrcsel.utils.extracting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class WebsiteToDBSplitter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int offset = 9;
		
		String list = "data/websites/websites.list.4";
		
		String computers = "data/websites/computers.training.list";
		
		int size = 8;
		
		List<String> websites = FileUtils.readLines(new File(list));

		List<String> compus = FileUtils.readLines(new File(computers));
		
		Collections.shuffle(compus);
		
		int indexC = 0;
		
		for (int i = 0; i < websites.size(); i++) {
			
			int start = i;
			
			List<String> save = new ArrayList<String>(size);
			
			while (i < websites.size() && i < start + size){
				
				save.add(websites.get(i));
				
				i++;
			
			}
			
			int val = (indexC / compus.size()) + offset;
			
			FileUtils.writeLines(new File("data/websites/splits/" + compus.get(indexC % compus.size()) + "-" + val), save);
			
			indexC++;
			
		}
		
	}

}
