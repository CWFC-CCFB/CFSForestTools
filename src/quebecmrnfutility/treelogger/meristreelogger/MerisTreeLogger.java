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

import java.util.List;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;

/**
 * A tree logger class implementing the MERIS product matrix.
 * @author Mathieu Fortin - December 2025 
 */
public class MerisTreeLogger extends TreeLogger<MerisTreeLoggerParameters, MerisLoggableTree> {

	@Override
	protected void logThisTree(MerisLoggableTree tree) {
		List<MerisWoodPiece> woodPieces = getTreeLoggerParameters().currentMatrix.processTree(tree);
		for (MerisWoodPiece wp : woodPieces) {
			addWoodPiece(tree, wp);
		}
	}

	@Override
	public void setTreeLoggerParameters(MerisTreeLoggerParameters params) {
		this.params = createDefaultTreeLoggerParameters();
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
	public MerisLoggableTree getEligible(LoggableTree t) {
		if (t instanceof MerisLoggableTree) {
			MerisLoggableTree merisTree = (MerisLoggableTree) t;
			return merisTree.getDbhCm() >= 9.1 ? merisTree : null;
		}
		return null;
	}

	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		return check.getTreeInstance() instanceof MerisLoggableTree;
	}

}
