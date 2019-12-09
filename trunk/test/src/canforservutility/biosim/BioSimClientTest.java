package canforservutility.biosim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.biosim.BioSimClient.Period;
import canforservutility.biosim.BioSimClient.Variable;
import quebecmrnfutility.biosim.BioSimClient.BioSimVersion;
import quebecmrnfutility.biosim.PlotLocation;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("deprecation")
public class BioSimClientTest {

	private static int nbObjectsBefore;
	
	static {
		try {
			nbObjectsBefore = BioSimClient.getNbWgoutObjectsOnServer();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	static class FakeLocation implements GeographicalCoordinatesProvider {

		private final double elevationM;
		private final double latitude;
		private final double longitude;

		FakeLocation(double latitudeDeg, double longitudeDeg, double elevationM) {
			this.latitude = latitudeDeg;
			this.longitude = longitudeDeg;
			this.elevationM = elevationM;
		}



		@Override
		public double getElevationM() {return elevationM;}

		@Override
		public double getLatitudeDeg() {return latitude;}

		@Override
		public double getLongitudeDeg() {return longitude;}


		@Override
		public String toString() {return latitude + "_" + longitude + "_" + elevationM;}

	}
	
	@Test
	public void comparisonElapsedTimeBioSimOldVsBioSimNew() throws Exception {
		for (int k = 0; k < 10; k++) {
			quebecmrnfutility.biosim.BioSimClient client = quebecmrnfutility.biosim.BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
			List<quebecmrnfutility.biosim.PlotLocation> plotLocations = new ArrayList<quebecmrnfutility.biosim.PlotLocation>();
			for (int i = 1; i <= 30; i++) {
				plotLocations.add(new quebecmrnfutility.biosim.PlotLocation("Plot " + i, new FakeLocation(46 + StatisticalUtility.getRandom().nextDouble() * 4,
						-74 + StatisticalUtility.getRandom().nextDouble() * 8,
						200 + StatisticalUtility.getRandom().nextDouble() * 500)));
			}
			
			long start = System.currentTimeMillis();
			List<quebecmrnfutility.biosim.ClimateVariables> refVariables = client.getClimateVariables(plotLocations);
			double elapsedTimeOriginal = ((System.currentTimeMillis() - start) *.001);
//			System.out.println("Original BioSim client = " + elapsedTimeOriginal + " sec.");
			client.close();

			
			List<BioSimClient.Variable> variables = new ArrayList<BioSimClient.Variable>();
			variables.add(BioSimClient.Variable.TN);
			variables.add(BioSimClient.Variable.TX);
			variables.add(BioSimClient.Variable.P);
	
			if (k==0) {	// make sure that the model list overhead is computed
				BioSimClient.getAnnualNormals(Period.FromNormals1971_2000, variables, (List) plotLocations);
			}
			
			start = System.currentTimeMillis();
			Map<PlotLocation, Map> output = BioSimClient.getAnnualNormals(Period.FromNormals1971_2000, variables, (List) plotLocations);
			double elapsedTimeWithoutOverhead = ((System.currentTimeMillis() - start) *.001);
			System.out.println("Original = " + elapsedTimeOriginal + " sec.; New version = " + elapsedTimeWithoutOverhead + " sec.");

			Assert.assertTrue("Evaluating performance of new client at run " + k, elapsedTimeWithoutOverhead/elapsedTimeOriginal < 1.5);
		}
	}
	
	
	
	/*
	 * Tests if the wgout id is kept in memory for further use instead of generating the wgouts over and
	 * over again.
	 */
	@Test
	public void testingMemorizer() throws IOException {
		List<GeographicalCoordinatesProvider> locations = new ArrayList<GeographicalCoordinatesProvider>();
		for (int i = 0; i < 100; i++) {
			FakeLocation loc = new FakeLocation(45 + StatisticalUtility.getRandom().nextDouble() * 7,
					-74 + StatisticalUtility.getRandom().nextDouble() * 8,
					300 + StatisticalUtility.getRandom().nextDouble() * 400);
			locations.add(loc);
		}
		List<Variable> var = new ArrayList<Variable>();
		var.add(Variable.TN);
		var.add(Variable.TX);
		var.add(Variable.P);
		long initialTime;
		double nbSecs1, nbSecs2;

		initialTime = System.currentTimeMillis();
		LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> teleIORefs = BioSimClient.getClimateVariables(2018, 2019, var, locations, "DegreeDay_Annual");
		nbSecs1 = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs1 + " size = " + teleIORefs.size());

		for (int i = 0; i < 10; i++) {
			initialTime = System.currentTimeMillis();
			LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> teleIORefs2 = BioSimClient.getClimateVariables(2018, 2019, var, locations, "DegreeDay_Annual");
			nbSecs2 = (System.currentTimeMillis() - initialTime) * .001;
//			System.out.println("Elapsed time = " + nbSecs2 + " size = " + teleIORefs.size());
			
			Assert.assertTrue(nbSecs1 > (nbSecs2 * 5));
			
			for (GeographicalCoordinatesProvider location : locations) {
				Map<Integer, Double> expectedMap = teleIORefs.get(location);
				Map<Integer, Double> actualMap = teleIORefs2.get(location);
				Assert.assertTrue(expectedMap.size() > 0);
				Assert.assertEquals("Testing map size",  expectedMap.size(), actualMap.size());
				for (Integer expectedKey : expectedMap.keySet()) {
					double expectedValue = expectedMap.get(expectedKey);
					double actualValue = actualMap.get(expectedKey);
					Assert.assertEquals("Testing value for key: " + expectedKey,  
							expectedValue, 
							actualValue,
							1E-8);
				}
			}
		}
	}

	
	/*
	 * Tests if the weather generation over several contexts.
	 */
	@Test
	public void testingWeatherGenerationOverSeveralContexts() throws IOException {
		List<GeographicalCoordinatesProvider> locations = new ArrayList<GeographicalCoordinatesProvider>();
		for (int i = 0; i < 10; i++) {
			FakeLocation loc = new FakeLocation(45 + StatisticalUtility.getRandom().nextDouble() * 7,
					-74 + StatisticalUtility.getRandom().nextDouble() * 8,
					300 + StatisticalUtility.getRandom().nextDouble() * 400);
			locations.add(loc);
		}
		List<Variable> var = new ArrayList<Variable>();
		var.add(Variable.TN);
		var.add(Variable.TX);
		var.add(Variable.P);
		long initialTime;
		double nbSecs1, nbSecs2;

		initialTime = System.currentTimeMillis();
		LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> teleIORefs = BioSimClient.getClimateVariables(2000, 2019, var, locations, "DegreeDay_Annual");
		nbSecs1 = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs1 + " size = " + teleIORefs.size());

		for (int i = 0; i < 10; i++) {
			initialTime = System.currentTimeMillis();
			LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> teleIORefs2 = BioSimClient.getClimateVariables(2000, 2019, var, locations, "DegreeDay_Annual");
			nbSecs2 = (System.currentTimeMillis() - initialTime) * .001;
//			System.out.println("Elapsed time = " + nbSecs2 + " size = " + teleIORefs.size());
			
			Assert.assertTrue(nbSecs1 > (nbSecs2 * 5));
			
			for (GeographicalCoordinatesProvider location : locations) {
				Map<Integer, Double> expectedMap = teleIORefs.get(location);
				Map<Integer, Double> actualMap = teleIORefs2.get(location);
				Assert.assertTrue(expectedMap.size() > 0);
				Assert.assertEquals("Testing map size",  expectedMap.size(), actualMap.size());
				for (Integer expectedKey : expectedMap.keySet()) {
					double expectedValue = expectedMap.get(expectedKey);
					double actualValue = actualMap.get(expectedKey);
					Assert.assertEquals("Testing value for key: " + expectedKey,  
							expectedValue, 
							actualValue,
							1E-8);
				}
			}
		}
	}
	
	
	@Test
	public void testingMemoryManagementOnServerThroughEventualShutdownHook() throws Exception {
		System.out.println("Nb objects before starting test on shutdown hook = " + nbObjectsBefore);
		List<GeographicalCoordinatesProvider> locations = new ArrayList<GeographicalCoordinatesProvider>();
		for (int i = 0; i < 10; i++) {
			FakeLocation loc = new FakeLocation(45 + StatisticalUtility.getRandom().nextDouble() * 7,
					-74 + StatisticalUtility.getRandom().nextDouble() * 8,
					300 + StatisticalUtility.getRandom().nextDouble() * 400);
			locations.add(loc);
		}
		List<Variable> var = new ArrayList<Variable>();
		var.add(Variable.TN);
		var.add(Variable.TX);
		var.add(Variable.P);
		
		BioSimClient.getClimateVariables(2018, 2019, var, locations, "DegreeDay_Annual");
		
		System.out.println("Nb objects immediately before eventual shutdown hook = " + BioSimClient.getNbWgoutObjectsOnServer());
		System.out.println("Calling eventual shutdown hook...");
		BioSimClient.removeWgoutObjectsFromServer();
		int nbObjectsAfter = BioSimClient.getNbWgoutObjectsOnServer();
		System.out.println("Nb objects after testing eventual shutdown hook = " + nbObjectsAfter);
		Assert.assertEquals("Testing if the number of objects before and after is consistent", nbObjectsBefore, nbObjectsAfter);
	}

	
}
