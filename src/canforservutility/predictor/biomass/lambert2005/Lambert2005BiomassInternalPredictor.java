/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021-2026 His Majesty the King in right of Canada
 * Authors: Jean-Francois Lavoie and Mathieu Fortin, Canadian Forest Service
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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
import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.ModelVersion;
import canforservutility.predictor.biomass.lambert2005.Lambert2005Tree.Lambert2005Species;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;

/**
 * Implement the biomass models in Lambert et al. (2005) for each individual species.
 * @author <ul><li>Jean-Francois Lavoie 2021 <li>Mathieu Fortin February 2026 (refactoring)</ul>
 * @see <a href=https://doi.org/10.1139/x05-112> Lambert, M.-C., C.-H. Ung, and F. Raulier. 2005. Canadian
 * national tree aboveground biomass equations. Canadian Journal of Forest Research 35(8): 1996-2018 
 * </a>
 */
@SuppressWarnings("serial")
final class Lambert2005BiomassInternalPredictor extends REpiceaPredictor {
	
	final Matrix parameterEstimates;
	final Matrix parameterCovariance;	
	
	final SymmetricMatrix errorCovariance;	
	final Matrix c;	// column vector
	final Lambert2005Species species;
	Matrix cholesky; 
	int nbTotalParms;
	final ModelVersion version;
	
	Lambert2005BiomassInternalPredictor(ModelVersion v, Lambert2005Species species, boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled){
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		this.species = species;
		this.version = v;
		nbTotalParms = version.nbParms * BiomassCompartment.getBasicBiomassCompartments().size();
		parameterEstimates = new Matrix(nbTotalParms, 1);
		parameterCovariance = new Matrix(nbTotalParms, nbTotalParms);
		errorCovariance = new SymmetricMatrix(Lambert2005BiomassPredictor.ERROR_COVARIANCE_EQUATION_LABELS.size());
		c = new Matrix(Lambert2005BiomassPredictor.ESTIMATED_WEIGHT_LABELS.size(), 1);		
	}
	
	void setParameterEstimate(int index, double value){
		parameterEstimates.setValueAt(index, 0, value);
	}
			
	void setParameterCovariance(int index, double[] value){
		for (int i = 0; i < nbTotalParms; i++)			
			parameterCovariance.setValueAt(index, i, value[i]);
	}
	
	void setEstimatedWeight(int index, double value) {
		c.setValueAt(index, 0, value);
	}
	
	void setErrorCovariance(int index, double[] value){
		for (int i = 0; i < value.length; i++)			
			errorCovariance.setValueAt(index, i, value[i]);
	}
		
	@Override
	protected void init() {
		// here we need to provide a covariance matrix that doesn't present any row or column for 0.0 parameters
		List<Integer> validIndices = new ArrayList<Integer>();
		for (int i = 0; i < parameterEstimates.m_iRows; i++) {
			if (parameterEstimates.getValueAt(i, 0) != 0.0) {
				validIndices.add(i);
			}
		}

		SymmetricMatrix variance = SymmetricMatrix.convertToSymmetricIfPossible(parameterCovariance.getSubMatrix(validIndices, validIndices));
		setParameterEstimates(new SASParameterEstimates(parameterEstimates, variance));
		try {
			cholesky = errorCovariance.getLowerCholTriangle();
		} catch(UnsupportedOperationException e) {
//			Matrix m = errorCovariance.diagonalVector().elementWisePower(0.5);
//			Matrix m1 = m.multiply(m.transpose());
//			Matrix corr = errorCovariance.elementWiseDivide(m1);
			System.err.println("Unable to calculate Cholesky decomposition for species " + this.species.name() + " with model " + version.name());
		}
	}	
	
	Matrix getWeight(Lambert2005Tree tree) {
		double dbhcm = tree.getDbhCm();
		Matrix w = c.powMatrix(dbhcm).elementWisePower(0.5);						
		return w;
	}
	
	Matrix getCholesky() {
		return cholesky;
	}

	Matrix predictBiomass(Lambert2005Tree tree) {
		
		Matrix beta = getParametersForThisRealization(tree);
		double dbhCm = tree.getDbhCm();
		double heightM = tree.implementHeighMProvider() ? 
				((HeightMProvider) tree).getHeightM() :
					-999;
		return internalPredictBiomass(beta, dbhCm, heightM);
	}

	
	
	Matrix internalPredictBiomass(Matrix beta, double dbhCm, double heightM) {
		
		
		List<BiomassCompartment> compValues = BiomassCompartment.getBasicBiomassCompartments(); 
		Matrix result = new Matrix(BiomassCompartment.values().length, 1);
		
		for (BiomassCompartment cat : compValues) {
			result.setValueAt(cat.ordinal(), 0, predictSingleBiomass(beta, cat, dbhCm, heightM));
		}		
		
		if (this.isResidualVariabilityEnabled) {
			Matrix weight = c.powMatrix(dbhCm).elementWisePower(0.5);
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
		int baseIndex = comp.rank * (version == ModelVersion.Complete ? 3 : 2);
		double term1 = beta.getValueAt(baseIndex, 0);
		double term2 = Math.pow(dbhcm, beta.getValueAt(baseIndex + 1, 0));
		double term3 = version == ModelVersion.Complete ? 
				Math.pow(hm, beta.getValueAt(baseIndex + 2, 0)) :
					1d;
		return term1 * term2 * term3;
	}

	/*
	 * For test purposes only.
	 */
	Matrix testParametersForThisRealization(Lambert2005Tree tree) {
		return getParametersForThisRealization(tree);
	}

	/*
	 * For test purposes only.
	 */
	Matrix testMeanParameters() {
		return getParameterEstimates().getMean();
	}

	double predictTotalBiomassMg(Lambert2005Species species, double dbhCm, Double heightM) {
		Matrix pred = internalPredictBiomass(getParameterEstimates().getMean(),
				dbhCm,
				heightM);
		return pred.getValueAt(BiomassCompartment.TOTAL.ordinal(), 0) * 0.001;
	}
	
}
