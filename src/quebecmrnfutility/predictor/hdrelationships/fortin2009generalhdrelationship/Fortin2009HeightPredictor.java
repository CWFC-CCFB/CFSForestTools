/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge Epicea
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
package quebecmrnfutility.predictor.hdrelationships.fortin2009generalhdrelationship;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import quebecmrnfutility.predictor.QuebecGeneralSettings;
import quebecmrnfutility.predictor.hdrelationships.fortin2009generalhdrelationship.Fortin2009HeightableTree.Hd2009Species;
import repicea.math.DiagonalMatrix;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.climate.REpiceaClimateVariableInformation.EvaluationDate;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.climate.REpiceaClimateVariableProvider;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.hdrelationships.HDRelationshipPredictor;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;


/**
 * This class implements the general height-diameter relationship published in Fortin et al. (2009)
 * @author Mathieu Fortin - October 2009
 * @see <a href=https://mffp.gouv.qc.ca/nos-publications/relation-hauteur-diametre-especes-commerciales>  
 * Fortin, M., Bernier, S., Saucier, J.-P., and Labbe, F. 2009. Une relation hauteur-diametre tenant 
 * compte de l'influence de la station et du climat pour 20 especes commerciales du Quebec. 
 * Gouvernement du Quebec, Ministere des Ressources naturelles et de la Faune, Direction de 
 * la recherche forestiere. Memoire de recherche forestiere no 153. 22 p.
 * </a>
 */
@SuppressWarnings({ "serial", "deprecation" })
@SimulationModule(type = ModuleType.HDRelationship, scope = SpeciesLocale.Quebec)
public final class Fortin2009HeightPredictor extends HDRelationshipPredictor<Fortin2009HeightableStand, Fortin2009HeightableTree> 
												implements ClimateSensitivePredictor,
															REpiceaSpeciesCompliantObject {

	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Fortin2009HeightableStand.class, 
				Fortin2009HeightableStand.ClimateVariableResolution, 
				EvaluationDate.Now);
	}

	
	private static final List<Species> SpeciesList = Collections.unmodifiableList(Arrays.asList(
			Species.Betula_alleghaniensis,
			Species.Betula_papyrifera,
			Species.Quercus_rubra,
			Species.Picea_glauca,
			Species.Picea_mariana,
			Species.Picea_rubens,
			Species.Acer_rubrum,
			Species.Acer_saccharum,
			Species.Fraxinus_nigra,
			Species.Fagus_grandifolia,
			Species.Larix_laricina,
			Species.Ostrya_virginiana,
			Species.Populus_grandidentata,
			Species.Populus_tremuloides,
			Species.Pinus_strobus,
			Species.Pinus_banksiana,
			Species.Tsuga_canadensis,
			Species.Abies_balsamea,
			Species.Thuja_occidentalis,
			Species.Tilia_americana));
	
	
	static final Map<Species, Matrix> DummyMatrixMap = new HashMap<Species, Matrix>();
	static {
		for (int i = 0; i < SpeciesList.size(); i++) {
			Matrix dummy = new Matrix(1,SpeciesList.size());
			dummy.setValueAt(0, i, 1d);
			DummyMatrixMap.put(SpeciesList.get(i), dummy);
		}
		
	}

	private static enum DisturbanceType {HUMAN, 
		NATURAL, 
		NONE;
	
		private Matrix dummy;
		
		DisturbanceType() {
			dummy = new Matrix(1,3);
			dummy.setValueAt(0, ordinal(), 1d);
		}
		
		public Matrix getDummy() {return this.dummy;}
		
	}
		
	private final static Map<String,Matrix> DUMMY_ECO_REGION = new HashMap<String,Matrix>();
	static {
		Matrix dummy;
		dummy = new Matrix(1,4);
		dummy.setValueAt(0, 0, 1d);
		DUMMY_ECO_REGION.put("2b", dummy);
		DUMMY_ECO_REGION.put("2c", dummy);
		DUMMY_ECO_REGION.put("3c", dummy);
		DUMMY_ECO_REGION.put("3d", dummy);
		DUMMY_ECO_REGION.put("4d", dummy);
		DUMMY_ECO_REGION.put("4e", dummy);
		DUMMY_ECO_REGION.put("4f", dummy);
		DUMMY_ECO_REGION.put("5e", dummy);
		DUMMY_ECO_REGION.put("5f", dummy);	// region CENTRE
		
		dummy = new Matrix(1,4);
		dummy.setValueAt(0, 1, 1d);
		DUMMY_ECO_REGION.put("6h", dummy);
		DUMMY_ECO_REGION.put("6i", dummy);
		DUMMY_ECO_REGION.put("6j", dummy);
		DUMMY_ECO_REGION.put("6k", dummy);
		DUMMY_ECO_REGION.put("6l", dummy);
		DUMMY_ECO_REGION.put("6n", dummy);
		DUMMY_ECO_REGION.put("6o", dummy);
		DUMMY_ECO_REGION.put("6p", dummy);
		DUMMY_ECO_REGION.put("6q", dummy);
		DUMMY_ECO_REGION.put("6r", dummy);
		DUMMY_ECO_REGION.put("6s", dummy);
		DUMMY_ECO_REGION.put("7a", dummy);
		DUMMY_ECO_REGION.put("7b", dummy);
		DUMMY_ECO_REGION.put("7c", dummy);	// region N_EST
		
		dummy = new Matrix(1,4);
		dummy.setValueAt(0, 2, 1d);
		DUMMY_ECO_REGION.put("1a", dummy);
		DUMMY_ECO_REGION.put("2a", dummy);
		DUMMY_ECO_REGION.put("3a", dummy);
		DUMMY_ECO_REGION.put("3b", dummy);
		DUMMY_ECO_REGION.put("4a", dummy);
		DUMMY_ECO_REGION.put("4b", dummy);
		DUMMY_ECO_REGION.put("4c", dummy);
		DUMMY_ECO_REGION.put("5a", dummy);
		DUMMY_ECO_REGION.put("5b", dummy);
		DUMMY_ECO_REGION.put("5c", dummy);
		DUMMY_ECO_REGION.put("5d", dummy);
		DUMMY_ECO_REGION.put("6a", dummy);
		DUMMY_ECO_REGION.put("6b", dummy);
		DUMMY_ECO_REGION.put("6c", dummy);
		DUMMY_ECO_REGION.put("6d", dummy);
		DUMMY_ECO_REGION.put("6e", dummy);
		DUMMY_ECO_REGION.put("6f", dummy);
		DUMMY_ECO_REGION.put("6g", dummy);	// region OUEST
		
		dummy = new Matrix(1,4);
		dummy.setValueAt(0, 3, 1d);
		DUMMY_ECO_REGION.put("4g", dummy);
		DUMMY_ECO_REGION.put("4h", dummy);
		DUMMY_ECO_REGION.put("5g", dummy);
		DUMMY_ECO_REGION.put("5h", dummy);
		DUMMY_ECO_REGION.put("5i", dummy);
		DUMMY_ECO_REGION.put("5j", dummy); 
		DUMMY_ECO_REGION.put("6m", dummy);	// region S_EST
	}
	
	private static Map<String, Species> SpeciesLookupMap;
	
	private static synchronized Map<String, Species> getSpeciesLookupMap() {
		if (SpeciesLookupMap == null) {
			SpeciesLookupMap = new HashMap<String, Species>();
			for (Species s : SpeciesList) {
				SpeciesLookupMap.put(s.getLatinName().toLowerCase().trim(), s);
			}
			for (Hd2009Species vs : Hd2009Species.values()) {
				SpeciesLookupMap.put(vs.name().toLowerCase(), vs.species);
			}
		}
		return SpeciesLookupMap;
	}

	/**
	 * Provide a Species enum instance from a species code.
	 * @param speciesName a three-character species code (e.g., BOP) or the Latin name.
	 * @return a Species enum or null if the species is not eligible
	 */
	public static Species getSpeciesFromString(String speciesName) {
		return getSpeciesLookupMap().get(speciesName.toLowerCase().trim());
	}


	static Matrix Beta;
	static SymmetricMatrix Omega;
	static Matrix CovParms;
	
	final ConcurrentHashMap<Species, Species> surrogateMap;
	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isVariabilityEnabled a boolean that enables the stochastic mode
	 */
	public Fortin2009HeightPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled);
		surrogateMap = new ConcurrentHashMap<Species, Species>();
		setSurrogateMapToDefaultValue();
		init();
		oXVector = new Matrix(1,getParameterEstimates().getMean().m_iRows);
	}

	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public Fortin2009HeightPredictor() {
		this(false);
	}

	@Override
	protected synchronized void init() {
		if (Beta == null) {
			try {
				String path = ObjectUtility.getRelativePackagePath(getClass());
				String betaFilename = path + "0_HDRelationBeta.csv";
				String omegaFilename = path + "0_HDRelationOmega.csv";
				String covparmsFilename = path + "0_HDRelationCovParms.csv";
				
				Beta = ParameterLoader.loadVectorFromFile(betaFilename).get();
				Omega = SymmetricMatrix.convertToSymmetricIfPossible(
						ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym());
				CovParms = ParameterLoader.loadVectorFromFile(covparmsFilename).get();
			} catch (Exception e) {
				throw new RuntimeException("Unable to load the parameters of " + getClass().getSimpleName(), e);
			}
		} 				
				
		setParameterEstimates(new SASParameterEstimates(Beta.getDeepClone(), Omega.getDeepClone()));
				
		DiagonalMatrix matrixG = CovParms.getSubMatrix(0, 19, 0, 0).matrixDiagonal();
		Matrix defaultRandomEffectsMean = new Matrix(matrixG.m_iRows, 1);
		setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
		SymmetricMatrix sigma2 = SymmetricMatrix.convertToSymmetricIfPossible(CovParms.getSubMatrix(20, 20, 0, 0));
		double phi = CovParms.getValueAt(21, 0);
		setDefaultResidualError(SpeciesType.BroadleavedSpecies, new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.LINEAR));
				
		sigma2 = SymmetricMatrix.convertToSymmetricIfPossible(CovParms.getSubMatrix(22, 22, 0, 0));
		phi = CovParms.getValueAt(23, 0);
		setDefaultResidualError(SpeciesType.ConiferousSpecies, new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.LINEAR));			
	}
	
//	private Species convertSpeciesEnumToSpecies(REpiceaSpecies speciesEnum) {
//		if (speciesEnum instanceof Species) {
//			if (!SpeciesList.contains(speciesEnum)) {
//				throw new UnsupportedOperationException("The " + getClass().getSimpleName() + 
//						" does not support species " + speciesEnum.getLatinName() + "!");
//			}
//			return (Species) speciesEnum;
//		} else if (speciesEnum instanceof Hd2009Species) {
//			return ((Hd2009Species) speciesEnum).species;
//		} else {
//			throw new UnsupportedOperationException("The " + getClass().getSimpleName() + 
//						" does not support species " + speciesEnum.getLatinName() + "!");
//		}
//	}

	
	@Override
	protected synchronized RegressionElements fixedEffectsPrediction(Fortin2009HeightableStand stand, Fortin2009HeightableTree t, Matrix beta) {
		Matrix modelParameters = beta;
		double basalArea = stand.getBasalAreaM2Ha();
		if (basalArea < 0d) {
			System.out.println("Error in HD relationship: The basal area of the plot has not been calculated yet!");
			throw new InvalidParameterException("The basal area of the plot has not been calculated yet!");
		}
		double averageTemp = stand.getMeanAnnualTemperatureCelsius(this, Fortin2009HeightableStand.ClimateVariableResolution);
		DrainageGroup drainageGroup = getDrainageGroup(stand);
		String ecoRegion = stand.getEcoRegion();
		boolean isInterventionResult = stand.isInterventionResult();
		boolean isDefoliated = stand.isSBWDefoliated();
		
		Matrix dummyDrainageClass = drainageGroup.getDrainageDummy();
		Matrix dummyEcoRegion = Fortin2009HeightPredictor.DUMMY_ECO_REGION.get(ecoRegion);
		Matrix dummyDisturbance;
		if (isInterventionResult) {
			dummyDisturbance = DisturbanceType.HUMAN.getDummy();
		} else if (isDefoliated) {
			dummyDisturbance = DisturbanceType.NATURAL.getDummy();
		} else {
			dummyDisturbance = DisturbanceType.NONE.getDummy();
		}
		
		oXVector.resetMatrix();
		int pointer = 0;
//		Species species = this.convertSpeciesEnumToSpecies(t.getHeightableTreeSpecies());
		Species species = convertToEligibleSpecies(t.getREpiceaSpecies());
		double lnDbh = t.getLnDbhCmPlus1();
		double SSI = t.getSocialStatusIndex();
		double lnDbh2 = t.getSquaredLnDbhCmPlus1();
		Matrix dummySpecies = DummyMatrixMap.get(species);

		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummySpecies.m_iCols;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh * basalArea), 0, pointer);
		pointer += dummySpecies.m_iCols;
		oXVector.setValueAt(0, pointer, lnDbh * averageTemp);
		pointer ++;
		oXVector.setSubMatrix(dummyDrainageClass.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummyDrainageClass.m_iCols;
		oXVector.setSubMatrix(dummyEcoRegion.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummyEcoRegion.m_iCols;
		oXVector.setSubMatrix(dummyDisturbance.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummyDisturbance.m_iCols;
		oXVector.setValueAt(0, pointer, lnDbh * SSI);
		pointer ++;
		oXVector.setValueAt(0, pointer, lnDbh2 * SSI);
		pointer ++;

		Matrix matZ_i = dummySpecies.scalarMultiply(lnDbh2);	// design vector for the plot random effect
		oXVector.setSubMatrix(matZ_i, 0, pointer);
		pointer += dummySpecies.m_iCols;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh2 * basalArea), 0, pointer);
		pointer += dummySpecies.m_iCols;

		double fResult = 1.3 + oXVector.multiply(modelParameters).getValueAt(0, 0);
		
		RegressionElements regElements = new RegressionElements();
		
		regElements.fixedPred = fResult;
		regElements.vectorZ = matZ_i;
		regElements.species = species;
		
		return regElements;
	}
	
	
	private DrainageGroup getDrainageGroup(Fortin2009HeightableStand stand) {
		DrainageGroup drainageGroup = stand.getDrainageGroup();
		if (drainageGroup == null) {
			if (stand.getEcologicalType() != null && stand.getEcologicalType().length() >= 4) {	// else if the ecological type is available then provide a typical class that corresponds to the grouping XERIC MESIC SUBHYDRIC HYDRIC
				String environmentType = stand.getEcologicalType().substring(3, 4);
				if (!environmentType.isEmpty()) {
					drainageGroup = QuebecGeneralSettings.ENVIRONMENT_TYPE.get(environmentType);	
				} 
			}
		} 
		return drainageGroup; 
	}
	
	/**
	 * For testing only
	 * @param stand a Heightable2009Stand instance
	 * @return a Matrix instance
	 */
	public Matrix getBlups(Fortin2009HeightableStand stand) {
		if (doBlupsExistForThisSubject(stand)) {
			return getBlupsForThisSubject(stand).getMean();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Fortin2009HeightableTree> getTreesFromStand(Fortin2009HeightableStand stand) {
		return stand.getTrees(StatusClass.alive);
	}

	@Override
	public Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {
		return CLIMATE_INFO;
	}

	@Override
	public List<Species> getEligibleSpecies() {return SpeciesList;}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Quebec;}

	@Override
	public ConcurrentHashMap<Species, Species> getSurrogateMap() {return surrogateMap;}

	@Override
	public void setSurrogateMapToDefaultValue() {
		getSurrogateMap().clear();
		getSurrogateMap().put(Species.Other, Species.Betula_papyrifera);
		getSurrogateMap().put(Species.Other_broadleaved, Species.Betula_papyrifera);
		getSurrogateMap().put(Species.Other_coniferous, Species.Picea_mariana);
		getSurrogateMap().put(Species.Betula_spp, Species.Betula_papyrifera);
		getSurrogateMap().put(Species.Quercus_spp, Species.Quercus_rubra);
		getSurrogateMap().put(Species.Acer_spp, Species.Acer_rubrum);
		getSurrogateMap().put(Species.Fraxinus_spp, Species.Fraxinus_nigra);
		getSurrogateMap().put(Species.Populus_spp, Species.Populus_tremuloides);
		getSurrogateMap().put(Species.Pinus_resinosa, Species.Pinus_strobus);
	}
	
	

}