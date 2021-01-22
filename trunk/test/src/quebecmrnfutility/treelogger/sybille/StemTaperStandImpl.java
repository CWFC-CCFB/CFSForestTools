package quebecmrnfutility.treelogger.sybille;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperStand;

public class StemTaperStandImpl implements StemTaperStand {

	private int monteCarloId;
	
	@Override
	public String getSubjectId() {return ((Integer) hashCode()).toString();}

	protected void setMonteCarloRealizationId(int i) {this.monteCarloId = i;}

	@Override
	public String getEcologicalType() {return "MS22";}

	@Override
	public String getEcoRegion() {return "4a";}

	@Override
	public QcDrainageClass getDrainageClass() {return QcDrainageClass.C3;}

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
