package quebecmrnf.volumecomparison;

import java.util.List;

import quebecmrnfutility.predictor.merchantablevolume.VolumableTree;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperStand;
import quebecmrnfutility.treelogger.sybille.SybilleLoggableTree;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.stemtaper.StemTaperCrossSection;

class Tree implements SybilleLoggableTree, VolumableTree {

	final String id;
	double dbhCm;
	double heightM;
	VolSpecies volSpecies;
	StemTaperTreeSpecies taperSpecies;
	final StemTaperStand stand;
	double commercialVolumeM3;
	
	Tree(StemTaperStand stand, 
			String id, 
			double dbhCm, 
			double heightM, 
			VolSpecies volSpecies, 
			StemTaperTreeSpecies taperSpecies) {
		this.stand = stand;
		this.id = id;
		setTreeCharacteristics(dbhCm, heightM, volSpecies, taperSpecies);
	}
	
	void setTreeCharacteristics(double dbhCm, 
			double heightM, 
			VolSpecies volSpecies, 
			StemTaperTreeSpecies taperSpecies) {
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.volSpecies = volSpecies;
		this.taperSpecies = taperSpecies;
	}	
	@Override
	public String getSubjectId() {return id;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	@Override
	public int getMonteCarloRealizationId() {return getStand().getMonteCarloRealizationId();}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getSquaredDbhCm() {return getDbhCm() * getDbhCm();}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public VolSpecies getVolumableTreeSpecies() {return volSpecies;}

	@Override
	public StemTaperStand getStand() {return stand;}

	@Override
	public List<StemTaperCrossSection> getCrossSections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StemTaperTreeSpecies getStemTaperTreeSpecies() {return taperSpecies;}


	@Override
	public double getCommercialVolumeM3() {
		return 0;
	}
	
	void setCommercialVolumeM3(double volumeM3) {
		this.commercialVolumeM3 = volumeM3;
	}

	@Override
	public String getSpeciesName() {
		return getStemTaperTreeSpecies().name();
	}


	@Override
	public double getNumber() {return 1d;}

	@Override
	public String toString() {
		return "Species " + this.getStemTaperTreeSpecies().name() + " dbhcm = " + this.getDbhCm() + " heightm  = " + this.getHeightM(); 
	}
}
