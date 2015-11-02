package quebecmrnfutility.predictor.betahdrelation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;


public class BetaHDRelationTests {

	
	static Collection<BetaHeightableStand> stands;

	
	static void ReadStands() {
		String filename = ObjectUtility.getPackagePath(BetaHDRelationTests.class) + "fichier_test_unitaire.csv";
		Map<String, BetaHeightableStand> standMap = new HashMap<String, BetaHeightableStand>();
		
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
//				String clDrai = record[18].toString();
				double meanAnnualPrecipitationMm = Double.parseDouble(record[19].toString());
				double meanAnnualTemperatureC = Double.parseDouble(record[21].toString());
				
				double dbhCm = Double.parseDouble(record[4].toString());
				String species = record[5].toString();
				double heightM = Double.parseDouble(record[6].toString());
				
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
				
				
				
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	

	@Test
	public void comparePredictionsWithSAS() {
		if (stands == null) {
			BetaHDRelationTests.ReadStands();
		}
	}
	
}
