/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
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
package quebecmrnfutility.treelogger.meristreelogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.SpeciesProvider;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;

/**
 * A tree logger class implementing the MERIS product matrix.
 * @author Mathieu Fortin - December 2025 
 */
public final class MerisTreeLogger extends TreeLogger<MerisTreeLoggerParameters, LoggableTree> {

	final Map<Class<?>, Boolean> validatedClasses;
	
	public MerisTreeLogger() {
		super();
		validatedClasses = new HashMap<Class<?>, Boolean>();
	}
	
	
	@Override
	protected void logThisTree(LoggableTree tree) {
		List<MerisWoodPiece> woodPieces = getTreeLoggerParameters().currentMatrix.processTree(tree);
		for (MerisWoodPiece wp : woodPieces) {
			addWoodPiece(tree, wp);
		}
	}
	
	@Override
	public void setTreeLoggerParameters() {
		setTreeLoggerParameters(null);
		params.showUI(null);
	}
	
	@Override
	public MerisTreeLoggerParameters createDefaultTreeLoggerParameters() {
		MerisTreeLoggerParameters params = new MerisTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		return params;
	}

	@Override
	public LoggableTree getEligible(LoggableTree t) {
		return internalCheck(t) ? t : null;
	}

	private synchronized boolean internalCheck(Object tree) {
		Class<?> clazz = tree.getClass();
		if (!validatedClasses.containsKey(clazz)) {
			validatedClasses.put(clazz, tree instanceof LoggableTree && 
				tree instanceof DbhCmProvider &&
				tree instanceof SpeciesProvider);
		}
		return validatedClasses.get(clazz);
	}
	
	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		Object o = check.getTreeInstance();
		return o == null ? false : internalCheck(o);
	}

}
