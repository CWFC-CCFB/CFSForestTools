package quebecmrnfutility.treelogger.sybille;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethod;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperStand;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.io.FormatReader;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.ObjectUtility;

@SuppressWarnings("rawtypes")
public class SybilleTreeLoggerTest {

	private class StemTaperStandInternalImpl implements StemTaperStand {

		private int monteCarloRealizationID;
		private double basalAreaM2Ha;
		private double nbStemsHa;
		private String ecoRegion;
		private String ecologicalType;
		private String drainageClass;
		private double elevationM;

		private StemTaperStandInternalImpl(int monteCarloRealizationID,
				double basalAreaM2Ha,
				double nbStemsHa,
				String ecoRegion,
				String ecologicalType,
				String drainageClass,
				double elevationM) {
			this.monteCarloRealizationID = monteCarloRealizationID;
			this.basalAreaM2Ha = basalAreaM2Ha;
			this.nbStemsHa = nbStemsHa;
			this.ecoRegion = ecoRegion;
			this.ecologicalType = ecologicalType;
			this.drainageClass = drainageClass;
			this.elevationM = elevationM;
		}
		
		
		@Override
		public int getSubjectId() {return hashCode();}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

//		@Override
//		public void setMonteCarloRealizationId(int i) {this.monteCarloRealizationID = i;}

		@Override
		public int getMonteCarloRealizationId() {return monteCarloRealizationID;}

		@Override
		public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

		@Override
		public double getNumberOfStemsHa() {return nbStemsHa;}

		@Override
		public String getEcoRegion() {return ecoRegion;}

		@Override
		public String getEcologicalType() {return ecologicalType;}

		@Override
		public String getDrainageClass() {return drainageClass;}

		@Override
		public double getElevationM() {return elevationM;}
		
	}
	
	@Test
	public void SybilleTestOnSASData() throws IOException {
		String path = ObjectUtility.getPackagePath(getClass());
		String filename = path + "test_3tigesSAS.dbf";
		String paramFilename = path + "paramSasCompTest.tlp";
		FormatReader<?> dbfReader = FormatReader.createFormatReader(filename);
		Object[] record;
		
		while ((record = dbfReader.nextRecord()) != null) {
			int monteCarloRealizationID = 1;
			double basalAreaM2Ha = Double.parseDouble(record[dbfReader.getHeader().getIndexOfThisField("ST_HA")].toString());
			double nbStemsHa = Double.parseDouble(record[dbfReader.getHeader().getIndexOfThisField("nbTi_HA")].toString());
			String ecoRegion = record[dbfReader.getHeader().getIndexOfThisField("REG_ECO")].toString().trim();
			String ecologicalType = record[dbfReader.getHeader().getIndexOfThisField("TYPECO_VAL")].toString().trim();
			String drainageClass = record[dbfReader.getHeader().getIndexOfThisField("cl_drai")].toString().trim();
			double elevationM = Double.parseDouble(record[dbfReader.getHeader().getIndexOfThisField("altitude")].toString());
			StemTaperStand stand = new StemTaperStandInternalImpl(monteCarloRealizationID,
							basalAreaM2Ha,
							nbStemsHa,
							ecoRegion,
							ecologicalType,
							drainageClass,
							elevationM);
			String speciesName = record[dbfReader.getHeader().getIndexOfThisField("essence")].toString().trim();
			StemTaperTreeSpecies species = StemTaperTreeSpecies.valueOf(speciesName.trim().toUpperCase()); 
			double dbhCm = Double.parseDouble(record[dbfReader.getHeader().getIndexOfThisField("dhpcm")].toString());
			double heightM = Double.parseDouble(record[dbfReader.getHeader().getIndexOfThisField("hautm_arte")].toString());
			double refVolume = Double.parseDouble(record[dbfReader.getHeader().getIndexOfThisField("vol_defil0")].toString());
			LoggableTreeImplTest tree = new LoggableTreeImplTest(stand, species, dbhCm, heightM, refVolume);
			SybilleTreeLogger treeLogger = new SybilleTreeLogger();
			SybilleTreeLoggerParameters param = treeLogger.createDefaultTreeLoggerParameters();
			param.load(paramFilename);
			treeLogger.setTreeLoggerParameters(param);
			treeLogger.logThisTree(tree);
			Map<LoggableTree, Collection<WoodPiece>> woodPieces = treeLogger.getWoodPieces();
			double merchantableVolume = 0d;
			for (WoodPiece woodPiece : woodPieces.get(tree)) {
				merchantableVolume += woodPiece.getVolumeM3();
			}	
			double expectedVolume = tree.getExpectedVolume() * .001;
			Assert.assertEquals("Comparing species " + species.name(), expectedVolume, merchantableVolume, 2E-3);
		}
	}
	
	private Map<String, Double> getObservedMap(EstimationMethod estimationMethod, boolean optimization) throws IOException {
		StemTaperStand stand = new StemTaperStandImplTest();
		
		Collection<SybilleLoggableTree> coll = new ArrayList<SybilleLoggableTree>();
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.BOP, 25, 20));
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.EPB, 25, 20));
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.EPN, 25, 20));
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.EPR, 25, 20));
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.PET, 25, 20));
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.PEG, 25, 20));
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.SAB, 25, 20));
		coll.add(new LoggableTreeImplTest(stand, StemTaperTreeSpecies.THO, 25, 20));
		
		SybilleTreeLogger treeLogger = new SybilleTreeLogger();
		treeLogger.init(coll);
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		treeLogger.getTreeLoggerParameters().setEstimationMethod(estimationMethod);
		treeLogger.getTreeLoggerParameters().setIntegrationOptimizationEnabled(optimization);
		treeLogger.run();

		double volume = 0d;
		
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (Collection<WoodPiece> oColl : treeLogger.getWoodPieces().values()) {
			if (!oColl.isEmpty()) {
				for (WoodPiece piece : oColl) {
					String logCategoryName = piece.getLogCategory().getName();
					volume = piece.getWeightedVolumeM3();
					if (obsMap.containsKey(logCategoryName)) {
						volume += obsMap.get(logCategoryName);
					}
					obsMap.put(logCategoryName, volume);
				}
			}
		}
		
		return obsMap;
	}
	
	
	@Test
	public void SimpleTestOnAllSpeciesWithFirstOrderApproxWithTrapezoidalRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingTestWithFirstOrder.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.FirstOrder, false);
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//  	try {
//  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//  		ObjectOutputStream out = new ObjectOutputStream(fos);
//  		out.writeObject(obsMap);
//  		out.close();
//  	} catch(IOException ex) {
//  		ex.printStackTrace();
//  		throw ex;
//  	}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}

	@Test
	public void SimpleTestOnAllSpeciesWithFirstOrderApproxMeanOnlyWithTrapezoidalRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingTestWithFirstOrder.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.FirstOrderMeanOnly, false);
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//  	try {
//  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//  		ObjectOutputStream out = new ObjectOutputStream(fos);
//  		out.writeObject(obsMap);
//  		out.close();
//  	} catch(IOException ex) {
//  		ex.printStackTrace();
//  		throw ex;
//  	}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}


	@Test
	public void SimpleTestOnAllSpeciesWithSecondOrderWithTrapezoidalRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingSimpleTest.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.SecondOrder, false);
	
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//  	try {
//  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//  		ObjectOutputStream out = new ObjectOutputStream(fos);
//  		out.writeObject(obsMap);
//  		out.close();
//  	} catch(IOException ex) {
//  		ex.printStackTrace();
//  		throw ex;
//  	}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}
	
	
	@Test
	public void SimpleTestOnAllSpeciesWithSecondOrderMeanOnlyWithTrapezoidalRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingSimpleTest.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.SecondOrderMeanOnly, false);

//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//  	try {
//  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//  		ObjectOutputStream out = new ObjectOutputStream(fos);
//  		out.writeObject(obsMap);
//  		out.close();
//  	} catch(IOException ex) {
//  		ex.printStackTrace();
//  		throw ex;
//  	}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}

	@Test
	public void SimpleTestOnAllSpeciesWithFirstOrderApproxWithSimpsonRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingTestWithSimpsonRuleFO.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.FirstOrder, true);
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(obsMap);
//			out.close();
//		} catch(IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}

	@Test
	public void SimpleTestOnAllSpeciesWithFirstOrderMeanOnlyApproxWithSimpsonRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingTestWithSimpsonRuleFO.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.FirstOrderMeanOnly, true);
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(obsMap);
//			out.close();
//		} catch(IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}

	@Test
	public void SimpleTestOnAllSpeciesWithSecondOrderApproxWithSimpsonRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingTestWithSimpsonRuleSO.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.SecondOrder, true);
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(obsMap);
//			out.close();
//		} catch(IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}


	@Test
	public void SimpleTestOnAllSpeciesWithSecondOrderMeanOnlyApproxWithSimpsonRule() throws Exception {
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "sybilleLoggingTestWithSimpsonRuleSO.ser";
		
		Map<String, Double> obsMap = getObservedMap(EstimationMethod.SecondOrderMeanOnly, true);
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(obsMap);
//			out.close();
//		} catch(IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}
	
		System.out.println("Loading reference map...");
		HashMap refMap;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = (HashMap) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}

		System.out.println("Comparing results...");
		for (Object key : refMap.keySet()) {
			double volumeRef = (Double) refMap.get(key);
			double estimatedVolume = (Double) obsMap.get(key);
			assertEquals("Testing log grade " + key.toString(), volumeRef, estimatedVolume, 0.0001);
		}

	}

	
	
}
