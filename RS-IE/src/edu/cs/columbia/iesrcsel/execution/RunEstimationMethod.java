package edu.cs.columbia.iesrcsel.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimatorBuilder;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.CollisionCounter;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.poolfree.UsefulDocumentCountEstimatorPoolFreeUnweighted;
import edu.cs.columbia.iesrcsel.execution.logger.CollisionLogger;
import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.execution.logger.impl.PrintStreamCollisionLogger;
import edu.cs.columbia.iesrcsel.execution.logger.impl.PrintStreamLogger;
import edu.cs.columbia.iesrcsel.model.CostAwareTextCollection;
import edu.cs.columbia.iesrcsel.model.collection.CrawledLuceneCollection;
import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.extractor.CostAwareInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CrawledInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.DeepWebInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.databaseWriter;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class RunEstimationMethod {

	private static PrintStream outsummary;
	private static PrintStream outStreamDetailed;
	private static PrintStream outCollisions;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// first  arg is task (training, testing, deepweb), next args are conf file. Last is file.

		boolean focusonuseful=false;
		
		System.out.println("Is not focusing on useful. See RunEstimationMethod code.");
		
		String task = args[0];
		
		boolean strata = Boolean.parseBoolean(args[1]);
		
		String relation = args[2]; //"ManMadeDisaster";
		String extractor = args[3]; //"SSK";
		
		String extractorFile = "Sub-sequences";
		
		if (extractor.equals("BONG"))
			extractorFile = "N-Grams";
				
		int firstConfig = 4;
		
		String outcollisions = args[args.length - 2]  + "." + task + "." + relation + "." + extractor + "-collisions.output";
		
		String outstr = args[args.length - 2];
		
		if (!outstr.equals("null")){
			outstr = args[args.length - 2]  + "." + task + "." + relation + "." + extractor + "-detailed.output";
			
		}
		File outsumm = new File(args[args.length - 1] + "." + task + "." + relation + "." + extractor + "-summary.output");
						
		databaseWriter dW = new databaseWriter();
		
		Set<String> broken = new HashSet<String>();
		
		
		boolean deepweb = false;
		
		if (task.equals("training")){
			//training
			//NOT BROKEN ANY MORE broken.add("www.usbr.gov/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.jsu.edu/"); //nairobi
			//IN JAPANESE broken.add("www.chiba-bus-kyokai.or.jp/"); //jakarta
			broken.add("www.chiba-bus-kyokai.or.jp/");
		} else if (task.equals("testing")){

			//testing
			//NOT BROKEN ANY MORE broken.add("www.everythingfurniture.com/"); //nairobi
			//NOT BROKEN ANY MORE broken.add("www.recipesource.com/"); //nairobi
			//NOT BROKEN ANY MORE broken.add("www.journaldugeek.com/"); //cairo
			//NOT BROKEN ANY MORE broken.add("www.tcd.ie/"); //dhaka
			//NOT BROKEN ANY MORE broken.add("www.planet-pets.com/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.csiro.au/"); //suva
			//NOT BROKEN ANY MORE broken.add("www.statefarm.com/"); //dhaka
			//IN CATALAN broken.add("www.campingo.com/"); //yerevan
			//NOT BROKEN ANY MORE broken.add("www.bbc.co.uk/"); //jakarta
			//NOT BROKEN ANY MORE broken.add("www.nndb.com/"); //budapest
			//NOT BROKEN ANY MORE broken.add("www.sco.ca.gov/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.mclink.it/"); //kathmandu
			//NOT BROKEN ANY MORE broken.add("www.eda.admin.ch/"); //budapest
			//NOT BROKEN ANY MORE broken.add("www.hihostels.com/"); //beirut
			//NOT BROKEN ANY MORE broken.add("www.voy.com/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.creditinfocenter.com/"); //tripoli
			//NOT BROKEN ANY MORE broken.add("www.bikingthings.com/"); //budapest
			//NOT BROKEN ANY MORE broken.add("www.p-pokemon.com/"); //yerevan
			//NOT BROKEN ANY MORE broken.add("www.timvp.com/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.torsby.se/"); //budapest
			//NOT BROKEN ANY MORE broken.add("www.devexpress.com/"); //dhaka
			//NOT BROKEN ANY MORE broken.add("www.latimes.com/"); //kathmandu
			//NOT BROKEN ANY MORE broken.add("www.rugsusa.com/"); //kathmandu
			//NOT BROKEN ANY MORE broken.add("www.magnumphotos.com/"); //yerevan
			//NOT BROKEN ANY MORE broken.add("www.habitas.org.uk/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.ncdc.noaa.gov/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.ohloh.net/"); //db-pc04
			//NOT BROKEN ANY MORE broken.add("www.thebody.com/"); //jakarta
			//NOT BROKEN ANY MORE broken.add("www.iisd.ca/"); //kathmandu
			//NOT BROKEN ANY MORE broken.add("www.hawaii.edu/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.wikihow.com/"); //db-pc04
			//NOT BROKEN ANY MORE broken.add("www.whoi.edu/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.classical.net/"); //budapest
			//NOT BROKEN ANY MORE broken.add("www.ohnuts.com/"); //db-pc04
			//NOT BROKEN ANY MORE broken.add("www.cardplayer.com/"); //kathmandu
			//NOT BROKEN ANY MORE broken.add("www.itnsource.com/"); //kathmandu
			//NOT BROKEN ANY MORE broken.add("www.olemiss.edu/"); //db-pc04
			//NOT BROKEN ANY MORE broken.add("www.geekstogo.com/"); //budapest
			//NOT BROKEN ANY MORE broken.add("www.omafra.gov.on.ca/"); //dhaka
			//NOT BROKEN ANY MORE broken.add("www.gamefaqs.com/"); //dhaka
			//NOT BROKEN ANY MORE broken.add("www.antonline.com/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.nber.org/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.elyrics.net/"); //tripoli
			//NOT BROKEN ANY MORE broken.add("www.alltiresupply.com/"); //santiago
			//NOT BROKEN ANY MORE broken.add("www.newcastle.edu.au/"); //kathmandu
			//NOT BROKEN ANY MORE broken.add("www.aikikai.or.jp/"); //santiago
			//NOT BROKEN ANY MORE broken.add("www.worldwidewords.org/"); //nairobi
			//NOT BROKEN ANY MORE broken.add("www.kidsreads.com/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.isi.edu/"); //db-pc04
			//NOT BROKEN ANY MORE broken.add("www.flickr.com/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.esrl.noaa.gov/"); //bucharest
			//NOT BROKEN ANY MORE broken.add("www.theromancereader.com/"); //yerevan

		} else{ //deep web
			deepweb=true;
		}

		Map<String,Integer> usefuls = null;
		Map<String,String> hostwebmap = new HashMap<String, String>();
		Set<String> set = null;
		boolean focusonbroken=false;
		if (deepweb){
			
			//Deep Web databases
			set = new HashSet<String>(FileUtils.readLines(new File("data/ordereddeepwebdatabases.list")));
			usefuls = (Map<String,Integer>) SerializationHelper.deserialize("data/stats/deepweb_useful"+extractorFile+"_"+relation+".ser");
			
			
		}else{
			usefuls = (Map<String,Integer>) SerializationHelper.deserialize("data/stats/"+task+"0.15_1000_useful"+extractorFile+"_"+relation+".ser");
			set = (Set<String>) SerializationHelper.deserialize("data/stats/"+task+"0.15_1000.ser");
			hostwebmap = (Map<String,String>) SerializationHelper.deserialize("data/stats/host_web_map.ser");
		}
		
		Set<String> stratifiedSample = null;
				
		if (strata)
			stratifiedSample = loadStrata(relation);
		
		int topass = broken.size();
		
		for (String website : set) {
			if (focusonuseful && !usefuls.containsKey(website))
				continue;
			if ((focusonbroken && broken.contains(website)) || (!focusonbroken && !broken.contains(website)))
				if (topass == 0 || focusonbroken){

					String url = website.replaceAll("\\p{Punct}", "");

					if (strata && !stratifiedSample.contains(url))
						continue;
					
					for (int i = firstConfig; i < args.length - 2; i++) {

						String confPath = "default";
						
						String file = args[i];
						
						TextCollection tc = null;
						TextCollection tcie = null;
						
						if (deepweb){
							
							String suffix = ""; 
							
							if (file.endsWith("ntf")){
								suffix = "ntf";
							} else if (file.endsWith("nidf")){
								suffix= "nidf";
							} else if (file.endsWith("ntfidf")){
								suffix = "ntfidf";
							}
							
							if (!new File("data/indexes/deepweb/tv-"+url+suffix+".idx").exists()){
								System.out.println("The collection " + url + suffix + "does not exist yet");
								continue;
							}
							
							tc = new DeepWebLuceneCollection(url, "data/indexes/deepweb/tv-"+url+suffix+".idx");
							//same id
							tcie = new DeepWebLuceneCollection(url, "data/indexes/deepweb/tv-"+url+".idx");
						}else{
							tc = new CrawledLuceneCollection(url, hostwebmap.get(website));
						}
						
						int ufuls = (usefuls.get(website) == null ? 0 : usefuls.get(website));
						
						System.err.println(url + "," + hostwebmap.get(website) + "," + ufuls + "," + tc.size());
						
						if (!file.equals("default") && !file.equals("empty")){
							File parameters = new File(file);
							confPath = parameters.getAbsolutePath();
						} else if (file.equals("empty")){
							confPath = "empty";
						}
													
						int startIndex = dW.isRunnable(tc.getId(), confPath, outstr, outsumm.getAbsolutePath());
						
						if (startIndex == databaseWriter.INCONSISTENT_PARAMETERS_FILE){
							try {
								throw new Exception("The parameters file has changed. Consider changing the file to avoid inconsitencies.");
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(1);
							}
							
						}
						
						if (startIndex == databaseWriter.FINISHED_VALUE || startIndex == databaseWriter.NOT_RUNNABLE)
							continue;
						
						InformationExtractionSystem ie = null;
								
						if (deepweb){
							ie = new DeepWebInformationExtractionSystem("ie."+relation+"."+extractor+"."+url, extractor, relation,(DeepWebLuceneCollection)tcie);
						}else{
							ie = new CrawledInformationExtractionSystem("ie."+relation+"."+extractor+"."+tc.getId(), relation, extractor,tc);
						}
						
						List<UsefulDocumentCountEstimator> estimators = UsefulDocumentCountEstimatorBuilder.create(file, ie);

						int currentIndex = -1;
						
						for (UsefulDocumentCountEstimator estimator : estimators) {
							
							currentIndex++;
							
							if (startIndex > currentIndex){
								continue;
							}
							
							long start = System.currentTimeMillis();
							
							performEstimation(estimator,tc,getOutStreamDetailed(outstr, dW),getOutSummary(outsumm.getAbsolutePath(),dW),getOutCollisions(outcollisions,dW), ie, ufuls);

							long time = System.currentTimeMillis() - start;
							
							System.out.println("Elapsed Time:" + estimator.getClass().getSimpleName() + "," + (double) time / 1000.0);
							
							estimator.reset();
							
							try{

								dW.closeConnection();
								
							}catch (Exception e){
								System.err.println("Took too long to finish...");
							}
							
							
							dW = new databaseWriter();
							
							dW.updateEstimationStatus(tc.getId(), confPath, outstr, outsumm.getAbsolutePath(),currentIndex+1);
							
						}
						
						dW.updateEstimationStatus(tc.getId(), confPath, outstr, outsumm.getAbsolutePath(),databaseWriter.FINISHED_VALUE);
						
						tc.close();	
					
						if (deepweb)
							tcie.close();
	
					}
					
				
					
				}
				else
					topass--;
		}

	}

	private static Set<String> loadStrata(String relation) throws IOException {
		
		//Generated with R code.
		
		BufferedReader r = new BufferedReader(new FileReader(new File("sampledstrata-" + relation + ".csv")));
		
		Set<String> ret = new HashSet<String>();
		
		String line;
		
		while ((line = r.readLine()) != null){
			
			ret.add(line.split(",")[0]);
			
		}
		
		r.close();
		
		return ret;
		
	}

	private static PrintStream getOutCollisions(String outstr, databaseWriter dW) throws FileNotFoundException {
		
		if (outCollisions == null){ //It's a singleton

			OutputStream outStream = new NullOutputStream();
			
			if (!outstr.equals("null")){
				
				String fileName = new File(outstr).getAbsolutePath();
				
				int currentLogSplit = dW.getCurrentSplitForFile(fileName);
				
				outStream = new FileOutputStream(new File(fileName + "." + currentLogSplit));
			}
			
			outCollisions= new PrintStream(outStream);
			
		}
		
		return outCollisions;
		
	}
	
	private static PrintStream getOutStreamDetailed(String outstr, databaseWriter dW) throws FileNotFoundException {
		
		if (outStreamDetailed == null){ //It's a singleton

			OutputStream outStream = new NullOutputStream();
			
			if (!outstr.equals("null")){
				
				String fileName = new File(outstr).getAbsolutePath();
				
				int currentLogSplit = dW.getCurrentSplitForFile(fileName);
				
				outStream = new FileOutputStream(new File(fileName + "." + currentLogSplit));
			}
			
			outStreamDetailed = new PrintStream(outStream);
			
		}
		
		return outStreamDetailed;
		
	}

	private static PrintStream getOutSummary(String outsumm, databaseWriter dW) throws FileNotFoundException {
		
		if (outsummary == null){ //It's a singleton
			
			int currentLogSplit = dW.getCurrentSplitForFile(outsumm);
			
			outsummary = new PrintStream(new File(outsumm + "." + currentLogSplit));
		}
		return outsummary;

	}

	private static void performEstimation(UsefulDocumentCountEstimator estimator, TextCollection tc, PrintStream out, PrintStream outfinal, PrintStream outCol, InformationExtractionSystem ie, int ufuls){

		CostLogger cl = new CostLogger(estimator, new PrintStreamLogger(out));

		CostAwareTextCollection ctc = new CostAwareTextCollection(tc,cl);

		CostAwareInformationExtractionSystem cie = new CostAwareInformationExtractionSystem(ie,cl);

		cl.register(ctc);
		cl.register(cie);
		
		CollisionLogger colL = new CollisionLogger(ctc,cie,estimator, ufuls, new PrintStreamCollisionLogger(outCol));
		
		CollisionCounter colC = new CollisionCounter(colL);
		
		System.out.println("Estimation: " + estimator.getNumberOfUsefulDocuments(ctc, cie,colC,cl));
		
		colL.dump();
		
		cl.log(out);
		cl.dump();
		cl.log(outfinal);
		cl.dump(outfinal);
		
		System.out.println(estimator.getParams());
		
		System.out.println(ctc.getCost().toString());

		System.out.println(cie.getCost().toString());
		
		ctc.clearCost();
		cie.clearCost();
		
	}

	private static void testDeepWeb(DeepWebLuceneCollection dwtc, UsefulDocumentCountEstimator estimator, DeepWebInformationExtractionSystem ie, PrintStream out) {

		CostLogger cl = new CostLogger(estimator, new PrintStreamLogger(out));

		CostAwareTextCollection ctc = new CostAwareTextCollection(dwtc,cl);

		CostAwareInformationExtractionSystem cie = new CostAwareInformationExtractionSystem(ie,cl);

		cl.register(ctc);
		cl.register(cie);

		System.out.println("Estimation: " + estimator.getNumberOfUsefulDocuments(ctc, cie, null));

		System.out.println(ctc.getCost().toString());

		System.out.println(cie.getCost().toString());

	}

}
