package canforservutility.biodiversity.indices;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.biodiversity.indices.MultipleSiteIndex.BetaIndex;
import canforservutility.biodiversity.indices.MultipleSiteIndex.DiversityIndex;
import canforservutility.biodiversity.indices.MultipleSiteIndex.Mode;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.stats.estimates.SimpleEstimate;
import repicea.stats.sampling.SamplingUtility;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
class PopulationTests {

	class Plot extends ArrayList<Integer> {
		
		Plot(List<Integer> list) {
			for (Integer i : list) {
				add(i);
			}
		}
		
	}

	@SuppressWarnings("rawtypes")
	class Population extends HashMap<String, List> {}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	Population createPopulation(List<Integer> originalList, int populationSize, int minNb, int maxNb) {
		Population pop = new Population();
		List<Integer> possibleNb = new ArrayList<Integer>();
		for (int nb = minNb; nb <= maxNb; nb++) {
			possibleNb.add(nb);
		}
		int nbSp;
		for (int i = 0; i < populationSize; i++) {
			if (maxNb == minNb) {
				nbSp = minNb;
			} else {
				nbSp = (Integer) SamplingUtility.getSample(possibleNb, 1).get(0);
			}
			Plot newPlot = new Plot((List) SamplingUtility.getSample(originalList, nbSp));

			pop.put(((Integer) i).toString(), newPlot);
		}
		return pop;
	}
	
	
	
	private static Object[] testThisPopulation(int populationSize, int minSp, int maxSp, int nbTaxa) throws Exception {
		List<Integer> originalList = new ArrayList<Integer>();
		for (int i = 0; i < nbTaxa; i++) {
			originalList.add(i);
		}
		
		PopulationTests popTest = new PopulationTests();
		Population pop = popTest.createPopulation(originalList, populationSize, minSp, maxSp);
		if (populationSize == 1000) {
			XmlSerializer serializer = new XmlSerializer(ObjectUtility.getPackagePath(PopulationTests.class).replace("bin", "manuscripts") + "pop" + populationSize + "_" + minSp + "_" + maxSp);
			serializer.writeObject(pop);
		}
		MultipleSiteIndex msi = new MultipleSiteIndex();
		Map<BetaIndex, Double> currentIndices = msi.getMultiplesiteDissimilarityIndices(pop);
		Map<BetaIndex, Double> newIndices = msi.getAdaptedMultiplesiteDissimilarityIndices(pop);
		Object[] record = new Object[9];
		record[0] = populationSize;
		record[1] = minSp;
		record[2] = maxSp;
		record[3] = currentIndices.get(BetaIndex.Simpson);
		record[4] = currentIndices.get(BetaIndex.Sorensen);
		record[5] = currentIndices.get(BetaIndex.Nestedness);
		record[6] = newIndices.get(BetaIndex.Simpson);
		record[7] = newIndices.get(BetaIndex.Sorensen);
		record[8] = newIndices.get(BetaIndex.Nestedness);
		return record;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void testThisSample(Population pop, int sampleSize, int nbRealizations, Map<BetaIndex, Double> populationParameters, CSVWriter writer) throws Exception {
		MultipleSiteIndex msi = new MultipleSiteIndex();
		for (int real = 0; real < nbRealizations; real++) {
			Map sample = SamplingUtility.getSample(pop, sampleSize);
			Map<DiversityIndex, SimpleEstimate> indices = msi.getDissimilarityIndicesMultiplesiteEstimator(sample, pop.size(), true, Mode.LeaveOneOut);
			SimpleEstimate betaDiversity = indices.get(DiversityIndex.Beta);
			Object[] record = new Object[10];
			record[0] = sampleSize;
			record[1] = populationParameters.get(BetaIndex.Simpson);
			record[2] = populationParameters.get(BetaIndex.Sorensen);
			record[3] = populationParameters.get(BetaIndex.Nestedness);
			record[4] = betaDiversity.getMean().m_afData[0][0];
			record[5] = betaDiversity.getVariance().m_afData[0][0];
			record[6] = betaDiversity.getMean().m_afData[1][0];
			record[7] = betaDiversity.getVariance().m_afData[1][1];
			record[8] = betaDiversity.getMean().m_afData[2][0];
			record[9] = betaDiversity.getVariance().m_afData[2][2];
			writer.addRecord(record);
		}
	}

	public static void main(String[] args) throws Exception {
		String rootPath = ObjectUtility.getPackagePath(PopulationTests.class).replace("bin", "manuscripts");
		String filename;
		CSVWriter writer;
		List<FormatField> fields;
		
		
		filename =  rootPath + "populationTests.csv";
		writer = new CSVWriter(new File(filename), false);
		fields = new ArrayList<FormatField>();
		fields.add(new CSVField("PopSize"));
		fields.add(new CSVField("minSp"));
		fields.add(new CSVField("maxSp"));
		fields.add(new CSVField("Simpson"));
		fields.add(new CSVField("Sorensen"));
		fields.add(new CSVField("Nestedness"));
		fields.add(new CSVField("SimpsonCorr"));
		fields.add(new CSVField("SorensenCorr"));
		fields.add(new CSVField("NestednessCorr"));
		writer.setFields(fields);
		
		List<Integer> populationSizes = new ArrayList<Integer>();
		populationSizes.add(5);
		populationSizes.add(10);
		populationSizes.add(20);
		populationSizes.add(30);
		populationSizes.add(40);
		populationSizes.add(50);
		populationSizes.add(60);
		populationSizes.add(70);
		populationSizes.add(80);
		populationSizes.add(90);
		populationSizes.add(100);
		populationSizes.add(200);
		populationSizes.add(300);
		populationSizes.add(400);
		populationSizes.add(500);
		populationSizes.add(600);
		populationSizes.add(700);
		populationSizes.add(800);
		populationSizes.add(900);
		populationSizes.add(1000);
		
		for (int i = 0; i < 4; i++) {
			int minSp = i * 10 + 1;
			int maxSp = (i + 1) * 10;
			for (Integer populationSize : populationSizes) {
				int nbTaxa = 100 - i * 20;
				System.out.println("Simulation population size " + populationSize + " with minSp = " + minSp + " and maxSp = " + maxSp + " and nbTaxa = " + nbTaxa);
				Object[] record = PopulationTests.testThisPopulation(populationSize, minSp, maxSp, nbTaxa);
				writer.addRecord(record);
			}
		}
		writer.close();
		
		Map<String, Population> populationMap = new HashMap<String, Population>();
		String prefix = "pop1000_";
		for (int i = 0; i < 4; i++) {
			int min = i * 10 + 1;
			int max = (i + 1) * 10;
			for (int j = 3; j >= 0; j--) {
				int pool = j * 20 + 40;
				String suffixe = min + "_" + max + "_" + pool;
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
			Map<BetaIndex, Double> newIndices = new MultipleSiteIndex().getAdaptedMultiplesiteDissimilarityIndices(pop);

			filename = rootPath + "sampleTest" + key + ".csv";
			writer = new CSVWriter(new File(filename), false);
			fields = new ArrayList<FormatField>();
			fields.add(new CSVField("SampleSize"));
			fields.add(new CSVField("SimpsonTrue"));
			fields.add(new CSVField("SorensenTrue"));
			fields.add(new CSVField("NestednessTrue"));
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
