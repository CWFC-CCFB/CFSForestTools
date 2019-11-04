package canforservutility.biodiversity.indices;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.biodiversity.indices.DiversityIndices.BetaIndex;
import canforservutility.biodiversity.indices.MultipleSiteIndex.Mode;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.SimpleEstimate;
import repicea.stats.sampling.SamplingUtility;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
class PopulationTests {

	private class Plot extends ArrayList<Integer> {
		private Plot() {
			super();
		}
	}

	@SuppressWarnings("rawtypes")
	private class Population extends HashMap<String, List> {}
	

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Population createBetaPopulation(int populationSize, int speciesRichness, double scale1, double scale2) {
		List<Integer> plotIndex = new ArrayList<Integer>();
		Population pop = new Population();
		for (int i = 0; i < populationSize; i++) {
			pop.put(((Integer) i).toString(), new Plot());
			plotIndex.add(i);
		}
		int i = 1;
		while (i <= speciesRichness) {
			double prob = StatisticalUtility.getRandom().nextBeta(scale1, scale2);
			int nbPlots = (int) Math.round(plotIndex.size() * prob);
			if (nbPlots > 0) {
				List<Integer> plotIds = (List) SamplingUtility.getSample(plotIndex, nbPlots);
				for (Integer plotId : plotIds) {
					pop.get(plotId.toString()).add(i);
				}
				i++;
			}
		}
		return pop;
	}

	private enum BetaDistributionType {
		
		JustScarceSpecies(1d,5d),
		JustAbundantSpecies(5d,1d),
		ScarceAndAbundantSpecies(0.5,0.5),
		MidSpecies(2d,2d);
		
		double scale1;
		double scale2;
		
		BetaDistributionType(double scale1, double scale2) {
			this.scale1 = scale1;
			this.scale2 = scale2;
		}
		
		double getScale1() {return scale1;}
		double getScale2() {return scale2;}
	}
	
	
	private static Object[] testThisBetaPopulation(int populationSize, int speciesRichness, BetaDistributionType type) throws Exception {
		PopulationTests popTest = new PopulationTests();
		Population pop = popTest.createBetaPopulation(populationSize, speciesRichness, type.getScale1(), type.getScale2());
		if (populationSize == 1000) {
			XmlSerializer serializer = new XmlSerializer(ObjectUtility.getPackagePath(PopulationTests.class).replace("bin", "manuscripts") + "pop" + populationSize + "_" + speciesRichness + "_" + type.name());
			serializer.writeObject(pop);
		}
		MultipleSiteIndex msi = new MultipleSiteIndex();
		DiversityIndices currentIndices = msi.getMultiplesiteDissimilarityIndices(pop);
		DiversityIndices newIndices = msi.getAdaptedMultiplesiteDissimilarityIndices(pop);
		Object[] record = new Object[11];
		record[0] = populationSize;
		record[1] = newIndices.getGammaDiversity();
		record[2] = newIndices.getAlphaDiversity();
		record[3] = type.getScale1();
		record[4] = type.getScale2();
		record[5] = currentIndices.getBetaDiversity(BetaIndex.Simpson);
		record[6] = currentIndices.getBetaDiversity(BetaIndex.Sorensen);
		record[7] = currentIndices.getBetaDiversity(BetaIndex.Nestedness);
		record[8] = newIndices.getBetaDiversity(BetaIndex.Simpson);
		record[9] = newIndices.getBetaDiversity(BetaIndex.Sorensen);
		record[10] = newIndices.getBetaDiversity(BetaIndex.Nestedness);
		return record;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void testThisSample(Population pop, int sampleSize, int nbRealizations, DiversityIndices populationParameters, CSVWriter writer) throws Exception {
		MultipleSiteIndex msi = new MultipleSiteIndex();
		for (int real = 0; real < nbRealizations; real++) {
			Map sample = SamplingUtility.getSample(pop, sampleSize);
			DiversityIndicesEstimates estimatedIndices = msi.getDissimilarityIndicesMultiplesiteEstimator(sample, pop.size(), true, Mode.LeaveOneOut);
			SimpleEstimate betaDiversity = estimatedIndices.getBetaDiversity();
			Object[] record = new Object[13];
			record[0] = sampleSize;
			record[1] = populationParameters.getGammaDiversity();
			record[2] = populationParameters.getBetaDiversity(BetaIndex.Simpson);
			record[3] = populationParameters.getBetaDiversity(BetaIndex.Sorensen);
			record[4] = populationParameters.getBetaDiversity(BetaIndex.Nestedness);
			record[5] = estimatedIndices.getGammaDiversity().getMean().m_afData[0][0];
			record[6] = estimatedIndices.getGammaDiversity().getVariance().m_afData[0][0];
			record[7] = betaDiversity.getMean().m_afData[0][0];
			record[8] = betaDiversity.getVariance().m_afData[0][0];
			record[9] = betaDiversity.getMean().m_afData[1][0];
			record[10] = betaDiversity.getVariance().m_afData[1][1];
			record[11] = betaDiversity.getMean().m_afData[2][0];
			record[12] = betaDiversity.getVariance().m_afData[2][2];
			writer.addRecord(record);
		}
	}

	public static void main(String[] args) throws Exception {
		String rootPath = ObjectUtility.getPackagePath(PopulationTests.class).replace("bin", "manuscripts");
		String filename;
		CSVWriter writer;
		List<FormatField> fields;
		
		
//		filename =  rootPath + "populationTests.csv";
//		writer = new CSVWriter(new File(filename), false);
//		fields = new ArrayList<FormatField>();
//		fields.add(new CSVField("PopSize"));
//		fields.add(new CSVField("speciesRichness"));
//		fields.add(new CSVField("averageSpecies"));
//		fields.add(new CSVField("scale1"));
//		fields.add(new CSVField("scale2"));
//		fields.add(new CSVField("Simpson"));
//		fields.add(new CSVField("Sorensen"));
//		fields.add(new CSVField("Nestedness"));
//		fields.add(new CSVField("SimpsonCorr"));
//		fields.add(new CSVField("SorensenCorr"));
//		fields.add(new CSVField("NestednessCorr"));
//		writer.setFields(fields);
//		
//		List<Integer> populationSizes = new ArrayList<Integer>();
//		populationSizes.add(20);
//		populationSizes.add(30);
//		populationSizes.add(40);
//		populationSizes.add(50);
//		populationSizes.add(60);
//		populationSizes.add(70);
//		populationSizes.add(80);
//		populationSizes.add(90);
//		populationSizes.add(100);
//		populationSizes.add(200);
//		populationSizes.add(300);
//		populationSizes.add(400);
//		populationSizes.add(500);
//		populationSizes.add(600);
//		populationSizes.add(700);
//		populationSizes.add(800);
//		populationSizes.add(900);
//		populationSizes.add(1000);
//		
//		for (BetaDistributionType type : BetaDistributionType.values()) {
//			for (Integer populationSize : populationSizes) {
//				for (int j = 0; j < 4; j++) {
//					int nbTaxa = 100 - j * 20;
//					System.out.println("Simulation population size " + populationSize + " with species richness = " + nbTaxa + " and species distribution as " + type.name());
//					Object[] record = PopulationTests.testThisBetaPopulation(populationSize, nbTaxa, type);
//					writer.addRecord(record);
//				}
//			}
//		}
//		writer.close();
		
		Map<String, Population> populationMap = new HashMap<String, Population>();
		String prefix = "pop1000_";
		for (BetaDistributionType type : BetaDistributionType.values()) {
			for (int j = 3; j >= 0; j--) {
				int pool = j * 20 + 40;
				String suffixe = pool + "_" + type.name();
				filename = prefix + suffixe;
				XmlDeserializer deserializer = new XmlDeserializer(rootPath + filename);
				System.out.println("Loading file : " + filename);
				Population pop = (Population) deserializer.readObject();
				populationMap.put(suffixe, pop);
			}
		}

		
		
		List<Integer> sampleSizes = new ArrayList<Integer>();
		sampleSizes.add(5);
		sampleSizes.add(10);
		sampleSizes.add(25);
		sampleSizes.add(50);

		int nbRealizations = 10000;
		for (String key : populationMap.keySet()) {
			Population pop = populationMap.get(key);
			DiversityIndices newIndices = new MultipleSiteIndex().getAdaptedMultiplesiteDissimilarityIndices(pop);

			filename = rootPath + "sampleTest" + key + ".csv";
			writer = new CSVWriter(new File(filename), false);
			fields = new ArrayList<FormatField>();
			fields.add(new CSVField("SampleSize"));
			fields.add(new CSVField("SpeciesRichnessTrue"));
			fields.add(new CSVField("SimpsonTrue"));
			fields.add(new CSVField("SorensenTrue"));
			fields.add(new CSVField("NestednessTrue"));
			fields.add(new CSVField("SpeciesRichnessPoint"));
			fields.add(new CSVField("SpeciesRichnessVar"));
			fields.add(new CSVField("SimpsonPoint"));
			fields.add(new CSVField("SimpsonVar"));
			fields.add(new CSVField("SorensenPoint"));
			fields.add(new CSVField("SorensenVar"));
			fields.add(new CSVField("NestednessPoint"));
			fields.add(new CSVField("NestednessVar"));
			writer.setFields(fields);
			for (Integer sampleSize : sampleSizes) {
				System.out.println("Testing sample size " + sampleSize + " on population " + key);
				PopulationTests.testThisSample(pop, sampleSize, nbRealizations, newIndices, writer);
			}
			writer.close();
		}
	}
	
}
