package quebecmrnfutility.volumecomparison;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import quebecmrnfutility.predictor.volumemodels.merchantablevolume.MerchantableVolumePredictor;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableTree.VolSpecies;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import quebecmrnfutility.treelogger.sybille.SybilleTreeLogCategory;
import quebecmrnfutility.treelogger.sybille.SybilleTreeLogCategory.LengthID;
import quebecmrnfutility.treelogger.sybille.SybilleTreeLogger;
import quebecmrnfutility.treelogger.sybille.SybilleTreeLoggerParameters;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.WoodPiece;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class ComparisonPredictions {

	final Stand s;
	final Tree t;
	final MerchantableVolumePredictor pred;
	final SybilleTreeLogger loggerTree;
	final Collection<Tree> trees;
	final CSVWriter writer;
	
	ComparisonPredictions() throws IOException {
		writer = new CSVWriter(new File(ObjectUtility.getPackagePath(getClass()) + "output.csv"), false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("species"));
		fields.add(new CSVField("dbhCm"));
		fields.add(new CSVField("heightM"));
		fields.add(new CSVField("meanVol"));
		fields.add(new CSVField("varVol"));
		fields.add(new CSVField("meanTaper"));
		fields.add(new CSVField("varTaper"));
		writer.setFields(fields);
		
		s = new Stand("testStand", 20, 600, "5a", "RS22");
		t = new Tree(s, "testTree", 20, 15, VolSpecies.SAB, StemTaperTreeSpecies.SAB);
		pred = new MerchantableVolumePredictor(true);
		
		loggerTree = new SybilleTreeLogger(true);
		SybilleTreeLoggerParameters parms = loggerTree.createDefaultTreeLoggerParameters();
		for (Object species : parms.getLogCategories().keySet()) {
			List<SybilleTreeLogCategory> logCategory = parms.getLogCategories().get(species);
			logCategory.clear();
			logCategory.add(new SybilleTreeLogCategory("CommVol", species.toString(), LengthID.NoLimit, 9d));
		}
		loggerTree.setTreeLoggerParameters(parms);
		trees = new ArrayList<Tree>();
		trees.add(t);
	}
	
	
	void predictVolumeForThisTree() throws IOException {
		MonteCarloEstimate estimateVol = new MonteCarloEstimate();
		MonteCarloEstimate estimateTaperTree = new MonteCarloEstimate();
		
		Matrix vol;
		for (int i = 0; i < 1000; i++) {
			s.setMonteCarloRealizationId(i);
			vol = new Matrix(1,1);
			vol.m_afData[0][0] = pred.predictTreeCommercialUnderbarkVolumeDm3(s, t) * .001;
			estimateVol.addRealization(vol);
			
			loggerTree.init(trees);
			s.isOverride = true;
			loggerTree.run();
			estimateTaperTree.addRealization(getVolumeFromLogger(loggerTree, t));
			s.isOverride = false;
		}

		double meanVol = estimateVol.getMean().m_afData[0][0];
		double varVol = estimateVol.getVariance().m_afData[0][0];
		
		double meanTaperTree = estimateTaperTree.getMean().m_afData[0][0];
		double varTaperTree = estimateTaperTree.getVariance().m_afData[0][0];

		Object[] record = new Object[7];
		record[0] = t.getStemTaperTreeSpecies().name();
		record[1] = t.dbhCm;
		record[2] = t.heightM;
		record[3] = meanVol;
		record[4] = varVol;
		record[5] = meanTaperTree;
		record[6] = varTaperTree;
		writer.addRecord(record);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Matrix getVolumeFromLogger(TreeLogger logger, Tree t) {
		double loggerVolume = 0d;
		Collection<WoodPiece> woodPieceMap = (Collection) logger.getWoodPieces().get(t);
		if (woodPieceMap != null) {
			for (WoodPiece woodPiece : woodPieceMap) {
				loggerVolume += woodPiece.getWeightedWoodVolumeM3();
			}
		}
		// TODO utiliser la variance du tarif de cubage et verfier la variance de l'erreur residuelle dans le modele de defilement
		Matrix taper = new Matrix(1,1);
		taper.m_afData[0][0] = loggerVolume;
		return taper;
	}
	
	void predictTheseTrees() throws IOException {
//		List<StemTaperTreeSpecies> speciesList = new ArrayList<StemTaperTreeSpecies>();
//		speciesList.add(StemTaperTreeSpecies.EPB);
//		speciesList.add(StemTaperTreeSpecies.EPN);
//		speciesList.add(StemTaperTreeSpecies.EPR);
//		speciesList.add(StemTaperTreeSpecies.PIB);
//		speciesList.add(StemTaperTreeSpecies.PIG);
//		speciesList.add(StemTaperTreeSpecies.SAB);

		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			for (double dbhCm = 15; dbhCm <= 40; dbhCm += 5) {
				double heightIncrement = (25d-10d) / (40d -15d); 
				double heightM = 10d + heightIncrement * (dbhCm - 15d);
				VolSpecies volSpecies = VolSpecies.valueOf(species.name());
				t.setTreeCharacteristics(dbhCm, heightM, volSpecies, species);
				System.out.println("Processing " + t.toString());
				predictVolumeForThisTree();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		ComparisonPredictions comp = new ComparisonPredictions();
		comp.predictTheseTrees();
		comp.writer.close();
	}
	
}
