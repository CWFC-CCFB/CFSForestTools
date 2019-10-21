package canforservutility.biodiversity.indices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.biodiversity.indices.DiversityIndices.DiversityIndex;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.serial.xml.XmlDeserializer;
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
		Map<String, Map<String, Matrix>> observedMap = new HashMap<String, Map<String, Matrix>>();
		for (String key : getStrata().keySet()) {
			if (!observedMap.containsKey(key)) {
				observedMap.put(key, new HashMap<String, Matrix>());
			}
			Map<String, Matrix> innerMap = observedMap.get(key);
			Map population = getStrata().get(key);
			DiversityIndicesEstimates indices = msi.getDissimilarityIndicesMultiplesiteEstimator(population, 100000);
			for (DiversityIndex name : DiversityIndex.values()) {
				innerMap.put(name.name(), indices.getEstimate(name).getMean());
			}
		}

//		UNCOMMENT THIS PART TO UPDATE THE REFERENCE OF THE TEST
//		XmlSerializer serializer = new XmlSerializer(RootPath + "referenceIndices.xml");
//		serializer.writeObject(observedMap);

		XmlDeserializer deserializer = new XmlDeserializer(RootPath + "referenceIndices.xml");
		Map refMap = (Map) deserializer.readObject();
		
		Assert.assertEquals("Testing nb strata", refMap.size(), observedMap.size());
		
		for (Object stratum : refMap.keySet()) {
			Map<String, Matrix> refInnerMap = (Map) refMap.get(stratum);
			Map<String, Matrix> obsInnerMap = observedMap.get(stratum);
			for (String indexName : refInnerMap.keySet()) {
				Matrix expected = refInnerMap.get(indexName);
				Matrix actual = obsInnerMap.get(indexName);
				Assert.assertTrue("Comparing index " + indexName, expected.equals(actual));
			}
		}
	}
}
