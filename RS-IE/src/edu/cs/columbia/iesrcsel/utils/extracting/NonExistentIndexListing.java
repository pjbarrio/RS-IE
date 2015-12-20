package edu.cs.columbia.iesrcsel.utils.extracting;

import java.util.List;

import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class NonExistentIndexListing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		List<String> nonExistentIndex = (List<String>)SerializationHelper.deserialize("data/stats/nonexistent.ser");
		
		for (String string : nonExistentIndex) {
			
			String[] spl = string.split("\\*");
			
			String host = spl[0].substring(0, spl[0].lastIndexOf('-'));
			
			System.out.println(string);
			
		}
		
		
		
	}

}
