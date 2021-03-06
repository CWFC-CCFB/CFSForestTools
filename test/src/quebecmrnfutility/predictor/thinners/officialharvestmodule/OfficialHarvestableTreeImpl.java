package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;

public class OfficialHarvestableTreeImpl implements OfficialHarvestableTree {

	private OfficialHarvestableSpecies species;
	private double dbh;
	private double predictedProbabilityFromFile;
	
	protected OfficialHarvestableTreeImpl(String species, double dbh, double predictedProbabilityFromFile) {
		this(OfficialHarvestableSpecies.valueOf(species.toUpperCase().trim()), dbh, predictedProbabilityFromFile);
//		this.species = OfficialHarvestableSpecies.valueOf(species.toUpperCase().trim());
//		this.dbh = dbh;
//		this.predictedProbabilityFromFile = predictedProbabilityFromFile;
	}
	
	protected OfficialHarvestableTreeImpl(OfficialHarvestableSpecies species, double dbh, double predictedProbabilityFromFile) {
		this.species = species;
		this.dbh = dbh;
		this.predictedProbabilityFromFile = predictedProbabilityFromFile;
	}

	protected double getPredictedProbabilityFromFile() {return predictedProbabilityFromFile;}




	@Override
	public double getSquaredDbhCm() {
		return dbh * dbh;
	}




	@Override
	public double getDbhCm() {
		return dbh;
	}

	@Override
	public OfficialHarvestableSpecies getOfficialHarvestableTreeSpecies(TreatmentType treatment) {
		return species;
	}

}
