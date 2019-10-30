/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Forest Service
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.biodiversity.indices;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.biodiversity.indices.DiversityIndices.BetaIndex;
import canforservutility.biodiversity.indices.IndexUtility.ValidatedHashMap;
import repicea.math.Matrix;
import repicea.stats.estimates.JackknifeEstimate;
import repicea.stats.estimates.SimpleEstimate;
import repicea.util.ObjectUtility;

/**
 * This class implements the multiple site versions of Simpson and Sorensen dissimilarity indices. 
 * This multiple site versions are described in Baselga (2010). Adapted versions to make these indices
 * population size independent are also provided. Estimators of the adapted versions are available.
 * 
 * @author Mathieu Fortin - February 2019
 * 
 * @see <a href=https://onlinelibrary.wiley.com/doi/full/10.1111/j.1466-8238.2009.00490.x> Baselga, A. 
 * 2010. Partitioning the turnover and nestedness components of beta diversity. Global Ecology and 
 * Biogeography 19(1): 134-143.
 * </a>
 */
public class MultipleSiteIndex {
	
	
	public static enum Mode {LeaveOneOut, DeleteTwo}
	
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
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private DissimilarityFeatures getInnerDissimilarity(Map<String, List> oMap) {
		ValidatedHashMap<String, List> validatedMap = IndexUtility.validateMap(oMap);
		List completeSpeciesList = IndexUtility.getUniqueSpeciesList(validatedMap);
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
		    		List intersectList = ObjectUtility.copyList(plot_i);
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
	 * @return a DiversityIndices instance
	 */
	@SuppressWarnings("rawtypes")
	public DiversityIndices getMultiplesiteDissimilarityIndices(Map<String, List> oMap) {
		DissimilarityFeatures f = getInnerDissimilarity(oMap);

		double simpson = ((double) f.sumMin_ij) / (f.sumS_i - f.totalNbSpecies + f.sumMin_ij);
		double sorensen = ((double) (f.sumMin_ij + f.sumMax_ij)) /(2 * (f.sumS_i - f.totalNbSpecies) + f.sumMin_ij + f.sumMax_ij);
		double nestedness = sorensen - simpson;
		
		DiversityIndices di = new DiversityIndices();
		di.setAlphaDiversity(f.sumS_i / f.nbPlots);
		di.setGammaDiversity(f.totalNbSpecies);
		di.setBetaDiversity(BetaIndex.Simpson, simpson);
		di.setBetaDiversity(BetaIndex.Sorensen, sorensen);
		di.setBetaDiversity(BetaIndex.Nestedness, nestedness);

		return di;
	}
	
	/**
	 * This method returns adapted multiple-site versions of Simpson's and Sorensen's dissimilarity 
	 * indices for a population. 
	 * @param oMap a Map instance that stands for the population of sites
	 * @return a Map with the index names as key and the values of this indices
	 */
	@SuppressWarnings("rawtypes")
	public DiversityIndices getAdaptedMultiplesiteDissimilarityIndices(Map<String, List> oMap) {
		DissimilarityFeatures f = getInnerDissimilarity(oMap);
		double sumMinCorr = (2d * f.sumMin_ij) / f.nbPlots;
		double sumMaxCorr = (2d * f.sumMax_ij) / f.nbPlots;
		double simpson = sumMinCorr / (f.sumS_i - f.totalNbSpecies + sumMinCorr);
		double sorensen = (sumMinCorr + sumMaxCorr) /(2 * (f.sumS_i - f.totalNbSpecies) + sumMinCorr + sumMaxCorr);
		double nestedness = sorensen - simpson;

		DiversityIndices di = new DiversityIndices();
		di.setAlphaDiversity(f.sumS_i / f.nbPlots);
		di.setGammaDiversity(f.totalNbSpecies);
		di.setBetaDiversity(BetaIndex.Simpson, simpson);
		di.setBetaDiversity(BetaIndex.Sorensen, sorensen);
		di.setBetaDiversity(BetaIndex.Nestedness, nestedness);
		
		return di;
	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private List cloneThisList(List list) {
//		List newList = new ArrayList();
//		for (Object s : list) {
//			newList.add(s);
//		}
//		return newList;
//	}
	
	
	
	private SimpleEstimate getSimpleEstimate(double value) {
		SimpleEstimate estimate = new SimpleEstimate();
		Matrix pointEstimate = new Matrix(1,1);
		pointEstimate.m_afData[0][0] = value;
		estimate.setMean(pointEstimate);
		return estimate;
	}
	
	/**
	 * This method implements estimators of the adapted dissmilarity indices.
	 * @param oMap the sample
	 * @param populationSize the size of the population
	 * @param jackknife a boolean that enables the jackknife variance estimation 
	 * @return a Map that contains the indices
	 */
	@SuppressWarnings("rawtypes")
	public DiversityIndicesEstimates getDissimilarityIndicesMultiplesiteEstimator(Map<String, List> oMap, int populationSize, boolean jackknife, Mode mode) {
		ValidatedHashMap<String, List> vMap = IndexUtility.validateMap(oMap);
		if (jackknife && vMap.size() <= 2 && mode != Mode.LeaveOneOut) {
			throw new InvalidParameterException("There must be at least three observations in the sample to use the Delete-2 or Efron and Stein's (1981) correction!");
		}
		
		SimpleEstimate chao2Estimate = Chao2Estimator.getChao2Estimate(vMap, populationSize);
		SimpleEstimate chao2Estimate2 = Chao2Estimator.getChao2Estimate(vMap);
		DissimilarityFeatures f = getInnerDissimilarity(vMap);
		double meanMin_hat = (2d * f.sumMin_ij) / (f.nbPlots * (f.nbPlots - 1));
		double meanMax_hat = (2d * f.sumMax_ij) / (f.nbPlots * (f.nbPlots - 1));
		double meanS_hat = ((double) f.sumS_i) / f.nbPlots;
		double totalS_hat = chao2Estimate.getMean().m_afData[0][0];

		DiversityIndicesEstimates di = new DiversityIndicesEstimates();
		
		double simpson = (populationSize-1) * meanMin_hat / (populationSize * meanS_hat - totalS_hat + (populationSize-1) * meanMin_hat);
		double sorensen = (populationSize-1) * (meanMin_hat + meanMax_hat) / (2d * (populationSize * meanS_hat - totalS_hat) + (populationSize-1) * (meanMin_hat + meanMax_hat));
		double nestedness = sorensen - simpson;

		Matrix beta = new Matrix(3,1);
		beta.m_afData[0][0] = simpson;
		beta.m_afData[1][0] = sorensen;
		beta.m_afData[2][0] = nestedness;
		SimpleEstimate betaEstimate = new SimpleEstimate();
		betaEstimate.setMean(beta);
		
		di.setAlphaDiversity(getSimpleEstimate(meanS_hat));
		di.setBetaDiversity(betaEstimate);
		di.setGammaDiversity(chao2Estimate);
		
		if (jackknife) {
			List<String> siteIds = new ArrayList<String>(vMap.keySet());
			int nbPlots = vMap.size();
			JackknifeEstimate varianceEstimate;
			switch(mode) {
			case LeaveOneOut:
				varianceEstimate = new JackknifeEstimate(nbPlots, 1);
				for (int i = 0; i < siteIds.size(); i++) {
					ValidatedHashMap<String, List> newMap = vMap.getMapWithoutThisKey(siteIds.get(i));
					DiversityIndicesEstimates estimateMap = getDissimilarityIndicesMultiplesiteEstimator(newMap, populationSize);
					varianceEstimate.addRealization(estimateMap.getBetaDiversity().getMean());
				}
				((SimpleEstimate) di.getBetaDiversity()).setVariance(varianceEstimate.getVariance());
				break;
			case DeleteTwo:
				varianceEstimate = new JackknifeEstimate(nbPlots, 2);
				for (int i = 0; i < siteIds.size() - 1; i++) {
					ValidatedHashMap<String, List> newMap = vMap.getMapWithoutThisKey(siteIds.get(i));
					for (int j = i + 1; j < siteIds.size(); j++) {
						ValidatedHashMap<String, List> newMap2 = newMap.getMapWithoutThisKey(siteIds.get(j));
						DiversityIndicesEstimates estimateMap = getDissimilarityIndicesMultiplesiteEstimator(newMap2, populationSize);
						varianceEstimate.addRealization(estimateMap.getBetaDiversity().getMean());
					}
				}
				((SimpleEstimate) di.getBetaDiversity()).setVariance(varianceEstimate.getVariance());
				break;
//			case EfronSteinCorrection:
//				List<String> toBeRemoved = new ArrayList<String>();
//				varianceEstimate = new JackknifeEstimate(nbPlots, 1);
//				Map<IndexName, MonteCarloEstimate> qTerms = new HashMap<IndexName, MonteCarloEstimate>();
//				for (IndexName iName: IndexName.values()) {
//					qTerms.put(iName, new MonteCarloEstimate());
//				}
//				
//				Map<Integer, Map<IndexName, SimpleEstimate>> referenceLeaveOneOutMap = new HashMap<Integer, Map<IndexName, SimpleEstimate>>();
//				for (int i = 0; i < siteIds.size(); i++) {
//					SimpleEstimate estimateMapI = getSimpleEstimate(varianceEstimates,
//							referenceLeaveOneOutMap, 
//							vMap, 
//							i,
//							siteIds, 
//							populationSize);
//					if (i < siteIds.size() - 1) {
//						for (int j = i + 1; j < siteIds.size(); j++) {
//							SimpleEstimate estimateMapJ = getSimpleEstimate(varianceEstimates,
//									referenceLeaveOneOutMap, 
//									vMap, 
//									j,
//									siteIds, 
//									populationSize);
//							toBeRemoved.clear();
//							toBeRemoved.add(siteIds.get(i));
//							toBeRemoved.add(siteIds.get(j));
//							ValidatedHashMap<String, List> newMap = vMap.getMapWithoutTheseKeys(toBeRemoved);
//							Map<DiversityIndex, SimpleEstimate> estimateMap = getDissimilarityIndicesMultiplesiteEstimator(newMap, populationSize);
//							for (IndexName iName : IndexName.values()) {
//								Matrix s_ii_Term = estimateMap.get(iName).getMean().scalarMultiply(nbPlots - 2);
//								Matrix s_i_Term = estimateMapI.get(iName).getMean().add(estimateMapJ.get(iName).getMean()).scalarMultiply(nbPlots - 1);
//								Matrix s_Term = outputMap.get(iName).getMean().scalarMultiply(nbPlots);
//								Matrix realizationQ = s_Term.subtract(s_i_Term).add(s_ii_Term);
//								qTerms.get(iName).addRealization(realizationQ);
//							}
//						}
//					}
//				}
//				
//				for (IndexName iName : IndexName.values()) {
//					Matrix correction = qTerms.get(iName).getVariance().scalarMultiply((0.5 * nbPlots * (nbPlots - 1) - 1) / (nbPlots * (nbPlots + 1)));
//					Matrix variance = varianceEstimates.get(iName).getVariance().subtract(correction);
//					outputMap.get(iName).setVariance(variance);
//				}
//				break;
			}
		} 
		return di;
	}

	
	
//	@SuppressWarnings("rawtypes")
//	private Map<IndexName, SimpleEstimate> getSimpleEstimate(Map<IndexName, JackknifeEstimate> varianceEstimates,
//			Map<Integer, Map<IndexName, SimpleEstimate>> referenceLeaveOneOutMap, 
//			ValidatedHashMap<String, List> referenceValidatedMap, 
//			int index,
//			List<String> referenceIds,
//			int populationSize) {
//		if (!referenceLeaveOneOutMap.containsKey(index)) {
//			ValidatedHashMap<String, List> newMapI = referenceValidatedMap.getMapWithoutThisKey(referenceIds.get(index));
//			Map<IndexName, SimpleEstimate> estimateMap = getDissimilarityIndicesMultiplesiteEstimator(newMapI, populationSize);
//			referenceLeaveOneOutMap.put(index, estimateMap);
//			for (IndexName iName : IndexName.values()) {
//				varianceEstimates.get(iName).addRealization(estimateMap.get(iName).getMean());
//			}
//		}
//		return referenceLeaveOneOutMap.get(index);
//	}
	
	@SuppressWarnings("rawtypes")
	public DiversityIndicesEstimates getDissimilarityIndicesMultiplesiteEstimator(Map<String, List> oMap, int populationSize) {
		return getDissimilarityIndicesMultiplesiteEstimator(oMap, populationSize, false, Mode.LeaveOneOut);
	}	
}
