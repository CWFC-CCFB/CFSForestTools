package manuscript.harvestmodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestableStandImpl;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestableTree;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestableTree.OfficialHarvestableSpecies;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestableTreeImpl;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;

public class Script {
	
	OfficialHarvestModel harvester;
	
	Script() {
		harvester = new OfficialHarvestModel(false, false);
	}

	
	public void harvestThisStand(String filename) throws IOException {

		CSVWriter writer = new CSVWriter(new File(filename), false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("Treatment"));
		fields.add(new CSVField("Density"));
		fields.add(new CSVField("Species"));
		fields.add(new CSVField("dbh"));
		fields.add(new CSVField("prob"));
		writer.setFields(fields);

		List<String> treatments = new ArrayList<String>();
		treatments.add(TreatmentType.CJ_2004.name());
		treatments.add(TreatmentType.CJMSCR.name());
		treatments.add(TreatmentType.EC.name());

		for (String treatment : treatments) {
			double numberOfStems;
			for (int i = 0; i < 3; i++) {
				if (i==0) {
					numberOfStems = 1000d;
				} else if (i==1) {
					numberOfStems = 500d;
				} else {
					numberOfStems = 250d;
				}
				OfficialHarvestableStandImpl stand = new OfficialHarvestableStandImpl(numberOfStems, treatment);
				List<OfficialHarvestableTree> trees = new ArrayList<OfficialHarvestableTree>();
				OfficialHarvestableTreeImpl tree;
				for (OfficialHarvestableSpecies species : OfficialHarvestableSpecies.values()) {
					for (double dbh = 10; dbh < 60; dbh+=.5) {
						tree = new OfficialHarvestableTreeImpl(species.name(), dbh, 0d);
						trees.add(tree);
					}
				}

				Object[] record = new Object[5];
				record[0] = stand.getTreatment().name();
				record[1] = numberOfStems;

				for (OfficialHarvestableTree t : trees) {
					double prob = (Double) harvester.predictEvent(stand, t);
					record[2] = t.getOfficialHarvestableTreeSpecies(stand.getTreatment()).toString();
					record[3] = t.getDbhCm();
					record[4] = prob;
					writer.addRecord(record);
				}
			}
		}		
		writer.close();
	}

	
	public static void main(String[] args) throws IOException {
		String path = "D:" + File.separator +
				"Travail" + File.separator +
				"3_Publications" + File.separator +
				"1_Manuscrits" + File.separator +
				"3_RevisionInterne" + File.separator +
				"2013 - Modele prelevement" + File.separator +
				"simulations" + File.separator;
		Script s = new Script();
		s.harvestThisStand(path + "simul.csv");
	}
	
}
