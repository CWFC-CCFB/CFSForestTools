/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades;

import java.util.ArrayList;

import repicea.math.Matrix;
import repicea.stats.estimates.PopulationTotalEstimate;
import repicea.stats.sampling.PopulationUnit;

@SuppressWarnings("serial")
class PlotList extends ArrayList<WBirchLogGradesStandImpl> {

	void setRealization(int id) {
		for (WBirchLogGradesStandImpl plot : this) {
			plot.setMonteCarloRealizationId(id);
		}
	}
	
	PopulationTotalEstimate getHorvitzThompsonEstimate(int populationSize) {
		PopulationTotalEstimate estimate = new PopulationTotalEstimate(populationSize);
		
		for (WBirchLogGradesStandImpl plot : this) {
			Matrix plotTotal = new Matrix(7,1);
			for (WBirchLogGradesTreeImpl tree : plot.getTrees().values()) {
				plotTotal = plotTotal.add(tree.getRealizedValues());
			}
			estimate.addObservation(new PopulationUnit(plot.toString(), plotTotal));
		}
		return estimate;
	}
	
}
