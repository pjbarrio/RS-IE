package edu.cs.columbia.iesrcsel.execution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CombineOutputLogs {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//because summary files are never broken!
		boolean isSummary = Boolean.getBoolean(args[0]); //true
		
		String fileName = args[1]; ///proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/estimations/poolBasedAvgTermFactors.training.output

		String outputFileName = args[2]; ///proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/estimations/combined/poolBasedAvgTermFactors.training.output
		
		int currentSplit = 0;
		
		File f = new File(fileName + "." + currentSplit);
		
		Map<String,List<String>> outputs = new HashMap<String,List<String>>();
		
		List<String> currentList = null;
		
		while (f.exists()){
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			String line=null;
			
			while ((line = br.readLine()) != null){
				
				if (line.contains("estimator.useful.document.count.estimator")){ //is header
					
					currentList = outputs.get(line);
					
					if (currentList == null){
						currentList = new ArrayList<String>();
						outputs.put(line, currentList);
					}
					
				} else if (!line.trim().isEmpty()){ //is Estimation
					
						currentList.add(line);
					
				}
				
			}
			
			br.close();
			
			currentSplit++;
			f = new File(fileName + "." + currentSplit);
		}
		
		int set = 1;
		
		for (Entry<String,List<String>> output : outputs.entrySet()) {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFileName + "-combined." + set + ".csv")));
			
			bw.write(output.getKey());
			bw.newLine();
			
			System.out.println(output.getValue().hashCode());
			
			for (String string : output.getValue()) {
				
				bw.write(string);
				bw.newLine();
				
			}
		
			bw.close();
			
			set++;
			
		}
		
	}

}
