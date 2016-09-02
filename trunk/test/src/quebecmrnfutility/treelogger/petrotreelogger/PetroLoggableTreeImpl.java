package quebecmrnfutility.treelogger.petrotreelogger;

public class PetroLoggableTreeImpl implements PetroLoggableTree {

	final PetroLoggerSpecies species;
	final double dbhCm;
	VigorClass vigorClass;
	MSCRPriority mscrPriority;
	ABCDQuality abcdQuality;
	
	
	private PetroLoggableTreeImpl(PetroLoggerSpecies species, double dbhCm, VigorClass vigorClass, MSCRPriority mscrPriority, ABCDQuality abcdQuality) {
		this.species = species;
		this.dbhCm = dbhCm;
		this.vigorClass = vigorClass;
		this.abcdQuality = abcdQuality;
		this.mscrPriority = mscrPriority;
	}

	
	PetroLoggableTreeImpl(PetroLoggerSpecies species, double dbhCm) {
		this(species, dbhCm, null, null, null);
	}

	PetroLoggableTreeImpl(PetroLoggerSpecies species, double dbhCm, VigorClass vigorClass) {
		this(species, dbhCm, vigorClass, null, null);
	}

	PetroLoggableTreeImpl(PetroLoggerSpecies species, double dbhCm, MSCRPriority mscrPriority) {
		this(species, dbhCm, null, mscrPriority, null);
	}

	PetroLoggableTreeImpl(PetroLoggerSpecies species, double dbhCm, ABCDQuality abcdQuality) {
		this(species, dbhCm, null, null, abcdQuality);
	}
	
	
	@Override
	public double getCommercialVolumeM3() {
		return 1d;
	}

	@Override
	public String getSpeciesName() {
		return getPetroLoggableTreeSpecies().toString();
	}

	@Override
	public double getNumber() {
		return 1;
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
	public PetroLoggerSpecies getPetroLoggableTreeSpecies() {
		return species;
	}

}
