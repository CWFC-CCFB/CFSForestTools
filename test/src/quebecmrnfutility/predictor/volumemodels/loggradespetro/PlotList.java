/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import java.util.ArrayList;

import repicea.math.Matrix;
import repicea.stats.sampling.FinitePopulationEstimate;

@SuppressWarnings("serial")
class PlotList extends ArrayList<Plot> {

	void setRealization(int id) {
		for (Plot plot : this) {
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				tree.setRealization(id);
			}
		}
	}
	
	FinitePopulationEstimate getHorvitzThompsonEstimate(int populationSize) {
		FinitePopulationEstimate estimate = new FinitePopulationEstimate(populationSize);
		
		for (Plot plot : this) {
			Matrix plotTotal = new Matrix(5,1);
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				plotTotal = plotTotal.add(tree.getRealizedValues());
			}
			estimate.addObservation(plotTotal, plot.toString());
		}
		return estimate;
	}
	
}
