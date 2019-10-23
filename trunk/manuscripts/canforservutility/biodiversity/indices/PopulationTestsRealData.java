package canforservutility.biodiversity.indices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.biodiversity.indices.DiversityIndices.BetaIndex;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.serial.xml.XmlSerializer;
import repicea.stats.sampling.SamplingUtility;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class PopulationTestsRealData {

	private class Plot extends ArrayList<String> {
		private Plot() {
			super();
		}
	}

	@SuppressWarnings("rawtypes")
	private class Population extends HashMap<String, List> {}

	
	private Map<String, Population> readData() throws IOException {
		Map<String, Population> populations = new HashMap<String, Population>();
		String filename = ObjectUtility.getPackagePath(getClass()) + "bioclimDomain.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			String plotId = record[1].toString();
			String domBio = record[6].toString();
			String species = record[4].toString();
			if (!populations.containsKey(domBio)) {
				populations.put(domBio, new Population());
			}
			Population pop = populations.get(domBio);
			if (!pop.containsKey(plotId)) {
				pop.put(plotId, new Plot());
			}
			Plot plot = (Plot) pop.get(plotId);
			if (!species.equals("NA")) {
				plot.add(species);
			} 
		}
		reader.close();
		return populations;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object[] testThisSubPopulation(Population originalPopulation, int populationSize, String suffix) throws Exception {
		Map pop = SamplingUtility.getSample(originalPopulation, populationSize);
		if (populationSize == 1000) {
			XmlSerializer serializer = new XmlSerializer(ObjectUtility.getPackagePath(PopulationTests.class).replace("bin", "manuscripts") + "pop" + suffix + populationSize);
			serializer.writeObject(pop);
		}
		MultipleSiteIndex msi = new MultipleSiteIndex();
		DiversityIndices currentIndices = msi.getMultiplesiteDissimilarityIndices(pop);
		DiversityIndices newIndices = msi.getAdaptedMultiplesiteDissimilarityIndices(pop);
		Object[] record = new Object[10];
		record[0] = populationSize;
		record[1] = newIndices.getGammaDiversity();
		record[2] = newIndices.getAlphaDiversity();
		record[3] = suffix;
		record[4] = currentIndices.getBetaIndex(BetaIndex.Simpson);
		record[5] = currentIndices.getBetaIndex(BetaIndex.Sorensen);
		record[6] = currentIndices.getBetaIndex(BetaIndex.Nestedness);
		record[7] = newIndices.getBetaIndex(BetaIndex.Simpson);
		record[8] = newIndices.getBetaIndex(BetaIndex.Sorensen);
		record[9] = newIndices.getBetaIndex(BetaIndex.Nestedness);
		return record;
	}

	
	
	public static void main(String[] args) throws Exception {
		String rootPath = ObjectUtility.getPackagePath(PopulationTests.class).replace("bin", "manuscripts");
		String filename;
		CSVWriter writer;
		List<FormatField> fields;
		
		
		filename =  rootPath + "populationRealDataTests.csv";
		writer = new CSVWriter(new File(filename), false);
		fields = new ArrayList<FormatField>();
		fields.add(new CSVField("PopSize"));
		fields.add(new CSVField("speciesRichness"));
		fields.add(new CSVField("averageSpecies"));
		fields.add(new CSVField("bioClimDomain"));
		fields.add(new CSVField("Simpson"));
		fields.add(new CSVField("Sorensen"));
		fields.add(new CSVField("Nestedness"));
		fields.add(new CSVField("SimpsonCorr"));
		fields.add(new CSVField("SorensenCorr"));
		fields.add(new CSVField("NestednessCorr"));
		writer.setFields(fields);
		
		List<Integer> populationSizes = new ArrayList<Integer>();
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

		PopulationTestsRealData instance = new PopulationTestsRealData();
		Map<String, Population> populations = instance.readData();
		for (String bioClimDomain : populations.keySet()) {
			Population originalPopulation = populations.get(bioClimDomain);
			if (originalPopulation.size() > 1000) {
				for (Integer populationSize : populationSizes) {
					System.out.println("Simulation population size " + populationSize + " with bioclimatic domain = " + bioClimDomain);
					Object[] record = instance.testThisSubPopulation(originalPopulation, populationSize, bioClimDomain);
					writer.addRecord(record);
				}
			}
		}
		writer.close();
	}
}
