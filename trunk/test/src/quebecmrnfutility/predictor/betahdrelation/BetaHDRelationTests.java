package quebecmrnfutility.predictor.betahdrelation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.betahdrelation.BetaHeightableTree.BetaHdSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;


public class BetaHDRelationTests {

	
	static Map<String, BetaHeightableStand> standMap;

	
	static void ReadStands() {
		String filename = ObjectUtility.getPackagePath(BetaHDRelationTests.class) + "fichier_test_unitaire.csv";
		standMap = new HashMap<String, BetaHeightableStand>();
		CSVReader reader;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			int treeID = 0;
			while ((record = reader.nextRecord()) != null) {
				String placetteID = record[0].toString().trim().concat(record[2].toString().trim());
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
					standMap.put(placetteID, new BetaHeightableStandImpl(placetteID,
							basalAreaM2Ha,
							meanQuadraticDiameterCm,
							regEco,
							typeEco,
							elevationM,
							meanAnnualTemperatureC,
							meanAnnualPrecipitationMm));
				}
				BetaHeightableStandImpl stand = (BetaHeightableStandImpl) standMap.get(placetteID);
				new BetaHeightableTreeImpl(stand, dbhCm, predictedHeight, treeID++, species);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void comparePredictionsWithSAS() {
		if (standMap == null) {
			BetaHDRelationTests.ReadStands();
		}
		List<Integer> measurementDates = new ArrayList<Integer>();
		measurementDates.add(2015);
		BetaHeightPredictor predictor = new BetaHeightPredictor();
		
		int goodMatches = 0;
		List<BetaHdSpecies> goodMatchingSpecies = new ArrayList<BetaHdSpecies>();
		for (BetaHeightableStand stand : standMap.values()) {
			Collection<BetaHeightableTree> trees = stand.getTrees();
			for (BetaHeightableTree t : trees) {
				double actual = predictor.predictHeight(stand, t);
				double expected = ((BetaHeightableTreeImpl) t).getPredictedHeight();
				if (Math.abs(expected-actual) < 1E-8) {
					goodMatches++;
					if (!goodMatchingSpecies.contains(t.getBetaHeightableTreeSpecies())) {
						goodMatchingSpecies.add(t.getBetaHeightableTreeSpecies());
					}
				} else {
					goodMatchingSpecies.remove(t.getBetaHeightableTreeSpecies());
				}
				Assert.assertEquals("Comparing predicted Heights", expected, actual, 1E-8);
				goodMatches++;
			}
		}
		System.out.println("BetaHDRelationTests.comparePredictionsWithSAS - Successful comparisons " + goodMatches);
	}
	
}
