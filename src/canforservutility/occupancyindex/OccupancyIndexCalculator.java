/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package canforservutility.occupancyindex;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.geographic.GeographicDistanceCalculator;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.sampling.PopulationMeanEstimate;

/**
 * A class to calculate the occupancy index. <p>
 * The occupancy index is actually an estimate of the occupancy within a particular radius
 * around each plot. 
 * @author Mathieu Fortin - Sept 2022, October 2025
 */
public class OccupancyIndexCalculator implements Cloneable {

	private final int minYearDiff;
	private final int maxYearDiff;

	final Map<String, List<String>> nearestNeighborsMap;
	Map<String, List<OccupancyIndexCalculablePlot>> plotRegistry;
	
	private Map<Enum<?>, Map<Integer, Map<String, GaussianEstimate>>> cacheMap; // species, plotId, occupancy index

	private final Integer referenceYearForStaticSimulations;

	/**
	 * Constructor 1. <p>
	 * It is assumed that the plots with the same subjectId have the same
	 * geographical coordinates. The constructor first sets the distance 
	 * matrix. Only the first entry of the set of plots with the same subjectId
	 * is considered in the calculation of the distance matrix.<p>
	 * The constructor first sets the distances. Then, the occupancy index can be 
	 * obtained through the OccupancyIndexCalculator.getOccupancyIndex(List, OccupancyIndexCalculablePlot, Enum, double) 
	 * method.
	 * 
	 * @param plots a List of OccupancyIndexCalculablePlot instances
	 * @param minYearDiff the minimum number of years between the measurement dates to 
	 * be considered in the sample. Must be equal to or greater than 0.
	 * @param maxYearDiff the maximum number of years between the measurement dates to 
	 * be considered in the sample. Must be equal to or greater than minYearDiff argument
	 * @param radiusKm the radius (km) of the area upon which the occupancy is calculated
	 * @param referenceYearForStaticSimulations an optional integer that specifies the reference year 
	 * for the occupancy indices, which are calculated once and will not be
	 * recalculated afterwards. This typically happens with stand-level simulation where the
	 * sample is not large enough to ensure a proper evaluation through time.
	 */
	public OccupancyIndexCalculator(List<OccupancyIndexCalculablePlot> plots, 
			int minYearDiff,
			int maxYearDiff,
			double radiusKm,
			Integer referenceYearForStaticSimulations) {
		this.referenceYearForStaticSimulations = referenceYearForStaticSimulations;
//		this.isStatic = isStatic;
		if (minYearDiff < 0) {
			throw new InvalidParameterException("The minYearDiff argument should be greater to or equal to 0!");
		}
		this.minYearDiff = minYearDiff;
		if (maxYearDiff < minYearDiff) {
			throw new InvalidParameterException("The maxYearDiff argument should be greater to or equal to the minYearDiff argument!");
		}
		this.maxYearDiff = maxYearDiff;
		if (plots == null || plots.isEmpty()) {
			throw new InvalidParameterException("The plots argument should be a non empty list!");
		}
		if (radiusKm <= 0d) {
			throw new InvalidParameterException("The radius argument must be a positive number!");
		}
//		this.radiusKm = radiusKm;
		Map<String, Integer> plotsId = new HashMap<String, Integer>();
		plotRegistry = new HashMap<String, List<OccupancyIndexCalculablePlot>>();
		// first screen for the first entry plots
//		long initTime = System.currentTimeMillis();
		List<OccupancyIndexCalculablePlot> firstEntryPlots = new ArrayList<OccupancyIndexCalculablePlot>();
		for (int i = 0; i < plots.size(); i++) {
			OccupancyIndexCalculablePlot p = plots.get(i);
			if (!plotsId.containsKey(p.getSubjectId())) {
				plotsId.put(p.getSubjectId(), firstEntryPlots.size());
				firstEntryPlots.add(p);
			} 
		}
//		System.out.println("Time to list the plots " + (System.currentTimeMillis() - initTime) + " ms.");

		// construct the latitude and longitude vectors
//		initTime = System.currentTimeMillis();
		Matrix latitudes = new Matrix(firstEntryPlots.size(), 1);
		Matrix longitudes = new Matrix(firstEntryPlots.size(), 1);
		for (int i = 0; i < firstEntryPlots.size(); i++) {
			OccupancyIndexCalculablePlot p = firstEntryPlots.get(i);
			latitudes.setValueAt(i, 0, p.getLatitudeDeg());
			longitudes.setValueAt(i, 0, p.getLongitudeDeg());
		}
		// calculate the distance matrix
		Matrix distances = GeographicDistanceCalculator.getDistanceBetweenTheseCoordinates(latitudes, longitudes, radiusKm);
//		System.out.println("Time to set the distance matrix " + (System.currentTimeMillis() - initTime) + " ms.");
		
		
//		initTime = System.currentTimeMillis();
		nearestNeighborsMap = new HashMap<String, List<String>>();
		for (int i = 0; i < distances.m_iRows; i++) {
			OccupancyIndexCalculablePlot p1 = firstEntryPlots.get(i);
			if (!nearestNeighborsMap.containsKey(p1.getSubjectId())) {
				nearestNeighborsMap.put(p1.getSubjectId(), new ArrayList<String>());
			}
			List<String> p1List = nearestNeighborsMap.get(p1.getSubjectId());
			for (int j = i; j < distances.m_iCols; j++) {
				double distanceKm = distances.getValueAt(i, j); 
				if (!Double.isNaN(distanceKm)) {
					OccupancyIndexCalculablePlot p2 = firstEntryPlots.get(j);
					if (!nearestNeighborsMap.containsKey(p2.getSubjectId())) {
						nearestNeighborsMap.put(p2.getSubjectId(), new ArrayList<String>());
					}
					p1List.add(p2.getSubjectId());
					if (!p1.equals(p2)) {
						nearestNeighborsMap.get(p2.getSubjectId()).add(p1.getSubjectId());
					}
				}
			}
//			if (p1List.size() < 2) {	// MF20260327 Reenable this condition when a proper reference dataset can be provided.
//				throw new UnsupportedOperationException("There is less than 2 plots with a " + radiusKm + " km radius of plot " + p1.getSubjectId());
//			}
		}
//		System.out.println("Time to log distances in the map " + (System.currentTimeMillis() - initTime) + " ms.");
		cacheMap = new HashMap<Enum<?>, Map<Integer, Map<String, GaussianEstimate>>>();
	}

	/**
	 * Constructor 2. <p>
	 * It is assumed that the plots with the same subjectId have the same
	 * geographical coordinates. The constructor first sets the distance 
	 * matrix. Only the first entry of the set of plots with the same subjectId
	 * is considered in the calculation of the distance matrix.<p>
	 * The constructor first sets the distances. Then, the occupancy index can be 
	 * obtained through the OccupancyIndexCalculator.getOccupancyIndex(List, OccupancyIndexCalculablePlot, Enum, double) 
	 * method.<p>
	 * This constructor assumes minimum and maximum year differences of 0 and 10, respectively.
	 * 
	 * @param plots a List of OccupancyIndexCalculablePlot instances
	 * @param radiusKm the radius (km) of the area upon which the occupancy is calculated
	 * @param referenceYearForStaticSimulations an optional integer that specifies the reference year 
	 * for the occupancy indices, which are calculated once and will not be
	 * recalculated afterwards. This typically happens with stand-level simulation where the
	 * sample is not large enough to ensure a proper evaluation through time.
	 */
	public OccupancyIndexCalculator(List<OccupancyIndexCalculablePlot> plots, 
			double radiusKm,
			Integer referenceYearForStaticSimulations) {
		this(plots, 0, 10, radiusKm, referenceYearForStaticSimulations);
	}

	private boolean isStatic() {return referenceYearForStaticSimulations != null;}

	@Override
	public OccupancyIndexCalculator clone() {
		try {
			OccupancyIndexCalculator clone = (OccupancyIndexCalculator) super.clone();
			clone.plotRegistry = new HashMap<String, List<OccupancyIndexCalculablePlot>>();
			for (String s : plotRegistry.keySet()) {
				clone.plotRegistry.put(s, new ArrayList<OccupancyIndexCalculablePlot>());
				clone.plotRegistry.get(s).addAll(plotRegistry.get(s));
			}
			clone.cacheMap = new HashMap<Enum<?>, Map<Integer, Map<String, GaussianEstimate>>>();
			if (isStatic()) {
				for (Enum<?> sp : cacheMap.keySet()) {
					clone.cacheMap.put(sp, new HashMap<Integer, Map<String, GaussianEstimate>>());
					Map<Integer, Map<String, GaussianEstimate>> innerMap = cacheMap.get(sp);
					Map<Integer, Map<String, GaussianEstimate>> cloneInnerMap = clone.cacheMap.get(sp);
					for (Integer dateYr : innerMap.keySet()) {
						cloneInnerMap.put(dateYr, new HashMap<String, GaussianEstimate>());
						cloneInnerMap.get(dateYr).putAll(innerMap.get(dateYr));
					}
				}
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Register plots.<p>
	 * This is typically called once before call the getOccupancyIndex method on 
	 * individual plots.
	 * @param plots a list of OccupancyIndexCalculablePlot instances
	 */
	public void registerPlots(List<OccupancyIndexCalculablePlot> plots) {
		if (plotRegistry.isEmpty() || !isStatic()) {
			for (OccupancyIndexCalculablePlot p : plots) {
				String plotId = p.getSubjectId();
				if (!nearestNeighborsMap.containsKey(plotId)) {
					throw new UnsupportedOperationException("The plot " + plotId + " has not been included in the original distance matrix calculation (see constructor)!");
				} else {
					if (!plotRegistry.containsKey(plotId)) {
						plotRegistry.put(plotId, new ArrayList<OccupancyIndexCalculablePlot>());
					}
					plotRegistry.get(plotId).add(p);
				}
			}
		}
	}

	/**
	 * Provide the number of subjects.
	 * @return an integer 
	 */
	public int getNumberOfSubjects() {
		return nearestNeighborsMap.size();
	}

	private int getOccurrence(OccupancyIndexCalculablePlot plot, Enum<?> species) {
		boolean occurred = plot.getBasalAreaM2HaForThisSpecies(species) > 0;
		return occurred ? 1 : 0;
	}

	private boolean isCached(Enum<?> species, int dateYr, String plotId) {
		if (cacheMap.containsKey(species)) {
			if (cacheMap.get(species).containsKey(dateYr)) {
				return cacheMap.get(species).get(dateYr).containsKey(plotId);
			} 
		}
		return false;
	}
	
	/**
	 * Provide an estimate of the occupancy index. <p>
	 * The method implements the design-based estimators. If there is only one plot in the
	 * sample, then a GaussianEstimate with mean NaN and variance NaN is produced. 
	 * 
	 * @param thisPlot the plot of interest
	 * @param species an enum standing for the species
	 * @return a GaussiEstimate instance, the mean and variance of which are NaN if the variance
	 * cannot be calculated, i.e. if there is only one plot within the radius.
	 */
	public GaussianEstimate getOccupancyIndex(OccupancyIndexCalculablePlot thisPlot, 
			Enum<?> species) {
		String plotId = thisPlot.getSubjectId();
		GaussianEstimate occupancyEstimate;
		int cachedDateYr = isStatic() ? referenceYearForStaticSimulations : thisPlot.getDateYr();
		if (isCached(species, cachedDateYr, plotId)) {
			return cacheMap.get(species).get(cachedDateYr).get(plotId);
		} else {
			List<String> nearestNeighbors = nearestNeighborsMap.get(plotId);
			if (nearestNeighbors == null) {
				throw new UnsupportedOperationException("This plot " + plotId + " can be found in the nearest neighbor map!");
			}
			List<OccupancyIndexCalculablePlot> plotsWithinRadiusKm = new ArrayList<OccupancyIndexCalculablePlot>();
			for (String pId : nearestNeighbors) {
				plotsWithinRadiusKm.addAll(plotRegistry.get(pId));
			}
			List<OccupancyIndexCalculablePlot> plotsWithinRadiusKmWithinPeriod = new ArrayList<OccupancyIndexCalculablePlot>();
			for (OccupancyIndexCalculablePlot p : plotsWithinRadiusKm) {
				int dateYrOfThatPlot = p.equals(thisPlot) ? cachedDateYr : p.getDateYr(); 
				if (cachedDateYr - dateYrOfThatPlot >= minYearDiff && cachedDateYr - dateYrOfThatPlot <= maxYearDiff) {
					plotsWithinRadiusKmWithinPeriod.add(p);
				}
			}
			Map<String, OccupancyIndexCalculablePlot> singletonMap = new HashMap<String, OccupancyIndexCalculablePlot>();
			// if we have two measurements of the same plot, we keep that with the conspecific.
			for (OccupancyIndexCalculablePlot p : plotsWithinRadiusKmWithinPeriod) {
				if (!singletonMap.containsKey(p.getSubjectId())) {
					singletonMap.put(p.getSubjectId(), p);
				} else {
					if (singletonMap.get(p.getSubjectId()).getBasalAreaM2HaForThisSpecies(species) == 0d &&
							p.getBasalAreaM2HaForThisSpecies(species) > 0d) {
						singletonMap.put(p.getSubjectId(), p);
					}
				}
			}

			plotsWithinRadiusKmWithinPeriod.clear();
			plotsWithinRadiusKmWithinPeriod.addAll(singletonMap.values());

			if (plotsWithinRadiusKmWithinPeriod.size() == 1) {
				throw new UnsupportedOperationException("Occupancy index could not be calculated for plot " + plotId + " for date " + cachedDateYr + " since there is only one plot within the radius!");
			} else {
				int n = plotsWithinRadiusKmWithinPeriod.size();
				PopulationMeanEstimate estimate = new PopulationMeanEstimate();
				Matrix obs;
				for (int i = 0; i < n; i++) {
					obs = new Matrix(1, 1, getOccurrence(plotsWithinRadiusKmWithinPeriod.get(i), species), 0);
					estimate.addObservation(obs, i + "");
				}
				occupancyEstimate = new GaussianEstimate(estimate.getMean(), estimate.getVariance());
			}
			if (!cacheMap.containsKey(species)) {
				cacheMap.put(species, new HashMap<Integer, Map<String, GaussianEstimate>>());
			}
			Map<Integer, Map<String, GaussianEstimate>> innerMap = cacheMap.get(species);
			if (!innerMap.containsKey(cachedDateYr)) {
				innerMap.put(cachedDateYr, new HashMap<String, GaussianEstimate>());
			} 
			innerMap.get(cachedDateYr).put(plotId, occupancyEstimate);
			return occupancyEstimate;
		} 
	}


	/**
	 * Provide the minimum year difference for a plot measurement to be considered
	 * in the calculation.
	 * @return the number of years 
	 */
	public int getMinimumYearDifference() {return minYearDiff;}

	/**
	 * Provide the maximum year difference for a plot measurement to be considered
	 * in the calculation.
	 * @return the number of years 
	 */
	public int getMaximumYearDifference() {return maxYearDiff;}

}
