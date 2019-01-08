package quebecmrnf.modelcombination;

import java.util.List;

import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableTree;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperStand;
import quebecmrnfutility.treelogger.sybille.SybilleLoggableTree;
import repicea.simulation.stemtaper.StemTaperCrossSection;

public class FakeTree implements VolumableTree, SybilleLoggableTree {

	
	private final double dbhCm;
	private final VolSpecies volSpecies;
	private final StemTaperTreeSpecies stemTaperSpecies;
	private final double heightM;
	private final StemTaperStand stand;

	FakeTree(StemTaperStand stand, 
			VolSpecies volSpecies, 
			StemTaperTreeSpecies stemTaperSpecies, 
			double dbhCm,
			double heightM) {
		this.stand = stand;
		this.volSpecies = volSpecies;
		this.stemTaperSpecies = stemTaperSpecies;
		this.dbhCm = dbhCm;
		this.heightM = heightM;
	}
	
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getSquaredDbhCm() {return getDbhCm() * getDbhCm();}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public double getCommercialVolumeM3() {
		return 0;
	}

	@Override
	public String getSpeciesName() {return volSpecies.name();}

	@Override
	public StemTaperStand getStand() {return stand;}

	@Override
	public List<StemTaperCrossSection> getCrossSections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StemTaperTreeSpecies getStemTaperTreeSpecies() {return stemTaperSpecies;}

	/*
	 * Useless (non-Javadoc)
	 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getMonteCarloRealizationId()
	 */
	@Override
	public String getSubjectId() {
		return null;
	}

	/*
	 * Useless (non-Javadoc)
	 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getMonteCarloRealizationId()
	 */
	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public VolSpecies getVolumableTreeSpecies() {return volSpecies;}


}
