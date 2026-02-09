/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2015 Gouvernement du Quebec 
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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.QuebecGeneralSettings;
import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.GeneralHeight2014Predictor.DisturbanceType;
import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.GeneralHeight2014Predictor.Effect;
import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.Heightable2014Tree.Hd2014Species;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.hdrelationships.HDRelationshipPredictor;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;

@SuppressWarnings("serial")
class GeneralHeight2014InternalPredictor extends HDRelationshipPredictor<Heightable2014Stand, Heightable2014Tree> {

	private final Hd2014Species species;
	private Map<String, Matrix> subDomainDummyMap;
	private Map<String, Matrix> vegPotDummyMap;
	private Map<String, Matrix> ecoTypeDummyMap;
	private Map<String, Matrix> disturbanceDummyMap;
	private List<Effect> effectList;
	
	protected GeneralHeight2014InternalPredictor(Hd2014Species species, boolean isVariabilityEnabled) {
		super(isVariabilityEnabled);
		this.species = species;
	}


	@Override
	protected void init() {}

	/*
	 * For extended visibility
	 */
	@Override
	protected void setDefaultRandomEffects(HierarchicalLevel level, Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> estimate) {
		super.setDefaultRandomEffects(level, estimate);
	}
	
	/*
	 * For extended visibility
	 */
	@Override
	protected void setParameterEstimates(ModelParameterEstimates defaultBeta) {
		super.setParameterEstimates(defaultBeta);
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
	}
	
	/*
	 * For extended visibility
	 */
	@Override
	protected void setDefaultResidualError(Enum<?> enumVar, GaussianErrorTermEstimate estimate) {
		super.setDefaultResidualError(enumVar, estimate);
	}
	
	protected void setSubDomainDummyMap(Map<String, Matrix> oMap) {
		subDomainDummyMap = oMap;
	}

	protected void setVegPotDummyMap(Map<String, Matrix> oMap) {
		vegPotDummyMap = oMap;
	}

	protected void setEcoTypeDummyMap(Map<String, Matrix> oMap) {
		ecoTypeDummyMap = oMap;
	}

	protected void setDisturbanceDummyMap(Map<String, Matrix> oMap) {
		disturbanceDummyMap = oMap;
	}
	
	protected void setEffectList(List<Effect> list) {
		effectList = list;
	}
	
	/**
	 * This method computes the fixed effect prediction and put the prediction,
	 * the Z vector,
	 * and the species name into m_oRegressionOutput member. The method applies
	 * in any cases no matter
	 * it is deterministic or stochastic.
	 * 
	 * @param stand a HeightableStand instance
	 * @param t a HeightableTree instance
	 */
	@Override
	protected synchronized RegressionElements fixedEffectsPrediction(Heightable2014Stand stand, Heightable2014Tree t, Matrix beta) {
//		Matrix modelParameters = getParametersForThisRealization(new BetaHeightableStandMonteCarlo(stand, t.getBetaHeightableTreeSpecies()));

		double basalArea = stand.getBasalAreaM2Ha();
		if (basalArea < 0d) {
			System.out.println("Error in HD relationship: The basal area of the plot has not been calculated yet!");
			throw new InvalidParameterException("The basal area of the plot has not been calculated yet!");
		}
		double averageTemp = stand.getMeanAnnualTemperatureCelsius(GeneralHeight2014Predictor.Normals30YearTemporalResolution);	
		String ecoRegion = stand.getEcoRegion();
		boolean isInterventionResult = stand.isInterventionResult();
		boolean isDefoliated = stand.isSBWDefoliated();
		double averageQDiam  = stand.getMeanQuadraticDiameterCm();
		double elevation = stand.getElevationM();	

		String subDomain = QuebecGeneralSettings.ECO_REGION_MAP.get(ecoRegion);
//		Map<String, Matrix> oMap = subDomainDummyReferenceMap.get(t.getBetaHeightableTreeSpecies());
		Matrix dummySubDomain = subDomainDummyMap.get(subDomain.toUpperCase());
		
		String potentialVegetation = stand.getEcologicalType().substring(0,3).toUpperCase();	
		Matrix dummyPotVeg = vegPotDummyMap.get(potentialVegetation.toUpperCase());

		String environmentType = stand.getEcologicalType().substring(3, 4);
		Matrix dummyEcoType = ecoTypeDummyMap.get(environmentType.toUpperCase());
		
		Matrix dummyDisturbance;
		if (isInterventionResult) {
			dummyDisturbance = disturbanceDummyMap.get(DisturbanceType.INT.toString());
		} else if (isDefoliated) {
			dummyDisturbance = disturbanceDummyMap.get(DisturbanceType.MOY.toString());
		} else {
			dummyDisturbance = disturbanceDummyMap.get(DisturbanceType.NON.toString());
		}

		oXVector.resetMatrix();
		
		int pointeur = 0;
		Hd2014Species species = t.getHeightable2014TreeSpecies();
		double lnDbh = t.getLnDbhCmPlus1();		
		double lnDbh2 = t.getSquaredLnDbhCmPlus1();
		

//		List<Effect> effects = listEffectReferenceMap.get(t.getBetaHeightableTreeSpecies());
		for (Effect effect : effectList) {
			switch (effect) {
			case LogDbh:
				oXVector.setValueAt(0, pointeur++, lnDbh);
				break;
			case LogDbh2:
				oXVector.setValueAt(0, pointeur++, lnDbh2);
				break;
			case LogDbh_basalArea:
				oXVector.setValueAt(0, pointeur++, lnDbh * basalArea);
				break;
			case LogDbh_ratioDbh:
				oXVector.setValueAt(0, pointeur++, lnDbh * (t.getDbhCm()/averageQDiam));
				break;
//			case LogDbh_basalAreaGreaterThan:
//				oXVector.m_afData[0][pointeur++] = lnDbh * t.getBasalAreaLargerThanSubjectM2Ha();
//				break;
//			case LogDbh2_basalAreaGreaterThan:
//				oXVector.m_afData[0][pointeur++] = lnDbh2 * t.getBasalAreaLargerThanSubjectM2Ha();
//				break;
			case LogDbh2_ratioDbh:
				oXVector.setValueAt(0, pointeur++, lnDbh2 * (t.getDbhCm()/averageQDiam));
				break;
			case LogDbh_SubDom:
				oXVector.setSubMatrix(dummySubDomain.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummySubDomain.m_iCols;
				break;
			case LogDbh_PotVeg:
				oXVector.setSubMatrix(dummyPotVeg.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummyPotVeg.m_iCols;
				break;
			case LogDbh_Elevation:
				oXVector.setValueAt(0, pointeur++, lnDbh * elevation);
				break;
			case LogDbh_EcoType:
				oXVector.setSubMatrix(dummyEcoType.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummyEcoType.m_iCols;
				break;
			case LogDbh_Disturb:
				oXVector.setSubMatrix(dummyDisturbance.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummyDisturbance.m_iCols;
				break;
			case LogDbh_meanT:
				oXVector.setValueAt(0, pointeur++, lnDbh * averageTemp);
				break;
			case LogDbh_pTot:
				oXVector.setValueAt(0, pointeur++, lnDbh * stand.getTotalAnnualPrecipitationMm(GeneralHeight2014Predictor.Normals30YearTemporalResolution));
				break;
//			case LogDbh_Dens:
//				oXVector.m_afData[0][pointeur++] = lnDbh * stand.getNumberOfStemsHa();
//				break;
			case LogDbh_NotOuest:
				int isNotOuest = 0;
				String ouest = subDomain.substring(1, subDomain.length()).toUpperCase();
				if(!ouest.equals("OUEST")){
					isNotOuest = 1;
				}
				oXVector.setValueAt(0, pointeur++, lnDbh * isNotOuest);
				break;
//			case LogDbh_Is:
//				oXVector.m_afData[0][pointeur++] = lnDbh * t.getSocialStatusIndex();
//				break;
			default:
				System.out.println("BetaHeightPredictor Class : Unable to apply effect "+effect);
				break;
			}
		}

		Matrix Z_i = new Matrix(1,1);
		Z_i.setValueAt(0, 0, lnDbh2);	// design vector for the plot random effect

		double fResult = 1.3 + oXVector.multiply(beta).getValueAt(0, 0);

		RegressionElements regElements = new RegressionElements();

		regElements.fixedPred = fResult;
		regElements.vectorZ = Z_i;
		regElements.species = species;

		return regElements;
	}

	
	@Override
	protected Collection<Heightable2014Tree> getTreesFromStand(Heightable2014Stand stand) {
		Collection<Heightable2014Tree> treesToBeReturned = new ArrayList<Heightable2014Tree>();
		Collection<?> trees = stand.getTrees();
		if (trees != null && !trees.isEmpty()) {
			for (Object tree : trees) {
				if (tree instanceof Heightable2014Tree) {
					Heightable2014Tree t = (Heightable2014Tree) tree;
					if (t.getHeightable2014TreeSpecies() == species) {
						treesToBeReturned.add(t);
					}
				}
			}
		}
		return treesToBeReturned;

	}

}
