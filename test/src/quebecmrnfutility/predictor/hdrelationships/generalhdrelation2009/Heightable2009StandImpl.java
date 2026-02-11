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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009;

import java.util.ArrayList;
import java.util.Collection;

import repicea.simulation.climate.REpiceaClimateManager.ClimateVariableTemporalResolution;
import repicea.simulation.covariateproviders.plotlevel.MeanQuadraticDiameterCmProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

public class Heightable2009StandImpl implements Heightable2009Stand, MeanQuadraticDiameterCmProvider {

	final String subjectID;
	int monteCarloRealizationID;
	final double basalAreaM2Ha;
	final double meanQuadraticDiameter;
	final String ecoRegion;
	final String ecoType;
	boolean isInterventionResult;
	final double elevationM;
	final double meanAnnualTemperatureC;
	final double meanAnnualPrecipitationMm;
	boolean isDefoliated;
	final Collection<Heightable2009Tree> trees;
	
	Heightable2009StandImpl(String subjectID,
			double basalAreaM2Ha, 
			double meanQuadraticDiameter, 
			String ecoRegion, 
			String ecoType, 
			double elevationM, 
			double meanAnnualTemperatureC,
			double meanAnnualPrecipitationMm) {
		this.subjectID = subjectID;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.meanQuadraticDiameter = meanQuadraticDiameter;
		this.ecoRegion = ecoRegion;
		this.ecoType = ecoType;
		this.elevationM = elevationM;
		this.meanAnnualTemperatureC = meanAnnualTemperatureC;
		this.meanAnnualPrecipitationMm = meanAnnualPrecipitationMm;
		this.trees = new ArrayList<Heightable2009Tree>();
	}
	
	
	@Override
	public String getSubjectId() {return subjectID;}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloRealizationID;
	}

	@Override
	public double getBasalAreaM2Ha() {
		return basalAreaM2Ha;
	}

	@Override
	public double getMeanAnnualTemperatureCelsius(ClimateVariableTemporalResolution resolution) {
		return meanAnnualTemperatureC;
	}

	@Override
	public String getEcoRegion() {
		return ecoRegion;
	}

	@Override
	public String getEcologicalType() {
		return ecoType;
	}

	@Override
	public boolean isInterventionResult() {
		return isInterventionResult;
	}

//	@Override
//	public double getElevationM() {
//		return elevationM;
//	}

	@Override
	public boolean isSBWDefoliated() {
		return isDefoliated;
	}

//	@Override
//	public double getNumberOfStemsHa() {
//		return numberOfStemsHa;
//	}

//	@Override
//	public double getMeanAnnualPrecipitationMm() {
//		return meanAnnualPrecipitationMm;
//	}
//
	@Override
	public double getMeanQuadraticDiameterCm() {
		return meanQuadraticDiameter;
	}

//	@SuppressWarnings("rawtypes")
//	@Override
//	public Collection getTrees() {
//		return trees;
//	}


	@SuppressWarnings("rawtypes")
	@Override
	public Collection getTrees(StatusClass statusClass) {
		return trees;
	}


	@Override
	public DrainageGroup getDrainageGroup() {
		return DrainageGroup.Mesic;			// mesic for the sake of simplicity
	}


//	@Override
//	public List<HDRelationshipStand> getAllHDStands() {
//		List<HDRelationshipStand> stands = new ArrayList<HDRelationshipStand>();
//		stands.add(this);
//		return stands;
//	}

}
