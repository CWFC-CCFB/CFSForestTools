package canforservutility.biosim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.biosim.BioSimEnums.Period;
import canforservutility.biosim.BioSimEnums.Variable;
import quebecmrnfutility.biosim.BioSimClient.BioSimVersion;
import quebecmrnfutility.biosim.PlotLocation;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.stats.StatisticalUtility;
import repicea.stats.data.DataSet;
import repicea.stats.data.Observation;
import repicea.util.ObjectUtility;

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
//			List<quebecmrnfutility.biosim.ClimateVariables> refVariables = client.getClimateVariables(plotLocations);
			double elapsedTimeOriginal = ((System.currentTimeMillis() - start) *.001);
//			System.out.println("Original BioSim client = " + elapsedTimeOriginal + " sec.");
			client.close();

			
			List<BioSimEnums.Variable> variables = new ArrayList<BioSimEnums.Variable>();
			variables.add(BioSimEnums.Variable.TN);
			variables.add(BioSimEnums.Variable.TX);
			variables.add(BioSimEnums.Variable.P);
	
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

	private static void comparisonValuesBioSimOldVsBioSimNew() throws Exception {
		String filename = ObjectUtility.getPackagePath(BioSimClientTest.class) + "comparisonOldVsNew.csv";
		CSVWriter writer = new CSVWriter(new File(filename), false);
		List<FormatField> fields = new ArrayList<FormatField>();	
		fields.add(new CSVField("latDeg"));
		fields.add(new CSVField("longDeg"));
		fields.add(new CSVField("elevM"));
		fields.add(new CSVField("meanTemp_old"));
		fields.add(new CSVField("meanTemp_new"));
		fields.add(new CSVField("meanPrec_old"));
		fields.add(new CSVField("meanPrec_new"));
		writer.setFields(fields);

		Object[] record;

		List<BioSimEnums.Variable> variables = new ArrayList<BioSimEnums.Variable>();
		variables.add(BioSimEnums.Variable.TN);
		variables.add(BioSimEnums.Variable.TX);
		variables.add(BioSimEnums.Variable.P);
		
		
		int nbPlots = 100;
		
		for (int plot = 0; plot < nbPlots; plot++) {
			double latDeg = 46 + StatisticalUtility.getRandom().nextDouble() * 4;
			double longDeg = -74 + StatisticalUtility.getRandom().nextDouble() * 8;
			List<quebecmrnfutility.biosim.PlotLocation> plotLocations = new ArrayList<quebecmrnfutility.biosim.PlotLocation>();
			for (int i = 1; i <= 500; i++) {	// five hundred plots with random elevation
				plotLocations.add(new quebecmrnfutility.biosim.PlotLocation("Plot " + i, new FakeLocation(latDeg,
						longDeg,
						1 + StatisticalUtility.getRandom().nextDouble() * 1000)));
			}

			quebecmrnfutility.biosim.BioSimClient client = quebecmrnfutility.biosim.BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
			long start = System.currentTimeMillis();
			List<quebecmrnfutility.biosim.ClimateVariables> refVariables = client.getClimateVariables(plotLocations);
			double elapsedTimeOriginal = ((System.currentTimeMillis() - start) *.001);
			client.close();

			plotLocations.get(0).setElevationM(Double.NaN);
			start = System.currentTimeMillis();
			Map<PlotLocation, Map> output = BioSimClient.getAnnualNormals(Period.FromNormals1971_2000, variables, (List) plotLocations);
			double elapsedTimeWithoutOverhead = ((System.currentTimeMillis() - start) *.001);
			System.out.println("Original = " + elapsedTimeOriginal + " sec.; New version = " + elapsedTimeWithoutOverhead + " sec.");
			
			PlotLocation nearestPlotLocation = BioSimClientTest.findNearestPlotInAltitude(plotLocations.get(0), output);
			
			quebecmrnfutility.biosim.ClimateVariables formerValues = refVariables.get(plotLocations.indexOf(nearestPlotLocation));
			Map newValues = output.get(nearestPlotLocation);
			record = new Object[7];
			record[0] = nearestPlotLocation.getLatitudeDeg();
			record[1] = nearestPlotLocation.getLongitudeDeg();
			record[2] = nearestPlotLocation.getElevationM();
			record[3] = formerValues.getVariable(quebecmrnfutility.biosim.ClimateVariables.Variable.MeanAnnualTempC);
			record[5] = formerValues.getVariable(quebecmrnfutility.biosim.ClimateVariables.Variable.MeanAnnualPrecMm);
			record[4] = ((Double) newValues.get(BioSimEnums.Variable.TX) + (Double) newValues.get(BioSimEnums.Variable.TN)) * .5;
			record[6] = newValues.get(BioSimEnums.Variable.P);
			writer.addRecord(record);
		}
		writer.close();
	}

	private static PlotLocation findNearestPlotInAltitude(PlotLocation refPlot, Map<PlotLocation, Map> output) {
		Matrix refMatrix = new Matrix(3,1);
		refMatrix.m_afData[0][0] = (Double) output.get(refPlot).get(BioSimEnums.Variable.TX);
		refMatrix.m_afData[1][0] = (Double) output.get(refPlot).get(BioSimEnums.Variable.TN);
	//	refMatrix.m_afData[2][0] = (Double) output.get(refPlot).get(BioSimEnums.Variable.P);
		double sseRef = Double.NaN;
		PlotLocation nearestPlot = null;
		for (PlotLocation location : output.keySet()) {
			if (!location.equals(refPlot)) {
				Matrix mat = new Matrix(3,1);
				mat.m_afData[0][0] = (Double) output.get(location).get(BioSimEnums.Variable.TX);
				mat.m_afData[1][0] = (Double) output.get(location).get(BioSimEnums.Variable.TN);
	//			mat.m_afData[2][0] = (Double) output.get(location).get(BioSimEnums.Variable.P);
				Matrix diff = mat.subtract(refMatrix);
				Matrix sse = diff.transpose().multiply(diff);
				double sseValue = sse.m_afData[0][0];
				if (Double.isNaN(sseRef) || sseValue < sseRef) {
					nearestPlot = location;
					sseRef = sseValue;
				}
			}
		}
		return nearestPlot;
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
		LinkedHashMap<GeographicalCoordinatesProvider, DataSet> teleIORefs = BioSimClient.getClimateVariables(2018, 2019, var, locations, "DegreeDay_Annual");
		nbSecs1 = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs1 + " size = " + teleIORefs.size());

		for (int i = 0; i < 10; i++) {
			initialTime = System.currentTimeMillis();
			LinkedHashMap<GeographicalCoordinatesProvider, DataSet> teleIORefs2 = BioSimClient.getClimateVariables(2018, 2019, var, locations, "DegreeDay_Annual");
			nbSecs2 = (System.currentTimeMillis() - initialTime) * .001;
//			System.out.println("Elapsed time = " + nbSecs2 + " size = " + teleIORefs.size());
			
			Assert.assertTrue(nbSecs1 > (nbSecs2 * 5));
			
			for (GeographicalCoordinatesProvider location : locations) {
				DataSet expectedMap = teleIORefs.get(location);
				DataSet actualMap = teleIORefs2.get(location);
				Assert.assertTrue(expectedMap.getNumberOfObservations() > 0);
				Assert.assertEquals("Testing map size", 
						expectedMap.getNumberOfObservations(), 
						actualMap.getNumberOfObservations());
				for (int j = 0; j < expectedMap.getNumberOfObservations(); j++) {
					Observation expected = expectedMap.getObservations().get(j);
					Observation actual = actualMap.getObservations().get(j);
					Assert.assertTrue("Testing if observations are equal",  
							expected.isEqualToThisObservation(actual));
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
		LinkedHashMap<GeographicalCoordinatesProvider, DataSet> teleIORefs = BioSimClient.getClimateVariables(2000, 2019, var, locations, "DegreeDay_Annual");
		nbSecs1 = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs1 + " size = " + teleIORefs.size());

		for (int i = 0; i < 10; i++) {
			initialTime = System.currentTimeMillis();
			LinkedHashMap<GeographicalCoordinatesProvider, DataSet> teleIORefs2 = BioSimClient.getClimateVariables(2000, 2019, var, locations, "DegreeDay_Annual");
			nbSecs2 = (System.currentTimeMillis() - initialTime) * .001;
//			System.out.println("Elapsed time = " + nbSecs2 + " size = " + teleIORefs.size());
			
			Assert.assertTrue(nbSecs1 > (nbSecs2 * 5));
			
			for (GeographicalCoordinatesProvider location : locations) {
				DataSet expectedMap = teleIORefs.get(location);
				DataSet actualMap = teleIORefs2.get(location);
				Assert.assertTrue(expectedMap.getNumberOfObservations() > 0);
				Assert.assertEquals("Testing map size", 
						expectedMap.getNumberOfObservations(),
						actualMap.getNumberOfObservations());
				for (int j = 0; j < expectedMap.getNumberOfObservations(); j++) {
					Observation expected = expectedMap.getObservations().get(j);
					Observation actual = actualMap.getObservations().get(j);
					Assert.assertTrue("Testing observations",  
								expected.isEqualToThisObservation(actual));
					
				}
			}
		}
	}
	
	@Test
	public void testingMemoryManagementOnServerAfterEphemeralOptionSetToTrue() throws Exception {
		for (int nbRuns = 0; nbRuns < 5; nbRuns++) {
			int nbObjectsBefore = BioSimClient.getNbWgoutObjectsOnServer();
			System.out.println("Nb objects before this function call = " + nbObjectsBefore);
			List<GeographicalCoordinatesProvider> locations = new ArrayList<GeographicalCoordinatesProvider>();
			for (int i = 0; i < 5; i++) {
				FakeLocation loc = new FakeLocation(45 + StatisticalUtility.getRandom().nextDouble() * 7,
						-74 + StatisticalUtility.getRandom().nextDouble() * 8,
						300 + StatisticalUtility.getRandom().nextDouble() * 400);
				locations.add(loc);
			}
			List<Variable> var = new ArrayList<Variable>();
			var.add(Variable.TN);
			var.add(Variable.TX);
			var.add(Variable.P);
			
			BioSimClient.getClimateVariables(2018, 2019, var, locations, "DegreeDay_Annual", true);	// is ephemeral: wgout instances are not stored on the server
			
			int nbObjectsAfter = BioSimClient.getNbWgoutObjectsOnServer();
			Assert.assertEquals("Testing if the number of objects before and after is consistent", nbObjectsBefore, nbObjectsAfter);
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
		BioSimClient.removeWgoutObjectsFromServer(BioSimClient.GeneratedClimateMap.values());
		int nbObjectsAfter = BioSimClient.getNbWgoutObjectsOnServer();
		System.out.println("Nb objects after testing eventual shutdown hook = " + nbObjectsAfter);
		Assert.assertEquals("Testing if the number of objects before and after is consistent", nbObjectsBefore, nbObjectsAfter);
	}

	
	public static void main(String[] args) throws Exception {
		BioSimClientTest.comparisonValuesBioSimOldVsBioSimNew();
	}
}
