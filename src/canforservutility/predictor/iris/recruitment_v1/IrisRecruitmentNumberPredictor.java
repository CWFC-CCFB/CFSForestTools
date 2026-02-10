/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris.recruitment_v1;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.predictor.iris.recruitment_v1.IrisTree.IrisSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.util.ObjectUtility;

/**
 * The Iris2020RecruitmentNumberPredictor class implements the negative binomial part of the recruitment module in the Iris simulator. 
 * @author Mathieu Fortin - June 2023
 */
@SuppressWarnings("serial")
public class IrisRecruitmentNumberPredictor extends REpiceaPredictor {

	static List<Integer> OccupancyIndexEffects = new ArrayList<Integer>();
	static {
		OccupancyIndexEffects.add(25);
	}

	static boolean IsForTestPurposes = false;
	
	private final Map<IrisSpecies, IrisRecruitmentNumberInternalPredictor> internalPredictors;
	final IrisRecruitmentOccurrencePredictor occurrencePredictor;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 * @param occurrencePredictor an IrisRecruitmentOccurrencePredictor instance
	 */
	public IrisRecruitmentNumberPredictor(boolean isVariabilityEnabled, IrisRecruitmentOccurrencePredictor occurrencePredictor) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled, occurrencePredictor);		
	}

	/**
	 * Protected constructor for test purposes.
	 * @param isParameterVariabilityEnabled true to enable the variability in the parameter estimates
	 * @param isRandomEffectsVariabilityEnabled true to enable the random effect variability
	 * @param isResidualVariabilityEnabled true to enable the residual variability
	 * @param occurrencePredictor an IrisRecruitmentOccurrencePredictor instance
	 */
	protected IrisRecruitmentNumberPredictor(boolean isParameterVariabilityEnabled, 
			boolean isRandomEffectsVariabilityEnabled,
			boolean isResidualVariabilityEnabled, 
			IrisRecruitmentOccurrencePredictor occurrencePredictor) {
		super(isParameterVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);		// no random effect in this module
		internalPredictors = new HashMap<IrisSpecies, IrisRecruitmentNumberInternalPredictor>();
		init();
		this.occurrencePredictor = occurrencePredictor;
	}

	@Override
	protected void init() {
		String rootPath = ObjectUtility.getRelativePackagePath(getClass());
		String betaFilename = rootPath + "0_RecruitmentNumberBeta.csv";
		String omegaFilename = rootPath + "0_RecruitmentNumberOmega.csv";
		String speciesEffectMatchesFilename = rootPath + "0_RecruitmentNumberSpeciesEffectMatches.csv";
		
		try {
			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
			ParameterMap speciesEffectMatchesMap = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchesFilename);
			for (IrisSpecies sp : IrisSpecies.values()) {
				Matrix beta = betaMap.get(sp.ordinal() + 1);
				SymmetricMatrix omega = omegaMap.get(sp.ordinal() + 1).squareSym();
				Matrix thetaMat = beta.getSubMatrix(beta.m_iRows - 1, beta.m_iRows - 1, 0, 0);  // theta was concatenated to beta in R
				beta = beta.getSubMatrix(0, beta.m_iRows - 2, 0, 0);  // drop theta from beta
				Matrix speciesEffectMatches = speciesEffectMatchesMap.get(sp.ordinal() + 1);
				speciesEffectMatches = speciesEffectMatches.getSubMatrix(0, speciesEffectMatches.m_iRows - 2, 0, 0); // remove the last effect which is theta
				IrisRecruitmentNumberInternalPredictor subPredictor = new IrisRecruitmentNumberInternalPredictor(this,
						isParametersVariabilityEnabled, 
						isRandomEffectsVariabilityEnabled,
						isResidualVariabilityEnabled, 
						thetaMat.getValueAt(0, 0),
						beta, 
						omega,
						speciesEffectMatches);
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters in the module of recruitment occurrence in Iris 2020!");
		}
	}

	/**
	 * Returns the number of recruits conditional on the occurrence of recruitment.
	 * @param plot an Iris2020CompatiblePlot instance
	 * @param species an IrisSpecies enum
	 * @return a double that is the number of recruits in the plot
	 */
	public double predictNumberOfRecruits(IrisRecruitmentPlot plot, IrisSpecies species) {
		return internalPredictors.get(species).predictNumberOfRecruits(plot, species);
	}
	

	/*
	 * For test purposes.
	 */
	double getInvThetaParameterEstimate(IrisSpecies species) {
		return internalPredictors.get(species).invTheta;
	}
}
