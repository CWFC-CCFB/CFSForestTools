package quebecmrnfutility.biosim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.biosim.BioSimClient.BioSimVersion;
import quebecmrnfutility.biosim.BioSimClient2.Period;
import quebecmrnfutility.biosim.ClimateVariables.Variable;
import repicea.net.server.BasicClient.BasicClientException;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;

public class BioSimClientTest {

	static class FakeLocation implements GeographicalCoordinatesProvider {

		final double elevationM;
		final double latitudeDeg;
		final double longitudeDeg;
		
		FakeLocation(double elevationM, double latitudeDeg, double longitudeDeg) {
			this.elevationM = elevationM;
			this.latitudeDeg = latitudeDeg;
			this.longitudeDeg = longitudeDeg;
		}
		
		
		@Override
		public double getElevationM() {return elevationM;}

		@Override
		public double getLatitudeDeg() {return latitudeDeg;}

		@Override
		public double getLongitudeDeg() {return longitudeDeg;}

	}

	
	@Test
	public void simpleConnectionTest() throws BasicClientException {
		BioSimClient client = BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
		List<PlotLocation> plotLocations = new ArrayList<PlotLocation>();
		plotLocations.add(new PlotLocation("Plot 1", new FakeLocation(300, 46, -74)));
		plotLocations.add(new PlotLocation("Plot 2", new FakeLocation(300, 48, -70)));
		List<ClimateVariables> variables = client.getClimateVariables(plotLocations);
		Assert.assertTrue(variables.get(0).getPlotId().equals("Plot 1"));
		Assert.assertTrue(variables.get(1).getPlotId().equals("Plot 2"));
		client.close();
	}

	
	@Test
	public void repeatedConnectionTestSameCoordinates() throws BasicClientException {
		for (int i = 0; i < 10; i++) {
			BioSimClient client = BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
		
			List<PlotLocation> plotLocations = new ArrayList<PlotLocation>();
			plotLocations.add(new PlotLocation("Plot 1", new FakeLocation(300, 46, -74)));
			plotLocations.add(new PlotLocation("Plot 2", new FakeLocation(300, 48, -70)));
			List<ClimateVariables> variables = client.getClimateVariables(plotLocations);
			Assert.assertTrue(variables.get(0).getPlotId().equals("Plot 1"));
			Assert.assertTrue(variables.get(1).getPlotId().equals("Plot 2"));
			client.close();
		}
	}

	@Test
	public void repeatedConnectionTestDifferentCoordinates() throws BasicClientException {
		Random randomGen = new Random();
		
		for (int i = 0; i < 10; i++) {
			BioSimClient client = BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);

			List<PlotLocation> plotLocations = new ArrayList<PlotLocation>();
			plotLocations.add(new PlotLocation("Plot 1", new FakeLocation(300 + randomGen.nextDouble(), 46 + randomGen.nextDouble(), -74 + randomGen.nextDouble())));
			plotLocations.add(new PlotLocation("Plot 2", new FakeLocation(300 + randomGen.nextDouble(), 48 + randomGen.nextDouble(), -70 + randomGen.nextDouble())));
			List<ClimateVariables> variables = client.getClimateVariables(plotLocations);
			Assert.assertTrue(variables.get(0).getPlotId().equals("Plot 1"));
			Assert.assertTrue(variables.get(1).getPlotId().equals("Plot 2"));
			client.close();
		}
	}

	@Test
	public void climateRecordingTestWithConnectionDisabled() throws Exception {
		BioSimClient client = BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
		List<PlotLocation> plotLocations = new ArrayList<PlotLocation>();
		plotLocations.add(new PlotLocation("Plot 1", new FakeLocation(300, 46, -74)));
		plotLocations.add(new PlotLocation("Plot 2", new FakeLocation(300, 48, -70)));
		List<ClimateVariables> refVariables	= client.getClimateVariables(plotLocations);
		client.close();
		
		BioSimClient client2 = BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
		client2.byPassConnectionForTesting = true;
		plotLocations = new ArrayList<PlotLocation>();
		plotLocations.add(new PlotLocation("Plot 1", new FakeLocation(300, 46, -74)));
		plotLocations.add(new PlotLocation("Plot 2", new FakeLocation(300, 48, -70)));
		List<ClimateVariables> variables = client2.getClimateVariables(plotLocations);
		client2.close();
		
		Assert.assertTrue("Testing size", refVariables.size() == variables.size());
		for (ClimateVariables cv : refVariables) {
			boolean found = false;
			for (ClimateVariables cv2 : variables) {
				if (cv.getPlotId().equals(cv2.getPlotId())) {
					found = true;
					Assert.assertEquals(cv.getVariable(Variable.MeanAnnualTempC),
							cv2.getVariable(Variable.MeanAnnualTempC),
							1E-8);
					Assert.assertEquals(cv.getVariable(Variable.MeanAnnualPrecMm),
							cv2.getVariable(Variable.MeanAnnualPrecMm),
							1E-8);
					break;
				}
			}
			if (!found) {
				throw new Exception("The plot id could not be found in the received climate variables!");
			}
		}
	}

	@Test
	public void comparisonBioSimOldVsBioSimNew() throws Exception {
		BioSimClient client = BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
		List<PlotLocation> plotLocations = new ArrayList<PlotLocation>();
		plotLocations.add(new PlotLocation("Plot 1", new FakeLocation(300, 46, -74)));
		plotLocations.add(new PlotLocation("Plot 2", new FakeLocation(300, 48, -70)));
		plotLocations.add(new PlotLocation("Plot 3", new FakeLocation(400, 50, -74)));
		long start = System.currentTimeMillis();
		List<ClimateVariables> refVariables	= client.getClimateVariables(plotLocations);
		System.out.println("Original BioSim client = " + ((System.currentTimeMillis() - start) *.001) + " sec.");
		client.close();

		List<BioSimClient2.Variable> variables = new ArrayList<BioSimClient2.Variable>();
		variables.add(BioSimClient2.Variable.TN);
		variables.add(BioSimClient2.Variable.TX);
		variables.add(BioSimClient2.Variable.P);
		
		start = System.currentTimeMillis();
		Map<PlotLocation, Map> output = BioSimClient2.getAnnualNormals(Period.FromNormals1971_2000, variables, plotLocations);
		System.out.println("New BioSim client = " + ((System.currentTimeMillis() - start) *.001) + " sec.");
		for (ClimateVariables var : refVariables) {
			PlotLocation selectedPlot = null;
			for (PlotLocation plotLocation : plotLocations) {
				if (plotLocation.getPlotId().equals(var.getPlotId())) {
					selectedPlot = plotLocation;
					break;
				}
			}
			Map vMap = output.get(selectedPlot);
			double actualMeanTemp = ((Double) vMap.get(BioSimClient2.Variable.TN) + (Double) vMap.get(BioSimClient2.Variable.TX)) * .5;
			double actualMeanPrec = (Double) vMap.get(BioSimClient2.Variable.P);
			double expectedMeanTemp = var.getVariable(Variable.MeanAnnualTempC);
			double expectedMeanPrec = var.getVariable(Variable.MeanAnnualPrecMm);
			Assert.assertEquals("Comparing mean temperature", expectedMeanTemp, actualMeanTemp, .6);
			Assert.assertEquals("Comparing mean precipitation", expectedMeanPrec, actualMeanPrec, 70);
		}
	}
	
	
	
	
}
