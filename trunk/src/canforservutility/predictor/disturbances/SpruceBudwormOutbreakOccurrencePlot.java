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
package canforservutility.predictor.disturbances;

public interface SpruceBudwormOutbreakOccurrencePlot {

	
	/**
	 * This method returns the number of years since the last outbreak. If the date of 
	 * the last outbreak is unknown, then it should return null.
	 * @return an Integer or null if the date of the last outbreak is unknown
	 */
	public Integer getTimeSinceLastOutbreakYrs();
	
	/**
	 * This method is called when the time since last outbreak is unknown.
	 * @return an integer
	 */
	public int getTimeSinceInitialKnownDateYrs();
}
