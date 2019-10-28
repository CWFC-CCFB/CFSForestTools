package canforservutility.biodiversity.indices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import canforservutility.biodiversity.indices.DiversityIndices.BetaIndex;

@SuppressWarnings("serial")
class PopulationTestsBaselga {

	private static class Plot extends ArrayList<Integer> {
		private Plot(Integer...integers) {
			super();
			for (Integer integer : integers) {
				add(integer);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static class Population extends HashMap<String, List> {}

	private static enum Pattern {
		A,B,C,D;
	}
	
	private static Population createPopulationA() {
		Population pop = new Population();
		Pattern p = Pattern.A;
		String prefix = "Site " + p.name();
		for (int site = 1; site <= 4; site++) {
			if (site == 1) {
				pop.put(prefix + site, new Plot(1,2,3)); 
			} else if (site == 2) {
				pop.put(prefix + site, new Plot(1,2,4));
			} else if (site == 3) {
				pop.put(prefix + site, new Plot(1,3,4));
			} else if (site == 4) {
				pop.put(prefix + site, new Plot(2,3,4));
			}
		}
		return pop;
	}

	private static Population createPopulationB() {
		Population pop = new Population();
		Pattern p = Pattern.B;
		String prefix = "Site " + p.name();
		for (int site = 1; site <= 4; site++) {
			if (site == 1) {
				pop.put(prefix + site, new Plot(1,2,3)); 
			} else if (site == 2) {
				pop.put(prefix + site, new Plot(1,2,4));
			} else if (site == 3) {
				pop.put(prefix + site, new Plot(1,2,5));
			} else if (site == 4) {
				pop.put(prefix + site, new Plot(1,2,6));
			}
		}
		return pop;
	}

	private static Population createPopulationC() {
		Population pop = new Population();
		Pattern p = Pattern.C;
		String prefix = "Site " + p.name();
		for (int site = 1; site <= 4; site++) {
			if (site == 1) {
				pop.put(prefix + site, new Plot(1,2,3,4)); 
			} else if (site == 2) {
				pop.put(prefix + site, new Plot(1,2,3));
			} else if (site == 3) {
				pop.put(prefix + site, new Plot(1,2));
			} else if (site == 4) {
				pop.put(prefix + site, new Plot(1));
			}
		}
		return pop;
	}

	private static Population createPopulationD() {
		Population pop = new Population();
		Pattern p = Pattern.D;
		String prefix = "Site " + p.name();
		for (int site = 1; site <= 4; site++) {
			if (site == 1) {
				pop.put(prefix + site, new Plot(1,2,3,4)); 
			} else if (site == 2) {
				pop.put(prefix + site, new Plot(1));
			} else if (site == 3) {
				pop.put(prefix + site, new Plot(1));
			} else if (site == 4) {
				pop.put(prefix + site, new Plot(1));
			}
		}
		return pop;
	}

	public static void main(String[] args) throws Exception {
		Map<Pattern, Population> populations = new TreeMap<Pattern, Population>();
		for (Pattern p : Pattern.values()) {
			switch(p) {
			case A:
				populations.put(p, createPopulationA());
				break;
			case B:
				populations.put(p, createPopulationB());
				break;
			case C:
				populations.put(p, createPopulationC());
				break;
			case D:
				populations.put(p, createPopulationD());
				break;
			}
		}
		
		MultipleSiteIndex msi = new MultipleSiteIndex();
		for (Pattern p : populations.keySet()) {
			Population pop = populations.get(p);
			DiversityIndices currentIndices = msi.getMultiplesiteDissimilarityIndices(pop);
			DiversityIndices newIndices = msi.getAdaptedMultiplesiteDissimilarityIndices(pop);
			System.out.println("Site " + p.name() + " Current Simpson = " + currentIndices.getBetaIndex(BetaIndex.Simpson));
			System.out.println("Site " + p.name() + " Current Sorensen = " + currentIndices.getBetaIndex(BetaIndex.Sorensen));
			System.out.println("Site " + p.name() + " Current nestedness = " + currentIndices.getBetaIndex(BetaIndex.Nestedness));
			System.out.println("Site " + p.name() + " Adapted Simpson = " + newIndices.getBetaIndex(BetaIndex.Simpson));
			System.out.println("Site " + p.name() + " Adapted Sorensen = " + newIndices.getBetaIndex(BetaIndex.Sorensen));
			System.out.println("Site " + p.name() + " Adapted nestedness = " + newIndices.getBetaIndex(BetaIndex.Nestedness));
		}

	}
	
}
