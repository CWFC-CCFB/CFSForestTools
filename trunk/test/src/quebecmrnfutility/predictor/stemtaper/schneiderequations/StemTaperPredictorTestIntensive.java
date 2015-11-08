package quebecmrnfutility.predictor.stemtaper.schneiderequations;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethod;
import repicea.io.FormatReader;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;
import repicea.simulation.stemtaper.StemTaperCrossSection;
import repicea.simulation.stemtaper.StemTaperEstimate;
import repicea.util.ObjectUtility;

public class StemTaperPredictorTestIntensive {
	
	private static class StemTaperStandImpl implements StemTaperStand {
		
		private double basalAreaM2Ha;
		private double numberOfStemsHa;
		private String ecoRegion;
		private String ecologicalType;
		private String drainageClass;
		private double elevationM;
		
		private StemTaperStandImpl(Object[] record) {
			basalAreaM2Ha = Double.parseDouble(record[10].toString());
			numberOfStemsHa = Double.parseDouble(record[11].toString());
			ecoRegion = record[12].toString().trim();
			ecologicalType = record[13].toString().trim();
			drainageClass = record[14].toString().trim();
			elevationM = Double.parseDouble(record[15].toString());
		}

		
//		@Override
//		public Object getSubjectPlusMonteCarloSpecificId() {return null;}

		@Override
		public int getSubjectId() {return 0;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return null;}


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
		public String getDrainageClass() {return drainageClass.substring(0, 1);}

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
//			double sectionDiameter = Double.parseDouble(record[1].toString());
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
		
//		@Override
//		public Object getSubjectPlusMonteCarloSpecificId() {return null;}

		@Override
		public int getSubjectId() {return 0;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return null;}

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
			stm.setEstimationMethod(EstimationMethod.FirstOrderMeanOnly);
			for (StemTaperTree tree : trees) {
//				stm.setTree(tree);
				StemTaperEstimate estimate = stm.getPredictedTaperForTheseHeights(tree, ((StemTaperTreeImpl) tree).getHeightList());
				double newValue = estimate.getMean().m_afData[0][0];
				double oldValue = ((StemTaperTreeImpl) tree).getPredicted();
				assertEquals("Testing species " + species, 
						oldValue, 
						newValue,
						1E-8);	
			}
		}
	}
}
