/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.predictor.biomass.lambert2005;

import java.util.ArrayList;
import java.util.List;

import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.BiomassCompartment;
import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.BiomassParameter;
import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.ErrorCovarianceEquation;
import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.EstimatedWeightDependent;
import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.FileImportParameter;
import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;

public class Lambert2005BiomassInternalPredictor extends REpiceaPredictor {
	Matrix parameterEstimates;
	Matrix parameterCovariance;	
	
	Matrix errorCovariance;	
	Matrix c;	// column vector
	Matrix cholesky; 	
	
	Lambert2005BiomassInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled){
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
				
		parameterEstimates = new Matrix(FileImportParameter.fileImportParameterSize, 1);
		parameterCovariance = new Matrix(FileImportParameter.fileImportParameterSize, FileImportParameter.fileImportParameterSize);
		errorCovariance = new Matrix(ErrorCovarianceEquation.errorCovarianceEquationSize, ErrorCovarianceEquation.errorCovarianceEquationSize);
		c = new Matrix(EstimatedWeightDependent.values().length, 1);		
	}
	
	void setParameterEstimate(FileImportParameter param, double value){
		parameterEstimates.setValueAt(param.ordinal(), 0, value);
	}
			
	void setParameterCovariance(FileImportParameter param, double[] value){
		for (int i = 0; i < FileImportParameter.fileImportParameterSize; i++)			
			parameterCovariance.setValueAt(param.ordinal(), i, value[i]);
	}
	
	void setEstimatedWeight(EstimatedWeightDependent index, double value) {
		c.setValueAt(index.ordinal(), 0, value);
	}
	
	void setErrorCovariance(ErrorCovarianceEquation equation, double[] value){
		for (int i = 0; i < ErrorCovarianceEquation.errorCovarianceEquationSize; i++)			
			errorCovariance.setValueAt(equation.ordinal(), i, value[i]);
	}
		
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
		// here we need to provide a covariance matrix that doesn't present any row or column for 0.0 parameters
		List<Integer> validIndices = new ArrayList<Integer>();
		for (int i = 0; i < parameterEstimates.m_iRows; i++) {
			if (parameterEstimates.getValueAt(i, 0) != 0.0) {
				validIndices.add(i);
			}
		}
		
		this.setParameterEstimates(new SASParameterEstimates(parameterEstimates, parameterCovariance.getSubMatrix(validIndices, validIndices)));
		
		//this.setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(errorCovariance, 0.0, null));
	}	
	
	Matrix getWeight(Lambert2005Tree tree) {
		double dbhcm = tree.getDbhCm();
		Matrix w = c.powMatrix(dbhcm).elementWisePower(0.5);						
		return w;
	}
	
	Matrix getCholesky() {
		if (cholesky == null) {
			cholesky = errorCovariance.getLowerCholTriangle();
		}
		
		return cholesky;
	}
	
	Matrix predictBiomass(Lambert2005Tree tree) {
		
		Matrix beta = this.getParametersForThisRealization(tree);
		double dbhcm = tree.getDbhCm();
		double hm = tree.getHeightM();	
		
		List<BiomassCompartment> compValues = BiomassCompartment.getBasicBiomassCompartments(); 
		Matrix result = new Matrix(BiomassCompartment.values().length, 1);
		
		for (BiomassCompartment cat : compValues) {
			result.setValueAt(cat.ordinal(), 0, predictSingleBiomass(beta, cat, dbhcm, hm));
		}		
		
		if (this.isResidualVariabilityEnabled) {
			Matrix weight = c.powMatrix(dbhcm).elementWisePower(0.5);
			Matrix r = StatisticalUtility.drawRandomVector(weight.m_iRows, Distribution.Type.GAUSSIAN);		
			Matrix res = weight.matrixDiagonal().multiply(getCholesky()).multiply(r);
			
			result = result.add(res);
			
			// ensure that no negative values are returned			
			result.clampIfLowerThan(0.0);						
		}

		// compute derivates which are sums of other compartments
		
		// STEM		
		result.setValueAt(BiomassCompartment.STEM.ordinal(), 0, result.getValueAt(BiomassCompartment.WOOD.ordinal(), 0) + result.getValueAt(BiomassCompartment.BARK.ordinal(), 0));
		
		// CROWN		
		result.setValueAt(BiomassCompartment.CROWN.ordinal(), 0, result.getValueAt(BiomassCompartment.BRANCHES.ordinal(), 0) + result.getValueAt(BiomassCompartment.FOLIAGE.ordinal(), 0));

		// TOTAL		
		result.setValueAt(BiomassCompartment.TOTAL.ordinal(), 0, result.getValueAt(BiomassCompartment.STEM.ordinal(), 0) + result.getValueAt(BiomassCompartment.CROWN.ordinal(), 0));
		
		return result;
	}
	
	double predictSingleBiomass(Matrix beta, BiomassCompartment comp, double dbhcm, double hm) {
		int baseIndex = comp.rank * FileImportParameter.biomassParameterSize;
		double term1 = beta.getValueAt(baseIndex + BiomassParameter.BETA1.ordinal(), 0);
		double term2 = Math.pow(dbhcm, beta.getValueAt(baseIndex + BiomassParameter.BETA2.ordinal(), 0));
		double term3 = Math.pow(hm, beta.getValueAt(baseIndex + BiomassParameter.BETA3.ordinal(), 0));
		
		return term1 * term2 * term3;
	}
}
