/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2019 Her Majesty the Queen in Right of Canada
 * Author: Mathieu Fortin - Canadian Forest Service
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
package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

import repicea.simulation.HierarchicalLevel;

/**
 * This class is a basic implementation of the DefoliationPlot interface. It can be used
 * for punctual call to the DefoliationPredictor class.
 * @author Mathieu Fortin - May 2019
 */
@SuppressWarnings("serial")
public class DefoliationPlotImpl implements DefoliationPlot {

	private final double latitudeDeg;
	private final double longitudeDeg;
	private final double elevationM;
	private final double volM3HaBlackSpruce;
	private final double volM3HaFirOtherSpruces;
	private final double propForestedArea;
	private int dateYr;

	/**
	 * Constructor.
	 * @param latitudeDeg the latitude in degrees
	 * @param longitudeDeg the longitude in degrees
	 * @param elevationM the elevation (m)
	 * @param volM3HaBlackSpruce the volume (m3/ha) of black spruce
	 * @param volM3HaFirOtherSpruces the volume (m3/ha) of fir and other spruce species
	 * @param propForestedArea the proportion of forested area
	 * @param dateYr the date (yr)
	 */
	public DefoliationPlotImpl(double latitudeDeg, 
			double longitudeDeg, 
			double elevationM, 
			double volM3HaBlackSpruce, 
			double volM3HaFirOtherSpruces, 
			double propForestedArea,
			int dateYr) {
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg;
		this.elevationM = elevationM;
		this.volM3HaBlackSpruce = volM3HaBlackSpruce;
		this.volM3HaFirOtherSpruces = volM3HaFirOtherSpruces;
		this.propForestedArea = propForestedArea;
		this.dateYr = dateYr;
	}
	
	
	
	
	@Override
	public double getLatitudeDeg() {return latitudeDeg;}

	@Override
	public double getLongitudeDeg() {return longitudeDeg;}

	@Override
	public double getElevationM() {return elevationM;}

	@Override
	public double getVolumeM3HaOfBlackSpruce() {return volM3HaBlackSpruce;}

	@Override
	public double getVolumeM3HaOfFirAndOtherSpruces() {return volM3HaFirOtherSpruces;}

	@Override
	public double getProportionForestedArea() {return propForestedArea;}

	@Override
	public String getSubjectId() {return "test";}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public int getDateYr() {return dateYr;}


}
