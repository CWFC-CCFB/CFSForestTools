package quebecmrnfutility.biosim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import quebecmrnfutility.biosim.BioSimClient.BioSimVersion;
import quebecmrnfutility.biosim.BioSimClient2.Period;
import quebecmrnfutility.biosim.ClimateVariables.Variable;
import repicea.net.server.BasicClient.BasicClientException;
import repicea.simulation.covariateproviders.plotlevel.GeographicalCoordinatesProvider;
import repicea.stats.StatisticalUtility;

@Deprecated
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

	@Ignore
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


	@Ignore
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

	@Ignore
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

	@Ignore
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

	
	
	
	
}
