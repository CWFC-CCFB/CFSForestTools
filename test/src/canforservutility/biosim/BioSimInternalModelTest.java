package canforservutility.biosim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.biosim.BioSimClientTest.FakeLocation;
import canforservutility.biosim.BioSimEnums.Variable;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.stats.StatisticalUtility;
import repicea.stats.data.DataSet;

public class BioSimInternalModelTest {

	
	@Test
	public void testingLengthOfDatasets() throws IOException, NoSuchMethodException, SecurityException {
		List<GeographicalCoordinatesProvider> locations = new ArrayList<GeographicalCoordinatesProvider>();
		for (int i = 0; i < 1; i++) {
			FakeLocation loc = new FakeLocation(45 + StatisticalUtility.getRandom().nextDouble() * 7,
					-74 + StatisticalUtility.getRandom().nextDouble() * 8,
					300 + StatisticalUtility.getRandom().nextDouble() * 400);
			locations.add(loc);
		}
		List<Variable> var = new ArrayList<Variable>();
		var.add(Variable.TN);
		var.add(Variable.TX);
		var.add(Variable.P);
		boolean failed = false;
		List<String> modelList = BioSimClient.getModelList();
		for (String model : modelList) {
			if (!model.toLowerCase().contains("hourly") && 
					!model.equals("ForestTentCaterpillar") &&
					!model.equals("Insect_Development_Database_II") &&
					!model.equals("Insect_Development_Database_III")) { // TODO fix the ForestTentCaterpillar it seems to produce a Runtime exception from time to time
//				System.out.print("Testing model: " + model);
				long start = System.currentTimeMillis();
				double elapsedTimeSec;
				model = "Insect_Development_Database_II";
				try {
//					if (model.equals("WhitePineWeevil")) {
//						int u = 0;
//					}					
					Map<GeographicalCoordinatesProvider, DataSet> output = BioSimClient.getClimateVariables(2015, 2019, var, locations, model);
					elapsedTimeSec = (System.currentTimeMillis() - start) * .001; 
					for (DataSet ds : output.values()) {
						if (model.equals("EmeraldAshBorer") ||
								model.equals("EuropeanElmScale_param") ||
								model.equals("EuropeanElmScale") ||
								model.equals("FallCankerworms") ||
								model.equals("HWA_Phenology") ||
								model.equals("HemlockLooper") ||
								model.equals("HemlockLooperRemi") ||
								model.equals("Jackpine_Budworm") ||
								model.equals("LaricobiusNigrinus") ||
								model.equals("ObliqueBandedLeafroller") ||
								model.equals("Soil_Temperature") ||
								model.equals("SpringCankerworms") ||
								model.equals("Spruce_Budworm_Biology") ||
								model.equals("Spruce_Budworm_Manitoba") ||
								model.equals("Western_Spruce_Budworm") ||
								model.equals("WhitePineWeevil") ||
								model.toLowerCase().contains("daily")) {
							Assert.assertTrue(model + ": Testing size of DataSet instance", ds.getNumberOfObservations() == 1826);
//							System.out.println(" - Ok " + elapsedTimeSec + " sec.");
						} else if (model.equals("Gypsy_Moth_Seasonality")) {
							Assert.assertTrue(model + ": Testing size of DataSet instance", ds.getNumberOfObservations() == 1096);
//							System.out.println(" - Ok " + elapsedTimeSec + " sec.");
						} else if (model.equals("Standardised_Precipitation_Evapotranspiration_Index_Ex") ||
								model.equals("Standardised_Precipitation_Evapotranspiration_Index") ||
								model.toLowerCase().contains("monthly")) {
							Assert.assertTrue(model + ": Testing size of DataSet instance", ds.getNumberOfObservations() == 60);
//							System.out.println(" - Ok " + elapsedTimeSec + " sec.");
						} else if (model.equals("BlueStainIndex") ||
								model.equals("MPB_SLR") ||
								model.equals("PlantHardiness") ||
								model.equals("ReverseDegreeDay_Overall_years")) {
							Assert.assertTrue(model + ": Testing size of DataSet instance", ds.getNumberOfObservations() == 2);
//							System.out.println(" - Ok " + elapsedTimeSec + " sec.");
						} else if (model.equals("BudBurst") ||
								model.equals("ForestTentCaterpillar") ||
								model.equals("HemlockWoollyAdelgid_Annual") ||
								model.equals("MPB_Cold_Tolerance_Annual") ||
								model.equals("MPBiModel_Annual") ||
								model.equals("Spruce_Budworm_Biology_Annual") ||
								model.equals("SpruceBeetle")) {
							Assert.assertTrue(model + ": Testing size of DataSet instance", ds.getNumberOfObservations() == 3);
//							System.out.println(" - Ok " + elapsedTimeSec + " sec.");
						} else if (model.equals("BlueStainVariables") ||
								model.equals("GrowingSeason") ||
								model.toLowerCase().contains("annual")) {
							Assert.assertTrue(model + ": Testing size of DataSet instance", ds.getNumberOfObservations() == 5);
//							System.out.println(" - Ok " + elapsedTimeSec + " sec.");
						} 
					}
				} catch (Exception e) {
//					e.printStackTrace();
					failed = true;
					System.out.println(model + " - Failed for this reason " + e.toString());
				}
			}
		}
		Assert.assertTrue("No exception thrown", !failed);
	}
	
}
