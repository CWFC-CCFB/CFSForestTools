/*
 * This file is part of the cfsforesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package canforservutility.predictor.iris.recruitment_v1;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import canforservutility.predictor.iris.recruitment_v1.IrisCompatibleTree.IrisSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.geographic.GeographicDistanceCalculator;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.PopulationMeanEstimate;
import repicea.stats.estimates.PopulationTotalEstimate;
import repicea.stats.sampling.PopulationUnitWithEqualInclusionProbability;
import repicea.stats.sampling.PopulationUnitWithUnequalInclusionProbability;

/**
 * A class to calculate the occupancy index required by the recruitment module. <p>
 * The occupancy index is actually an estimate of the occupancy within a particular radius
 * around each plot. 
 * @author Mathieu Fortin - Sept 2022
 */
public class IrisOccupancyIndexCalculator {

//	class InternalWorker extends Thread {
//		
//		final BlockingQueue<Integer> queue;
//		final Map<Integer, Estimate<?>> estimateMap;
//		final List<IrisProtoPlot> plots;
//		final IrisSpecies species;
//		final int id;
//		final Map<Integer, List<IrisProtoPlot>> dataCache;
//		
//		InternalWorker(int id, 
//				BlockingQueue<Integer> queue, 
//				Map<Integer, Estimate<?>> estimateMap, 
//				List<IrisProtoPlot> plots, 
//				IrisSpecies species,
//				Map<Integer, List<IrisProtoPlot>> dateFilteredPlots) {
//			super("Proximity index calculator thread " + id);
//			this.id = id;
//			this.queue = queue;
//			this.estimateMap = estimateMap;
//			this.plots = plots;
//			this.species = species;
//			this.dataCache = dateFilteredPlots;
//		}
//		
//		@Override
//		public void run() {
//			int order;
//			try {
//				while(!isInterrupted()) {
//					order = queue.take();
//					if (order == FinalToken) 
//						break;
//					Estimate<?> estimate = getOccupancyIndex(plots, plots.get(order), species, dataCache);
//					estimateMap.put(order, estimate);
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
		
	final SymmetricMatrix distances;
	private final double maximumDistanceKm;
	final Map<String, Integer> plotsId;
	private final int minYearDiff = 0;
	private final int maxYearDiff = 10;
	
//	private static final int FinalToken = -999;
	
	/**
	 * Constructor. <p>
	 * It is assumed that the plots with the same subjectId have the same
	 * geographical coordinates. The constructor first sets the distance 
	 * matrix. Only the first entry of the set of plots with the same subjectId
	 * is considered in the calculation of the distance matrix.
	 * 
	 * @param plots a List of Iris2020ProtoPlot instances
	 * @param maxDistanceKm the radius around the plot of interest for estimating the occupancy index (e.g. 15 km)
	 * @param bootstrapRealizations the number of bootstrap realizations for the variance of the index
	 */
	public IrisOccupancyIndexCalculator(List<IrisProtoPlot> plots, double maxDistanceKm) {
		plotsId = new HashMap<String, Integer>();
		// first screen for the first entry plots
		List<IrisProtoPlot> firstEntryPlots = new ArrayList<IrisProtoPlot>();
		for (int i = 0; i < plots.size(); i++) {
			IrisProtoPlot p = plots.get(i);
			if (!plotsId.containsKey(p.getSubjectId())) {
				plotsId.put(p.getSubjectId(), firstEntryPlots.size());
				firstEntryPlots.add(p);
			} 
		}		
		// construct the latitude and longitude vectors
		Matrix latitudes = new Matrix(firstEntryPlots.size(), 1);
		Matrix longitudes = new Matrix(firstEntryPlots.size(), 1);
		for (int i = 0; i < firstEntryPlots.size(); i++) {
			IrisProtoPlot p = firstEntryPlots.get(i);
			latitudes.setValueAt(i, 0, p.getLatitudeDeg());
			longitudes.setValueAt(i, 0, p.getLongitudeDeg());
		}
		// calculate the distance matrix
		distances = GeographicDistanceCalculator.getDistanceBetweenTheseCoordinates(latitudes, longitudes);
		maximumDistanceKm = maxDistanceKm;
	}

	/**
	 * Return the distance between two plots.
	 * 
	 * @param plot1 an Iris2020CompatiblePlot instance
	 * @param plot2 an Iris2020CompatiblePlot instance 
	 * @return the distance (km)
	 */
	protected double getDistanceKmBetweenThesePlots(IrisProtoPlot plot1, IrisProtoPlot plot2) {
		int index1 = plotsId.get(plot1.getSubjectId());
		if (index1 == -1) {
			throw new InvalidParameterException("The plot1 argument is not found in the plot list!");
		}
		int index2 = plotsId.get(plot2.getSubjectId());
		if (index2 == -1) {
			throw new InvalidParameterException("The plot2 argument is not found in the plot list!");
		}
		double distanceKm = distances.getValueAt(index1, index2);
		return distanceKm;
	}

	int getOccurrence(IrisProtoPlot plot, IrisSpecies species) {
		boolean occurred = plot.getBasalAreaM2HaForThisSpecies(species) > 0;
		return occurred ? 1 : 0;
	}
	
	/**
	 * Provide an estimate of the occupancy index. <p>
	 * The method implements the design-based estimators. 
	 * 
	 * @param plots the list of plots
	 * @param thisPlot the plot of interest
	 * @param species the species of interest
	 * @param dateCache a Map in which the subsets of the sample are stored
	 * @return a GaussiEstimate instance
	 */
	protected GaussianEstimate getOccupancyIndex(List<IrisProtoPlot> plots, 
			IrisProtoPlot thisPlot, 
			IrisSpecies species,
			Map<Integer, List<IrisProtoPlot>> dateCache) {
		
		List<IrisProtoPlot> plotsWithinLast10Yrs;
		if (dateCache != null) {
			if (!dateCache.containsKey(thisPlot.getDateYr())) {
				dateCache.put(thisPlot.getDateYr(), plots.stream().
						filter(p -> thisPlot.getDateYr() - p.getDateYr() >= minYearDiff && thisPlot.getDateYr() - p.getDateYr() <= maxYearDiff).
						collect(Collectors.toList()));
			} 
			plotsWithinLast10Yrs = new ArrayList<IrisProtoPlot>(dateCache.get(thisPlot.getDateYr()));
		} else {
			plotsWithinLast10Yrs = plots.stream().
					filter(p -> thisPlot.getDateYr() - p.getDateYr() >= minYearDiff && thisPlot.getDateYr() - p.getDateYr() <= maxYearDiff).
					collect(Collectors.toList());
		}
		
		List<IrisProtoPlot> plotsWithinDistanceWithinLast10Yrs = plotsWithinLast10Yrs.stream().
				filter(p -> getDistanceKmBetweenThesePlots(thisPlot, p) < maximumDistanceKm).
				collect(Collectors.toList());
		
		Map<String, IrisProtoPlot> singletonMap = new HashMap<String, IrisProtoPlot>();
		// if we have two measurements of the same plot, we keep that with the conspecific.
		for (IrisProtoPlot p : plotsWithinDistanceWithinLast10Yrs) {
			if (!singletonMap.containsKey(p.getSubjectId())) {
				singletonMap.put(p.getSubjectId(), p);
			} else {
				if (singletonMap.get(p.getSubjectId()).getBasalAreaM2HaForThisSpecies(species) == 0d &&
						p.getBasalAreaM2HaForThisSpecies(species) > 0d) {
					singletonMap.put(p.getSubjectId(), p);
				}
			}
		}

		plotsWithinDistanceWithinLast10Yrs.clear();
		plotsWithinDistanceWithinLast10Yrs.addAll(singletonMap.values());
		
		if (plotsWithinDistanceWithinLast10Yrs.size() == 1) {
			throw new UnsupportedOperationException("The occupancy index cannot be calculated since there is only one plot in the sample!");
////			CaseCount[0]++;
//			Matrix mean = new Matrix(1,
//					1, 
//					getOccurrence(plotsWithinDistanceWithinLast10Yrs.get(0), species),
//					0);
//			SymmetricMatrix variance = new SymmetricMatrix(1);
//			variance.setValueAt(0, 0, Double.NaN);
//			return new SimpleEstimate(mean, variance);
		} else {
			int n = plotsWithinDistanceWithinLast10Yrs.size();
			boolean allWeightsEqual = plotsWithinDistanceWithinLast10Yrs.stream().
					allMatch(p -> p.getWeight() == plotsWithinDistanceWithinLast10Yrs.get(0).getWeight());
			
			if (allWeightsEqual) {	// Then we assume random sampling without replacement
//				CaseCount[1]++;
				PopulationMeanEstimate estimate = new PopulationMeanEstimate();
				Matrix obs;
				for (int i = 0; i < n; i++) {
					obs = new Matrix(1, 1, getOccurrence(plotsWithinDistanceWithinLast10Yrs.get(i), species), 0);
					estimate.addObservation(new PopulationUnitWithEqualInclusionProbability(i + "", obs));
				}
				return new GaussianEstimate(estimate.getMean(), estimate.getVariance());
			} else {	// Otherwise we use a HT estimator. We aksi assume the area for each weight is estimated as (n_k * w_k) / (sum_k n_k w_k)
//				CaseCount[2]++;
				double N = (maximumDistanceKm * maximumDistanceKm * Math.PI * 100) / thisPlot.getAreaHa();
				PopulationTotalEstimate estimate = new PopulationTotalEstimate();
				double sumWeight = 0d;
				for (int i = 0; i < n; i++) {
					sumWeight += plotsWithinDistanceWithinLast10Yrs.get(i).getWeight();
				}
				Matrix obs;
				for (int i = 0; i < n; i++) {
					IrisProtoPlot p = plotsWithinDistanceWithinLast10Yrs.get(i);
					double newWeight = sumWeight / (p.getWeight() * N);	// this is the simplification of n_k / N_k with N_k = n_k * w_k / (sum_k n_k w_k) * N
					obs = new Matrix(1, 1, getOccurrence(p, species), 0);
					estimate.addObservation(new PopulationUnitWithUnequalInclusionProbability(i + "", obs, newWeight));
				}
				return new GaussianEstimate(estimate.getMean().scalarMultiply(1d/N), estimate.getVariance().scalarMultiply(1d/(N*N)));
			}
		}
	}


	/**
	 * Provide an estimate of the occupancy index. <p>
	 * The method implements the design-based estimators. 
	 * 
	 * @param plots the list of plots
	 * @param thisPlot the plot of interest
	 * @param species the species of interest
	 * @return a GaussiEstimate instance
	 */
	protected GaussianEstimate getOccupancyIndex(List<IrisProtoPlot> plots, 
			IrisProtoPlot thisPlot, 
			IrisSpecies species) {
		return getOccupancyIndex(plots, thisPlot, species, null);
	}

//	/**
//	 * Provide estimates of the proximity index for all the plots in the list. <p>
//	 * The variance of the estimate is obtained through a bootstrap estimator.
//	 * 
//	 * @param plots the list of plots
//	 * @param thisPlot the plot of interest
//	 * @param species the species of interest
//	 * @param nbThreads the number of threads to run the bootstrap simulation (between 1 and 4). By default, 
//	 * the number of threads is set to the number of cores minus 1 if the nbThreads value is greater than 
//	 * the number of cores minus 1.
//	 * @return an Estimate instance
//	 */
//	public List<Estimate<?>> getOccupancyIndexForThesePlots(List<IrisProtoPlot> plots, IrisSpecies species, int nbThreads) throws Exception {
//		if (nbThreads < 1 || nbThreads > 4) {
//			throw new InvalidParameterException("The nbThreads argument must be an integer between 1 and 4!");
//		}
//		int availableCores = Runtime.getRuntime().availableProcessors();
//		if (nbThreads > availableCores - 1)
//			nbThreads = availableCores - 1;
//		
//		ConcurrentHashMap<Integer, Estimate<?>> estimateMap = new ConcurrentHashMap<Integer, Estimate<?>>();
//		ConcurrentHashMap<Integer, List<IrisProtoPlot>> dateCache = new ConcurrentHashMap<Integer, List<IrisProtoPlot>>();
//		BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
//
//		for (int i = 0; i < plots.size(); i++) {
//			queue.add(i);
//		}
//
//		List<InternalWorker> workers = new ArrayList<InternalWorker>();
//		for (int i = 0; i < nbThreads; i++) {
//			workers.add(new InternalWorker(i, queue, estimateMap, plots, species, dateCache));
//			queue.add(FinalToken);
//		}
//		
//		for (InternalWorker w : workers)
//			w.start();
//		
//		boolean interrupted = false;
//		for (InternalWorker w : workers) {
//			try {
//				w.join();
//			} catch (InterruptedException e) {
//				interrupted = true;
//			}
//		}
//		if (estimateMap.size() != plots.size()) {
//			throw new Exception("The number of estimates is different from the number of plots!");
//		}
//		List<Estimate<?>> estimateList = new ArrayList<Estimate<?>>();
//		for (int i = 0; i < plots.size(); i++) {
//			if (estimateMap.containsKey(i)) {
//				estimateList.add(estimateMap.get(i));
//			} 
//		}
//		return interrupted ? null : estimateList;
//	}

//	/**
//	 * A static method to extract the means from the estimate list.
//	 * @param estimates a List of Estimate instance
//	 * @return a List of doubles
//	 */
//	public static List<Double> getMeansFromEstimates(List<Estimate<?>> estimates) {
//		List<Double> means = new ArrayList<Double>();
//		for (Estimate<?> e : estimates) {
//			means.add(e.getMean().getValueAt(0, 0));
//		}
//		return means;
// 	}
//
//	/**
//	 * A static method to extract the variances from the estimate list.
//	 * @param estimates a List of Estimate instance
//	 * @return a List of doubles
//	 */
//	public static List<Double> getVariancesFromEstimates(List<Estimate<?>> estimates) {
//		List<Double> variances = new ArrayList<Double>();
//		for (Estimate<?> e : estimates) {
//			variances.add(e.getVariance().getValueAt(0, 0));
//		}
//		return variances;
// 	}

}
