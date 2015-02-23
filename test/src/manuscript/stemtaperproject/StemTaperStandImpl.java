package manuscript.stemtaperproject;

import quebecmrnfutility.predictor.stemtaper.StemTaperStand;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

public class StemTaperStandImpl implements StemTaperStand {

	int monteCarloID;
		
//	@Override
//	public Object getSubjectPlusMonteCarloSpecificId() {
//		return this.toString() + monteCarloID;
//	}
	
	void setMonteCarloID(int monteCarloID) {
		this.monteCarloID = monteCarloID;
	}

	@Override
	public int getSubjectId() {
		return this.hashCode();
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.Plot;
	}

	@Override
	public void setMonteCarloRealizationId(int i) {
		monteCarloID = i;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloID;
	}

}
