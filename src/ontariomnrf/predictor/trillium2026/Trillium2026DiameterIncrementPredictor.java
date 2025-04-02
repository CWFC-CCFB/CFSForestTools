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
package ontariomnrf.predictor.trillium2026;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ontariomnrf.predictor.trillium2026.Trillium2026DiameterIncrementInternalPredictor.Effect;
import ontariomnrf.predictor.trillium2026.Trillium2026Tree.Trillium2026TreeSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.util.ObjectUtility;

/**
 * A class that implements Liam Gilson's diameter increment model.
 * @author Mathieu Fortin - March 2025
 */
@SuppressWarnings("serial")
public class Trillium2026DiameterIncrementPredictor extends REpiceaPredictor {

	
	private static Map<Trillium2026TreeSpecies, Matrix> CoefMap;
	private static Map<Trillium2026TreeSpecies, SymmetricMatrix> VCovMap;
	private static Map<Trillium2026TreeSpecies, List<Effect>> EffectMap;
	private static Map<Trillium2026TreeSpecies, Double> SigmaMap;
	
	private final Map<Trillium2026TreeSpecies, Trillium2026DiameterIncrementInternalPredictor> internalPredictorMap;


	/**
	 * Constructor.
	 * @param isVariabilityEnabled a boolean to enable/disable the stochastic variability
	 */
	public Trillium2026DiameterIncrementPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}

	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled a boolean to enable/disable the stochastic variability in the parameter estimates
	 * @param isResidualVariabilityEnabled a boolean to enable/disable the stochastic variability in the residual error term.
	 */
	protected Trillium2026DiameterIncrementPredictor(boolean isParametersVariabilityEnabled,
			boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled); // there are no random effects in this model 
		internalPredictorMap = new HashMap<Trillium2026TreeSpecies, Trillium2026DiameterIncrementInternalPredictor>();
		init();
	}

	@Override
	protected synchronized void init() {
		if (CoefMap == null) {
			instantiateStaticMaps();
		}
		for (Trillium2026TreeSpecies sp : CoefMap.keySet()) {
			Matrix beta = CoefMap.get(sp);
			SymmetricMatrix vcov = VCovMap.get(sp);
			ModelParameterEstimates parmEstimates = new ModelParameterEstimates(beta, vcov);
			Trillium2026DiameterIncrementInternalPredictor pred = new Trillium2026DiameterIncrementInternalPredictor(sp, isParametersVariabilityEnabled, isResidualVariabilityEnabled);
			pred.setParameterEstimates(parmEstimates);
			pred.setEffects(EffectMap.get(sp));
			internalPredictorMap.put(sp, pred);
			pred.setResidualStandardDeviation(SigmaMap.get(sp));
		}
	}

	
	@SuppressWarnings("resource")
	private synchronized void instantiateStaticMaps() {
		if (CoefMap == null) { // second check in case several threads are waiting in row to get in MF20250327
			Map<Trillium2026TreeSpecies, List<Double>> localCoefMap = new HashMap<Trillium2026TreeSpecies, List<Double>>();
			Map<Trillium2026TreeSpecies, List<String>> localEffectMap = new HashMap<Trillium2026TreeSpecies, List<String>>();
			List<String> uniqueEffects = new ArrayList<String>();
			Map<Trillium2026TreeSpecies, List<Double>> localVCovMap = new HashMap<Trillium2026TreeSpecies, List<Double>>();

			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_diaminc_coefs.csv";
			String vcovFilename = path + "0_diaminc_vcov.csv";
			String sigmaFilename = path + "0_diaminc_sigma.csv";

			CSVReader reader = null;
			try {
				reader = new CSVReader(betaFilename);
				Object[] record;
				while ((record = reader.nextRecord()) != null) {
					String species = record[1].toString();
					Trillium2026TreeSpecies spEnum = Trillium2026TreeSpecies.getTrilliumSpecies(species);
					if (!localCoefMap.containsKey(spEnum)) {
						localCoefMap.put(spEnum, new ArrayList<Double>());
						localEffectMap.put(spEnum, new ArrayList<String>());
					}
					String effect = record[2].toString();
					localEffectMap.get(spEnum).add(effect);
					if (!uniqueEffects.contains(effect)) {
						uniqueEffects.add(effect);
					}
					double coef = Double.parseDouble(record[3].toString());
					localCoefMap.get(spEnum).add(coef);
				}
	 		} catch (Exception e) {
	 			throw new UnsupportedOperationException("Enable to set the parameter estimates of " + getClass().getSimpleName());
	 		} finally {
	 			if (reader != null) {
	 				reader.close();
	 			}
	 		}
			
			try {
				reader = new CSVReader(vcovFilename);
				Object[] record;
				while ((record = reader.nextRecord()) != null) {
					String species = record[1].toString();
					Trillium2026TreeSpecies spEnum = Trillium2026TreeSpecies.getTrilliumSpecies(species);
					if (!localVCovMap.containsKey(spEnum)) {
						localVCovMap.put(spEnum, new ArrayList<Double>());
					}
					double vcov = Double.parseDouble(record[2].toString());
					localVCovMap.get(spEnum).add(vcov);
				}
	 		} catch (Exception e) {
	 			throw new UnsupportedOperationException("Enable to set the covariance of " + getClass().getSimpleName());
	 		} finally {
	 			if (reader != null) {
	 				reader.close();
	 			}
	 		}

			SigmaMap = new HashMap<Trillium2026TreeSpecies, Double>();
			
			try {
				reader = new CSVReader(sigmaFilename);
				Object[] record;
				while ((record = reader.nextRecord()) != null) {
					String species = record[1].toString();
					Trillium2026TreeSpecies spEnum = Trillium2026TreeSpecies.getTrilliumSpecies(species);
					double sigma = Double.parseDouble(record[2].toString());
					if (!SigmaMap.containsKey(spEnum)) {
						SigmaMap.put(spEnum, sigma);
					}
				}
	 		} catch (Exception e) {
	 			throw new UnsupportedOperationException("Enable to set the residual variance of " + getClass().getSimpleName());
	 		} finally {
	 			if (reader != null) {
	 				reader.close();
	 			}
	 		}

			
			CoefMap = new HashMap<Trillium2026TreeSpecies, Matrix>();
			VCovMap = new HashMap<Trillium2026TreeSpecies, SymmetricMatrix>();
			EffectMap = new HashMap<Trillium2026TreeSpecies, List<Effect>>();
			
			for (Trillium2026TreeSpecies sp : localCoefMap.keySet()) {
				CoefMap.put(sp, new Matrix(localCoefMap.get(sp)));
				SymmetricMatrix vcovMatrix = SymmetricMatrix.convertToSymmetricIfPossible(new Matrix(localVCovMap.get(sp)).squareSym());
				VCovMap.put(sp, vcovMatrix);
				EffectMap.put(sp, new ArrayList<Effect>());
				for (String effectStr : localEffectMap.get(sp)) {
					Effect effect = Trillium2026DiameterIncrementInternalPredictor.EffectMap.get(effectStr);
					EffectMap.get(sp).add(effect);
				}
			}
		}
	}
	
	
	public double predictGrowth(Trillium2026Plot plot, Trillium2026Tree tree) {
		Trillium2026TreeSpecies species = tree.getTrillium2026TreeSpecies();
		if (species == null) {
			throw new InvalidParameterException("The species of the Trillium2026Tree instance cannot be null!");
		}
		return internalPredictorMap.get(species).predictGrowth(plot, tree);
	}
	
	public static void main(String[] args) {
		new Trillium2026DiameterIncrementPredictor(false);
	}
}
