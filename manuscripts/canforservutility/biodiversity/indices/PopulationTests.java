package canforservutility.biodiversity.indices;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.biodiversity.indices.MultipleSiteIndex.IndexName;
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
		Map<IndexName, Double> currentIndices = msi.getMultiplesiteDissimilarityIndices(pop);
		Map<IndexName, Double> newIndices = msi.getAdaptedMultiplesiteDissimilarityIndices(pop);
		Object[] record = new Object[7];
		record[0] = populationSize;
		record[1] = minSp;
		record[2] = maxSp;
		record[3] = currentIndices.get(IndexName.Simpson);
		record[4] = currentIndices.get(IndexName.Sorensen);
		record[5] = newIndices.get(IndexName.Simpson);
		record[6] = newIndices.get(IndexName.Sorensen);
		return record;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void testThisSample(Population pop, int sampleSize, int nbRealizations, Map<IndexName, Double> populationParameters, CSVWriter writer) throws Exception {
		MultipleSiteIndex msi = new MultipleSiteIndex();
		for (int real = 0; real < nbRealizations; real++) {
			Map sample = SamplingUtility.getSample(pop, sampleSize);
			Map<IndexName, SimpleEstimate> indices = msi.getDissimilarityIndicesMultiplesiteEstimator(sample, pop.size(), true);
			Object[] record = new Object[7];
			record[0] = sampleSize;
			record[1] = populationParameters.get(IndexName.Simpson);
			record[2] = populationParameters.get(IndexName.Sorensen);
			record[3] = indices.get(IndexName.Simpson).getMean().m_afData[0][0];
			record[4] = indices.get(IndexName.Simpson).getVariance().m_afData[0][0];
			record[5] = indices.get(IndexName.Sorensen).getMean().m_afData[0][0];
			record[6] = indices.get(IndexName.Sorensen).getVariance().m_afData[0][0];
			writer.addRecord(record);
		}
	}

	public static void main(String[] args) throws Exception {
		String rootPath = ObjectUtility.getPackagePath(PopulationTests.class).replace("bin", "manuscripts");
//		String filename =  rootPath + "populationTests.csv";
//		CSVWriter writer = new CSVWriter(new File(filename), false);
//		List<FormatField> fields = new ArrayList<FormatField>();
//		fields.add(new CSVField("PopSize"));
//		fields.add(new CSVField("minSp"));
//		fields.add(new CSVField("maxSp"));
//		fields.add(new CSVField("Simpson"));
//		fields.add(new CSVField("Sorensen"));
//		fields.add(new CSVField("SimpsonCorr"));
//		fields.add(new CSVField("SorensenCorr"));
//		writer.setFields(fields);
//		
//		List<Integer> populationSizes = new ArrayList<Integer>();
//		populationSizes.add(5);
//		populationSizes.add(10);
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
//		for (int i = 0; i < 4; i++) {
//			int minSp = i * 10 + 1;
//			int maxSp = (i + 1) * 10;
//			for (Integer populationSize : populationSizes) {
//				int nbTaxa = 100 - i * 20;
//				System.out.println("Simulation population size " + populationSize + " with minSp = " + minSp + " and maxSp = " + maxSp + " and nbTaxa = " + nbTaxa);
//				Object[] record = PopulationTests.testThisPopulation(populationSize, minSp, maxSp, nbTaxa);
//				writer.addRecord(record);
//			}
//		}
//		writer.close();
		
		
		XmlDeserializer deserializer = new XmlDeserializer(rootPath + "pop1000_1_10");
		Population pop01_10 = (Population) deserializer.readObject();

		deserializer = new XmlDeserializer(rootPath + "pop1000_11_20");
		Population pop11_20 = (Population) deserializer.readObject();

		deserializer = new XmlDeserializer(rootPath + "pop1000_21_30");
		Population pop21_30 = (Population) deserializer.readObject();

		deserializer = new XmlDeserializer(rootPath + "pop1000_31_40");
		Population pop31_40 = (Population) deserializer.readObject();

		Map<String, Population> populationMap = new HashMap<String, Population>();
		populationMap.put("01_10", pop01_10);
		populationMap.put("11_20", pop11_20);
		populationMap.put("21_30", pop21_30);
		populationMap.put("31_40", pop31_40);
		
		
		List<Integer> sampleSizes = new ArrayList<Integer>();
		sampleSizes.add(5);
		sampleSizes.add(10);
		sampleSizes.add(25);
		sampleSizes.add(50);

		int nbRealizations = 10000;
		for (String key : populationMap.keySet()) {
			Population pop = populationMap.get(key);
			Map<IndexName, Double> newIndices = new MultipleSiteIndex().getAdaptedMultiplesiteDissimilarityIndices(pop);

			String filename = rootPath + "sampleTest" + key + ".csv";
			CSVWriter writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("SampleSize"));
			fields.add(new CSVField("SimpsonTrue"));
			fields.add(new CSVField("SorensenTrue"));
			fields.add(new CSVField("SimpsonPoint"));
			fields.add(new CSVField("SimpsonVar"));
			fields.add(new CSVField("SorensenPoint"));
			fields.add(new CSVField("SorensenVar"));
			writer.setFields(fields);
			for (Integer sampleSize : sampleSizes) {
				System.out.println("Testing sample size " + sampleSize + " on population " + key);
				PopulationTests.testThisSample(pop, sampleSize, nbRealizations, newIndices, writer);
			}
			writer.close();
		}
	}
	
}
