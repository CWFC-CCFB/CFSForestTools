package quebecmrnf.volumecomparison;

import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableStand;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperStand;
import repicea.simulation.HierarchicalLevel;

public class Stand implements StemTaperStand, VolumableStand {

	final String id;
	int monteCarloRealizationId;
	final double basalAreaM2Ha;
	final double stemDensity;
	final String ecoRegion;
	final String ecoType;
	boolean isOverride = false;
	
	
	Stand(String id, double basalAreaM2Ha, double stemDensity, String ecoRegion, String ecoType) {
		this.id = id;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.stemDensity = stemDensity;
		this.ecoRegion = ecoRegion;
		this.ecoType = ecoType;
	}
	 
	void setMonteCarloRealizationId(int i) {
		this.monteCarloRealizationId = i;
	}
	
	@Override
	public String getSubjectId() {return id;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloRealizationId;}

	@Override
	public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

	@Override
	public double getNumberOfStemsHa() {return stemDensity;}

	@Override
	public String getEcoRegion() {return ecoRegion;}

	@Override
	public String getEcologicalType() {return ecoType;}

	
	@Override
	public String getDrainageClass() {
		return "";	// TO MAKE SURE THE TREE VERSION IS GOING TO BE USED
	}

	@Override
	public double getElevationM() {return 300;}

	@Override
	public String getCruiseLineID() {return null;}

}
