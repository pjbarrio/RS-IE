package edu.cs.columbia.iesrcsel.utils.extracting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;

import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ExtractionStats {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {

		String task = "testing"; //"training";//

		System.setOut(new PrintStream(new File("data/stats/" + task + "-extraction-stats.csv")));
		
		Set<String> set = (Set<String>) SerializationHelper.deserialize("data/stats/"+task+"0.15_1000.ser");

		int startSplit = 1;

		String[] extractors = {"Sub-sequences", "N-Grams"};

		String[] relations = {"ManMadeDisaster","PersonCareer","NaturalDisaster","VotingResult","Indictment-Arrest-Trial"};

		System.setErr(new PrintStream(new NullOutputStream()));

		for (int k = 0; k < extractors.length; k++) {

			String extractor = extractors[k];

			for (int p = 0; p < relations.length; p++) {

				String relationship = relations[p];

				Map<String,Integer> includeDocs = new HashMap<String, Integer>();

				for (int i = 0; i < Statistics.files.length; i++) {

					List<String> lines = new ArrayList<String>(0);

					try {

						lines = FileUtils.readLines(new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/crawlSplits/" + Statistics.files[i]));

						for (int j = 0; j < lines.size(); j++) {

							String url = lines.get(j).endsWith("/") ? lines.get(j) : lines.get(j) + "/";

							if (!set.contains(url)){
								continue;
							}

							String website = lines.get(j).replaceAll("\\p{Punct}", "");

							String outpref = "data/extraction/crawl/" + website + "/";

							int currentSplit = startSplit;

							String outputFile = outpref + extractor + "_" + relationship + "-" +currentSplit+ ".data";

							int usefulDocs = 0;

							while (new File(outputFile).exists()){

								Map<Integer,List<Tuple>> extractions = (Map<Integer, List<Tuple>>) SerializationHelper.deserialize(outputFile);

								if (extractions != null) {

									for (Entry<Integer,List<Tuple>> tuples : extractions.entrySet()) {

										if (!tuples.getValue().isEmpty())
											usefulDocs++;

									}
								}	
								currentSplit++;

								outputFile = outpref + extractor + "_" + relationship + "-" +currentSplit+ ".data";



							}

							if (usefulDocs > 0){
								System.out.println(relationship + "," + extractor + "," + url + "," + usefulDocs);
								includeDocs.put(url,usefulDocs);
							}
						}



					} catch (IOException e) {
						e.printStackTrace();
					}

					SerializationHelper.serialize("data/stats/"+task+"0.15_1000_useful"+extractor+"_"+relationship+".ser",includeDocs);

				}

			}

		}



	}

}
