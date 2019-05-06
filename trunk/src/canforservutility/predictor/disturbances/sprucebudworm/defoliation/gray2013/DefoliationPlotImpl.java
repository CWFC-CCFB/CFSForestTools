package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

/**
 * This class is a basic implementation of the DefoliationPlot interface. It can be used
 * for punctual call to the DefoliationPredictor class.
 * @author Mathieu Fortin - May 2019
 */
public class DefoliationPlotImpl implements DefoliationPlot {

	private final double latitudeDeg;
	private final double longitudeDeg;
	private final double elevationM;
	private final double volM3HaBlackSpruce;
	private final double volM3HaFirOtherSpruces;
	private final double propForestedArea;
	
	/**
	 * Constructor.
	 */
	public DefoliationPlotImpl(double latitudeDeg, double longitudeDeg, double elevationM, 
			double volM3HaBlackSpruce, double volM3HaFirOtherSpruces, double propForestedArea) {
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg;
		this.elevationM = elevationM;
		this.volM3HaBlackSpruce = volM3HaBlackSpruce;
		this.volM3HaFirOtherSpruces = volM3HaFirOtherSpruces;
		this.propForestedArea = propForestedArea;
	}
	
	@Override
	public double getLatitudeDeg() {return latitudeDeg;}

	@Override
	public double getLongitudeDeg() {return longitudeDeg;}

	@Override
	public double getElevationM() {return elevationM;}

	@Override
	public double getVolumeM3HaOfBlackSpruce() {return volM3HaBlackSpruce;}

	@Override
	public double getVolumeM3HaOfFirAndOtherSpruces() {return volM3HaFirOtherSpruces;}

	@Override
	public double getProportionForestedArea() {return propForestedArea;}

}
