/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in Right of Canada
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
package quebecmrnfutility.predictor.artemis2014;

import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.allometrycalculator.LightAllometryCalculableTree;
import repicea.simulation.covariateproviders.treelevel.BasalAreaLargerThanSubjectM2Provider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmProvider;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;

/**
 * Ensure the tree instance is compatible with Artemis 2014's modules.<p>
 * @author Mathieu Fortin - November 2025
 */
public interface Artemis2014CompatibleTree extends BasalAreaLargerThanSubjectM2Provider,
													LightAllometryCalculableTree,
													LnDbhCmProvider,
													MonteCarloSimulationCompliantObject,
													IndexableErrorTerm,
													QcTreeQualityProvider {

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	
	/**
	 * This method returns the species group name.
	 * @return a String
	 */
	public String getSpeciesGroupName(); 

}
