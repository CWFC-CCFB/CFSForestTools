/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.matapedia;

import java.util.Map;

import quebecmrnfutility.predictor.matapedia.MatapediaTree.MatapediaTreeSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.math.integral.GaussHermiteQuadrature;
import repicea.math.integral.GaussHermiteQuadrature.GaussHermiteQuadratureCompatibleFunction;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * The MatapediaMortalityPredictor class implements a mortality module for the
 * Matapedia simulator. The compatibility with this module is ensured through 
 * the MatapediaTree and MatapediaStand interfaces. The random effect implementation
 * includes a Gauss-Hermite quadrature to account for the interval random effect. 
 *
 * @author Mathieu Fortin - September 2012
 * 
 * @see <a href=http://pubs.cif-ifc.org/doi/10.5558/tfc2014-101> Fortin, M., S. Tremblay and R. Schneider.
 * 2014. Evaluating a single tree-based growth model for even-aged stands against the maximum size–density 
 * relationship: Some insights from balsam fir stands in Quebec, Canada. The Forestry Chronicle 90(4): 503-515 
 * </a>
 * 
 * 
 * @see <a href=http://www.nrcresearchpress.com/doi/pdf/10.1139/cjfr-2012-0268> 
 * Fortin, M. 2013. Population-averaged predictions with generalized linear mixed-effects models
 * in forestry: an estimator based on Gauss-Hermite quadrature. Canadian Journal of Forest Research
 * 43: 129-138. </a> 
 */
public final class MatapediaMortalityPredictor extends REpiceaBinaryEventPredictor<MatapediaStand, MatapediaTree>{

	private static final long serialVersionUID = 20120912L;

	private final static double offset5Years = Math.log(5d);		
	private final static int IndexParameterToBeIntegrated = 0;
	
	private final EmbeddedLinkFunction linkFunction;
	private final GaussHermiteQuadrature ghq;
//	private final List<Integer> indicesForGaussianQuad;
	
	@SuppressWarnings("serial")
	class EmbeddedLinkFunction extends LinkFunction implements GaussHermiteQuadratureCompatibleFunction<Double> {

		final double standardDeviation;
		
		EmbeddedLinkFunction(Type linkFunctionType, double randomEffectVariance) {
			super(linkFunctionType);
			standardDeviation = Math.sqrt(randomEffectVariance);
		}
		
		@Override
		public double convertFromGaussToOriginal(double x, double mu, int covarianceIndexI, int covarianceIndexJ) {
			return mu + Math.sqrt(2d) * x * standardDeviation;
		}
		
	}
	
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the variability in the parameter estimates, the random effects and the residual error terms
	 */
	public MatapediaMortalityPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled,isVariabilityEnabled,isVariabilityEnabled);
	}

	
	MatapediaMortalityPredictor(boolean isParameterVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
		init();
		Matrix variance = getDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT).getDistribution().getVariance();

		linkFunction = new EmbeddedLinkFunction(Type.CLogLog, variance.getValueAt(0, 0));
		linkFunction.setParameterValue(0, 0d);		// random parameter
		linkFunction.setVariableValue(0, 1d);		// variable that multiplies the random parameter
		linkFunction.setParameterValue(1, 1d);		// parameter that multiplies the xBeta
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N15);
//		indicesForGaussianQuad = new ArrayList<Integer>();
//		indicesForGaussianQuad.add(0);
	}
	
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MortBeta.csv";
			String omegaFilename = path + "0_MortOmega.csv";
			String covParmsFilename = path + "0_MortCovParms.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			SymmetricMatrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			SymmetricMatrix randomEffectVariance = SymmetricMatrix.convertToSymmetricIfPossible(
					ParameterLoader.loadVectorFromFile(covParmsFilename).get());
			
			Matrix meanRandomEffect = new Matrix(1,1);
			setDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, new GaussianEstimate(meanRandomEffect, randomEffectVariance));
			ModelParameterEstimates estimate = new SASParameterEstimates(defaultBetaMean, defaultBetaVariance);
			setParameterEstimates(estimate); 
			oXVector = new Matrix(1, estimate.getMean().m_iRows);
			
		} catch (Exception e) {
			System.out.println("MatapediaMortalityPredictor.init() : Unable to initialize the mortality module!");
		}
	}
	
	/**
	 * This method predicts the probability of mortality in the upcoming 5 years. Note that this method needs
	 * to be synchronized as several threads may access the xVector at the same time otherwise
	 * @param stand a MatapediaStand instance
	 * @param tree a MatapediaTree instance
	 * @param parms some additional parameters
	 * @return the predicted probability of mortality
	 */
	@Override
	public synchronized double predictEventProbability(MatapediaStand stand, MatapediaTree tree, Map<String, Object> parms) {
		
		double etaValue = fixedEffectsPrediction(stand, tree);
		linkFunction.setVariableValue(1, etaValue);
		double prob;
		
		if (isRandomEffectsVariabilityEnabled) { 
			IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
			Matrix randomEffects = getRandomEffectsForThisSubject(interval);
			linkFunction.setParameterValue(0, randomEffects.getValueAt(0, 0));
			prob = linkFunction.getValue();
		} else {
			linkFunction.setParameterValue(0, 0d);
			prob = ghq.getIntegralApproximation(linkFunction, 
					IndexParameterToBeIntegrated, 
					true);
		}
		
		if (parms != null && parms.containsKey(DisturbanceParameter.ParmTimeStep)) {
			double timeStep = ((Number) parms.get(DisturbanceParameter.ParmTimeStep)).doubleValue();
			prob = 1 - Math.pow (1 - prob, timeStep / 5d);		// correction in case of 6-yr growth step
		}
		return prob;
	}

	
	private double fixedEffectsPrediction(MatapediaStand stand, MatapediaTree tree) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(stand);
		
		int pointer = 0;
		
		oXVector.setValueAt(0, pointer, 1d);
		pointer++;
		
		MatapediaTreeSpecies species = tree.getMatapediaTreeSpecies();
		Matrix dummySpeciesDbh0 = species.getDummy().scalarMultiply(tree.getDbhCm());
		oXVector.setSubMatrix(dummySpeciesDbh0, 0, pointer);
		pointer += species.getDummy().m_iCols;

		oXVector.setValueAt(0, pointer, tree.getSquaredDbhCm());
		pointer++;
		
		if (stand.isSBWDefoliated() && !stand.isSprayed()) {
			oXVector.setSubMatrix(dummySpeciesDbh0, 0, pointer);
		} 
		pointer += species.getDummy().m_iCols;
		
		double bal = tree.getBasalAreaLargerThanSubjectM2Ha();
		oXVector.setSubMatrix(species.getDummy().scalarMultiply(bal), 0, pointer);
		pointer += species.getDummy().m_iCols;
		
		oXVector.setValueAt(0, pointer, offset5Years);
		pointer++;
		
		double result = oXVector.multiply(beta).getValueAt(0, 0);
		return result;
	}


}
