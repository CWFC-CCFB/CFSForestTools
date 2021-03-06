package quebecmrnfutility.predictor.matapedia;

public class MatapediaTreeImpl implements MatapediaTree {

	private MatapediaTreeSpecies species;
	private double dbh;
	private double bal;
	
	public MatapediaTreeImpl(MatapediaTreeSpecies species, double dbh, double bal) {
		this.species = species;
		this.dbh = dbh;
		this.bal = bal;
	}
	
//	@Override
//	public Object getSubjectPlusMonteCarloSpecificId() {
//		return this;
//	}

	@Override
	public String getSubjectId() {
		return ((Integer) hashCode()).toString();
	}

	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}



	@Override
	public double getDbhCm() {
		return dbh;
	}

	@Override
	public double getSquaredDbhCm() {
		return dbh * dbh;
	}

	@Override
	public MatapediaTreeSpecies getMatapediaTreeSpecies() {
		return species;
	}

	@Override
	public double getBasalAreaLargerThanSubjectM2Ha() {
		return bal;
	}

}
