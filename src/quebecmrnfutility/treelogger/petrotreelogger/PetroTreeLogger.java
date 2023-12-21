/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.treelogger.petrotreelogger;

import java.util.Collection;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradePredictor;
import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import repicea.math.Matrix;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;

/**
 * This TreeLogger class makes it possible to estimate the underbark volumes by log grades in
 * sugar maple and yellow birch trees. It is based on the Petro grades (F1, F2, F3, F4, P). This class
 * does not implement the stochastic mode yet.
 * @author Mathieu Fortin - October 2009
 */
public class PetroTreeLogger extends TreeLogger<PetroTreeLoggerParameters, PetroLoggableTree> {

	private static final double VERY_SMALL = 1E-6;
	
	private PetroGradePredictor predictor;
	
	/**
	 * Official constructor for the extension
	 */
	public PetroTreeLogger() {
		this(false);
	}

	/**
	 * General constructor. 
	 * @param isVariabilityEnabled true to enable the variability in the parameter estimates
	 */
	public PetroTreeLogger(boolean isVariabilityEnabled) {
		super();
		predictor = new PetroGradePredictor(isVariabilityEnabled);
	}
	
	/**
	 * Constructor for script mode.
	 * @param params a PetroTreeLoggerParameters instance
	 * @param trees a collection of PetroLoggableTree instances
	 */
	public PetroTreeLogger(PetroTreeLoggerParameters params, Collection<PetroLoggableTree> trees) {
		this();
		setTreeLoggerParameters(params);
		init(trees);
	}
	
	@Override
	public void setTreeLoggerParameters(PetroTreeLoggerParameters params) {
		this.params = createDefaultTreeLoggerParameters();
	}
	
	
	@Override
	public void setTreeLoggerParameters() {
		setTreeLoggerParameters(null);
		params.showUI(null);
	}
	
	@Override
	protected void logThisTree(PetroLoggableTree tree) {
		Matrix volumes = predictor.getPredictedGradeUnderbarkVolumes(tree);
		PetroGradeSpecies species = tree.getPetroGradeSpecies(); 
		for (int i = 0; i < volumes.m_iRows; i++) {
			if (volumes.getValueAt(i, 0) > VERY_SMALL) {
				PetroTreeLogCategory product = getTreeLoggerParameters().getSpeciesLogCategories(species.name()).get(i);
				PetroTreeLoggerWoodPiece piece = new PetroTreeLoggerWoodPiece(product, tree, volumes.getValueAt(i, 0));
				addWoodPiece(tree, piece);
			}
		}
	}

	@Override
	public PetroTreeLoggerParameters createDefaultTreeLoggerParameters() {
		PetroTreeLoggerParameters params = new PetroTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		return params;
	}

	@Override
	public PetroLoggableTree getEligible(LoggableTree t) {
		if (t instanceof PetroLoggableTree) {
			PetroLoggableTree tree = (PetroLoggableTree) t;
			if (tree.getPetroGradeSpecies() != null && tree.getDbhCm() > 23d) {
				return tree;
			}
			
		}
		return null;
	}

	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		return check.getTreeInstance() instanceof PetroLoggableTree;
	}


//	public static void main(String[] args) {
//		PetroTreeLogger treeLogger = new PetroTreeLogger();
//		treeLogger.setTreeLoggerParameters();
//		int u = 0;
//	}
	
}
