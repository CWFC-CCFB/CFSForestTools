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

/**
 * This class is a basic implementation of the SpruceBudwormOutbreakOccurrencePlot interface.
 * @author Mathieu Fortin - March 2019
 */
public class SpruceBudwormOutbreakOccurrencePlotImpl implements SpruceBudwormOutbreakOccurrencePlot {

	
	private final Integer timeSinceLastOutbreakYrs;
	private int initialKnownDate;

	/**
	 * Constructor.
	 * @param timeSinceLastOutbreakYrs an Integer instance (can be null)
	 * @param initialKnownDate an integer
	 */
	public SpruceBudwormOutbreakOccurrencePlotImpl(Integer timeSinceLastOutbreakYrs, int initialKnownDate) {
		this.timeSinceLastOutbreakYrs = timeSinceLastOutbreakYrs;
		this.initialKnownDate = initialKnownDate;
	}
	
	@Override
	public Integer getTimeSinceLastOutbreakYrs() {
		return timeSinceLastOutbreakYrs;
	}

	@Override
	public int getTimeSinceInitialKnownDateYrs() {
		return initialKnownDate;
	}

}
