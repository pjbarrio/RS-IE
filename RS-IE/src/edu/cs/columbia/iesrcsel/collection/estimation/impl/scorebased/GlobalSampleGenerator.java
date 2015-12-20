package edu.cs.columbia.iesrcsel.collection.estimation.impl.scorebased;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import edu.cs.columbia.iesrcsel.collection.loader.CollectionLoader;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGeneratorBuilder;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGeneratorEnum;

public class GlobalSampleGenerator {

	/**
	 * @param args
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ConfigurationException {
		
		String task = "deepweb";
		
		String sampleGenerator = "QBSSampleGenerator";
		
		String sgConfigFile = "conf/sigir/sampleGenerator-ResSel.conf";
		
		Parameters params = new Parameters();

		FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class).configure(params.properties().setFileName(sgConfigFile));

		Configuration config = builder.getConfiguration();
	
		List<SampleGenerator> sgs = SampleGeneratorBuilder.create(SampleGeneratorEnum.valueOf(sampleGenerator), config, null);
		
		int index = 0;
		
		for (SampleGenerator sg : sgs) {
			
			CollectionLoader colLoad = new CollectionLoader(task);

			List<Sample> samples = new ArrayList<Sample>();
			
			for (TextCollection tc : colLoad.collections()) {

				samples.add(sg.generateSample(tc));
				
				tc.close();
				
			}

			
			
			index++;
		}
		
		
	}

}
