/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2026 His Majesty the King in right of Canada
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
package canforservutility.predictor.iris.recruitment_v1;

import canforservutility.predictor.iris.recruitment_v1.IrisTree.IrisSpecies;

/**
 * A package interface for test purpose.
 */
interface IrisRecruitmentPlotWithKnownOccupancy {

	/**
	 * Provide the occupancy index within a 10-km radius.<p>
	 * This is for test purpose.
	 * @param species an IrisSpecies enum variable
	 * @return the occupancy index
	 */
	public double getOccupancyIndex10km(IrisSpecies species);
}
