package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;

class StemTaperStandImpl implements StemTaperStand {

	double gHa;
	double stemHa;
	
	protected StemTaperStandImpl(double gHa, double stemHa) {
		this.gHa = gHa;
		this.stemHa = stemHa;
	}
	
		
	@Override
	public String getSubjectId() {
		return ((Integer) hashCode()).toString();
	}

	@Override
	public String getEcologicalType() {
		return "MS2";
	}

	@Override
	public String getEcoRegion() {
		return "3a";
	}

	@Override
	public QcDrainageClass getDrainageClass() {
		return QcDrainageClass.C3;
	}

	@Override
	public double getElevationM() {
		return 300;
	}

	@Override
	public double getNumberOfStemsHa() {
		return stemHa;
	}

	@Override
	public double getBasalAreaM2Ha() {
		return gHa;
	}



	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}

}
