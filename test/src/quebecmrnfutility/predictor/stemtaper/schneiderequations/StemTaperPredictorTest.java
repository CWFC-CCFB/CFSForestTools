package quebecmrnfutility.predictor.stemtaper.schneiderequations;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethod;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.math.Matrix;
import repicea.serial.DeprecatedObject;
import repicea.simulation.stemtaper.AbstractStemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.stats.estimates.Estimate;
import repicea.util.ObjectUtility;


@SuppressWarnings("rawtypes")
public class StemTaperPredictorTest {
	
	static String path = ObjectUtility.getPackagePath(StemTaperPredictorTest.class);
	
	
	
	private Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> runSimulation(EstimationMethod method) throws Exception {
		StemTaperPredictor ste = new StemTaperPredictor();
		StemTaperStandImpl stand = new StemTaperStandImpl(20, 700);		// 20 m2/ha and 700 stems / ha
		List<StemTaperTreeImpl> trees = new ArrayList<StemTaperTreeImpl>();
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.BOP, stand, 30, 20));	// 30 cm in dbh and 20 m in height
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.EPB, stand, 30, 20)); 
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.EPN, stand, 30, 20));
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.EPR, stand, 30, 20));
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.PEG, stand, 30, 20));		
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.PET, stand, 30, 20));
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.SAB, stand, 30, 20)); 
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.THO, stand, 30, 20));
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.PIG, stand, 30, 20));
		trees.add(new StemTaperTreeImpl(StemTaperTreeSpecies.PIB, stand, 30, 20));


		AbstractStemTaperEstimate stemTaper;
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> outputMap = new HashMap<StemTaperTreeSpecies, AbstractStemTaperEstimate>();
		for (StemTaperTreeImpl tree : trees) {
//			heights = tree.getHeightsForTaper();
//			ste.setTree(tree);
			stemTaper = ste.getPredictedTaperForTheseSegments(tree, tree.getSegments(), method);
			outputMap.put(tree.getStemTaperTreeSpecies(), stemTaper);
		}
		
		return outputMap;
	}

	
	
	@SuppressWarnings("unchecked")
	private HashMap deserializeMap(ObjectInputStream in) throws IOException, ClassNotFoundException {
		HashMap refMap = (HashMap) in.readObject();
		for (Object key : refMap.keySet()) {
			Object value = refMap.get(key);
			if (value instanceof DeprecatedObject) {
				Object newValue = ((DeprecatedObject) value).convertIntoAppropriateClass();
				refMap.put(key, newValue);
			}
		}
		return refMap;
	}
	
	/**
	 * This test chacks whether the stem taper without approximation is still consistent. A stem taper for a tree that is 30 cm 
	 * in dbh and 20 m in height is generated for each one of the 8 species. The resulting taper is compared with a reference.
	 * @throws Exception
	 */
	@Test
	public void TestStemProfileWithFirstOrderApprox() throws Exception {
		String referenceFilename = path + "refTaperFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.FirstOrder);
		
		Map<StemTaperTreeSpecies, Matrix> outputMap = new HashMap<StemTaperTreeSpecies, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species, taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(outputMap);
//			out.close();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}

		System.out.println("Loading reference map...");
		HashMap refMap;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = deserializeMap(in);
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species);
			Matrix currentValue = outputMap.get(species);
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.m_afData[i][0], 
						currentValue.m_afData[i][0],
						1E-8);
			}
		}
		
//		int[] allo = StemTaperPredictor.timer;
	} 

	
	
	/**
	 * This test chacks whether the stem taper without approximation is still consistent. A stem taper for a tree that is 30 cm 
	 * in dbh and 20 m in height is generated for each one of the 8 species. The resulting taper is compared with a reference.
	 * @throws Exception
	 */
	@Test
	public void TestStemProfileWithFirstOrderApproxMeanOnly() throws Exception {
		String referenceFilename = path + "refTaperFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.FirstOrderMeanOnly);
		
		Map<StemTaperTreeSpecies, Matrix> outputMap = new HashMap<StemTaperTreeSpecies, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species, taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
		//  	try {
		//  		FileOutputStream fos = new FileOutputStream(referenceFilename);
		//  		ObjectOutputStream out = new ObjectOutputStream(fos);
		//  		out.writeObject(outputMap);
		//  		out.close();
		//  	} catch(IOException ex) {
		//  		ex.printStackTrace();
		//  		throw ex;
		//  	}

		System.out.println("Loading reference map...");
		HashMap refMap;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = deserializeMap(in);
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species);
			Matrix currentValue = outputMap.get(species);
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.m_afData[i][0], 
						currentValue.m_afData[i][0],
						1E-8);
			}
		}
	} 

	@Test
	public void TestMeanVolumeWithFirstOrderApprox() throws Exception {
		String referenceFilename = path + "refVolumesFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.FirstOrder);
		
		Map<StemTaperTreeSpecies, Double> outputMap = new HashMap<StemTaperTreeSpecies, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species, volume);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

//				UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		  	try {
//		  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//		  		ObjectOutputStream out = new ObjectOutputStream(fos);
//		  		out.writeObject(outputMap);
//		  		out.close();
//		  	} catch(IOException ex) {
//		  		ex.printStackTrace();
//		  		throw ex;
//		  	}

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
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species);
			double currentValue = outputMap.get(species);
			assertEquals("Testing species " + species.name(), ref, currentValue, 1E-8);
		}
	} 

	
	@Test
	public void TestVarianceVolumeWithFirstOrderApprox() throws Exception {
		String referenceFilename = path + "refVariancesFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.FirstOrder);

		Map<StemTaperTreeSpecies, Double> outputMap = new HashMap<StemTaperTreeSpecies, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species, variance);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

//				UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		  	try {
//		  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//		  		ObjectOutputStream out = new ObjectOutputStream(fos);
//		  		out.writeObject(outputMap);
//		  		out.close();
//		  	} catch(IOException ex) {
//		  		ex.printStackTrace();
//		  		throw ex;
//		  	}

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
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species);
			double currentValue = outputMap.get(species);
			assertEquals("Testing species " + species.name(), ref, currentValue, 1E-8);
		}
	} 

	
	/**
	 * This test chacks whether the stem taper without approximation is still consistent. A stem taper for a tree that is 30 cm 
	 * in dbh and 20 m in height is generated for each one of the 8 species. The resulting taper is compared with a reference.
	 * @throws Exception
	 */
	@Test
	public void TestStemProfileWithSecondOrderApprox() throws Exception {
		String referenceFilename = path + "refTaperSecondOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.SecondOrder);
		
		Map<StemTaperTreeSpecies, Matrix> outputMap = new HashMap<StemTaperTreeSpecies, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species, taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(outputMap);
//			out.close();
//		} catch(IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}

		System.out.println("Loading reference map...");
		HashMap refMap;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = deserializeMap(in);
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species);
			Matrix currentValue = outputMap.get(species);
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.m_afData[i][0], 
						currentValue.m_afData[i][0],
						1E-8);
			}
		}
	} 

	/**
	 * This test chacks whether the stem taper without approximation is still consistent. A stem taper for a tree that is 30 cm 
	 * in dbh and 20 m in height is generated for each one of the 8 species. The resulting taper is compared with a reference.
	 * @throws Exception
	 */
	@Test
	public void TestStemProfileWithSecondOrderApproxMeanOnly() throws Exception {
		String referenceFilename = path + "refTaperSecondOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.SecondOrderMeanOnly);
		
		Map<StemTaperTreeSpecies, Matrix> outputMap = new HashMap<StemTaperTreeSpecies, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species, taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(outputMap);
//			out.close();
//		} catch(IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}

		System.out.println("Loading reference map...");
		HashMap refMap;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refMap = deserializeMap(in);
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species);
			Matrix currentValue = outputMap.get(species);
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.m_afData[i][0], 
						currentValue.m_afData[i][0],
						1E-8);
			}
		}
	} 

	
	@Test
	public void TestMeanVolumeWithSecondOrderApprox() throws Exception {
		String referenceFilename = path + "refVolumesSecondOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.SecondOrder);
		
		Map<StemTaperTreeSpecies, Double> outputMap = new HashMap<StemTaperTreeSpecies, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species, volume);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

//				UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		  	try {
//		  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//		  		ObjectOutputStream out = new ObjectOutputStream(fos);
//		  		out.writeObject(outputMap);
//		  		out.close();
//		  	} catch(IOException ex) {
//		  		ex.printStackTrace();
//		  		throw ex;
//		  	}

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
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species);
			double currentValue = outputMap.get(species);
			assertEquals("Testing species " + species.name(), ref, currentValue, 1E-8);
		}
	} 

	
	@Test
	public void TestVarianceVolumeWithSecondOrderApprox() throws Exception {
		String referenceFilename = path + "refVariancesSecondOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethod.SecondOrder);

		Map<StemTaperTreeSpecies, Double> outputMap = new HashMap<StemTaperTreeSpecies, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species, variance);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

//				UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
		  	try {
		  		FileOutputStream fos = new FileOutputStream(referenceFilename);
		  		ObjectOutputStream out = new ObjectOutputStream(fos);
		  		out.writeObject(outputMap);
		  		out.close();
		  	} catch(IOException ex) {
		  		ex.printStackTrace();
		  		throw ex;
		  	}

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
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species);
			double currentValue = outputMap.get(species);
			assertEquals("Testing species " + species.name(), ref, currentValue, 1E-8);
		}
	} 
	
	
	
	@Test
	public void TestComparisonGaussLegendreAndTrapezoidalRuleForBottomSections() throws Exception {
	
		StemTaperPredictor ste = new StemTaperPredictor();
		StemTaperStandImpl stand = new StemTaperStandImpl(20, 700);		// 20 m2/ha and 700 stems / ha
		StemTaperTreeImpl tree = new StemTaperTreeImpl(StemTaperTreeSpecies.EPB, stand, 30, 20); 

//		ste.setTree(tree);
		StemTaperSegmentList segments = tree.getGaussLegendreBottomSegments();
		AbstractStemTaperEstimate taperEstimate = ste.getPredictedTaperForTheseSegments(tree, segments, EstimationMethod.SecondOrder);
			
		Estimate<?> volumeEstimate = taperEstimate.getVolumeEstimate(segments);
		double volume = volumeEstimate.getMean().getSumOfElements();
		double variance = volumeEstimate.getVariance().getSumOfElements();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
		System.out.println("Species " + tree.getStemTaperTreeSpecies().name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			assertEquals("Testing Gauss-Legendre quadrature mean", 240.5348, volume, 1E-3);
			assertEquals("Testing Gauss-Legendre quadrature variance", 79.06141, variance, 1E-3);
		
		

		ste = new StemTaperPredictor();
//		ste.setTree(tree);
		segments = tree.getTrapezoidalBottomSegments();
		taperEstimate = ste.getPredictedTaperForTheseSegments(tree, segments);
			
		volumeEstimate = taperEstimate.getVolumeEstimate(segments);
		volume = volumeEstimate.getMean().getSumOfElements();
		variance = volumeEstimate.getVariance().getSumOfElements();
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
		System.out.println("Species " + tree.getStemTaperTreeSpecies().name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			assertEquals("Testing Trapezoidal rule mean", 240.8067, volume, 1E-3);
			assertEquals("Testing Gauss-Legendre quadrature variance", 72.4178, variance, 1E-3);

	} 
	
//	private static class StemTaperStandPrivateImpl implements StemTaperStand {
//
//		@Override
//		public Object getSubjectPlusMonteCarloSpecificId() {
//			return this;
//		}
//
//		@Override
//		public Object getSubjectId() {
//			return this;
//		}
//
//		@Override
//		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Plot;}
//
//		@Override
//		public void setMonteCarloRealizationId(int i) {
//		}
//
//		@Override
//		public int getMonteCarloRealizationId() {
//			return 0;
//		}
//
//		@Override
//		public double getBasalAreaM2Ha() {
//			return 34.577;
//		}
//
//		@Override
//		public double getNumberOfStemsHa() {
//			return 825;
//		}
//
//		@Override
//		public String getEcoRegion() {
//			return "3b";
//		}
//
//		@Override
//		public String getEcologicalType() {
//			return "FE2";
//		}
//
//		@Override
//		public String getDrainageClass() {
//			return "2";
//		}
//
//		@Override
//		public double getElevationM() {
//			return 300;
//		}
//		
//	}
//	
//	private static class StemTaperTreePrivateImpl implements StemTaperTree {
//
//		private StemTaperStand stand;
//		
//		private StemTaperTreePrivateImpl(StemTaperStand stand) {
//			this.stand = stand;
//		}
//		
//		
//		@Override
//		public Object getSubjectPlusMonteCarloSpecificId() {
//			return this;
//		}
//
//		@Override
//		public Object getSubjectId() {
//			return this;
//		}
//
//		@Override
//		public HierarchicalLevel getHierarchicalLevel() {
//			return HierarchicalLevel.Tree;
//		}
//
//		@Override
//		public void setMonteCarloRealizationId(int i) {
//		
//		}
//
//		@Override
//		public int getMonteCarloRealizationId() {
//			return 0;
//		}
//
//		@Override
//		public double getDbhCm() {
//			return 45.5;
//		}
//
//		@Override
//		public double getSquaredDbhCm() {
//			return getDbhCm() * getDbhCm();
//		}
//
//		@Override
//		public double getHeightM() {
//			return 25.381;
//		}
//
//		@Override
//		public StemTaperStand getStand() {
//			return stand;
//		}
//
//		@Override
//		public List<StemTaperCrossSection> getCrossSections() {
//			return null;
//		}
//
//		@Override
//		public StemTaperTreeSpecies getStemTaperTreeSpecies() {
//			return StemTaperTreeSpecies.PEG;
//		}
//		
//	}
//	public static void main(String[] args) throws IOException {
//		StemTaperStand stand = new StemTaperStandPrivateImpl();
//		StemTaperTree tree = new StemTaperTreePrivateImpl(stand);
//		StemTaperPredictor stp = new StemTaperPredictor();
//		stp.setTree(tree);
//		List<Double> h = new ArrayList<Double>();
//		double c = 0.5 * (tree.getHeightM() + 0.15);
//		h.add(c);
//		StemTaperEstimate est = stp.getPredictedTaperForTheseHeights(h);
//		int u = 0;
//	}
	
	
}
