package quebecmrnfutility.treelogger.petrotreelogger;

public class PetroLoggableTreeImpl implements PetroLoggableTree {

	final PetroGradeSpecies species;
	final double dbhCm;
	QcVigorClass vigorClass;
	QcMarkingPriority mscrPriority;
	QcTreeQuality abcdQuality;
	
	
	private PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcVigorClass vigorClass, QcMarkingPriority mscrPriority, QcTreeQuality abcdQuality) {
		this.species = species;
		this.dbhCm = dbhCm;
		this.vigorClass = vigorClass;
		this.abcdQuality = abcdQuality;
		this.mscrPriority = mscrPriority;
	}

	
	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm) {
		this(species, dbhCm, null, null, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcVigorClass vigorClass) {
		this(species, dbhCm, vigorClass, null, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcMarkingPriority mscrPriority) {
		this(species, dbhCm, null, mscrPriority, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcTreeQuality abcdQuality) {
		this(species, dbhCm, null, null, abcdQuality);
	}
	
	
	@Override
	public double getCommercialVolumeM3() {
		return 1d;
	}

	@Override
	public String getSpeciesName() {
		return getPetroGradeSpecies().toString();
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
	public QcTreeQuality getABCDQuality() {
		return abcdQuality;
	}

	@Override
	public QcMarkingPriority getMSCRPriority() {
		return mscrPriority;
	}

	@Override
	public QcVigorClass getVigorClass() {
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
		return "";
	}

	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}

}
