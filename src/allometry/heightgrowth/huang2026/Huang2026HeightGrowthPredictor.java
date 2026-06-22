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
package allometry.heightgrowth.huang2026;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.climate.REpiceaClimateVariableInformation.BioSimModel;
import repicea.simulation.climate.REpiceaClimateVariableInformation.EvaluationDate;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.climate.REpiceaClimateVariableProvider;
import repicea.simulation.climate.REpiceaMonthlyClimateCompilationInformation;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanJulyTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalAnnualPrecipitationMmProvider;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.util.ObjectUtility;

/**
 * A class that implements Huang et al.'s (2026) height growth models. 
 * 
 * @author Mathieu Fortin - June 2026
 * 
 * @see <a href=https://open.alberta.ca/publications/improved-height-growth-models-for-major-alberta-tree-species>  
 * Huang, S., Stadt, K., Hossain, K., and Odell, R. 2026. Improved Height Growth Models for Major Alberta Tress
 * Species with and without Climate Variables. Government of Alberta. Alberta Forestry and Parks. Forestry Division. 
 * Technical Report Pub. No.: T/2026-RIBS01
 * </a>  */
@SuppressWarnings("serial")
@SimulationModule(type = ModuleType.HeightGrowth, scope = SpeciesLocale.Alberta)
public class Huang2026HeightGrowthPredictor extends REpiceaPredictor implements REpiceaSpeciesCompliantObject, 
																			ClimateSensitivePredictor {

	
	final static List<Species> EligibleSpecies = Collections.unmodifiableList(Arrays.asList(Species.Picea_glauca, Species.Populus_tremuloides));

	final static Map<Species, String> SuffixMap = new HashMap<Species, String>();
	static {
		SuffixMap.put(Species.Populus_tremuloides, "AW");
		SuffixMap.put(Species.Picea_glauca, "SW");
	}
	
	
	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Huang2026HeightGrowthPlot.class, 
				Resolution.IntervalAveragedStarting20YrsBeforeFinalMeasurement, EvaluationDate.Now);
		CLIMATE_INFO.get(MeanJulyTemperatureCelsiusProvider.class).put(Resolution.Normals30Year, 
				new REpiceaClimateVariableInformation(Resolution.Normals30Year, BioSimModel.Normals1961_1990, "T", EvaluationDate.Now, new REpiceaMonthlyClimateCompilationInformation(new Integer[] {7}, false))); 
		CLIMATE_INFO.get(TotalAnnualPrecipitationMmProvider.class).put(Resolution.Normals30Year, 
				new REpiceaClimateVariableInformation(Resolution.Normals30Year, BioSimModel.Normals1961_1990, "PRCP_TT", EvaluationDate.Now)); 
	}

	static Map<Species, Matrix> CoefMap;
	static Map<Species, SymmetricMatrix> OmegaMap;
	static Map<Species, Double> Sigma2Map;
		
	private final ConcurrentHashMap<Species, Species> surrogateMap;
	private final Map<Species, Huang2026HeightGrowthInternalPredictor> internalPredictorMap;
	
	public Huang2026HeightGrowthPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);  // no random effects
		internalPredictorMap = new HashMap<Species, Huang2026HeightGrowthInternalPredictor>();
		surrogateMap = new ConcurrentHashMap<Species, Species>();
		setSurrogateMapToDefaultValue();
		init();
	}

	@Override
	public List<Species> getEligibleSpecies() {return EligibleSpecies;}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Alberta;}

	@Override
	public ConcurrentHashMap<Species, Species> getSurrogateMap() {return surrogateMap;}

	@Override
	public void setSurrogateMapToDefaultValue() {
		getSurrogateMap().clear();
	}

	@Override
	protected synchronized void init() {
		if (CoefMap == null) {
			CoefMap = new HashMap<Species, Matrix>();
			OmegaMap = new HashMap<Species, SymmetricMatrix>();
			Sigma2Map = new HashMap<Species, Double>();
			try {
				String path = ObjectUtility.getRelativePackagePath(getClass());
				String betaFilename, omegaFilename, resFilename;
				for (Species sp : getEligibleSpecies()) {
					String suffix = SuffixMap.get(sp);
					betaFilename = path + "parms_" + suffix + ".csv";
					omegaFilename = path + "cov_" + suffix + ".csv";
					resFilename = path + "res_" + suffix + ".csv";
					Matrix coef = ParameterLoader.loadVectorFromFile(betaFilename).get();
					CoefMap.put(sp, coef);
					Matrix omegaTrans = ParameterLoader.loadVectorFromFile(omegaFilename).get();
					SymmetricMatrix omega = omegaTrans.squareSym();
					OmegaMap.put(sp, omega);
					Matrix sigma2res = ParameterLoader.loadVectorFromFile(resFilename).get();
					Sigma2Map.put(sp, sigma2res.getValueAt(0, 0));
				}
			} catch (Exception e) {
				throw new RuntimeException("Unable to load the parameters of " + getClass().getSimpleName(), e);
			}
		}
		for (Species sp : getEligibleSpecies()) {
			internalPredictorMap.put(sp, new Huang2026HeightGrowthInternalPredictor(isParametersVariabilityEnabled, 
					isResidualVariabilityEnabled,
					this,
					sp,
					CoefMap.get(sp),
					OmegaMap.get(sp),
					Sigma2Map.get(sp)));
		}
	}

	@Override
	public Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {return CLIMATE_INFO;}
	
	Huang2026HeightGrowthInternalPredictor getInternalPredictor(Species sp) {
		if (!internalPredictorMap.containsKey(sp)) {
			throw new UnsupportedOperationException("The species " + sp.getLatinName() + " is not supported yet!");
		}
		return internalPredictorMap.get(sp);
	}

	/**
	 * Predict the tree height as a function of age and  dominant height.
	 * @param p a Huang2026SiteIndexPlot instance
	 * @return stand dominant height (m)
	 */
	public double predictHeightM(Huang2026HeightGrowthPlot p) {
		return getInternalPredictor(p.getDominantSpecies()).predictHeightM(p);
	}

}
