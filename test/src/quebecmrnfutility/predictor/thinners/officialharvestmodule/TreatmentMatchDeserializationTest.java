package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector.Mode;
import repicea.serial.MarshallingException;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider.LandUse;
import repicea.util.ObjectUtility;

public class TreatmentMatchDeserializationTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSimpleDeserialization() throws UnmarshallingException, MarshallingException {
		String filename = ObjectUtility.getPackagePath(getClass()) + "treatmentMatch20210215.xml";
		XmlDeserializer deser = new XmlDeserializer(filename);
		OfficialHarvestSubmodelSelector o = (OfficialHarvestSubmodelSelector) deser.readObject();
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "treatmentMatchReference.zml";
		Map<Object, OfficialHarvestTreatmentDefinition> treatmentMatches = o.getMatchesMap(LandUse.WoodProduction);
//		Map<Object, OfficialHarvestTreatmentDefinition> treatmentMatches = o.getMatchMap();
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
	
	
	
	@Test
	public void testSerializedDeserialized() throws UnmarshallingException, MarshallingException {
		String filename = ObjectUtility.getPackagePath(getClass()) + "serializationTest.xml";
		OfficialHarvestSubmodelSelector reference = new OfficialHarvestSubmodelSelector();
		XmlSerializer ser = new XmlSerializer(filename);
		ser.writeObject(reference);
		
		XmlDeserializer deser = new XmlDeserializer(filename);
		OfficialHarvestSubmodelSelector actual = (OfficialHarvestSubmodelSelector) deser.readObject();
		
		Assert.assertEquals("Making sure the mode maps are the same size",
				reference.modes.size(),
				actual.modes.size());
		for (Enum<?> category : reference.modes.keySet()) {
			Mode expectedMode = reference.modes.get(category);
			Mode actualMode = actual.modes.get(category);
			Assert.assertEquals("Making sure the modes are equal for category: " + category.name(),
					expectedMode,
					actualMode);
		}
		
		Assert.assertEquals("Making sure the single treatment maps are the same size",
				reference.singleTreatments.size(),
				actual.singleTreatments.size());
		for (Enum<?> category : reference.singleTreatments.keySet()) {
			OfficialHarvestTreatmentDefinition expectedSingleTreatment = reference.singleTreatments.get(category);
			OfficialHarvestTreatmentDefinition actualSingleTreatment = actual.singleTreatments.get(category);
			Assert.assertEquals("Making sure the modes are equal for category: " + category.name(),
					expectedSingleTreatment,
					actualSingleTreatment);
		}

		Assert.assertEquals("Making sure the area limitation maps are the same size",
				reference.areaLimitations.areaLimitationMap.size(),
				actual.areaLimitations.areaLimitationMap.size());
		for (Enum<?> category : reference.areaLimitations.areaLimitationMap.keySet()) {
			double expectedLimitation = reference.areaLimitations.areaLimitationMap.get(category);
			double actualLimitation = actual.areaLimitations.areaLimitationMap.get(category);
			Assert.assertEquals("Making sure the modes are equal for category: " + category.name(),
					expectedLimitation,
					actualLimitation,
					1E-8);
		}

	}
	
}
