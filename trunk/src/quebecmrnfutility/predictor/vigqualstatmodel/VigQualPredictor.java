/*
 * English version follows
 * 
 * Ce fichier fait partie de la bibliothèque mrnf-foresttools.
 * Il est protégé par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du ministère des Ressources naturelles et de la Faune du Gouvernement du 
 * Québec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Québec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Québec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Québec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.vigqualstatmodel;

import repicea.simulation.ModelBasedSimulator;

/**
 * The VigQualPredictor class implements a bivariate probit regression whose margins
 * respectively predicts tree vigor (MSCR) and tree quality (ABCD). The model has no plot random
 * effects. Consequently the random effect variability is disabled.
 * @author Mathieu Fortin - July 2012
 */
public class VigQualPredictor extends ModelBasedSimulator {

	private static final long serialVersionUID = 20120705L;

	/**
	 * General constructor.
	 * @param isParametersVariabilityEnabled true to enable the parameter variability or false to run in deterministic mode
	 * @param isResidualVariabilityEnabled true to enable the residual variability or false to run in deterministic mode
	 */
	public VigQualPredictor(boolean isParametersVariabilityEnabled,	boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect
	}

	
	
	
}
