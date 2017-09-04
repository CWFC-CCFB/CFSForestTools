package quebecmrnfutility.predictor.officialharvestmodule;

import java.util.ArrayList;
import java.util.Collection;

import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.simulation.HierarchicalLevel;

public class OfficialHarvestableStandImpl implements OfficialHarvestableStand {

	private double numberOfStemsHa;
	private Collection<OfficialHarvestableTree> trees;
	private TreatmentType treatment;
	
	
	/**
	 * Constructor
	 * @param numberOfStemsHa number of stems per hectare
	 */
	protected OfficialHarvestableStandImpl(double numberOfStemsHa, String treatment) {
		this.numberOfStemsHa = numberOfStemsHa;
		this.treatment = TreatmentType.valueOf(treatment.toUpperCase().trim());
		trees = new ArrayList<OfficialHarvestableTree>();
	}
	
	protected void addTree(OfficialHarvestableTree tree) {
		trees.add(tree);
	}

	protected TreatmentType getTreatment() {return treatment;}
	
	protected Collection<OfficialHarvestableTree> getTrees() {return trees;}
	
	@Override
	public String getSubjectId() {return ((Integer) hashCode()).toString();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}


	@Override
	public int getMonteCarloRealizationId() {					// no need
		return 0;
	}

	@Override
	public double getNumberOfStemsHa() {
		return this.numberOfStemsHa;
	}

	@Override
	public double getBasalAreaM2Ha() {		
		return 0;
	}

	/*
	 * Useless for the tests.
	 */
	@Override
	public String getPotentialVegetation() {
		return null;
	}
	
}
