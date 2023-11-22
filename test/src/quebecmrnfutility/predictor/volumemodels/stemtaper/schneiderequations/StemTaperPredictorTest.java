package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethodInDeterministicMode;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.stemtaper.AbstractStemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.stats.estimates.Estimate;
import repicea.util.ObjectUtility;


@SuppressWarnings("rawtypes")
public class StemTaperPredictorTest {
	
	static String path = ObjectUtility.getPackagePath(StemTaperPredictorTest.class);
	
	
	
	private Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> runSimulation(EstimationMethodInDeterministicMode method) throws Exception {
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

	
	
	/**
	 * This test chacks whether the stem taper without approximation is still consistent. A stem taper for a tree that is 30 cm 
	 * in dbh and 20 m in height is generated for each one of the 8 species. The resulting taper is compared with a reference.
	 * @throws Exception
	 */
	@Test
	public void TestStemProfileWithFirstOrderApprox() throws Exception {
		String referenceFilename = path + "refTaperFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.FirstOrder);
		
		Map<String, Matrix> outputMap = new HashMap<String, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species.name(), taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species.name());
			Matrix currentValue = outputMap.get(species.name());
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.getValueAt(i, 0), 
						currentValue.getValueAt(i, 0),
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
	public void TestStemProfileWithFirstOrderApproxMeanOnly() throws Exception {
		String referenceFilename = path + "refTaperFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.FirstOrderMeanOnly);
		
		Map<String, Matrix> outputMap = new HashMap<String, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species.name(), taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species.name());
			Matrix currentValue = outputMap.get(species.name());
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.getValueAt(i, 0), 
						currentValue.getValueAt(i, 0),
						1E-8);
			}
		}
	} 

	@Test
	public void TestMeanVolumeWithFirstOrderApprox() throws Exception {
		String referenceFilename = path + "refVolumesFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.FirstOrder);
		
		Map<String, Double> outputMap = new HashMap<String, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<Matrix, SymmetricMatrix, ?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species.name(), volume);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species.name());
			double currentValue = outputMap.get(species.name());
			assertEquals("Testing species " + species.name(), ref, currentValue, 1E-8);
		}
	} 

	
	@Test
	public void TestVarianceVolumeWithFirstOrderApprox() throws Exception {
		String referenceFilename = path + "refVariancesFirstOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.FirstOrder);

		Map<String, Double> outputMap = new HashMap<String, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<Matrix, SymmetricMatrix, ?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species.name(), variance);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species.name());
			double currentValue = outputMap.get(species.name());
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
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.SecondOrder);
		
		Map<String, Matrix> outputMap = new HashMap<String, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species.name(), taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species.name());
			Matrix currentValue = outputMap.get(species.name());
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.getValueAt(i, 0), 
						currentValue.getValueAt(i, 0),
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
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.SecondOrderMeanOnly);
		
		Map<String, Matrix> outputMap = new HashMap<String, Matrix>();
		AbstractStemTaperEstimate taperEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			outputMap.put(species.name(), taperEstimate.getMean());
		}
		
		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			Matrix ref = (Matrix) refMap.get(species.name());
			Matrix currentValue = outputMap.get(species.name());
			for (int i = 0; i < ref.m_iRows; i++) {
				assertEquals("Testing species " + species.name(), 
						ref.getValueAt(i, 0), 
						currentValue.getValueAt(i, 0),
						1E-8);
			}
		}
	} 

	
	@Test
	public void TestMeanVolumeWithSecondOrderApprox() throws Exception {
		String referenceFilename = path + "refVolumesSecondOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.SecondOrder);
		
		Map<String, Double> outputMap = new HashMap<String, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<Matrix, SymmetricMatrix, ?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species.name(), volume);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species.name());
			double currentValue = outputMap.get(species.name());
			assertEquals("Testing species " + species.name(), ref, currentValue, 1E-8);
		}
	} 

	
	@Test
	public void TestVarianceVolumeWithSecondOrderApprox() throws Exception {
		String referenceFilename = path + "refVariancesSecondOrderApprox.ser";
		Map<StemTaperTreeSpecies, AbstractStemTaperEstimate> tmpMap =  runSimulation(EstimationMethodInDeterministicMode.SecondOrder);

		Map<String, Double> outputMap = new HashMap<String, Double>();
		AbstractStemTaperEstimate taperEstimate;
		Estimate<Matrix, SymmetricMatrix, ?> volumeEstimate;
		for (StemTaperTreeSpecies species : tmpMap.keySet()) {
			taperEstimate = tmpMap.get(species);
			volumeEstimate = taperEstimate.getVolumeEstimate();
			double volume = volumeEstimate.getMean().getSumOfElements();
			double variance = volumeEstimate.getVariance().getSumOfElements();
			outputMap.put(species.name(), variance);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);
			nf.setMinimumFractionDigits(3);
			System.out.println("Species " + species.name() 
					+ "; Volume = " + nf.format(volume)
					+ "; Variance = " + nf.format(variance));
			
		}

		//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		XmlSerializer serializer = new XmlSerializer(referenceFilename);
//		serializer.writeObject(outputMap);

		System.out.println("Loading reference map...");
		HashMap refMap;
		XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
		refMap = (HashMap) deserializer.readObject();

		System.out.println("Comparing results...");
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			double ref = (Double) refMap.get(species.name());
			double currentValue = outputMap.get(species.name());
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
		AbstractStemTaperEstimate taperEstimate = ste.getPredictedTaperForTheseSegments(tree, segments, EstimationMethodInDeterministicMode.SecondOrder);
			
		Estimate<Matrix, SymmetricMatrix, ?> volumeEstimate = taperEstimate.getVolumeEstimate(segments);
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

		
}
