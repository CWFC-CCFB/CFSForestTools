package canforservutility.predictor.iris2020.recruitment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.DisturbanceType;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.OriginType;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.SoilDepth;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.SoilTexture;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class Iris2020RecruitmentOccurrenceTests {

	private static List<Iris2020CompatibleTestPlotImpl> PlotList;
	
	private static List<Iris2020CompatibleTestPlotImpl> getPlotList() throws IOException {
		if (PlotList == null) {
			PlotList = new ArrayList<Iris2020CompatibleTestPlotImpl>();
			String filename = ObjectUtility.getPackagePath(Iris2020RecruitmentOccurrenceTests.class) + "0_RecruitmentOccurrenceValidationDataset.csv";
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String plotId = record[0].toString();
				String speciesName = record[2].toString();
				Iris2020Species species = Iris2020Species.valueOf(speciesName);
				int dateYr = Integer.parseInt(record[3].toString());
				double growthStepYr = Double.parseDouble(record[4].toString());
				double basalAreaM2Ha = Double.parseDouble(record[5].toString());
				double stemDensity = Double.parseDouble(record[6].toString());
				double dd = Double.parseDouble(record[7].toString());
				double length = Double.parseDouble(record[8].toString());
				double prcp = Double.parseDouble(record[9].toString());
				double lowestTmin = Double.parseDouble(record[10].toString());
				double frostDay = Double.parseDouble(record[11].toString());
				String upcomingDistStr = record[12].toString().substring(1);
				DisturbanceType upcomingDist = DisturbanceType.valueOf(upcomingDistStr);
				String pastDistStr = record[13].toString().substring(1);
				DisturbanceType pastDist = DisturbanceType.valueOf(pastDistStr);
				String originStr = record[14].toString().substring(1);
				OriginType origin = OriginType.valueOf(originStr);
				double slope = Double.parseDouble(record[15].toString());
				String textureStr = record[16].toString().substring(1);
				SoilTexture soilTexture = SoilTexture.valueOf(textureStr);
				String depthStr = record[17].toString().substring(1);
				SoilDepth soilDepth = SoilDepth.valueOf(depthStr);
				boolean isOrganicSoil = record[18].toString().equals("1");
				double pred = Double.parseDouble(record[19].toString());
				Iris2020CompatibleTestPlotImpl plot = new Iris2020CompatibleTestPlotImpl(plotId,
						growthStepYr,
						basalAreaM2Ha,
						stemDensity,
						slope,
						dateYr,
						dd,
						prcp,
						length,
						frostDay,
						lowestTmin,
						soilDepth,
						pastDist,
						upcomingDist,
						origin,
						isOrganicSoil,
						soilTexture,
						species,
						pred);
				PlotList.add(plot);
			}
			reader.close();
		}
		return PlotList;
	}
	
	
	/*
	 * Validation test using R validation dataset
	 */
	@Test
	public void testPredictionsAgainstRPredictions() throws IOException {
		Iris2020RecruitmentOccurrencePredictor predictor = new Iris2020RecruitmentOccurrencePredictor(false); // deterministic
		List<Iris2020CompatibleTestPlotImpl> plots = getPlotList(); 
		int nbTested = 0;
		for (Iris2020CompatibleTestPlotImpl plot : plots) {
			Iris2020CompatibleTree tree = plot.getTreeInstance();
			double actual = predictor.predictEventProbability(plot, tree);
			double expected = plot.getPredProb();
			Assert.assertEquals("Testing probability for plot " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
					expected, 
					actual, 
					1E-8);
			nbTested++;
		}
		System.out.println("Number of successfully tested plots = " + nbTested);
	}
}
