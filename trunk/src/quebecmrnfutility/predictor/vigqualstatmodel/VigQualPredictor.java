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
package quebecmrnfutility.predictor.vigqualstatmodel;

import repicea.simulation.REpiceaPredictor;

/**
 * The VigQualPredictor class implements a bivariate probit regression whose margins
 * respectively predicts tree vigor (MSCR) and tree quality (ABCD). The model has no plot random
 * effects. Consequently the random effect variability is disabled.
 * @author Mathieu Fortin - July 2012
 */
public class VigQualPredictor extends REpiceaPredictor {

	private static final long serialVersionUID = 20120705L;

	/**
	 * General constructor.
	 * @param isVariabilityEnabled true to enable the parameter variability or false to run in deterministic mode
	 */
	public VigQualPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

	
}
