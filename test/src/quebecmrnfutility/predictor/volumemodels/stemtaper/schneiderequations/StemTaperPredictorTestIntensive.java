package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictor;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperStand;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperTree;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperEquationSettings.ModelType;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethodInDeterministicMode;
import repicea.io.FormatReader;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.stemtaper.AbstractStemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperCrossSection;
import repicea.util.ObjectUtility;

public class StemTaperPredictorTestIntensive {
	
	private static class StemTaperStandImpl implements StemTaperStand {
		
		private final double basalAreaM2Ha;
		private final double numberOfStemsHa;
		private final String ecoRegion;
		private final String ecologicalType;
		private final QcDrainageClass drainageClass;
		private final double elevationM;
		
		private StemTaperStandImpl(Object[] record) {
			basalAreaM2Ha = Double.parseDouble(record[10].toString());
			numberOfStemsHa = Double.parseDouble(record[11].toString());
			ecoRegion = record[12].toString().trim();
			ecologicalType = record[13].toString().trim();
			drainageClass = QcDrainageClass.valueOf("C".concat(record[14].toString().trim().substring(0, 1)));
			elevationM = Double.parseDouble(record[15].toString());
		}

		

		@Override
		public String getSubjectId() {return "0";}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}


		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

		@Override
		public double getNumberOfStemsHa() {return numberOfStemsHa;}

		@Override
		public String getEcoRegion() {return ecoRegion;}

		@Override
		public String getEcologicalType() {return ecologicalType;}

		@Override
		public QcDrainageClass getDrainageClass() {return drainageClass;}

		@Override
		public double getElevationM() {return elevationM;}
		
	}
	
	private static class StemTaperTreeImpl implements StemTaperTree {

		private double dbhCm;
		private double heightM;
		private StemTaperTreeSpecies species;
		private StemTaperStand stand;
		private double predicted;
		private List<Double> crossSectionsHeight;
		
		private StemTaperTreeImpl(Object[] record, String species) {
			double sectionHeight = Double.parseDouble(record[3].toString());
			crossSectionsHeight = new ArrayList<Double>();
			crossSectionsHeight.add(sectionHeight);
			dbhCm = Double.parseDouble(record[2].toString()) * .1;
			heightM = Double.parseDouble(record[6].toString());
			predicted = Double.parseDouble(record[record.length - 1].toString());
			this.species = StemTaperTreeSpecies.valueOf(species.trim().toUpperCase());
			this.stand = new StemTaperStandImpl(record);
		}
		
		private double getPredicted() {return predicted;}
		
		private List<Double> getHeightList() {return crossSectionsHeight;}
		

		@Override
		public String getSubjectId() {return "0";}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public double getDbhCm() {return dbhCm;}

		@Override
		public double getSquaredDbhCm() {return dbhCm*dbhCm;}

		@Override
		public double getHeightM() {return heightM;}

		@Override
		public StemTaperStand getStand() {return stand;}

		@Override
		public List<StemTaperCrossSection> getCrossSections() {
			return null;
		}

		@Override
		public StemTaperTreeSpecies getStemTaperTreeSpecies() {return species;}
		
	}

	static String path = ObjectUtility.getPackagePath(StemTaperPredictorTest.class);

	
	
	private static List<StemTaperTree> getTreeList(String species) throws IOException {
		List<StemTaperTree> treeList = new ArrayList<StemTaperTree>();
		String filename = path + species.trim().toLowerCase().concat("PredRef.csv");
		CSVReader reader = (CSVReader) FormatReader.createFormatReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			treeList.add(new StemTaperTreeImpl(record, species));
		}
		return treeList;
	}

	/**
	 * This test processes 500 observations of each species and compares with the predictions from R.
	 * @throws IOException
	 */
	@Test
	public void testSchneiderStemTaperModels() throws IOException {
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("bop");
		speciesList.add("epb");
		speciesList.add("epn");
		speciesList.add("epr");
		speciesList.add("peg");
		speciesList.add("pet");
		speciesList.add("sab");
		speciesList.add("tho");
		for (String species : speciesList) {
			System.out.println("Testing species: " + species);
			List<StemTaperTree> trees = getTreeList(species);
			StemTaperPredictor stm = new StemTaperPredictor();
			for (StemTaperTree tree : trees) {
				AbstractStemTaperEstimate estimate = stm.getPredictedTaperForTheseHeights(tree, ((StemTaperTreeImpl) tree).getHeightList(), EstimationMethodInDeterministicMode.FirstOrderMeanOnly, ModelType.TREEMODEL);
				double newValue = estimate.getMean().m_afData[0][0];
				double oldValue = ((StemTaperTreeImpl) tree).getPredicted();
//				if (Math.abs(oldValue - newValue) > 1E-8) {
//					int u = 0;
//				}
				assertEquals("Testing species " + species, 
						oldValue, 
						newValue,
						1E-8);	
			}
		}
	}
}
