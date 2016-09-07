package quebecmrnfutility.predictor.loggradespetro;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;

public class PetroGradeTreeImpl implements PetroGradeTree {

	final PetroGradeSpecies species;
	final double dbhCm;
	VigorClass vigorClass;
	MSCRPriority mscrPriority;
	ABCDQuality abcdQuality;
	int realization;
	private Matrix realizedValues;
	
	
	private PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, VigorClass vigorClass, MSCRPriority mscrPriority, ABCDQuality abcdQuality) {
		this.species = species;
		this.dbhCm = dbhCm;
		this.vigorClass = vigorClass;
		this.abcdQuality = abcdQuality;
		this.mscrPriority = mscrPriority;
		realization = 0;
	}

	void setRealizedValues(Matrix values) {
		this.realizedValues = values;
	}

	Matrix getRealizedValues() {
		return realizedValues;
	}
	
	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm) {
		this(species, dbhCm, null, null, null);
	}

	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, VigorClass vigorClass) {
		this(species, dbhCm, vigorClass, null, null);
	}

	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, MSCRPriority mscrPriority) {
		this(species, dbhCm, null, mscrPriority, null);
	}

	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, ABCDQuality abcdQuality) {
		this(species, dbhCm, null, null, abcdQuality);
	}
	

	@Override
	public double getDbhCm() {
		return dbhCm;
	}

	@Override
	public double getSquaredDbhCm() {
		return getDbhCm() * getDbhCm();
	}

	@Override
	public ABCDQuality getABCDQuality() {
		return abcdQuality;
	}

	@Override
	public MSCRPriority getMSCRPriority() {
		return mscrPriority;
	}

	@Override
	public VigorClass getVigorClass() {
		return vigorClass;
	}

	@Override
	public boolean isModelStochastic() {
		return false;
	}

	@Override
	public double getDbhCmVariance() {
		return 10;
	}

	@Override
	public PetroGradeSpecies getPetroGradeSpecies() {
		return species;
	}


	@Override
	public String getSubjectId() {
		return "test";
	}


	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}


	@Override
	public int getMonteCarloRealizationId() {
		return realization;
	}
	
	protected void setRealization(int i) {
		realization = i;
	}

}
