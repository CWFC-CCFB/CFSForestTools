package canforservutility.biodiversity.indices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

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
		return populations;
	}

	public static void main(String[] args) throws IOException {
		PopulationTestsRealData obj = new PopulationTestsRealData();
		Map<String, Population> populations = obj.readData();
	}
}
