/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
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
package canforservutility.predictor.prices.dura;

import repicea.simulation.MonteCarloSimulationCompliantObject;

public interface DuraPriceContext extends MonteCarloSimulationCompliantObject {

	
	public double getEXCAUSLag4();
	
	public double getCLIMCOSTLAG();
	
	public double getFEDFUNDSLag1();
	
	public double getPSAVERTLag4();
	
	public double getFEDFUNDSLag3();

	public boolean isCovidPeriod();
	
	public double getHOUST();
	
	public double getEXCAUSLag1();
	
}
