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
package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

import biosimclient.BioSimPlot;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.DateYrProvider;
import repicea.simulation.covariateproviders.plotlevel.GeographicalCoordinatesProvider;

/**
 * This interface ensures the plot provides the explanatory variables for using 
 * Gray's (2013) model of spruce budworm defoliation.
 * @author Mathieu Fortin - May 2019
 */
public interface DefoliationPlot extends BioSimPlot,
										GeographicalCoordinatesProvider, 
										DateYrProvider,
										MonteCarloSimulationCompliantObject {

	/**
	 * This method returns the volume (m3/ha) of black spruce in the plot.
	 * @return a double
	 */
	public double getVolumeM3HaOfBlackSpruce();
	
	/**
	 * This method returns the combined volume (m3/ha) of balsam fir, red spruce and
	 * white spruce.
	 * @return a double
	 */
	public double getVolumeM3HaOfFirAndOtherSpruces();
	
	/**
	 * This method returns the proportion of forested area around the plot (between 0 and 1).
	 * @return a double
	 */
	public double getProportionForestedArea();
	
	
	public int getDateYr();

//	/**
//	 * This method returns the spring (April-May) sum of the average monthly extreme maximum temperatures. It
//	 * is referred to as sp_emax in the original article of Gray (2013). 
//	 * @return a double 
//	 */
//	public double getSpringSumMaxTemp(); 
//	
//	/**
//	 * This method returns the spring (April-May) sum of the average accumulation of degree-days. It is 
//	 * referred to as sp_dd in the original article of Gray (2013).
//	 * @return a double
//	 */
//	public double getSpringSumDegreeDays();
//
//	/**
//	 * This method returns the summer (June-August) sum of the average monthly extreme
//	 * minimum temperatures. It is referred to as sm_emin in the original article of
//	 * Gray (2013).
//	 * @return a double
//	 */
//	public double getSummerSumMinTemp();
//	
//	
//	/**
//	 * This method return the summer (June-August) sum of the average monthly extreme
//	 * maximum temperatures. It is referred to as sm_emax in the original article of
//	 * Gray (2013).
//	 * @return a double
//	 */
//	public double getSummerSumMaxTemp();
	
	
	
}
