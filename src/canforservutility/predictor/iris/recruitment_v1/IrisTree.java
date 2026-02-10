/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.allometrycalculator.LightAllometryCalculableTree;
import repicea.simulation.covariateproviders.treelevel.BasalAreaLargerThanSubjectM2Provider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmProvider;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;

public interface IrisTree extends BasalAreaLargerThanSubjectM2Provider,
													LightAllometryCalculableTree,
													LnDbhCmProvider,
													MonteCarloSimulationCompliantObject,
													IndexableErrorTerm {

	public static enum IrisSpecies {
		BOG,
		BOJ,
		BOP,
		CET,
		CHR,
		EPB,
		EPN,
		EPR,
		ERE,
		ERP,
		ERR,
		ERS,
		FRA,
		FRN,
		HEG,
		MEL,
		ORA,
		OSV,
		PEB,
		PEG,
		PET,
		PIB,
		PIG,
		PIR,
		PRP,
		PRU,
		SAB,
		SAL,
		SmallTrees,
		Sorbus,
		SouthernSpecies,
		THO,
		TIL;
		
	}
	
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	

	/**
	 * This method returns the species.
	 * @return a Iris2020Species enum
	 */
	public IrisSpecies getSpecies(); 

}
