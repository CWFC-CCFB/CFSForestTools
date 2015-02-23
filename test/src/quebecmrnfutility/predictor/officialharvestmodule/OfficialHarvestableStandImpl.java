package quebecmrnfutility.predictor.officialharvestmodule;

import java.util.ArrayList;
import java.util.Collection;

import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestableStand;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestableTree;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

public class OfficialHarvestableStandImpl implements OfficialHarvestableStand {

	private double numberOfStemsHa;
	private Collection<OfficialHarvestableTree> trees;
	private TreatmentType treatment;
	
	
	/**
	 * Constructor
	 * @param numberOfStemsHa number of stems per hectare
	 */
	public OfficialHarvestableStandImpl(double numberOfStemsHa, String treatment) {
		this.numberOfStemsHa = numberOfStemsHa;
		this.treatment = TreatmentType.valueOf(treatment.toUpperCase().trim());
		trees = new ArrayList<OfficialHarvestableTree>();
	}
	
	protected void addTree(OfficialHarvestableTree tree) {
		trees.add(tree);
	}

	public TreatmentType getTreatment() {return treatment;}
	
	protected Collection<OfficialHarvestableTree> getTrees() {return trees;}
	
	@Override
	public int getSubjectId() {return hashCode();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Plot;}

	@Override
	public void setMonteCarloRealizationId(int i) {}			// no need

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
	
}
