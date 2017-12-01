package quebecmrnfutility.biosim;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.biosim.BioSimClient.BioSimVersion;
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
		BioSimClient client = new BioSimClient(BioSimVersion.VERSION_1971_2000);
		List<PlotLocation> plotLocations = new ArrayList<PlotLocation>();
		plotLocations.add(new PlotLocation("Plot 1", new FakeLocation(300, 46, -74)));
		plotLocations.add(new PlotLocation("Plot 2", new FakeLocation(300, 48, -70)));
		List<ClimateVariables> variables = client.getClimateVariables(plotLocations);
		Assert.assertTrue(variables.get(0).getPlotId().equals("Plot 1"));
		Assert.assertTrue(variables.get(1).getPlotId().equals("Plot 2"));
		client.close();
	}


	
	
}
