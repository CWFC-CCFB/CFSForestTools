/*
 * This file is part of the mrnf-foresttool- library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.loggradespetro;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.util.ObjectUtility;

/**
 * The PetroGradePredictor class generates predictions of volumes by log grades.
 * @author Mathieu Fortin - Sept 2016
 */
@SuppressWarnings("serial")
public class PetroGradePredictor extends REpiceaPredictor {

	public static enum PetroLoggerVersion {
		WITH_NO_VARIABLE,
		WITH_VIGOUR_1234,
		WITH_QUALITY_ABCD,
		WITH_HARV_PRIOR_MSCR;

		PetroLoggerVersion() {}

		public int getId() {return this.ordinal() + 1;}
	}

	private final Map<PetroLoggerVersion, PetroGradePredictorPresenceSubModule> presenceSubModules;
	private final Map<PetroLoggerVersion, PetroGradePredictorVolumeSubModule> volumeSubModules;
	
	public PetroGradePredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		presenceSubModules = new HashMap<PetroLoggerVersion, PetroGradePredictorPresenceSubModule>();
		volumeSubModules = new HashMap<PetroLoggerVersion, PetroGradePredictorVolumeSubModule>();
		for (PetroLoggerVersion version : PetroLoggerVersion.values()) {
			presenceSubModules.put(version, new PetroGradePredictorPresenceSubModule(isParametersVariabilityEnabled, isResidualVariabilityEnabled, version));
			volumeSubModules.put(version, new PetroGradePredictorVolumeSubModule(isParametersVariabilityEnabled, isResidualVariabilityEnabled, version));
		}
		init();
	}

	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());

			String strParametersPresencePath = path + "0_PetroProductPresBeta.csv";
			String strParametersVolumePath = path + "0_PetroProductVolBeta.csv";
			ParameterMap betaPres = ParameterLoader.loadVectorFromFile(1, strParametersPresencePath);
			ParameterMap betaVol = ParameterLoader.loadVectorFromFile(1, strParametersVolumePath);
			
			String strOmegaPresencePath = path + "0_PetroProductPresOmega.csv";
			String strOmegaVolumePath = path + "0_PetroProductVolOmega.csv";
			ParameterMap omegaPres = ParameterLoader.loadVectorFromFile(1, strOmegaPresencePath);
			ParameterMap omegaVol = ParameterLoader.loadVectorFromFile(1, strOmegaVolumePath);

			String strCovParmsVolumePath = path + "0_PetroProductVolCovParms.csv";
			ParameterMap covParmsVol = ParameterLoader.loadVectorFromFile(1, strCovParmsVolumePath);

			Matrix beta;
			Matrix omega;
			Matrix errorCovariance;
			for (PetroLoggerVersion version : PetroLoggerVersion.values()) {
				beta = betaPres.get(version.getId());
				omega = omegaPres.get(version.getId()).squareSym(); 
				presenceSubModules.get(version).setParameterEstimates(new SASParameterEstimates(beta,omega));
				beta = betaVol.get(version.getId());
				omega = omegaVol.get(version.getId()).squareSym(); 
				volumeSubModules.get(version).setParameterEstimates(new SASParameterEstimates(beta,omega));
				errorCovariance = covParmsVol.get(version.getId()).squareSym();
				volumeSubModules.get(version).setDefaultResidualError(new GaussianErrorTermEstimate(errorCovariance));
			}
		} catch (IOException e) {	
			System.out.println("Unable to load the parameters of the PetroTreeLoggerPredictor instance");
			e.printStackTrace();
		}

	}

	/**
	 * This method returns a 5x1 matrix of the predicted volumes by log grade. The grade are ordered
	 * according to the ProductType enum variable.
	 * @param tree a PetroGradeTree instance
	 * @return a 5x1 matrix
	 */
	public Matrix getPredictedGradeVolumes(PetroGradeTree tree) {
		PetroLoggerVersion selectedVersion = getAppropriateVersion(tree);
		PetroGradePredictorPresenceSubModule presenceSubModule = presenceSubModules.get(selectedVersion);
		Matrix presenceProbabilities = presenceSubModule.getPredictedGradePresences(tree);

		PetroGradePredictorVolumeSubModule volumeSubModule = volumeSubModules.get(selectedVersion);
		Matrix conditionalVolumes = volumeSubModule.getPredictedLogTransformedGradeVolumes(tree);
		
		Matrix volumes = presenceProbabilities.elementWiseMultiply(conditionalVolumes);
		return volumes;
	}
		
	/**
	 * This method sets the appropriate version according to the features of the typical tree.
	 */
	private PetroLoggerVersion getAppropriateVersion(PetroGradeTree typicalTree) {

		Object qualiteABCD = typicalTree.getABCDQuality();
		if (qualiteABCD != null) {
			return PetroLoggerVersion.WITH_QUALITY_ABCD;
		} 

		Object harvestPriorityMSCR = typicalTree.getMSCRPriority();
		if (harvestPriorityMSCR != null) {
			return PetroLoggerVersion.WITH_HARV_PRIOR_MSCR;
		}

		Object vigor1234 = typicalTree.getVigorClass();
		if (vigor1234 != null) {
			return PetroLoggerVersion.WITH_VIGOUR_1234;
		}

		return PetroLoggerVersion.WITH_NO_VARIABLE;
	}

	
//	public static void main(String[] args) {
//		new PetroTreeLoggerPredictor(false, false);
//	}
}
