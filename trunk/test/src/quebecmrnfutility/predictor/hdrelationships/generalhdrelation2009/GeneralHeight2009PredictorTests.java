package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.GeneralHeight2014PredictorTests;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;


public class GeneralHeight2009PredictorTests {


	static Map<String, Heightable2009Stand> standMap;

	static void ReadStands() {
		String filename = ObjectUtility.getPackagePath(GeneralHeight2014PredictorTests.class) + "fichier_test_unitaire.csv";
		standMap = new HashMap<String, Heightable2009Stand>();
		CSVReader reader;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			int treeID = 0;
			while ((record = reader.nextRecord()) != null) {
				String placetteID = record[0].toString().trim().concat(record[2].toString().trim());
				String obsHeightStr = record[6].toString();
				double heightM = 0;
				if (!obsHeightStr.isEmpty()) {
					heightM = Double.parseDouble(obsHeightStr);
				}
				double basalAreaM2Ha = Double.parseDouble(record[7].toString());
				double meanQuadraticDiameterCm = Double.parseDouble(record[8].toString());
				String regEco = record[13].toString().trim();
				String typeEco = record[15].toString().trim();
				double elevationM = Double.parseDouble(record[17].toString());
				double meanAnnualPrecipitationMm = Double.parseDouble(record[19].toString());
				double meanAnnualTemperatureC = Double.parseDouble(record[21].toString());
				
				double dbhCm = Double.parseDouble(record[4].toString());
				String species = record[5].toString();
				double predictedHeight = Double.parseDouble(record[29].toString());
				
				if (!standMap.containsKey(placetteID)) {
					standMap.put(placetteID, new Heightable2009StandImpl(placetteID,
							basalAreaM2Ha,
							meanQuadraticDiameterCm,
							regEco,
							typeEco,
							elevationM,
							meanAnnualTemperatureC,
							meanAnnualPrecipitationMm));
				}
				Heightable2009StandImpl stand = (Heightable2009StandImpl) standMap.get(placetteID);
				new Heightable2009TreeImpl(stand, dbhCm, predictedHeight, treeID++, species, heightM);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	

	/*
	 * This test compares the blups and the height predictions to reference tables. The simulation is deterministic.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void comparePredictionsAndBlups() throws Exception {
		if (standMap == null) {
			GeneralHeight2009PredictorTests.ReadStands();
		}
		
		List<String> plotIds = new ArrayList<String>();
		plotIds.addAll(standMap.keySet());
		
		Collections.sort(plotIds);
		
		GeneralHeight2009Predictor predictor = new GeneralHeight2009Predictor(); // deterministic simulations
		
		Map<String, Map<String, Double>> predictedHeights = new HashMap<String, Map<String, Double>>();
		Map<String, Matrix> blupMap = new HashMap<String, Matrix>();
		
		for (int i = 0; i < 10; i++) {
			String selectedPlotId = plotIds.get(i);
			Heightable2009Stand s = standMap.get(selectedPlotId);
			for (Object t : s.getTrees(StatusClass.alive)) {
				Heightable2009Tree tree = (Heightable2009Tree) t;
				double predictedHeightM = predictor.predictHeightM(s, tree);
				if (!predictedHeights.containsKey(s.getSubjectId())) {
					predictedHeights.put(s.getSubjectId(), new HashMap<String, Double>());
				}
				predictedHeights.get(s.getSubjectId()).put(tree.getSubjectId(), predictedHeightM);
			}
			blupMap.put(s.getSubjectId(), predictor.getBlups(s));
		}
		
		String refFilenamePredictions = ObjectUtility.getPackagePath(getClass()) + "refPredictions.xml";
		String refFilenameBlups = ObjectUtility.getPackagePath(getClass()) + "refBlup.xml";
		
		// UNCOMMENT THIS PART TO SAVE NEW REFERENCE FILES
//		XmlSerializer serializer = new XmlSerializer(refFilenamePredictions);
//		serializer.writeObject(predictedHeights);
//		serializer = new XmlSerializer(refFilenameBlups);
//		serializer.writeObject(blupMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(refFilenamePredictions);
		Map<String, Map<String, Double>> refMapPredictions = (Map) deserializer.readObject();
		deserializer = new XmlDeserializer(refFilenameBlups);
		Map<String, Matrix> refMapBlups = (Map) deserializer.readObject();

		// comparer les map
		compareMap(refMapPredictions, predictedHeights);
		compareMap(refMapBlups, blupMap);

		System.out.println("GeneralHeight2009PredictorTests.comparePredictionsAndBlups - Successful comparisons!");
	}

	
	@SuppressWarnings("rawtypes")
	void compareMap(Map map1, Map map2) {
		Assert.assertEquals(map1.size() == map2.size(), true);
		for (Object key1 : map1.keySet()) {
			Object value1 = map1.get((String) key1);
			Object value2 = map2.get((String) key1);
			if (value2 == null) {
				Assert.fail("The value corresponding to key "  + ((String) key1) + " in the first map is not found in the second map!");
			} else {
				if (value1 instanceof Map) {
					if (!(value2 instanceof Map)) {
						Assert.fail("The value is a map in the first map but not in the second map!");
					} else {
						compareMap((Map) value1, (Map) map2.get((String) key1));
					}
				} else if (value1 instanceof Matrix) {
					if (!(value2 instanceof Matrix)) {
						Assert.fail("The value is a matrix in the first map but not in the second map!");
					}
					Matrix m1 = (Matrix) value1;
					Matrix m2 = (Matrix) value2;
					Assert.assertEquals("Comparing the values of matrix 1 and matrix 2", true, m1.equals(m2));
				} else if (value1 instanceof Double) {
					if (!(value2 instanceof Double)) {
						Assert.fail("The value is a double in the first map but not in the second map!");
					}
					Assert.assertEquals("Comparing the values", (Double) value2, (Double) value1);
				}
			}
		}
	}
	
	
	/*
	 * This test compares the mean of stochastic prediction with the determinitic prediction for a single tree. 
	 * Sincen the model is purely linear, these two values should be very close. 
	 */
	@SuppressWarnings({"rawtypes" })
	@Test
	public void compareStochasticPredictions() throws Exception {
		if (standMap == null) {
			GeneralHeight2009PredictorTests.ReadStands();
		}
		
		List<String> plotIds = new ArrayList<String>();
		plotIds.addAll(standMap.keySet());
		
		Collections.sort(plotIds);
		
		GeneralHeight2009Predictor detPredictor = new GeneralHeight2009Predictor(); // deterministic simulations
		GeneralHeight2009Predictor stoPredictor = new GeneralHeight2009Predictor(true); // stochastic simulations
		
		
		String selectedPlotId = plotIds.get(0);
		Heightable2009Stand s = standMap.get(selectedPlotId);
		Heightable2009Tree tree = (Heightable2009Tree) ((ArrayList) s.getTrees(StatusClass.alive)).get(0);
		double detPred = detPredictor.predictHeightM(s, tree);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix realization;
		for (int i = 0; i < 100000; i++) {
			((Heightable2009StandImpl) s).monteCarloRealizationID = i;
			realization = new Matrix(1,1);
			realization.m_afData[0][0] = stoPredictor.predictHeightM(s, tree);
			estimate.addRealization(realization);
		}
		
		double actual = estimate.getMean().m_afData[0][0];
		Assert.assertEquals("Comparing deterministic and stochastic predictions", actual, detPred, 1E-2);
		System.out.println("GeneralHeight2009PredictorTests.compareStochasticPredictions - Successful comparisons!");
	}

	
}
