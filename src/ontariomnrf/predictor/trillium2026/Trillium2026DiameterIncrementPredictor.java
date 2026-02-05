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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ontariomnrf.predictor.trillium2026.Trillium2026DiameterIncrementInternalPredictor.Effect;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.util.ObjectUtility;

/**
 * A class that implements Liam Gilson's diameter increment model.
 * @author Mathieu Fortin - March 2025
 */
@SuppressWarnings("serial")
public class Trillium2026DiameterIncrementPredictor extends REpiceaPredictor implements REpiceaSpeciesCompliantObject {

	private static Map<String, Species> SpeciesLookupMap = new HashMap<String, Species>();
	static {
		Species[] species = new Species[] {Species.Abies_balsamea, Species.Acer_pensylvanicum, Species.Acer_rubrum,
				Species.Acer_saccharinum, Species.Acer_saccharum, Species.Betula_alleghaniensis,
				Species.Betula_papyrifera, Species.Fagus_grandifolia, Species.Fraxinus_americana,
				Species.Fraxinus_nigra, Species.Larix_laricina, Species.Ostrya_virginiana,
				Species.Picea_glauca, Species.Picea_mariana, Species.Pinus_banksiana,
				Species.Pinus_resinosa, Species.Pinus_strobus, Species.Populus_balsamifera,
				Species.Populus_grandidentata, Species.Populus_tremuloides, Species.Prunus_pensylvanica,
				Species.Prunus_serotina, Species.Quercus_rubra, Species.Thuja_occidentalis,
				Species.Tilia_americana, Species.Tsuga_canadensis, Species.Liriodendron_tulipifera};
		for (Species sp : species) {
			SpeciesLookupMap.put(sp.getLatinName().replace(" ", ""), sp);
		}
		SpeciesLookupMap.put("Caryasp.", Species.Carya_spp);
		SpeciesLookupMap.put("Juglanssp.", Species.Juglans_spp);
		SpeciesLookupMap.put("Fraxinuspennsylvanica", Species.Fraxinus_pensylvanica); // this one has a typo in R 
//		SpeciesLookupMap.put("Meridional species", Species.Other_broadleaved);
		SpeciesLookupMap.put("Quercussp.", Species.Quercus_spp);
//		SpeciesLookupMap.put("Shrubs", Species.Broadleaved_shrubs);
		SpeciesLookupMap.put("Ulmussp.", Species.Ulmus_spp);
	}
	
	
	private static Map<Species, Matrix> CoefMap;
	private static Map<Species, SymmetricMatrix> VCovMap;
	private static Map<Species, List<Effect>> EffectMap;
	private static Map<Species, Double> SigmaMap;
	
	private final Map<Species, Trillium2026DiameterIncrementInternalPredictor> internalPredictorMap;

	boolean doBackTransformation = true; // for test purpose 
	
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
		internalPredictorMap = new HashMap<Species, Trillium2026DiameterIncrementInternalPredictor>();
		init();
	}

	void enableBackTransformation(boolean doBackTransformation) {
		this.doBackTransformation = doBackTransformation;
	}
	

	@Override
	protected synchronized void init() {
		if (CoefMap == null) {
			instantiateStaticMaps();
		}
		for (Species sp : CoefMap.keySet()) {
			Matrix beta = CoefMap.get(sp);
			SymmetricMatrix vcov = VCovMap.get(sp);
			ModelParameterEstimates parmEstimates = new ModelParameterEstimates(beta, vcov);
			Trillium2026DiameterIncrementInternalPredictor pred = new Trillium2026DiameterIncrementInternalPredictor(this, sp, isParametersVariabilityEnabled, isResidualVariabilityEnabled);
			pred.setParameterEstimates(parmEstimates);
			pred.setEffects(EffectMap.get(sp));
			internalPredictorMap.put(sp, pred);
			pred.setResidualStandardDeviation(SigmaMap.get(sp));
		}
	}

	static Species getSpeciesFromString(String speciesName) {
		Species species = SpeciesLookupMap.get(speciesName);
		if (species == null) {
			throw new UnsupportedOperationException("The diameter increment model of Trillium 2026 does not support species: " + speciesName);
		}
		return species;
	}
	
	@SuppressWarnings("resource")
	private synchronized void instantiateStaticMaps() {
		if (CoefMap == null) { // second check in case several threads are waiting in row to get in MF20250327
			Map<Species, List<Double>> localCoefMap = new HashMap<Species, List<Double>>();
			Map<Species, List<String>> localEffectMap = new HashMap<Species, List<String>>();
			List<String> uniqueEffects = new ArrayList<String>();
			Map<Species, List<Double>> localVCovMap = new HashMap<Species, List<Double>>();

			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_diaminc_coefs.csv";
			String vcovFilename = path + "0_diaminc_vcov.csv";
			String sigmaFilename = path + "0_diaminc_sigma.csv";

			CSVReader reader = null;
			try {
				reader = new CSVReader(betaFilename);
				Object[] record;
				while ((record = reader.nextRecord()) != null) {
					String species = record[1].toString().replace(" ", "");
					Species spEnum = getSpeciesFromString(species);
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
					String species = record[1].toString().replace(" ", "");
					Species spEnum = getSpeciesFromString(species);
					if (!localVCovMap.containsKey(spEnum)) {
						localVCovMap.put(spEnum, new ArrayList<Double>());
					}
					double vcov = Double.parseDouble(record[2].toString());
					localVCovMap.get(spEnum).add(vcov);
				}
	 		} catch (Exception e) {
	 			e.printStackTrace();
	 			throw new UnsupportedOperationException("Enable to set the covariance of " + getClass().getSimpleName());
	 		} finally {
	 			if (reader != null) {
	 				reader.close();
	 			}
	 		}

			SigmaMap = new HashMap<Species, Double>();
			
			try {
				reader = new CSVReader(sigmaFilename);
				Object[] record;
				while ((record = reader.nextRecord()) != null) {
					String species = record[1].toString().replace(" ", "");
					Species spEnum = getSpeciesFromString(species);
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

			
			CoefMap = new HashMap<Species, Matrix>();
			VCovMap = new HashMap<Species, SymmetricMatrix>();
			EffectMap = new HashMap<Species, List<Effect>>();
			
			for (Species sp : localCoefMap.keySet()) {
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
	
	/**
	 * Provide a diameter increment prediction.<p>
	 * 
	 * @param plot a Trillium2026Plot instance
	 * @param tree a Trillium2026Tree instance
	 * @return the diameter increment (mm)
	 */
	public double predictGrowth(Trillium2026Plot plot, Trillium2026Tree tree) {
		Species species = tree.getTrillium2026TreeSpecies();
		if (!SpeciesLookupMap.values().contains(species)) {
			throw new UnsupportedOperationException("The diameter increment model of Trillium 2026 does not support species: " + species.getLatinName());
		}
		return internalPredictorMap.get(species).predictGrowth(plot, tree);
	}

	/**
	 * Provide the list of eligible species for this module.
	 * @return a List of Species enums
	 */
	@Override
	public List<Species> getEligibleSpecies() {
		List<Species> species = new ArrayList<Species>(SpeciesLookupMap.values());
		Collections.sort(species);
		return species;
	}
	
	public static void main(String[] args) {
		new Trillium2026DiameterIncrementPredictor(false);
	}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Ontario;}
}
