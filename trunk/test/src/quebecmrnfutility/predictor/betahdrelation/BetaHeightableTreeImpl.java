package quebecmrnfutility.predictor.betahdrelation;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.REpiceaPredictor.ErrorTermGroup;

class BetaHeightableTreeImpl implements BetaHeightableTree {

	
	final double dbhCm;
	final BetaHeightableStand stand;
	final int subjectID;
	double heightM;
	final double predictedHeightM;
	final BetaHdSpecies speciesCode;
	
	BetaHeightableTreeImpl(BetaHeightableStandImpl stand,
			double dbhCm,
			double predictedHeightM,
			int subjectID, 
			String species) {
		this.stand = stand;
		stand.trees.add(this);
		this.dbhCm = dbhCm;
		this.predictedHeightM = predictedHeightM;
		this.subjectID = subjectID;
		speciesCode = BetaHdSpecies.valueOf(species.toUpperCase().trim());
	}
	
	
	
	@Override
	public String getSubjectId() {
		return ((Integer) subjectID).toString();
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

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
	public BetaHdSpecies getBetaHeightableTreeSpecies() {
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
