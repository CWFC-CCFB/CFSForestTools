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
import repicea.math.Matrix;
import repicea.util.ObjectUtility;

public class Iris2020RecruitmentTests {

	private static List<Iris2020CompatibleTestPlotImpl> PlotListForOccurrences;
	private static List<Iris2020CompatibleTestPlotImpl> PlotListForNumbers;
	
	private static Iris2020CompatibleTestPlotImpl createPlotFromRecord(Object[] record) {
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
		Double gSpGr = null;
		if (record.length > 20) {
			gSpGr = Double.parseDouble(record[20].toString());
		}
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
				pred,
				gSpGr);
		return plot;
	}
	
	private static List<Iris2020CompatibleTestPlotImpl> getPlotListForOccurrences() throws IOException {
		if (PlotListForOccurrences == null) {
			PlotListForOccurrences = new ArrayList<Iris2020CompatibleTestPlotImpl>();
			String filename = ObjectUtility.getPackagePath(Iris2020RecruitmentTests.class) + "0_RecruitmentOccurrenceValidationDataset.csv";
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				PlotListForOccurrences.add(createPlotFromRecord(record));
			}
			reader.close();
		}
		return PlotListForOccurrences;
	}

	private static List<Iris2020CompatibleTestPlotImpl> getPlotListForNumbers() throws IOException {
		if (PlotListForNumbers == null) {
			PlotListForNumbers = new ArrayList<Iris2020CompatibleTestPlotImpl>();
			String filename = ObjectUtility.getPackagePath(Iris2020RecruitmentTests.class) + "0_RecruitmentNumberValidationDataset.csv";
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				PlotListForNumbers.add(createPlotFromRecord(record));
			}
			reader.close();
		}
		return PlotListForNumbers;
	}

	
	/*
	 * Validation test for occurrence using R validation dataset
	 */
	@Test
	public void testOccurrencePredictionsAgainstRPredictions() throws IOException {
		Iris2020RecruitmentOccurrencePredictor predictor = new Iris2020RecruitmentOccurrencePredictor(false); // deterministic
		List<Iris2020CompatibleTestPlotImpl> plots = getPlotListForOccurrences(); 
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

	
	/*
	 * Validation test for number of recruits using R validation dataset
	 */
	@Test
	public void testMeanNumberPredictionsAgainstRPredictions() throws IOException {
		Iris2020RecruitmentNumberPredictor predictor = new Iris2020RecruitmentNumberPredictor(false); // deterministic
		List<Iris2020CompatibleTestPlotImpl> plots = getPlotListForNumbers(); 
		int nbTested = 0;
		for (Iris2020CompatibleTestPlotImpl plot : plots) {
			Iris2020CompatibleTree tree = plot.getTreeInstance();
			double actual = predictor.predictNumberOfRecruits(plot, tree);
			double expected = plot.getPredProb() + 1d; // adding one because 
			Assert.assertEquals("Testing mean predicted number for plot " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
					expected, 
					actual, 
					1E-8);
			nbTested++;
		}
		System.out.println("Number of successfully tested plots = " + nbTested);
	}

	/*
	 * Validation test for stochastic implementation.
	 */
	@Test
	public void testStochasticMeanNumberPredictions() throws IOException {
		Iris2020RecruitmentNumberPredictor detPredictor = new Iris2020RecruitmentNumberPredictor(false); // deterministic
		Iris2020RecruitmentNumberPredictor stoPredictor = new Iris2020RecruitmentNumberPredictor(false, true); // stochastic but with variability disabled for parameter estimates
		int nbRealizations = 50000;
		List<Iris2020CompatibleTestPlotImpl> plots = getPlotListForNumbers(); 
		Iris2020CompatibleTestPlotImpl plot = plots.get(0);
		Iris2020CompatibleTree tree = plot.getTreeInstance();
		double detPred = detPredictor.predictNumberOfRecruits(plot, tree);
		Matrix realizations = new Matrix(nbRealizations, 1);
		for (int j = 0; j < nbRealizations; j++) {
			realizations.m_afData[j][0] = stoPredictor.predictNumberOfRecruits(plot, tree);
		}
		double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
		Matrix diff = realizations.scalarAdd(-meanStoPred);
		Matrix ssq = diff.transpose().multiply(diff);
		double variance = ssq.m_afData[0][0] / (realizations.m_iRows - 1);
		double thetaParmEst = stoPredictor.getThetaParameterEstimate(tree.getSpecies());
		double expectedVariance = (detPred - 1) + thetaParmEst * (detPred - 1) * (detPred - 1);
		Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
				detPred, 
				meanStoPred, 
				5E-2);
		System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
		Assert.assertEquals("Testing stochastic variance against expected variance " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
				expectedVariance, 
				variance, 
				1E-1);
		System.out.println("Expected variance = " + expectedVariance + " Actual variance = " + variance);
	}

}
