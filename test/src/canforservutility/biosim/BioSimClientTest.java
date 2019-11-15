package canforservutility.biosim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import canforservutility.biosim.BioSimClient.Period;
import canforservutility.biosim.BioSimClient.PlotLocation;
import quebecmrnfutility.biosim.BioSimClient.BioSimVersion;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("deprecation")
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
	public void comparisonBioSimOldVsBioSimNew() throws Exception {
		quebecmrnfutility.biosim.BioSimClient client = quebecmrnfutility.biosim.BioSimClient.getBioSimClient(BioSimVersion.VERSION_1971_2000);
		List<quebecmrnfutility.biosim.PlotLocation> plotLocations = new ArrayList<quebecmrnfutility.biosim.PlotLocation>();
		for (int i = 1; i <= 30; i++) {
			plotLocations.add(new quebecmrnfutility.biosim.PlotLocation("Plot " + i, new FakeLocation(200 + StatisticalUtility.getRandom().nextDouble() * 500, 
					46 + StatisticalUtility.getRandom().nextDouble() * 4,
					-74 + StatisticalUtility.getRandom().nextDouble() * 8)));
		}
		
		long start = System.currentTimeMillis();
		List<quebecmrnfutility.biosim.ClimateVariables> refVariables = client.getClimateVariables(plotLocations);
		System.out.println("Original BioSim client = " + ((System.currentTimeMillis() - start) *.001) + " sec.");
		client.close();

		
		List<BioSimClient.Variable> variables = new ArrayList<BioSimClient.Variable>();
		variables.add(BioSimClient.Variable.TN);
		variables.add(BioSimClient.Variable.TX);
		variables.add(BioSimClient.Variable.P);
		
		start = System.currentTimeMillis();
		Map<PlotLocation, Map> output = BioSimClient.getAnnualNormals(Period.FromNormals1971_2000, variables, (List) plotLocations);
		System.out.println("New BioSim client = " + ((System.currentTimeMillis() - start) *.001) + " sec.");
		int u = 0;
	}
	
	
	
	
}
