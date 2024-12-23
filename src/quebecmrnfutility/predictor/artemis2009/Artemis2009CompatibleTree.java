/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.artemis2009;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.allometrycalculator.LightAllometryCalculableTree;
import repicea.simulation.covariateproviders.treelevel.BasalAreaLargerThanSubjectM2Provider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmProvider;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;

public interface Artemis2009CompatibleTree extends BasalAreaLargerThanSubjectM2Provider,
													LightAllometryCalculableTree,
													LnDbhCmProvider,
													MonteCarloSimulationCompliantObject,
													IndexableErrorTerm {

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	
	/**
	 * This method returns the species group name.
	 * @return a String
	 */
	public String getSpeciesGroupName(); 

}
