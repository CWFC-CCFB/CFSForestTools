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
import java.util.stream.Collectors;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.geographic.GeographicDistanceCalculator;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.sampling.PopulationMeanEstimate;

/**
 * A class to calculate the occupancy index required by the recruitment module. <p>
 * The occupancy index is actually an estimate of the occupancy within a particular radius
 * around each plot. 
 * @author Mathieu Fortin - Sept 2022, October 2025
 */
public class OccupancyIndexCalculator {

		
	final SymmetricMatrix distances;
	private final double maximumDistanceKm;
	final Map<String, Integer> plotsId;
	private final int minYearDiff = 0;
	private final int maxYearDiff = 10;
	
	
	/**
	 * Constructor. <p>
	 * It is assumed that the plots with the same subjectId have the same
	 * geographical coordinates. The constructor first sets the distance 
	 * matrix. Only the first entry of the set of plots with the same subjectId
	 * is considered in the calculation of the distance matrix.
	 * 
	 * @param plots a List of Iris2020ProtoPlot instances
	 * @param maxDistanceKm the radius around the plot of interest for estimating the occupancy index (e.g. 15 km)
	 */
	public OccupancyIndexCalculator(List<OccupancyIndexCalculablePlot> plots, double maxDistanceKm) {
		plotsId = new HashMap<String, Integer>();
		// first screen for the first entry plots
		List<OccupancyIndexCalculablePlot> firstEntryPlots = new ArrayList<OccupancyIndexCalculablePlot>();
		for (int i = 0; i < plots.size(); i++) {
			OccupancyIndexCalculablePlot p = plots.get(i);
			if (!plotsId.containsKey(p.getSubjectId())) {
				plotsId.put(p.getSubjectId(), firstEntryPlots.size());
				firstEntryPlots.add(p);
			} 
		}		
		// construct the latitude and longitude vectors
		Matrix latitudes = new Matrix(firstEntryPlots.size(), 1);
		Matrix longitudes = new Matrix(firstEntryPlots.size(), 1);
		for (int i = 0; i < firstEntryPlots.size(); i++) {
			OccupancyIndexCalculablePlot p = firstEntryPlots.get(i);
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
	protected double getDistanceKmBetweenThesePlots(OccupancyIndexCalculablePlot plot1, OccupancyIndexCalculablePlot plot2) {
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

	private int getOccurrence(OccupancyIndexCalculablePlot plot, Enum<?> species) {
		boolean occurred = plot.getBasalAreaM2HaForThisSpecies(species) > 0;
		return occurred ? 1 : 0;
	}
	
	/**
	 * Provide an estimate of the occupancy index. <p>
	 * The method implements the design-based estimators. 
	 * 
	 * @param plots the list of plots
	 * @param thisPlot the plot of interest
	 * @param species an enum standing for the species
	 * @param dateCache a Map in which the subsets of the sample are stored
	 * @return a GaussiEstimate instance
	 */
	public GaussianEstimate getOccupancyIndex(List<OccupancyIndexCalculablePlot> plots, 
			OccupancyIndexCalculablePlot thisPlot, 
			Enum<?> species,
			Map<Integer, List<OccupancyIndexCalculablePlot>> dateCache) {
		
		List<OccupancyIndexCalculablePlot> plotsWithinLast10Yrs;
		if (dateCache != null) {
			if (!dateCache.containsKey(thisPlot.getDateYr())) {
				dateCache.put(thisPlot.getDateYr(), plots.stream().
						filter(p -> thisPlot.getDateYr() - p.getDateYr() >= minYearDiff && thisPlot.getDateYr() - p.getDateYr() <= maxYearDiff).
						collect(Collectors.toList()));
			} 
			plotsWithinLast10Yrs = new ArrayList<OccupancyIndexCalculablePlot>(dateCache.get(thisPlot.getDateYr()));
		} else {
			plotsWithinLast10Yrs = plots.stream().
					filter(p -> thisPlot.getDateYr() - p.getDateYr() >= minYearDiff && thisPlot.getDateYr() - p.getDateYr() <= maxYearDiff).
					collect(Collectors.toList());
		}
		
		List<OccupancyIndexCalculablePlot> plotsWithinDistanceWithinLast10Yrs = plotsWithinLast10Yrs.stream().
				filter(p -> getDistanceKmBetweenThesePlots(thisPlot, p) < maximumDistanceKm).
				collect(Collectors.toList());
		
		Map<String, OccupancyIndexCalculablePlot> singletonMap = new HashMap<String, OccupancyIndexCalculablePlot>();
		// if we have two measurements of the same plot, we keep that with the conspecific.
		for (OccupancyIndexCalculablePlot p : plotsWithinDistanceWithinLast10Yrs) {
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
		} else {
			int n = plotsWithinDistanceWithinLast10Yrs.size();
			PopulationMeanEstimate estimate = new PopulationMeanEstimate();
			Matrix obs;
			for (int i = 0; i < n; i++) {
				obs = new Matrix(1, 1, getOccurrence(plotsWithinDistanceWithinLast10Yrs.get(i), species), 0);
				estimate.addObservation(obs, i + "");
			}
			return new GaussianEstimate(estimate.getMean(), estimate.getVariance());
		}
	}


	/**
	 * Provide an estimate of the occupancy index. <p>
	 * The method implements the design-based estimators. 
	 * 
	 * @param plots the list of plots
	 * @param thisPlot the plot of interest
	 * @param species an enum standing for the species
	 * @return a GaussiEstimate instance
	 */
	public GaussianEstimate getOccupancyIndex(List<OccupancyIndexCalculablePlot> plots, 
			OccupancyIndexCalculablePlot thisPlot, 
			Enum<?> species) {
		return getOccupancyIndex(plots, thisPlot, species, null);
	}


}
