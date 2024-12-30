package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.serial.MarshallingException;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlDeserializer;
import repicea.util.ObjectUtility;

public class TreatmentMatchDeserializationTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSimpleDeserialization() throws UnmarshallingException, MarshallingException {
		String filename = ObjectUtility.getPackagePath(getClass()) + "treatmentMatch20210215.xml";
		XmlDeserializer deser = new XmlDeserializer(filename);
		OfficialHarvestSubmodelSelector o = (OfficialHarvestSubmodelSelector) deser.readObject();
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "treatmentMatchReference.zml";
		Map<Object, OfficialHarvestTreatmentDefinition> treatmentMatches = o.getMatchMap();
		// UNCOMMENT THE FOLLOWING LINES TO UPDATE THE TEST
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(treatmentMatches);
		
		Assert.assertTrue("Making sure the map is not null", treatmentMatches != null);
		deser = new XmlDeserializer(refFilename);
		Map<Object, OfficialHarvestTreatmentDefinition> refMap = (Map) deser.readObject();

		Assert.assertTrue("Making sure the map has the good size", treatmentMatches.size() == refMap.size());

		for (Object key : refMap.keySet()) {
			OfficialHarvestTreatmentDefinition actual = treatmentMatches.get(key);
			OfficialHarvestTreatmentDefinition expected = refMap.get(key);
			Assert.assertEquals("Checking if the observed treatment matches actual treatment for " + key.toString(), expected, actual);
		}
//		o.showUI(null);
	}
}
