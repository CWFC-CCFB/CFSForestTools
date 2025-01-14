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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades.simplelinearmodel;

import java.util.ArrayList;
import java.util.Random;

import repicea.stats.sampling.FinitePopulationEstimate;


@SuppressWarnings("serial")
class PlotList extends ArrayList<SamplePlot> {

	static final Random RANDOM = new Random();
	
	void setRealization(int id) {
		for (SamplePlot plot : this) {
			plot.setMonteCarloRealizationId(id);
		}
	}
	
	FinitePopulationEstimate getHorvitzThompsonEstimate(int populationSize) {
		FinitePopulationEstimate estimate = new FinitePopulationEstimate(populationSize);
		
		for (SamplePlot plot : this) {
			estimate.addObservation(plot.getY(), plot.toString());
		}
		return estimate;
	}
	
	PlotList getBootstrapSample() throws CloneNotSupportedException {
		PlotList newList = new PlotList();
		for (int i = 0; i < size(); i++) {
			int plotIndex = (int) Math.floor(RANDOM.nextDouble() * size());
			newList.add(get(plotIndex).clone());
		}
		return newList;
	}
	
}
