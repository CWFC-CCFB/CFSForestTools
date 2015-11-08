package quebecmrnfutility.treelogger.sybille;

import java.util.Vector;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperStand;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;
import repicea.simulation.stemtaper.StemTaperCrossSection;

public class LoggableTreeImplTest implements SybilleLoggableTree {

	
	private StemTaperStand stand;
	private StemTaperTreeSpecies species;
	private double dbhmm;
	private double heightm;
	private int monteCarloId;
	private double refVolume;
	
	
	/**
	 * Constructor
	 * @param dbhmm (cm)
	 * @param heightm (m)
	 */
	protected LoggableTreeImplTest(StemTaperStand stand, StemTaperTreeSpecies species, double dbhmm, double heightm, double refVolume) {
		this.stand = stand;
		this.species = species;
		this.dbhmm = dbhmm;
		this.heightm = heightm;
		this.refVolume = refVolume;
	}

	protected LoggableTreeImplTest(StemTaperStand stand, StemTaperTreeSpecies species, double dbhmm, double heightm) {
		this(stand, species, dbhmm, heightm, 0d);
	}

	
	@Override
	public int getSubjectId() {return this.hashCode();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Tree;}

//	@Override
//	public void setMonteCarloRealizationId(int i) {this.monteCarloId = i;}

	@Override
	public StemTaperStand getStand() {return stand;}

	@Override
	public double getDbhCm() {return dbhmm;}

	@Override
	public double getSquaredDbhCm() {return dbhmm * dbhmm;}

	@Override
	public double getHeightM() {return heightm;}

	@Override
	public Vector<StemTaperCrossSection> getCrossSections() {return null;}

	@Override
	public StemTaperTreeSpecies getStemTaperTreeSpecies() {return species;}

	@Override
	public String getSpeciesName() {return species.name();}

	@Override
	public double getNumber() {return 1;}

	@Override
	public TreeStatusPriorToLogging getTreeStatusPriorToLogging() {return TreeStatusPriorToLogging.Alive;}


	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloId;
	}


	@Override
	public double getCommercialVolumeM3() {
		return 0;
	}


	protected double getExpectedVolume() {
		return refVolume;
	}


}
