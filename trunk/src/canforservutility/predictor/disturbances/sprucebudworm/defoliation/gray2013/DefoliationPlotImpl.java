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
	private final double springSumMaxTemp;
	private final double springSumDegreeDays;
	private final double summerSumMinTemp;
	private final double summerSumMaxTemp;
	
	/**
	 * Constructor.
	 */
	public DefoliationPlotImpl(double latitudeDeg, 
			double longitudeDeg, 
			double elevationM, 
			double volM3HaBlackSpruce, 
			double volM3HaFirOtherSpruces, 
			double propForestedArea,
			double springSumMaxTemp,
			double springSumDegreeDays,
			double summerSumMinTemp,
			double summerSumMaxTemp) {
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg;
		this.elevationM = elevationM;
		this.volM3HaBlackSpruce = volM3HaBlackSpruce;
		this.volM3HaFirOtherSpruces = volM3HaFirOtherSpruces;
		this.propForestedArea = propForestedArea;
		this.springSumMaxTemp = springSumMaxTemp;
		this.springSumDegreeDays = springSumDegreeDays;
		this.summerSumMinTemp = summerSumMinTemp;
		this.summerSumMaxTemp = summerSumMaxTemp;
	}
	
	/*
	 * For test purpose only.
	 */
	DefoliationPlotImpl(double latitudeDeg, 
			double longitudeDeg, 
			double elevationM, 
			double volM3HaBlackSpruce, 
			double volM3HaFirOtherSpruces, 
			double propForestedArea) {
		this(latitudeDeg, 
				longitudeDeg, 
				elevationM, 
				volM3HaBlackSpruce, 
				volM3HaFirOtherSpruces, 
				propForestedArea, 
				DefoliationPredictor.getAverageClimateVariables().m_afData[0][1],
				DefoliationPredictor.getAverageClimateVariables().m_afData[0][2],
				DefoliationPredictor.getAverageClimateVariables().m_afData[0][3],
				DefoliationPredictor.getAverageClimateVariables().m_afData[0][4]);
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
	public double getSpringSumMaxTemp() {return springSumMaxTemp;}

	@Override
	public double getSpringSumDegreeDays() {return springSumDegreeDays;}

	@Override
	public double getSummerSumMinTemp() {return summerSumMinTemp;}

	@Override
	public double getSummerSumMaxTemp() {return summerSumMaxTemp;}

}
