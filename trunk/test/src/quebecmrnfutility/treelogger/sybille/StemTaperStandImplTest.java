package quebecmrnfutility.treelogger.sybille;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperStand;
import repicea.simulation.HierarchicalLevel;

public class StemTaperStandImplTest implements StemTaperStand {

	private int monteCarloId;
	
	@Override
	public String getSubjectId() {return ((Integer) hashCode()).toString();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

//	@Override
//	public void setMonteCarloRealizationId(int i) {this.monteCarloId = i;}

	@Override
	public String getEcologicalType() {return "MS22";}

	@Override
	public String getEcoRegion() {return "4a";}

	@Override
	public String getDrainageClass() {return "3";}

	@Override
	public double getElevationM() {return 500;}

	@Override
	public double getNumberOfStemsHa() {return 1000;}

	@Override
	public double getBasalAreaM2Ha() {return 20;}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloId;
	}

}
