package quebecmrnfutility.predictor.betahdrelation;

import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

class BetaHeightableTreeImpl implements BetaHeightableTree {

	
	final double dbhCm;
	final BetaHeightableStand stand;
	final int subjectID;
	double heightM;
	final double predictedHeightM;
	
	BetaHeightableTreeImpl(BetaHeightableStandImpl stand,
			double dbhCm,
			double observedHeightM,
			double predictedHeightM,
			int subjectID) {
		this.stand = stand;
		stand.trees.add(this);
		this.dbhCm = dbhCm;
		this.heightM = observedHeightM;
		this.predictedHeightM = predictedHeightM;
		this.subjectID = subjectID;
		//TODO FP rendu ici
	}
	
	
	
	@Override
	public int getSubjectId() {
		return subjectID;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Tree;}

	@Override
	public void setMonteCarloRealizationId(int i) {
		stand.setMonteCarloRealizationId(i);
	}

	@Override
	public int getMonteCarloRealizationId() {
		return stand.getMonteCarloRealizationId();
	}

	@Override
	public double getHeightM() {
		return heightM;
	}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

	@Override
	public double getSquaredLnDbhCmPlus1() {
		double lnDbhCmPlus1 = this.getLnDbhCmPlus1();
		return lnDbhCmPlus1;
	}

	@Override
	public double getBasalAreaLargerThanSubjectM2Ha() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getErrorTermIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BetaHdSpecies getBetaHeightableTreeSpecies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSocialStatusIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

}
