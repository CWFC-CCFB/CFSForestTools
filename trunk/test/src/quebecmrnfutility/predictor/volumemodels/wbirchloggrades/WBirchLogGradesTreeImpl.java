package quebecmrnfutility.predictor.volumemodels.wbirchloggrades;

import quebecmrnfutility.treelogger.wbirchprodvol.WBirchProdVolLoggableTree;
import repicea.math.Matrix;
import repicea.simulation.species.REpiceaSpecies;

class WBirchLogGradesTreeImpl implements WBirchProdVolLoggableTree {

	private final int treeID;
	private final double dbhCm;
	private QcTreeQuality quality;
	private final WBirchLogGradesStandImpl stand;
	private Matrix predRef;
	private double h20Obs;

	/**
	 * Basic constructor for manuscript on hybrid estimation
	 * @param treeID
	 * @param qualityString
	 * @param dbhCm
	 * @param stand
	 */
	WBirchLogGradesTreeImpl(int treeID, 
			String qualityString, 
			double dbhCm, 
			WBirchLogGradesStandImpl stand) {
		this.treeID = treeID;
		if (qualityString.equals("NC")) {
			quality = null;
		} else {
			quality = QcTreeQuality.valueOf(qualityString);
		}
		this.dbhCm = dbhCm;
		this.stand = stand;
		this.stand.getTrees().put(treeID, this);
	}


	/**
	 * Constructor for test.
	 * @param treeID
	 * @param qualityString
	 * @param dbhCm
	 * @param stand
	 * @param h20Obs
	 * @param realization
	 */
	WBirchLogGradesTreeImpl(int treeID, 
			String qualityString, 
			double dbhCm, 
			WBirchLogGradesStandImpl stand, 
			double h20Obs,
			Matrix realization) {
		this(treeID, qualityString, dbhCm, stand);
		this.h20Obs = h20Obs;

		setRealizedValues(realization);
	}
	
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public QcTreeQuality getTreeQuality() {return quality;}

	@Override
	public String getSubjectId() {return ((Integer) treeID).toString();}

	@Override
	public int getMonteCarloRealizationId() {return stand.getMonteCarloRealizationId();}

	protected Matrix getRealizedValues() {return predRef;}
	
	protected void setRealizedValues(Matrix realization) {this.predRef = realization;} 
	
	public double getH20Obs() {return h20Obs;}


	@Override
	public double getCommercialVolumeM3() {
		return predRef.m_afData[1][0];
	}


	@Override
	public String getSpeciesName() {
		return getWBirchProdVolTreeSpecies().toString();
	}


	@Override
	public WBirchLogGradesStand getStand() {return this.stand;}

	@Override
	public double getBarkProportionOfWoodVolume() {
		return REpiceaSpecies.Species.Betula_spp.getBarkProportionOfWoodVolume();
	}
	
	@Override
	public boolean isCommercialVolumeOverbark() {
		return false;
	}




}
