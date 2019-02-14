package canforservutility.biodiversity.indices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.stats.estimates.SimpleEstimate;

public class MultipleSiteIndex {
	
	static enum IndexName {Simpson, Sorensen}
	
	private static class DissimilarityFeatures {
		final int totalNbSpecies;
		final int nbPlots;
		final int sumS_i;
		final int sumMin_ij;
		final int sumMax_ij;
		
		DissimilarityFeatures(int totalNbSpecies, int nbPlots, int sumS_i, int sumMin_ij, int sumMax_ij) {
			this.totalNbSpecies = totalNbSpecies;
			this.nbPlots = nbPlots;
			this.sumS_i = sumS_i;
			this.sumMin_ij = sumMin_ij;
			this.sumMax_ij = sumMax_ij;
		}
	}
	
	@SuppressWarnings("serial")
	static class SpeciesFreqMap extends HashMap<Object, Integer> {
		
		int getNbSpeciesWithThisFreq(int f) {
			int nb = 0;
			for (Integer i : values()) {
				if (i == f) {
					nb++;
				}
			}
			return nb;
		}

	}
	
	@SuppressWarnings("serial")
	private static class ValidatedHashMap<K,V> extends HashMap<K,V> {
		
		ValidatedHashMap<K,V> getMapWithoutThisKey(K key) {
			ValidatedHashMap<K,V> newMap = new ValidatedHashMap<K,V>();
			for (K k : keySet()) {
				if (!k.equals(key)) {
					newMap.put(k, get(k));
				}
			}
			return newMap;
		}
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getUniqueSpeciesList(Map<String, List> oMap) {
		List speciesList = new ArrayList();
		for (List innerList : oMap.values()) {
			for (Object sp : innerList) {
				if (!speciesList.contains(sp)) {
					speciesList.add(sp);
				}
			}
		}
		return speciesList;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ValidatedHashMap<String, List> validateMap(Map<String, List> oMap) {
		if (oMap instanceof ValidatedHashMap) {
			return (ValidatedHashMap) oMap;
		} else {
			ValidatedHashMap<String, List> newMap = new ValidatedHashMap<String, List>();
			for (String key : oMap.keySet()) {
				List value = new ArrayList();
				newMap.put(key, value);
				for (Object s : oMap.get(key)) {
					if (!value.contains(s)) {
						value.add(s);
					}
				}
			}
			return newMap;
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private DissimilarityFeatures getInnerDissimilarity(Map<String, List> oMap) {
		ValidatedHashMap<String, List> validatedMap = validateMap(oMap);
		List completeSpeciesList = getUniqueSpeciesList(validatedMap);
		int totalNbSpecies = completeSpeciesList.size();
		int sumS_i = 0;
		int sumMin_ij = 0;
		int sumMax_ij = 0;  

		int nbPlots = validatedMap.size();
		List<String> codePoints = new ArrayList<String>();
		for (String codePoint : validatedMap.keySet()) {
			codePoints.add(codePoint);
		}

		for (int j = 0; j < codePoints.size(); j++) {
		    List plot_j = validatedMap.get(codePoints.get(j));
		    int nbSpecies_j = plot_j.size();
		    sumS_i += nbSpecies_j;
		    if (j >= 1) {
		    	for (int i = 0; i < j; i++) {
		    		List plot_i = validatedMap.get(codePoints.get(i));
		    		int nbSpecies_i = plot_i.size();
		    		List intersectList = cloneThisList(plot_i);
		    		intersectList.retainAll(plot_j);
		    		int nbCommonSpecies = intersectList.size();
		    		int b_ij = nbSpecies_i - nbCommonSpecies;
		    		int b_ji = nbSpecies_j - nbCommonSpecies;
		    		sumMin_ij += Math.min(b_ij, b_ji);
		    		sumMax_ij += Math.max(b_ij, b_ji);
		    	}
		    }
		}
		return new DissimilarityFeatures(totalNbSpecies, nbPlots, sumS_i, sumMin_ij, sumMax_ij);
	}

	/**
	 * This method returns the multiple-site versions of Simpson's and Sorensen's dissimilarity indices
	 * for a population. 
	 * @param oMap a Map instance that stands for the population of sites
	 * @return a Map with the index names as key and the values of this indices
	 */
	@SuppressWarnings("rawtypes")
	public Map<IndexName, Double> getMultiplesiteDissimilarityIndices(Map<String, List> oMap) {
		DissimilarityFeatures f = getInnerDissimilarity(oMap);

		double simpson = ((double) f.sumMin_ij) / (f.sumS_i - f.totalNbSpecies + f.sumMin_ij);
		double sorensen = ((double) (f.sumMin_ij + f.sumMax_ij)) /(f.sumS_i - f.totalNbSpecies + f.sumMin_ij + f.sumMax_ij);
		Map<IndexName, Double> indexMap = new HashMap<IndexName, Double>();
		indexMap.put(IndexName.Simpson, simpson);
		indexMap.put(IndexName.Sorensen, sorensen);
		return indexMap;
	}
	
	/**
	 * This method returns adapted multiple-site versions of Simpson's and Sorensen's dissimilarity 
	 * indices for a population. 
	 * @param oMap a Map instance that stands for the population of sites
	 * @return a Map with the index names as key and the values of this indices
	 */
	@SuppressWarnings("rawtypes")
	public Map<IndexName, Double> getAdaptedMultiplesiteDissimilarityIndices(Map<String, List> oMap) {
		DissimilarityFeatures f = getInnerDissimilarity(oMap);
		double sumMinCorr = (2d * f.sumMin_ij) / f.nbPlots;
		double sumMaxCorr = (2d * f.sumMax_ij) / f.nbPlots;
		double simpson = sumMinCorr / (f.sumS_i - f.totalNbSpecies + sumMinCorr);
		double sorensen = (sumMinCorr + sumMaxCorr) /(f.sumS_i - f.totalNbSpecies + sumMinCorr + sumMaxCorr);
		Map<IndexName, Double> outputMap = new HashMap<IndexName, Double>();
		outputMap.put(IndexName.Simpson, simpson);
		outputMap.put(IndexName.Sorensen, sorensen);
		return outputMap;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List cloneThisList(List list) {
		List newList = new ArrayList();
		for (Object s : list) {
			newList.add(s);
		}
		return newList;
	}
	
	@SuppressWarnings("rawtypes")
	private SpeciesFreqMap getSpeciesFreqMap(ValidatedHashMap<String, List> vMap) {
		SpeciesFreqMap speciesMap = new SpeciesFreqMap();
		for (List speciesList : vMap.values()) {
			for (Object s : speciesList) {
				if (!speciesMap.containsKey(s)) {
					speciesMap.put(s, 0);
				}
				speciesMap.put(s, speciesMap.get(s) + 1);
			}
		}
		return speciesMap;
	}
	
	/**
	 * THis methord returns the Chao2 estimate of species richness.
	 * @param oMap a Map instance that stands for the sample
	 * @return a SimpleEstimate instance
	 */
	@SuppressWarnings("rawtypes")
	public SimpleEstimate getChao2Estimator(Map<String, List> oMap) {
		ValidatedHashMap<String, List> validatedMap = validateMap(oMap);

		int nbPlots = validatedMap.size();

		SpeciesFreqMap speciesFreqMap = getSpeciesFreqMap(validatedMap);
		int f1 = speciesFreqMap.getNbSpeciesWithThisFreq(1);
		int f2 = speciesFreqMap.getNbSpeciesWithThisFreq(2);
		int s = getUniqueSpeciesList(oMap).size();
		double f1_f2 = ((double) f1) / f2;
		double k = ((double) (nbPlots - 1)) / nbPlots;
		double chao2;
		double variance;
		if (f2 == 0) {
			chao2 = s + ((double) (nbPlots - 1)) / nbPlots * f1 * (f1 - 1) / 2; 
			variance = k * f1 * (f1 - 1) / 2d + k * k * f1 * (2 * f1 - 1) * (2 * f1 - 1) / 4d + k * k * f1 * f1 * f1 * f1  / (4d * chao2); 
		} else {
			chao2 = s + ((double) (nbPlots - 1)) / nbPlots * f1 * f1 / (2d * f2); 
			variance = f2 * (.5 * k * f1_f2 * f1_f2 + k * k * f1_f2 * f1_f2 * f1_f2 + .25 * k * k * f1_f2 * f1_f2 * f1_f2 * f1_f2); 
		}
		SimpleEstimate estimate = new SimpleEstimate();
		Matrix mean = new Matrix(1,1);
		mean.m_afData[0][0] = chao2;
		Matrix var = new Matrix(1,1);
		var.m_afData[0][0] = variance;
		estimate.setMean(mean);
		estimate.setVariance(var);
		return estimate;
	}
	
	private SimpleEstimate getSimpleEstimate(double value) {
		SimpleEstimate estimate = new SimpleEstimate();
		Matrix pointEstimate = new Matrix(1,1);
		pointEstimate.m_afData[0][0] = value;
		estimate.setMean(pointEstimate);
		return estimate;
	}
	
	@SuppressWarnings("rawtypes")
	public Map<IndexName, SimpleEstimate> getDissimilarityIndicesMultiplesiteEstimator(Map<String, List> oMap, int populationSize, boolean jackknife) {
		ValidatedHashMap<String, List> vMap = validateMap(oMap);
		SimpleEstimate chao2Estimate = getChao2Estimator(vMap);
		DissimilarityFeatures f = getInnerDissimilarity(vMap);
		double meanMin_hat = (2d * f.sumMin_ij) / (f.nbPlots * (f.nbPlots - 1));
		double meanMax_hat = (2d * f.sumMax_ij) / (f.nbPlots * (f.nbPlots - 1));
		double meanS_hat = ((double) f.sumS_i) / f.nbPlots;
		double totalS_hat = chao2Estimate.getMean().m_afData[0][0];

		Map<IndexName, SimpleEstimate> outputMap = new HashMap<IndexName, SimpleEstimate>();
		double simpson = (populationSize-1) * meanMin_hat / (populationSize * meanS_hat - totalS_hat + (populationSize-1) * meanMin_hat);
		if (simpson < 0 || simpson > 1) {
			SimpleEstimate chao2Estimate2 = getChao2Estimator(vMap);
			int u = 0;
		}
		outputMap.put(IndexName.Simpson, getSimpleEstimate(simpson));

		double sorensen = (populationSize-1) * (meanMin_hat + meanMax_hat) / (populationSize * meanS_hat - totalS_hat + (populationSize-1) * (meanMin_hat + meanMax_hat));
		outputMap.put(IndexName.Sorensen, getSimpleEstimate(sorensen));


		if (jackknife) {
			Map<IndexName, MonteCarloEstimate> varianceEstimates = new HashMap<IndexName, MonteCarloEstimate>();
			varianceEstimates.put(IndexName.Simpson, new MonteCarloEstimate());
			varianceEstimates.put(IndexName.Sorensen, new MonteCarloEstimate());
			int nbPlots = vMap.size();
			for (String siteId : vMap.keySet()) {
				ValidatedHashMap<String, List> newMap = vMap.getMapWithoutThisKey(siteId);
				Map<IndexName, SimpleEstimate> estimateMap = getDissimilarityIndicesMultiplesiteEstimator(newMap, populationSize, false);
				varianceEstimates.get(IndexName.Simpson).addRealization(estimateMap.get(IndexName.Simpson).getMean());
				varianceEstimates.get(IndexName.Sorensen).addRealization(estimateMap.get(IndexName.Sorensen).getMean());
			}
			outputMap.get(IndexName.Simpson).setVariance(varianceEstimates.get(IndexName.Simpson).getVariance().scalarMultiply(((double) (nbPlots - 1) * (nbPlots - 1)) / nbPlots));
			outputMap.get(IndexName.Sorensen).setVariance(varianceEstimates.get(IndexName.Sorensen).getVariance().scalarMultiply(((double) (nbPlots - 1) * (nbPlots - 1)) / nbPlots));
		} 
		return outputMap;
	}


	public static void main(String[] args) {
		List<String> spList1 = new ArrayList<String>();
		spList1.add("carotte");
		spList1.add("carotte");
		spList1.add("patate");

		List<String> spList2 = new ArrayList<String>();
		spList2.add("carotte");
		spList2.add("navet");
		Map<String, List> oMap = new HashMap<String, List>();
		oMap.put("1", spList1);
		oMap.put("2", spList2);
		MultipleSiteIndex msi = new MultipleSiteIndex();
		Map result = msi.getMultiplesiteDissimilarityIndices(oMap);
		msi.getChao2Estimator(oMap);
		int u = 0;
	}
	
	
}
