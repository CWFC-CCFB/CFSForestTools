package quebecmrnfutility.biosim;

import java.io.Serializable;

import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;

public class PlotLocation implements Serializable, GeographicalCoordinatesProvider {
	
	private static final long serialVersionUID = -2185075635699712693L;
	
	private final double elevationM;
	private final double latitude;
	private final double longitude;
	private final String plotID;
	
	public PlotLocation(String plotID, GeographicalCoordinatesProvider geographicalProvider) {
		this.plotID = plotID;
		latitude = geographicalProvider.getLatitudeDeg();
		longitude = geographicalProvider.getLongitudeDeg();
		elevationM = geographicalProvider.getElevationM();
	}

	@Override
	public double getElevationM() {return elevationM;}

	@Override
	public double getLatitudeDeg() {return latitude;}

	@Override
	public double getLongitudeDeg() {return longitude;}

	public String getPlotId() {return plotID;}
	
	@Override
	public String toString() {return latitude + "_" + longitude + "_" + elevationM;}
	
}
