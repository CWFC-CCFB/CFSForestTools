/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020-2022 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris2020.recruitment;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.geographic.GeographicDistanceCalculator;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.SimpleEstimate;

/**
 * A class to calculate the occupancy index required by the recruitment module.
 * @author Mathieu Fortin - Sept 2020
 */
public class Iris2020OccupancyIndexCalculator {

	class InternalWorker extends Thread {
		
		final BlockingQueue<Integer> queue;
		final Map<Integer, Estimate<?>> estimateMap;
		final List<Iris2020ProtoPlot> plots;
		final Iris2020Species species;
		final int id;
		final Map<Integer, List<Iris2020ProtoPlot>> dataCache;
		
		InternalWorker(int id, 
				BlockingQueue<Integer> queue, 
				Map<Integer, Estimate<?>> estimateMap, 
				List<Iris2020ProtoPlot> plots, 
				Iris2020Species species,
				Map<Integer, List<Iris2020ProtoPlot>> dateFilteredPlots) {
			super("Proximity index calculator thread " + id);
			this.id = id;
			this.queue = queue;
			this.estimateMap = estimateMap;
			this.plots = plots;
			this.species = species;
			this.dataCache = dateFilteredPlots;
		}
		
		@Override
		public void run() {
			int order;
			try {
				while(!isInterrupted()) {
					order = queue.take();
					if (order == FinalToken) 
						break;
					Estimate<?> estimate = getProximityIndex(plots, plots.get(order), species, dataCache);
					estimateMap.put(order, estimate);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
		
	final SymmetricMatrix distances;
	private final double maximumDistanceKm;
	final Map<String, Integer> plotsId;
	private final int minYearDiff = 0;
	private final int maxYearDiff = 10;
	
	private static final int FinalToken = -999;
	
	/**
	 * Constructor. <br>
	 * <br>
	 * It is assumed that the plots with the same subjectId have the same
	 * geographical coordinates. The constructor first sets the distance 
	 * matrix. Only the first entry of the set of plots with the same subjectId
	 * is considered in the calculation of the distance matrix.
	 * 
	 * @param plots a List of Iris2020ProtoPlot instances
	 * @param quantile the quantile the index is based upon (between 0 and 1).
	 * @param bootstrapRealizations the number of bootstrap realizations for the variance of the index
	 */
	public Iris2020OccupancyIndexCalculator(List<Iris2020ProtoPlot> plots, double maxDistanceKm) {
		plotsId = new HashMap<String, Integer>();
		// first screen for the first entry plots
		List<Iris2020ProtoPlot> firstEntryPlots = new ArrayList<Iris2020ProtoPlot>();
		for (int i = 0; i < plots.size(); i++) {
			Iris2020ProtoPlot p = plots.get(i);
			if (!plotsId.containsKey(p.getSubjectId())) {
				plotsId.put(p.getSubjectId(), firstEntryPlots.size());
				firstEntryPlots.add(p);
			} 
		}		
		// construct the latitude and longitude vectors
		Matrix latitudes = new Matrix(firstEntryPlots.size(), 1);
		Matrix longitudes = new Matrix(firstEntryPlots.size(), 1);
		for (int i = 0; i < firstEntryPlots.size(); i++) {
			Iris2020ProtoPlot p = firstEntryPlots.get(i);
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
	protected double getDistanceKmBetweenThesePlots(Iris2020ProtoPlot plot1, Iris2020ProtoPlot plot2) {
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

	int getOccurrence(Iris2020ProtoPlot plot, Iris2020Species species) {
		boolean occurred = plot.getBasalAreaM2HaForThisSpecies(species) > 0;
		return occurred ? 1 : 0;
	}
	
	/**
	 * Provide an estimate of the proximity index. <br>
	 * <br>
	 * The variance of the estimate is obtained through a bootstrap estimator.
	 * 
	 * @param plots the list of plots
	 * @param thisPlot the plot of interest
	 * @param species the species of interest
	 * @return an Estimate instance
	 */
	protected Estimate<?> getProximityIndex(List<Iris2020ProtoPlot> plots, 
			Iris2020ProtoPlot thisPlot, 
			Iris2020Species species,
			Map<Integer, List<Iris2020ProtoPlot>> dateCache) {
		if (!dateCache.containsKey(thisPlot.getDateYr())) {
			dateCache.put(thisPlot.getDateYr(), plots.stream().
					filter(p -> thisPlot.getDateYr() - p.getDateYr() >= minYearDiff && thisPlot.getDateYr() - p.getDateYr() <= maxYearDiff).
					collect(Collectors.toList()));
		} 
		List<Iris2020ProtoPlot> plotsWithinLast10Yrs = new ArrayList<Iris2020ProtoPlot>(dateCache.get(thisPlot.getDateYr()));
		List<Iris2020ProtoPlot> plotsWithinDistanceWithinLast10Yrs = plotsWithinLast10Yrs.stream().
				filter(p -> getDistanceKmBetweenThesePlots(thisPlot, p) < maximumDistanceKm).
				collect(Collectors.toList());
		if (plotsWithinDistanceWithinLast10Yrs.size() == 1) {
			Matrix mean = new Matrix(1,
					1, 
					getOccurrence(plotsWithinDistanceWithinLast10Yrs.get(0), species),
					0);
			SymmetricMatrix variance = new SymmetricMatrix(1);
			variance.setValueAt(0, 0, Double.NaN);
			return new SimpleEstimate(mean, variance);
		} else {
			double N = (maximumDistanceKm * maximumDistanceKm * Math.PI * 100) / thisPlot.getAreaHa();
			double prob_gen = 1d / N;
			List<Integer> occurrences = plotsWithinDistanceWithinLast10Yrs.stream().
					map(p -> getOccurrence(p, species)).
					collect(Collectors.toList());
			int n = occurrences.size();
			List<Double> weights = plotsWithinDistanceWithinLast10Yrs.stream().
					map(p -> 1d / (prob_gen / p.getWeight() * n)).
					collect(Collectors.toList());
			
			double sum_wy = 0d;
			for (int i = 0; i < n; i++) {
				sum_wy += occurrences.get(i) * weights.get(i);
			}
			double mean_wy = sum_wy / n;
			double var = 0d;
			double wy;
			for (int i = 0; i < n; i++) {
				wy = occurrences.get(i) * weights.get(i);
				var += (wy - mean_wy) * (wy - mean_wy);
			}
			var *= ((double) n) / (n-1);
			Matrix mean = new Matrix(1,1);
			mean.setValueAt(0, 0, sum_wy / N);
			SymmetricMatrix variance = new SymmetricMatrix(1);
			variance.setValueAt(0, 0, var / (N *N));
			return new SimpleEstimate(mean, variance);
		}
	}

	
	/**
	 * Provide estimates of the proximity index for all the plots in the list. <br>
	 * <br>
	 * The variance of the estimate is obtained through a bootstrap estimator.
	 * 
	 * @param plots the list of plots
	 * @param thisPlot the plot of interest
	 * @param species the species of interest
	 * @param nbThreads the number of threads to run the bootstrap simulation (between 1 and 4). By default, 
	 * the number of threads is set to the number of cores minus 1 if the nbThreads value is greater than 
	 * the number of cores minus 1.
	 * @return an Estimate instance
	 */
	public List<Estimate<?>> getProximityIndexForThesePlots(List<Iris2020ProtoPlot> plots, Iris2020Species species, int nbThreads) throws Exception {
		if (nbThreads < 1 || nbThreads > 4) {
			throw new InvalidParameterException("The nbThreads argument must be an integer between 1 and 4!");
		}
		int availableCores = Runtime.getRuntime().availableProcessors();
		if (nbThreads > availableCores - 1)
			nbThreads = availableCores - 1;
		
		ConcurrentHashMap<Integer, Estimate<?>> estimateMap = new ConcurrentHashMap<Integer, Estimate<?>>();
		ConcurrentHashMap<Integer, List<Iris2020ProtoPlot>> dateCache = new ConcurrentHashMap<Integer, List<Iris2020ProtoPlot>>();
		BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();

		for (int i = 0; i < plots.size(); i++) {
			queue.add(i);
		}

		List<InternalWorker> workers = new ArrayList<InternalWorker>();
		for (int i = 0; i < nbThreads; i++) {
			workers.add(new InternalWorker(i, queue, estimateMap, plots, species, dateCache));
			queue.add(FinalToken);
		}
		
		for (InternalWorker w : workers)
			w.start();
		
		boolean interrupted = false;
		for (InternalWorker w : workers) {
			try {
				w.join();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (estimateMap.size() != plots.size()) {
			throw new Exception("The number of estimates is different from the number of plots!");
		}
		List<Estimate<?>> estimateList = new ArrayList<Estimate<?>>();
		for (int i = 0; i < plots.size(); i++) {
			if (estimateMap.containsKey(i)) {
				estimateList.add(estimateMap.get(i));
			} 
		}
		return interrupted ? null : estimateList;
	}

	
}
