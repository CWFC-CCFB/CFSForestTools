/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2026 His Majesty the King in right of Canada
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
package ontariomnrf.predictor.trillium2026.diameterincrement.gls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.climate.REpiceaClimateVariableInformation.EvaluationDate;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.climate.REpiceaClimateVariableProvider;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.util.ObjectUtility;

/**
 * A class that implements a diameter increment model based on an inverse 
 * hyperbolic sinus transformation.
 * 
 * @author Mathieu Fortin - May 2026
 */
@SuppressWarnings("serial")
@SimulationModule(type = ModuleType.DiameterIncrement, scope = SpeciesLocale.Ontario)
public class Trillium2026DiameterIncrementPredictor extends REpiceaPredictor 
												implements REpiceaSpeciesCompliantObject,
												ClimateSensitivePredictor {

	
	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Trillium2026DiameterIncrementPlot.class, 
				Trillium2026DiameterIncrementPlot.ClimateVariableResolution, EvaluationDate.EndOfInterval);
	}

	private static final Map<String, Species> SpeciesLookupMap = new HashMap<String, Species>();
	private static final Map<Integer, Species> InternalSpeciesLookupMap = new HashMap<Integer, Species>();
	private static final List<Species> SpeciesList = Collections.unmodifiableList(Arrays.asList(
				Species.Abies_balsamea, 
				Species.Acer_pensylvanicum, 
				Species.Acer_rubrum,
				Species.Acer_saccharinum, 
				Species.Acer_saccharum, 
				Species.Betula_alleghaniensis,
				Species.Betula_papyrifera, 
				Species.Carya_spp,
				Species.Fagus_grandifolia, 
				Species.Fraxinus_americana,
				Species.Fraxinus_nigra, 
				Species.Fraxinus_pennsylvanica,
				Species.Juglans_spp,
				Species.Larix_laricina, 
				Species.Other_broadleaved, // Meridional species
				Species.Ostrya_virginiana,
				Species.Picea_glauca, 
				Species.Picea_mariana, 
				Species.Pinus_banksiana,
				Species.Pinus_resinosa, 
				Species.Pinus_strobus, 
				Species.Populus_balsamifera,
				Species.Populus_grandidentata, 
				Species.Populus_tremuloides, 
				Species.Prunus_pensylvanica,
				Species.Prunus_serotina, 
				Species.Quercus_rubra, 
				Species.Quercus_spp,
				Species.Broadleaved_shrubs, // Shrubs
				Species.Thuja_occidentalis,
				Species.Tilia_americana, 
				Species.Tsuga_canadensis, 
				Species.Ulmus_spp));

	static {
		int i = 1;
		for (Species sp : SpeciesList) {
			SpeciesLookupMap.put(sp.getLatinName().trim().toLowerCase(), sp);
			InternalSpeciesLookupMap.put(i++, sp);
		}
		SpeciesLookupMap.put("carya sp.", Species.Carya_spp);
		SpeciesLookupMap.put("juglans sp.", Species.Juglans_spp);
		SpeciesLookupMap.put("meridional species", Species.Other_broadleaved);
		SpeciesLookupMap.put("quercus sp.", Species.Quercus_spp);
		SpeciesLookupMap.put("shrubs", Species.Broadleaved_shrubs);
		SpeciesLookupMap.put("ulmus sp.", Species.Ulmus_spp);
	}
	
	public static double MAXIMUM_PERIOD_ANNUAL_INCREMENT_CM = 1.35;
	public static double MINIMUM_PERIOD_ANNUAL_INCREMENT_CM = -0.85;
		
	static boolean Verbose = false;
	private static Map<Species, Matrix> CoefMap;
	static Map<Species, SymmetricMatrix> VCovMap;
	private static Map<Species, List<Integer>> EffectMap;
	static Map<Species, Double> RhoMap;
//	private static Map<Species, SymmetricMatrix> PlotRanefMap;
//	private static Map<Species, SymmetricMatrix> TreeRanefMap;
	static Map<Species, SymmetricMatrix> ResVarMap;
	
	final Map<Species, Trillium2026DiameterIncrementInternalPredictor> internalPredictorMap;

	boolean doBackTransformation = true; // for test purpose 
	boolean boundEnabled = true;

	public static void setVerbose(boolean verbose) {Verbose = verbose;}
	
	
	/**
	 * Simpler constructor.
	 * @param isVariabilityEnabled a boolean to enable/disable the stochastic variability
	 */
	public Trillium2026DiameterIncrementPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}

	/**
	 * General constructor.
	 * @param isParametersVariabilityEnabled a boolean to enable/disable the stochastic variability in the parameter estimates
	 * @param isResidualVariabilityEnabled a boolean to enable/disable the stochastic variability in the residual error term.
	 */
	public Trillium2026DiameterIncrementPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
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
			Trillium2026DiameterIncrementInternalPredictor pred = new Trillium2026DiameterIncrementInternalPredictor(this, 
					sp, 
					isParametersVariabilityEnabled, 
					isResidualVariabilityEnabled,
					parmEstimates,
					EffectMap.get(sp),
					RhoMap.get(sp),
					ResVarMap.get(sp));
			internalPredictorMap.put(sp, pred);
		}
	}

	static Species getSpeciesFromString(String speciesName) {
		Species species = SpeciesLookupMap.get(speciesName.trim().toLowerCase());
		if (species == null) {
			throw new UnsupportedOperationException("The diameter increment model of Trillium 2026 does not support species: " + speciesName);
		}
		return species;
	}
	
	private void instantiateStaticMaps() {
		CSVReader reader = null;
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_diaminc_coefs.csv";
			String vcovFilename = path + "0_diaminc_vcov.csv";
			String effectMatchFilename = path + "0_diaminc_effectMatch.csv";
			String rhoFilename = path + "0_diaminc_rho.csv";
			String resVarFilename = path + "0_diaminc_ResidualVar.csv";

			ParameterMap parmMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap vcovMap = ParameterLoader.loadVectorFromFile(1, vcovFilename);
			ParameterMap effectMatchMap = ParameterLoader.loadVectorFromFile(1, effectMatchFilename);
			ParameterMap rhoMap = ParameterLoader.loadVectorFromFile(1, rhoFilename);
			ParameterMap resVarianceMap = ParameterLoader.loadVectorFromFile(1, resVarFilename);

			CoefMap = new HashMap<Species, Matrix>();
			VCovMap = new HashMap<Species, SymmetricMatrix>();
			EffectMap = new HashMap<Species, List<Integer>>();
			RhoMap = new HashMap<Species, Double>();
			ResVarMap = new HashMap<Species, SymmetricMatrix>();

			for (Integer speciesID : InternalSpeciesLookupMap.keySet()) {
				Species sp = InternalSpeciesLookupMap.get(speciesID); 
				CoefMap.put(sp, parmMap.get(speciesID));
				SymmetricMatrix vcovMatrix = SymmetricMatrix.convertToSymmetricIfPossible(vcovMap.get(speciesID).squareSym());
				VCovMap.put(sp, vcovMatrix);
				EffectMap.put(sp, new ArrayList<Integer>());
				Matrix effectList = effectMatchMap.get(speciesID);
				for (int i = 0; i < effectList.m_iRows; i++) {
					EffectMap.get(sp).add(((Number) effectList.getValueAt(i, 0)).intValue());
				}
				Matrix rho = rhoMap.get(speciesID);
				if (rho != null) {
					RhoMap.put(sp, rho.getValueAt(0, 0));
				}
				ResVarMap.put(sp, SymmetricMatrix.convertToSymmetricIfPossible(resVarianceMap.get(speciesID)));
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException("Failed to initialize the instance of the " + getClass().getSimpleName() + " class!");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	/**
	 * Provide a diameter increment prediction.<p>
	 * 
	 * @param plot a Trillium2026DiameterIncrementPlot instance
	 * @param tree a Trillium2026Tree instance
	 * @return the diameter increment (cm)
	 */
	public double predictDiameterIncrementCm(Trillium2026DiameterIncrementPlot plot, Trillium2026DiameterIncrementTree tree) {
		Species species = tree.getTrillium2026TreeSpecies();
		if (!SpeciesLookupMap.values().contains(species)) {
			throw new UnsupportedOperationException("The diameter increment model of Trillium 2026 does not support species: " + species.getLatinName());
		}
		return internalPredictorMap.get(species).predictDiameterIncrementCm(plot, tree);
	}

	/**
	 * Provide the list of eligible species for this module.
	 * @return a List of Species enums
	 */
	@Override
	public List<Species> getEligibleSpecies() {return SpeciesList;}
	
	public static void main(String[] args) {
		new Trillium2026DiameterIncrementPredictor(false);
	}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Ontario;}

	@Override
	public Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {
		return CLIMATE_INFO;
	}
	
	
	
}
