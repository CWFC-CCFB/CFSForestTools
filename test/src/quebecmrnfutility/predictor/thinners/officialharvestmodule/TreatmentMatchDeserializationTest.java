package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelectorV2.Mode;
import repicea.serial.MarshallingException;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider.LandUse;
import repicea.util.ObjectUtility;

public class TreatmentMatchDeserializationTest {

	@Test
	public void test01SimpleDeserialization() throws Exception {
		String filename = ObjectUtility.getPackagePath(getClass()) + "treatmentMatch20210215.xml";
		OfficialHarvestSubmodelSelectorV2 selector = new OfficialHarvestSubmodelSelectorV2();
		selector.load(filename);
		
		Assert.assertEquals("Testing treatment for MS4 - Wood production", selector.getMatch(LandUse.WoodProduction, "MS4").getValue(), TreatmentType.CPI_CP_CIMOTF);
		Assert.assertEquals("Testing delay for MS4 - Wood production", selector.getMatch(LandUse.WoodProduction, "MS4").getDelayBeforeReentryYrs(), 0);
		Assert.assertEquals("Testing delay for MS4 - Wood sensitive", selector.getMatch(LandUse.SensitiveWoodProduction, "MS4").getValue(), TreatmentType.PROTECTION);
	}
	
	
	@Test
	public void test02SerializedDeserialized() throws UnmarshallingException, MarshallingException {
		String filename = ObjectUtility.getPackagePath(getClass()) + "serializationTest.xml";
		OfficialHarvestSubmodelSelectorV2 reference = new OfficialHarvestSubmodelSelectorV2();
		XmlSerializer ser = new XmlSerializer(filename);
		ser.writeObject(reference);
		
		XmlDeserializer deser = new XmlDeserializer(filename);
		OfficialHarvestSubmodelSelectorV2 actual = (OfficialHarvestSubmodelSelectorV2) deser.readObject();
		
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
			OfficialHarvestTreatmentDefinitionV2 expectedSingleTreatment = reference.singleTreatments.get(category);
			OfficialHarvestTreatmentDefinitionV2 actualSingleTreatment = actual.singleTreatments.get(category);
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
	
//	@Test
	public static void main(String[] args) throws Exception {
		String filename = ObjectUtility.getPackagePath(TreatmentMatchDeserializationTest.class) + "treatmentMatch20210215.xml";
		OfficialHarvestSubmodelSelectorV2 selector = new OfficialHarvestSubmodelSelectorV2();
		selector.load(filename);
		selector.showUI(null);
		
	}

	
}
