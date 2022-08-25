/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020-2021 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris2020.recruitment;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.util.ObjectUtility;

/**
 * The Iris2020RecruitmentNumberPredictor class implements the negative binomial part of the recruitment module in the Iris 2020 simulator.
 * @author Mathieu Fortin - May 2020
 */
@SuppressWarnings("serial")
public class Iris2020RecruitmentNumberPredictor extends REpiceaPredictor {

	private final Map<Iris2020Species, Iris2020RecruitmentNumberInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public Iris2020RecruitmentNumberPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);		
	}

	/**
	 * Protected constructor for test purposes.
	 * @param isParameterVariabilityEnabled 
	 * @param isResidualVariabilityEnabled
	 */
	protected Iris2020RecruitmentNumberPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this module
		internalPredictors = new HashMap<Iris2020Species, Iris2020RecruitmentNumberInternalPredictor>();
		init();
	}

	@Override
	protected void init() {
		String rootPath = ObjectUtility.getRelativePackagePath(getClass());
		String betaFilename = rootPath + "0_RecruitmentNumberBeta.csv";
		String omegaFilename = rootPath + "0_RecruitmentNumberOmega.csv";
		String thetaFilename = rootPath + "0_RecruitmentNumberTheta.csv";
		String speciesEffectMatchesFilename = rootPath + "0_RecruitmentNumberSpeciesEffectMatches.csv";
		
		try {
			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
			ParameterMap thetaMap = ParameterLoader.loadVectorFromFile(1, thetaFilename);
			ParameterMap speciesEffectMatchesMap = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchesFilename);
			for (Iris2020Species sp : Iris2020Species.values()) {
				Matrix beta = betaMap.get(sp.ordinal() + 1);
				SymmetricMatrix omega = omegaMap.get(sp.ordinal() + 1).squareSym();
				Matrix thetaMat = thetaMap.get(sp.ordinal() + 1);
				Matrix speciesEffectMatches = speciesEffectMatchesMap.get(sp.ordinal() + 1);
				Iris2020RecruitmentNumberInternalPredictor subPredictor = new Iris2020RecruitmentNumberInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled, thetaMat.getValueAt(0, 0));
				subPredictor.setBeta(beta, omega);
				subPredictor.setEffectList(speciesEffectMatches);
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters in the module of recruitment occurrence in Iris 2020!");
		}
	}

	/**
	 * Returns the number of recruits conditional on the occurrence of recruitment.
	 * @param plot an Iris2020CompatiblePlot instance
	 * @param tree an Iris2020CompatibleTree instance
	 * @return a double that is the number of recruits in the plot
	 */
	public double predictNumberOfRecruits(Iris2020CompatiblePlot stand, Iris2020CompatibleTree tree) {
		return internalPredictors.get(tree.getSpecies()).predictNumberOfRecruits(stand, tree);
	}
	
	public static void main(String[] args) {
		new Iris2020RecruitmentNumberPredictor(false);
	}

	/*
	 * For test purposes.
	 */
	double getInvThetaParameterEstimate(Iris2020Species species) {
		return internalPredictors.get(species).invTheta;
	}
}
