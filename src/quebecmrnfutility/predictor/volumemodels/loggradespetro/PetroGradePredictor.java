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
package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.util.ObjectUtility;

/**
 * The PetroGradePredictor class generates predictions of UNDER-bark volumes by log grades.
 * @author Mathieu Fortin - Sept 2016
 */
@SuppressWarnings("serial")
public class PetroGradePredictor extends REpiceaPredictor {

	private static ParameterMap betaPres;
	private static ParameterMap betaVol;
	private static ParameterMap omegaPres;
	private static ParameterMap omegaVol;
	private static ParameterMap covParmsVol;
	private static boolean loaded;
	
	protected static enum PetroGradePredictorVersion {
		WITH_NO_VARIABLE,
		WITH_VIGOUR_1234,
		WITH_QUALITY_ABCD,
		WITH_HARV_PRIOR_MSCR;

		PetroGradePredictorVersion() {}

		public int getId() {return this.ordinal() + 1;}
	}

	private final Map<PetroGradePredictorVersion, PetroGradePredictorPresenceSubModule> presenceSubModules;
	private final Map<PetroGradePredictorVersion, PetroGradePredictorVolumeSubModule> volumeSubModules;
	
	public PetroGradePredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}
	
	PetroGradePredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);
		presenceSubModules = new HashMap<PetroGradePredictorVersion, PetroGradePredictorPresenceSubModule>();
		volumeSubModules = new HashMap<PetroGradePredictorVersion, PetroGradePredictorVolumeSubModule>();
		for (PetroGradePredictorVersion version : PetroGradePredictorVersion.values()) {
			presenceSubModules.put(version, new PetroGradePredictorPresenceSubModule(isParametersVariabilityEnabled, isResidualVariabilityEnabled, version));
			volumeSubModules.put(version, new PetroGradePredictorVolumeSubModule(isParametersVariabilityEnabled, isResidualVariabilityEnabled, version));
		}
		init();
	}

	private synchronized void loadParametersFromFile() throws IOException {
		if (!loaded) {
			String path = ObjectUtility.getRelativePackagePath(getClass());

			String strParametersPresencePath = path + "0_PetroProductPresBeta.csv";
			String strParametersVolumePath = path + "0_PetroProductVolBeta.csv";
			betaPres = ParameterLoader.loadVectorFromFile(1, strParametersPresencePath);
			betaVol = ParameterLoader.loadVectorFromFile(1, strParametersVolumePath);
			
			String strOmegaPresencePath = path + "0_PetroProductPresOmega.csv";
			String strOmegaVolumePath = path + "0_PetroProductVolOmega.csv";
			omegaPres = ParameterLoader.loadVectorFromFile(1, strOmegaPresencePath);
			omegaVol = ParameterLoader.loadVectorFromFile(1, strOmegaVolumePath);

			String strCovParmsVolumePath = path + "0_PetroProductVolCovParms.csv";
			covParmsVol = ParameterLoader.loadVectorFromFile(1, strCovParmsVolumePath);
			loaded = true;
		}
	}
	
	@Override
	protected void init() {
		try {
			if (!loaded) {
				loadParametersFromFile();
			}
			
			Matrix beta;
			SymmetricMatrix omega;
			SymmetricMatrix errorCovariance;
			for (PetroGradePredictorVersion version : PetroGradePredictorVersion.values()) {
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
 	 * <ul>
	 * <li> slot 0 : volume of grade F1 (m3) </li>
	 * <li> slot 1 : volume of grade F2 (m3) </li>
	 * <li> slot 2 : volume of grade F3 (m3) </li>
	 * <li> slot 3 : volume of grade F4 (m3) </li>
	 * <li> slot 4 : volume of grade P (m3) </li>
	 * </ul>
	 */
	public Matrix getPredictedGradeUnderbarkVolumes(PetroGradeTree tree) {
		if (tree.getDbhCm() <= 23d) {
			return new Matrix(5,1);		// return a matrix of 0 : the trees must have at least 23.1 cm in dbh
		}
		PetroGradePredictorVersion selectedVersion = getAppropriateVersion(tree);
		PetroGradePredictorPresenceSubModule presenceSubModule = presenceSubModules.get(selectedVersion);
		Matrix presenceProbabilities = presenceSubModule.getPredictedGradePresences(tree);

		PetroGradePredictorVolumeSubModule volumeSubModule = volumeSubModules.get(selectedVersion);
		Matrix conditionalVolumes = volumeSubModule.getPredictedGradeVolumes(tree);
		
		Matrix volumes = presenceProbabilities.elementWiseMultiply(conditionalVolumes);
		return volumes;
	}
		
	/**
	 * This method sets the appropriate version according to the features of the typical tree.
	 */
	private PetroGradePredictorVersion getAppropriateVersion(PetroGradeTree typicalTree) {

		Object qualiteABCD = typicalTree.getTreeQuality();
		if (qualiteABCD != null) {
			return PetroGradePredictorVersion.WITH_QUALITY_ABCD;
		} 

		Object harvestPriorityMSCR = typicalTree.getHarvestPriority();
		if (harvestPriorityMSCR != null) {
			return PetroGradePredictorVersion.WITH_HARV_PRIOR_MSCR;
		}

		Object vigor1234 = typicalTree.getVigorClass();
		if (vigor1234 != null) {
			return PetroGradePredictorVersion.WITH_VIGOUR_1234;
		}

		return PetroGradePredictorVersion.WITH_NO_VARIABLE;
	}

	/*
	 * For manuscript purposes
	 */
	void replaceModelParameters() {
		presenceSubModules.get(PetroGradePredictorVersion.WITH_NO_VARIABLE).replaceModelParameters();
		volumeSubModules.get(PetroGradePredictorVersion.WITH_NO_VARIABLE).replaceModelParameters();
	}
	
//	@Override
//	public void clearDeviates() {
//		for (PetroGradePredictorPresenceSubModule p : presenceSubModules.values()) {
//			p.clearDeviates();
//		}
//		for (PetroGradePredictorVolumeSubModule p : volumeSubModules.values()) {
//			p.clearDeviates();
//		}
//	}

//	public static void main(String[] args) {
//		new PetroTreeLoggerPredictor(false, false);
//	}
}
