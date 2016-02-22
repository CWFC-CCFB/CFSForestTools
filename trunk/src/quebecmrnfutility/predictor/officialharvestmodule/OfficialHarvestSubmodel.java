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
package quebecmrnfutility.predictor.officialharvestmodule;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.MonteCarloSimulationCompliantObject;

/**
 * This private class contains a specific version of the harvest module. The versions depend on the nature of the treatment.
 * @author Mathieu Fortin - October 2011
 */
class OfficialHarvestSubmodel extends ModelBasedSimulator {

	private static final long serialVersionUID = 20100906L;

	
	/**
	 * General constructor that enables the stochastic implementation. The OfficialHarvestModel class contains 
	 * a map of instance of this class. Basically, the OfficialHarvestSubmodel implements a subversion of
	 * the Harvest model. NOTE: there is no random effect variability in this model.
	 * @param isParameterVariabilityEnabled = a boolean that enables the parameter variability
	 * @param isResidualVariabilityEnabled = a boolean that enables the residual variability
	 */
	protected OfficialHarvestSubmodel(boolean isParameterVariabilityEnabled,
			boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);
	}
	
	/**
	 * Default constructor with all uncertainty sources disabled.
	 */
	protected OfficialHarvestSubmodel() {
		this(false, false);
	}

	protected void setParameterEstimates(Matrix beta, Matrix variance) {
		setParameterEstimates(new SASParameterEstimate(beta, variance)); 
	}
	
	protected Matrix getSubParametersForThisStand(MonteCarloSimulationCompliantObject stand) {
		return getParametersForThisRealization(stand);
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#init()
	 */
	@Override
	protected void init() {}
}
