package quebecmrnfutility.predictor.generalhdrelation2014;

import repicea.simulation.REpiceaPredictor.ErrorTermGroup;

class Heightable2014TreeImpl implements Heightable2014Tree {

	
	final double dbhCm;
	final Heightable2014Stand stand;
	final int subjectID;
	double heightM;
	final double predictedHeightM;
	final Hd2014Species speciesCode;
	
	Heightable2014TreeImpl(Heightable2014StandImpl stand,
			double dbhCm,
			double predictedHeightM,
			int subjectID, 
			String species) {
		this.stand = stand;
		stand.trees.add(this);
		this.dbhCm = dbhCm;
		this.predictedHeightM = predictedHeightM;
		this.subjectID = subjectID;
		speciesCode = Hd2014Species.valueOf(species.toUpperCase().trim());
	}
	
	
	
	@Override
	public String getSubjectId() {
		return ((Integer) subjectID).toString();
	}

	@Override
	public int getMonteCarloRealizationId() {
		return stand.getMonteCarloRealizationId();
	}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

	@Override
	public double getSquaredLnDbhCmPlus1() {
		double lnDbhCmPlus1 = this.getLnDbhCmPlus1();
		return lnDbhCmPlus1 * lnDbhCmPlus1;
	}

//	@Override
//	public double getBasalAreaLargerThanSubjectM2Ha() {
//		return 0;
//	}

	@Override
	public int getErrorTermIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Hd2014Species getHeightable2014TreeSpecies() {
		return speciesCode;
	}

//	@Override
//	public double getSocialStatusIndex() {
//		return getDbhCm()/stand.getMeanQuadraticDiameterCm();
//	}

	protected double getPredictedHeight() {return predictedHeightM;}



	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return ErrorTermGroup.Default;
	}
}
