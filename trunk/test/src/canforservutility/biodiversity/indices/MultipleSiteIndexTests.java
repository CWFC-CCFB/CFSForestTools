package canforservutility.biodiversity.indices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.biodiversity.indices.MultipleSiteIndex.IndexName;
import repicea.io.javacsv.CSVReader;
import repicea.serial.xml.XmlDeserializer;
import repicea.stats.estimates.SimpleEstimate;
import repicea.util.ObjectUtility;

public class MultipleSiteIndexTests {
	
	private static String RootPath = ObjectUtility.getPackagePath(MultipleSiteIndexTests.class).replace("bin","test" + File.separator + "src");

	@SuppressWarnings("rawtypes")
	private static Map<String, Map<String, List>> STRATA;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, Map<String, List>> readStrata() throws IOException {
		String filename = RootPath + "4_ExportReleves.csv";
		CSVReader reader = new CSVReader(filename);

		Map<String, Map<String, List>> outputMap = new HashMap<String, Map<String, List>>();
		
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			String stratumName = record[0].toString();
			String plotId = record[1].toString();
			String speciesName = record[5].toString();
			if (!outputMap.containsKey(stratumName)) {
				outputMap.put(stratumName, new HashMap<String, List>());
			}
			Map<String, List> innerMap = outputMap.get(stratumName);
			if (!innerMap.containsKey(plotId)) {
				innerMap.put(plotId, new ArrayList());
			}
			innerMap.get(plotId).add(speciesName);
		}
		reader.close();
		return outputMap;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<String, Map<String, List>> getStrata() throws IOException {
		if (STRATA == null) {
			STRATA = readStrata();
		}
		return STRATA;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testDissimilarityIndices() throws IOException {
		MultipleSiteIndex msi = new MultipleSiteIndex();
		Map<String, Map<String, Double>> observedMap = new HashMap<String, Map<String, Double>>();
		for (String key : getStrata().keySet()) {
			if (!observedMap.containsKey(key)) {
				observedMap.put(key, new HashMap<String, Double>());
			}
			Map<String, Double> innerMap = observedMap.get(key);
			Map population = getStrata().get(key);
			Map<IndexName, SimpleEstimate> indices = msi.getDissimilarityIndicesMultiplesiteEstimator(population, 100000, false);
			for (IndexName name : indices.keySet()) {
				innerMap.put(name.name(), indices.get(name).getMean().m_afData[0][0]);
			}
		}

//		UNCOMMENT THIS PART TO UPDATE THE REFERENCE OF THE TEST
//		XmlSerializer serializer = new XmlSerializer(RootPath + "referenceIndices.xml");
//		serializer.writeObject(observedMap);

		XmlDeserializer deserializer = new XmlDeserializer(RootPath + "referenceIndices.xml");
		Map refMap = (Map) deserializer.readObject();
		
		Assert.assertEquals("Testing nb strata", refMap.size(), observedMap.size());
		
		for (Object stratum : refMap.keySet()) {
			Map<String, Double> refInnerMap = (Map) refMap.get(stratum);
			Map<String, Double> obsInnerMap = observedMap.get(stratum);
			Assert.assertEquals("Testing nb indices", refInnerMap.size(), obsInnerMap.size());
			for (String indexName : refInnerMap.keySet()) {
				double expected = refInnerMap.get(indexName);
				double actual = obsInnerMap.get(indexName);
				Assert.assertEquals("Comparing index " + indexName, expected, actual, 1E-8);
			}
		}
	}
}
