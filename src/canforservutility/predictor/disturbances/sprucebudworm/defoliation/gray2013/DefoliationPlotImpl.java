package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

import repicea.simulation.HierarchicalLevel;

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
	private int dateYr;
	
	/**
	 * Constructor.
	 */
	public DefoliationPlotImpl(double latitudeDeg, 
			double longitudeDeg, 
			double elevationM, 
			double volM3HaBlackSpruce, 
			double volM3HaFirOtherSpruces, 
			double propForestedArea,
			int dateYr) {
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg;
		this.elevationM = elevationM;
		this.volM3HaBlackSpruce = volM3HaBlackSpruce;
		this.volM3HaFirOtherSpruces = volM3HaFirOtherSpruces;
		this.propForestedArea = propForestedArea;
		this.dateYr = dateYr;
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

	@Override
	public String getSubjectId() {return "test";}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public int getDateYr() {return dateYr;}


}
