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
import java.util.Collections;
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
 * A class to calculate the occupancy index. <p>
 * The occupancy index is actually an estimate of the occupancy within a particular radius
 * around each plot. 
 * @author Mathieu Fortin - Sept 2022, October 2025
 */
public class OccupancyIndexCalculator {

	@SuppressWarnings("serial")
	static class NearestNeighborEntryList extends ArrayList<NearestNeighborEntry> {

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size(); i++) {
				if (i > 0) {
					sb.append(System.lineSeparator());
				}
				if (i == 50) {
					sb.append("...");
					break;
				}
				sb.append(get(i).toString());
			}
			return sb.toString();
		}
	}
	
	static class NearestNeighborEntry implements Comparable<NearestNeighborEntry> {
		final String plotId;
		final double distanceKm;
		NearestNeighborEntry(String plotId, double distanceKm) {
			this.plotId = plotId;
			this.distanceKm = distanceKm;
		}
		
		@Override
		public int compareTo(NearestNeighborEntry o) {
			if (this.distanceKm > o.distanceKm) {
				return -1;
			} else if (this.distanceKm < o.distanceKm) {
				return 1;
			} else return 0;
		}
		
		@Override
		public String toString() {
			return "Plot id = " + plotId + "; Nearest neighbor at " + distanceKm + " km.";
		}
		
	}
	
	final SymmetricMatrix distances;
	final Map<String, Integer> plotsId;
	private int minYearDiff = 0;
	private int maxYearDiff = 10;

	private final NearestNeighborEntryList nearestNeighbors;
	
	/**
	 * Constructor. <p>
	 * It is assumed that the plots with the same subjectId have the same
	 * geographical coordinates. The constructor first sets the distance 
	 * matrix. Only the first entry of the set of plots with the same subjectId
	 * is considered in the calculation of the distance matrix.<p>
	 * The constructor first sets the distances. Then, the occupancy index can be 
	 * obtained through the {@link OccupancyIndexCalculator#getOccupancyIndex(List, OccupancyIndexCalculablePlot, Enum, double)} 
	 * method.
	 * 
	 * @param plots a List of OccupancyIndexCalculablePlot instances
	 */
	public OccupancyIndexCalculator(List<OccupancyIndexCalculablePlot> plots) {
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

		nearestNeighbors = new NearestNeighborEntryList();
		for (int i = 0; i < firstEntryPlots.size(); i++) {
			OccupancyIndexCalculablePlot p = firstEntryPlots.get(i);
			double minForThisPlot = Double.POSITIVE_INFINITY;
			for (int j = 0; j < distances.m_iCols; j++) {
				double d = distances.getValueAt(i, j);
				if (j != i && d < minForThisPlot) {
					minForThisPlot = d;
				}
			}
			
			nearestNeighbors.add(new NearestNeighborEntry(p.getSubjectId(), minForThisPlot));
		}
		Collections.sort(nearestNeighbors);
	}

	/**
	 * Provide the number of subjects.
	 * @return an integer 
	 */
	public int getNumberOfSubjects() {
		return plotsId.size();
	}

	/**
	 * Provide the id of the plots with the maximum minimum distance to the nearest plot.
	 * @return a String
	 */
	public String getMaximumDistanceNearestPlot() {
		return nearestNeighbors.toString();
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
	 * The method implements the design-based estimators. If there is only one plot in the
	 * sample, then a GaussianEstimate with mean NaN and variance NaN is produced. 
	 * 
	 * @param plots the list of plots
	 * @param thisPlot the plot of interest
	 * @param species an enum standing for the species
	 * @param radiusKm the radius (km) of the area upon which the occupancy is calculated
	 * @param dateCache a Map in which the subsets of the sample are stored
	 * @return a GaussiEstimate instance, the mean and variance of which are NaN if the variance
	 * cannot be calculated, i.e. if there is only one plot within the radius.
	 */
	public GaussianEstimate getOccupancyIndex(List<OccupancyIndexCalculablePlot> plots, 
			OccupancyIndexCalculablePlot thisPlot, 
			Enum<?> species,
			double radiusKm,
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
				filter(p -> getDistanceKmBetweenThesePlots(thisPlot, p) < radiusKm).
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
			Matrix nullMatrix = new Matrix(1,1,Double.NaN,0);
			return new GaussianEstimate(nullMatrix, SymmetricMatrix.convertToSymmetricIfPossible(nullMatrix));
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
	 * @param radiusKm the radius (km) of the area upon which the occupancy is calculated
	 * 
	 * @return a GaussiEstimate instance
	 */
	public GaussianEstimate getOccupancyIndex(List<OccupancyIndexCalculablePlot> plots, 
			OccupancyIndexCalculablePlot thisPlot, 
			Enum<?> species,
			double radiusKm) {
		return getOccupancyIndex(plots, thisPlot, species, radiusKm, null);
	}

	/**
	 * Set the minimum year difference for a plot measurement to be considered 
	 * in the calculation. <p>
	 * This parameter is set to 0 by default.
	 * @param diff an integer equal to or greater than 0 and smaller than the maximum year difference.
	 */
	public void setMinimumYearDifference(int diff) {
		if (diff < 0 || diff > maxYearDiff) {
			throw new InvalidParameterException("The minimum year difference must be greater than or equal to 0 and smaller than the maximum year difference!");
		}
		this.minYearDiff = diff;
	}

	/**
	 * Set the maximum year difference for a plot measurement to be considered 
	 * in the calculation. <p>
	 * This parameter is set to 10 by default.
	 * @param diff an integer larger than the minimum  year difference.
	 */
	public void setMaximumYearDifference(int diff) {
		if (diff <= minYearDiff) {
			throw new InvalidParameterException("The maximum year difference must be larger than the minimum year difference!");
		}
		this.maxYearDiff = diff;
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
