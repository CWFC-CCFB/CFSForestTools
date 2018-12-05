package quebecmrnfutility.treelogger.petrotreelogger;

import repicea.simulation.HierarchicalLevel;

public class PetroLoggableTreeImpl implements PetroLoggableTree {

	final PetroGradeSpecies species;
	final double dbhCm;
	VigorClass vigorClass;
	MSCRPriority mscrPriority;
	ABCDQuality abcdQuality;
	
	
	private PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, VigorClass vigorClass, MSCRPriority mscrPriority, ABCDQuality abcdQuality) {
		this.species = species;
		this.dbhCm = dbhCm;
		this.vigorClass = vigorClass;
		this.abcdQuality = abcdQuality;
		this.mscrPriority = mscrPriority;
	}

	
	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm) {
		this(species, dbhCm, null, null, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, VigorClass vigorClass) {
		this(species, dbhCm, vigorClass, null, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, MSCRPriority mscrPriority) {
		this(species, dbhCm, null, mscrPriority, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, ABCDQuality abcdQuality) {
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
	public PetroGradeSpecies getPetroGradeSpecies() {
		return species;
	}


	@Override
	public String getSubjectId() {
		return "";
	}


	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}


	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}


	@Override
	public double getPlotWeight() {return 1d;}

}
