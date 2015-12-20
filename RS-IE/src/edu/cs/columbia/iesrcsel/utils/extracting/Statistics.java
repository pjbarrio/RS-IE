package edu.cs.columbia.iesrcsel.utils.extracting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class Statistics {

	public static String[] files = {"jakarta-20","pretoria-7","santiago-20","ankara-4","db-pc04-3","havana-2","beirut-9","nairobi-8","nairobi-12",
			"budapest-20","beirut-6","london-20","havana-1","cairo-5","bern-1","ankara-0","baghdad-25","santiago-5",
			"baghdad-8","suva-1","jakarta-22","singapore-5","ankara-6","singapore-10","hanoi-20","pretoria-20","cairo-1",
			"bern-20","bern-10","kathmandu-0","rabat-4","nassau-10","bern-8","jakarta-10","jerusalem-25","nairobi-10","ankara-9",
			"hanoi-13","singapore-3","ankara-2","nairobi-11","bern-5","bucharest-0","baghdad-10","yerevan-1","rabat-6","hanoi-10",
			"ankara-1","amman-5","jakarta-0","nairobi-13","amman-3","ankara-3","kathmandu-1","nassau-20","taipei-7","pretoria-9",
			"kathmandu-9","db-pc04-5","yerevan-3","nassau-11","hanoi-7","ankara-8","bucharest-21","beirut-3","hanoi-12","hanoi-9",
			"beirut-8","santiago-6","jerusalem-20","budapest-3","london-10","ankara-20","baghdad-20","beirut-10","amman-2","db-pc04-0",
			"london-9","dhaka-10","santiago-3","jerusalem-3","cairo-7","dhaka-8","tripoli-9","taipei-9","amman-0","baghdad-21","nassau-5",
			"bern-2","yerevan-5","nassau-12","dhaka-3","pretoria-6","nairobi-9","bucharest-1","bern-12","kathmandu-6","jerusalem-10",
			"dhaka-2","moscow-3","singapore-21","london-7","db-pc04-9","kathmandu-3","rabat-5","cairo-9","jakarta-7","lisbon-7",
			"wellington-20","yerevan-9","wellington-2","bern-0","kathmandu-7","dhaka-20","cairo-20","bucharest-20","hanoi-5",
			"baghdad-7","lisbon-0","amman-7","jakarta-9","singapore-20","beirut-5","lisbon-1","baghdad-0","dhaka-5","baghdad-3",
			"budapest-5","nassau-9","amman-9","beirut-20","bern-11","rabat-3","db-pc04-10","db-pc04-11","baghdad-1","singapore-9",
			"nairobi-21","suva-20","db-pc04-20","tripoli-5","cairo-3","wellington-1","bucharest-7","bern-4","bern-9","cairo-11",
			"nassau-1","beirut-11","kathmandu-2","ankara-5","kathmandu-5","singapore-7","amman-21","ankara-7","lisbon-9","rabat-20",
			"cairo-10","db-pc04-1","amman-20","cairo-8","yerevan-7","hanoi-8","tripoli-1","jakarta-3","nairobi-20","hanoi-11","tripoli-3",
			"baghdad-5","dhaka-0","santiago-4","cairo-4","jakarta-6","kathmandu-8","hanoi-3","kathmandu-4","baghdad-9","cairo-0","rabat-9",
			"nairobi-4","beirut-7","bern-3","dhaka-9","nassau-8","nassau-7","tripoli-0","bern-7","amman-10","lisbon-3","yerevan-0",
			"jakarta-5","yerevan-2","nairobi-5","kathmandu-20","santiago-7","nairobi-3","london-5","bucharest-9","jakarta-1","baghdad-6",
			"london-4","santiago-9","db-pc04-12","amman-1","db-pc04-2","amman-4","london-3","nairobi-7","moscow-7","db-pc04-8",
			"lisbon-20","bern-6","moscow-9","dhaka-1","amman-8","db-pc04-7","budapest-1","nairobi-1","budapest-0","nairobi-0","jakarta-2",
			"nassau-3"};
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args){
		
		List<String> corruptedIndex = new ArrayList<String>();
		List<String> nonExistentIndex = new ArrayList<String>();
		Map<String, Integer> sizes = new HashMap<String,Integer>();
		int zeroes = 0;
		for (int i = 0; i < files.length; i++) {
			
			System.err.println(i + "/" + files.length + " - loading: " + files[i]);
			
			String host = files[i].substring(0, files[i].lastIndexOf('-'));
			
			String split = files[i].substring(files[i].lastIndexOf('-') + 1);
			
			String prefix = "/proj/db-files2/NoBackup/pjbarrio/Dataset/crawl-" + host; 
			
			String path = prefix + "/apache-solr-3.1.0/example/multicore/";
			
			List<String> lines = new ArrayList<String>(0);
			try {
				lines = FileUtils.readLines(new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/crawlSplits/" + files[i]));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for (int j = 0; j < lines.size(); j++) {
				
				String website = lines.get(j).replaceAll("\\p{Punct}", "");
				
				Directory directory = null;
				try {
					directory = FSDirectory.open(new File(path + website + "/data/index/"));
				} catch (IOException e) {
					System.err.println("no directory for: " + website);
				}

				IndexReader indexReader;
				try {
					indexReader = IndexReader.open(directory);
					int size = indexReader.numDocs();
					System.out.println(website + " - " + size);
					sizes.put(lines.get(j), size);
					if (size == 0){
						zeroes++;
					}
					indexReader.close();
					directory.close();
				} catch (CorruptIndexException e) {
					System.err.println("corrupted index: " + website);
					corruptedIndex.add(files[i] + "*" + website);
				} catch (IOException e) {
					System.err.println("index does not exist: " + website);
					nonExistentIndex.add(files[i] + "*" + website);
				}
				
			}
			
		}
	
		System.err.println("Corrupted: " + corruptedIndex.size());
		System.err.println("Non-Existent: " + nonExistentIndex.size());
		System.err.println("Sizes: " + sizes.size());
		System.err.println("Zeroes: " + zeroes);
		
		SerializationHelper.serialize("data/stats/corrupted.ser", corruptedIndex);
		SerializationHelper.serialize("data/stats/nonexistent.ser", nonExistentIndex);
		SerializationHelper.serialize("data/stats/sizes.ser", sizes);
		
	}

}
