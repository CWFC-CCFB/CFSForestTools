/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.predictor.volumemodels.merchantablevolume;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableTree.VolSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;


/**
 * This class implements the merchantable volume equation published in Fortin et al. (2007).
 * NOTE: merchantability is defined as trees with diameter equal to or greater than 9.1 cm 
 * at breast height. The volume was calculated UNDER bark.
 * @author Mathieu Fortin - October 2009
 * @see <a href=https://doi.org/10.5558/tfc83754-5>  
 * Fortin, M., DeBlois, J., Bernier, S., and Blais, G. 2007. Mise au point d'un tarif de cubage general 
 * pour les forets quebecoises: une approche pour mieux evaluer l'incertitude associee aux previsions. 
 * The Forestry Chronicle 83(5) 754-765.
 * </a> 
 */
@SuppressWarnings({ "serial", "deprecation" })
@SimulationModule(type = ModuleType.Volume, scope = SpeciesLocale.Quebec)
public final class MerchantableVolumePredictor extends REpiceaPredictor implements REpiceaSpeciesCompliantObject {

	private static List<Species> SpeciesList = Arrays.asList(new Species[] {
			Species.Betula_populifolia, 
			Species.Betula_alleghaniensis,
			Species.Betula_papyrifera,
			Species.Prunus_serotina,
			Species.Quercus_rubra,
			Species.Picea_glauca,
			Species.Picea_mariana,
			Species.Picea_rubens,
			Species.Acer_rubrum,
			Species.Acer_saccharum,
			Species.Fraxinus_americana,
			Species.Fraxinus_nigra,
			Species.Fagus_grandifolia,
			Species.Larix_laricina,
			Species.Ulmus_americana,
			Species.Ostrya_virginiana,
			Species.Populus_balsamifera,
			Species.Populus_grandidentata,
			Species.Populus_tremuloides,
			Species.Pinus_strobus,
			Species.Pinus_banksiana,
			Species.Pinus_resinosa,
			Species.Tsuga_canadensis,
			Species.Abies_balsamea,
			Species.Thuja_occidentalis,
			Species.Tilia_americana});

	private static Map<Species, Matrix> DummyMap;
	
	private synchronized Map<Species, Matrix> getDummyMap() {
		if (DummyMap == null) {
			DummyMap = new HashMap<Species, Matrix>();
			for (Species s : SpeciesList) {
				Matrix m = new Matrix(1, SpeciesList.size());
				m.setValueAt(0, SpeciesList.indexOf(s), 1d);
				DummyMap.put(s, m);
			}
		}
		return DummyMap;
	}
	
	private static Map<String, Species> SpeciesLookupMap;
	
	private static synchronized Map<String, Species> getSpeciesLookupMap() {
		if (SpeciesLookupMap == null) {
			SpeciesLookupMap = new HashMap<String, Species>();
			for (Species s : SpeciesList) {
				SpeciesLookupMap.put(s.getLatinName().toLowerCase().trim(), s);
			}
			for (VolSpecies vs : VolSpecies.values()) {
				SpeciesLookupMap.put(vs.name().toLowerCase(), vs.species);
			}
		}
		return SpeciesLookupMap;
	}
	
	private static Matrix DefaultBetaMean; 
	private static SymmetricMatrix DefaultBetaVariance;
	private static Matrix CovParms;
		
	private Matrix sigma2;

	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isVariabilityEnabled = a boolean that enables the variability at the parameter level
	 */
	public MerchantableVolumePredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		init();
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
	}
	
	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public MerchantableVolumePredictor() {
		this(false);
	}

	@Override
	protected final void init() {
		if (DefaultBetaMean == null) {
			try {
				String path = ObjectUtility.getRelativePackagePath(getClass());
				String betaFilename = path + "0_MerchVolumeBeta.csv";
				String omegaFilename = path + "0_MerchVolumeOmega.csv";
				String covparmsFilename = path + "0_MerchVolumeCovParms.csv";

				DefaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
				DefaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
				CovParms = ParameterLoader.loadVectorFromFile(covparmsFilename).get();
			} catch (Exception e) {
				throw new RuntimeException("Unable to load the parameters of " + getClass().getSimpleName(), e);
			}
		}
		setParameterEstimates(new SASParameterEstimates(DefaultBetaMean.getDeepClone(), DefaultBetaVariance.getDeepClone()));
		SymmetricMatrix matrixGPlotLevel =  CovParms.getSubMatrix(0, 2, 0, 0).squareSym();
		Matrix defaultRandomEffectsPlotLevel = new Matrix(matrixGPlotLevel.m_iRows, 1);
		SymmetricMatrix matrixGCruiseLineLevel = CovParms.getSubMatrix(3, 5, 0, 0).squareSym();
		Matrix defaultRandomEffectsCruiseLineLevel = new Matrix(matrixGCruiseLineLevel.m_iRows, 1);
		sigma2 = CovParms.getSubMatrix(6, CovParms.m_iRows - 1, 0, 0);
		setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(defaultRandomEffectsPlotLevel, matrixGPlotLevel));
		setDefaultRandomEffects(HierarchicalLevel.CRUISE_LINE, new GaussianEstimate(defaultRandomEffectsCruiseLineLevel, matrixGCruiseLineLevel));
	}
	
	/**
	 * This method return the underbark volume estimate for an individual trees. 
	 * NOTE: Stochastic implementation is handled through the general constructor.
	 * The method returns 0 if the tree is smaller than 9.1 cm in dbh. It returns -1
	 * if the tree height has not been calculated.
	 * @param stand a VolumableStand object
	 * @param tree a TreeVolumable object
	 * @return the commercial underbark volume (dm3)
	 */
	public double predictTreeCommercialUnderbarkVolumeDm3(VolumableStand stand, VolumableTree tree) {
		if (tree.getDbhCm() < 9.1) {	// means this is a sapling
			return 0d;
		}

		if (tree.getHeightM() < 1.3) {	// means the height has not been calculated
			throw new InvalidParameterException("Volume cannot be calculated if the tree is not at least 1.3 m in height!");
		}
		
		REpiceaSpecies speciesEnum = tree.getVolumableTreeSpecies();
		Species species = convertSpeciesEnumToSpecies(speciesEnum);
		
		Matrix modelParameters = getParametersForThisRealization(stand);
		double volume = fixedEffectPrediction(stand, tree, modelParameters, species);
		volume += blupImplementation(stand, tree, species);
		volume += residualImplementation(tree, species);
		if (volume < 0d) {
			volume = 1d;		// at least 1 dm3 if dbh >= 9.1 Correction for negative volumes MF2021-03-25
		}
		return volume;
	}
	
	
	private Species convertSpeciesEnumToSpecies(REpiceaSpecies speciesEnum) {
		if (speciesEnum instanceof Species) {
			if (!SpeciesList.contains(speciesEnum)) {
				throw new UnsupportedOperationException("The " + MerchantableVolumePredictor.class.getSimpleName() + 
						" does not support species " + speciesEnum.getLatinName() + "!");
			}
			return (Species) speciesEnum;
		} else if (speciesEnum instanceof VolSpecies) {
			return ((VolSpecies) speciesEnum).species;
		} else {
			throw new UnsupportedOperationException("The " + MerchantableVolumePredictor.class.getSimpleName() + 
						" does not support species " + speciesEnum.getLatinName() + "!");
		}
	}

	/**
	 * Provide a Species enum instance from a species code.
	 * @param speciesName a three-character species code (e.g., BOP) or the Latin name.
	 * @return a Species enum or null if the species is not eligible
	 */
	public static Species getSpeciesFromString(String speciesName) {
		return getSpeciesLookupMap().get(speciesName.toLowerCase().trim());
	}

	
	
//	/**
//	 * Return the Latin names of the eligible species for this model.
//	 * @return a List of Strings.
//	 */
//	public static List<String> getEligibleSpecies() {
//		List<String> speciesList = new ArrayList<String>();
//		for (String sp : VolSpecies.getLatinNameList()) {
//			speciesList.add(sp.substring(0, 1).toUpperCase() + sp.substring(1));
//		}
//		return speciesList;
//	}
	
	/**
	 * A fast-track computation of deterministic predictions.
	 * @param speciesName the Latin name or the three-character code used in Quebec
	 * @param dbhCm tree dbh (cm)
	 * @param heightM tree height (m)
	 * @param overbark a boolean true to get the overbark volume 
	 * @return the volume (dm3)
	 */
	public double predictDeterministicTreeCommercialVolumeDm3(String speciesName, double dbhCm, double heightM, boolean overbark) {
		if (dbhCm < 9.1) {	// means this is a sapling
			return 0d;
		}
		if (heightM < 1.3) {	// means the height has not been calculated
			throw new InvalidParameterException("Volume cannot be calculated if the tree is not at least 1.3 m in height!");
		}
		Species species = getSpeciesFromString(speciesName);
		if (species == null) {
			throw new UnsupportedOperationException("The " + MerchantableVolumePredictor.class.getSimpleName() + 
					" does not support species " + speciesName + "!");
		}
		Matrix modelParameters = getParameterEstimates().getMean();
		double volume = computePrediction(dbhCm, dbhCm * dbhCm, heightM, modelParameters, species);
		if (overbark) {
			volume *= (1d + species.getBarkProportionOfWoodVolume(SpeciesLocale.Quebec));
		}
		return volume;
	}
	
	/**
	 * This method computes the fixed effect prediction.
	 * @param stand = a VolumableStand object
	 * @param t = a TreeVolumable object
	 * @return the fixed effect prediction (double)
	 * @throws Exception
	 */
	private double fixedEffectPrediction(VolumableStand stand, VolumableTree t, Matrix modelParameters, Species species) {
		double dbh = t.getDbhCm();
		double dbh2 = t.getSquaredDbhCm();
		double height = t.getHeightM();
		return computePrediction(dbh, dbh2, height, modelParameters, species);
	}

	private synchronized double computePrediction(double dbh, double dbh2, double height, Matrix modelParameters, Species species) {
		this.oXVector.resetMatrix();
		int pointeur = 0;
		double cylindre = Math.PI*dbh2*height*0.025;
		
		oXVector.setValueAt(0, pointeur, height/dbh);
		pointeur++;

		Matrix dummy = getDummyMap().get(species);
		oXVector.setSubMatrix(dummy.scalarMultiply(cylindre), 0, pointeur);
		pointeur += dummy.m_iCols;
		if (species.getSpeciesType() == SpeciesType.ConiferousSpecies) {
			double cylindreRes = cylindre*dbh;
			oXVector.setSubMatrix(dummy.scalarMultiply(cylindreRes), 0, pointeur);
		}
		
		return oXVector.multiply(modelParameters).getValueAt(0, 0);
	}
	
	
	/**
	 * This method accounts for the random effects in the predictions if the random effect variability is enabled. Otherwise, it returns 0d.
	 * @param stand = a VolumableStand object
	 * @param t = a TreeVolumable object
	 * @return a simulated random effect (double)
	 */
	private double blupImplementation(VolumableStand stand, VolumableTree t, Species species) {
		if (isRandomEffectsVariabilityEnabled) {					
			String cruiseLineID = stand.getCruiseLineID();
			if (cruiseLineID == null) {
				cruiseLineID = stand.getSubjectId();
			}
			CruiseLine cruiseLine = getCruiseLineForThisSubject(cruiseLineID, stand);
			Matrix cruiseLineRandomEffects = getRandomEffectsForThisSubject(cruiseLine);
			Matrix plotRandomEffects = getRandomEffectsForThisSubject(stand);
			Matrix totalRandomEffects = cruiseLineRandomEffects.add(plotRandomEffects);
//			VolSpecies species = t.getVolumableTreeSpecies();
			double dbh2 = t.getSquaredDbhCm();

			int type = 1;
			if (species.getSpeciesType() == SpeciesType.ConiferousSpecies) {
				type = 0;
			}
			return totalRandomEffects.getValueAt(type, 0) * dbh2;
		} else {
			return 0d;
		}
	}
	
	/**
	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
	 * @param stand a VolumableStand object
	 * @param t a TreeVolumable object
	 * @return a simulated residual (double)
	 */
	private double residualImplementation(VolumableTree t, Species species) {
		if (isResidualVariabilityEnabled) {
//			VolSpecies species = t.getVolumableTreeSpecies();
			Matrix dummy = getDummyMap().get(species);
			double dbh2 = t.getSquaredDbhCm();

			return Math.sqrt(dummy.multiply(sigma2).getValueAt(0, 0)) * dbh2 * StatisticalUtility.getRandom().nextGaussian();
		} else {
			return 0d;
		}
	}

	@Override
	public List<Species> getEligibleSpecies() {return SpeciesList;}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Quebec;}
	
//	/**
//	 * For testing purpose.
//	 * @param args
//	 */
//	public static void main (String[] args) {
//		@SuppressWarnings("unused")
//		MerchantableVolumePredictor test = new MerchantableVolumePredictor(false);
//		try {
//			System.out.println("Done without problems.");
//		} catch (Exception e) {
//			System.out.println("Problems!!!!!");
//			e.printStackTrace();
//		}
//	}

	
	
	
}

