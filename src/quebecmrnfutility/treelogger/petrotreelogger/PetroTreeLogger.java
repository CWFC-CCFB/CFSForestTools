/*
 * English version follows
 * 
 * Ce fichier fait partie de la biblioth�que mrnf-foresttools.
 * Il est prot�g� par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du minist�re des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Qu�bec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.treelogger.petrotreelogger;

import java.util.Collection;

import quebecmrnfutility.predictor.loggradespetro.PetroGradePredictor;
import quebecmrnfutility.predictor.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import repicea.math.Matrix;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;

/**
 * This TreeLogger class makes it possible to estimate the volumes by log grades in
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
		Matrix volumes = predictor.getPredictedGradeVolumes(tree);
		PetroGradeSpecies species = tree.getPetroGradeSpecies(); 
		for (int i = 0; i < volumes.m_iRows; i++) {
			if (volumes.m_afData[i][0] > VERY_SMALL) {
				PetroTreeLogCategory product = getTreeLoggerParameters().getSpeciesLogCategories(species.name()).get(i);
				PetroTreeLoggerWoodPiece piece = new PetroTreeLoggerWoodPiece(product, volumes.m_afData[i][0], tree);
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
	public boolean isCompatibleWith(Object referent) {
		return referent instanceof PetroLoggableTree;
	}


//	public static void main(String[] args) {
//		PetroTreeLogger treeLogger = new PetroTreeLogger();
//		treeLogger.setTreeLoggerParameters();
//		int u = 0;
//	}
	
}
