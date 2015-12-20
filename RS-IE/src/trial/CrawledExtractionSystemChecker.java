package trial;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class CrawledExtractionSystemChecker {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		File[] websites = new File("data/extraction/crawl/").listFiles();
		
		System.setErr(new PrintStream("crawledstatus.err"));
		
		for (File file : websites) {
			
			System.out.println(file.getName());
			
			File[] extractionfiles = file.listFiles();
				
			int tot = 0;
			
			for (File file2 : extractionfiles) {
				
				if (tot % 100 == 0)
					System.out.print(".");
				
				try{
					
					Object obj = null;
					
					InputStream fil = new FileInputStream(file2);
					InputStream buffer = new BufferedInputStream( fil );
					ObjectInput input = new ObjectInputStream ( buffer );

					try{

						obj = input.readObject();


					}
					finally{
						input.close();
					}
					
				} catch (Exception e){
					
					System.err.println(file2);
					
				}

				tot++;
				
			}
			
		}

	}

}
