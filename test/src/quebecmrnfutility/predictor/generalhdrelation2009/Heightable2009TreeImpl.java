package quebecmrnfutility.predictor.generalhdrelation2009;

class Heightable2009TreeImpl implements Heightable2009Tree {

	final double dbhCm;
	final Heightable2009Stand stand;
	final int subjectID;
	double heightM;
	final double predictedHeightM;
	Hd2009Species speciesCode;
	
	Heightable2009TreeImpl(Heightable2009StandImpl stand,
			double dbhCm,
			double predictedHeightM,
			int subjectID, 
			String species,
			double heightM) {
		this.stand = stand;
		stand.trees.add(this);
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.predictedHeightM = predictedHeightM;
		this.subjectID = subjectID;
		String speciesName = species.toUpperCase().trim();
		try {
			speciesCode = Hd2009Species.valueOf(speciesName);
		} catch (IllegalArgumentException e) {
			speciesCode = Hd2009Species.valueOf("FRN");		// default species for species that were modelled in 2014 but not in 2009
		}
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


	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Hd2009Species getHeightableTreeSpecies() {
		return speciesCode;
	}

	@Override
	public double getSocialStatusIndex() {
		return getDbhCm() - ((Heightable2009StandImpl) stand).getMeanQuadraticDiameterCm();
	}

	protected double getPredictedHeight() {return predictedHeightM;}



	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return getHeightableTreeSpecies().getSpeciesType();
	}
}
