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

import quebecmrnfutility.predictor.matapedia.MatapediaTree.MatapediaTreeSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.GrowthModel;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * The MatapediaDbhIncrementPredictor class implements a dbh increment module that was fitted using the
 * data of the Matapedia permanent plot network. 
 * @author Mathieu Fortin - September 2012
 * 
 * @see <a href=http://pubs.cif-ifc.org/doi/10.5558/tfc2014-101> Fortin, M., S. Tremblay and R. Schneider.
 * 2014. Evaluating a single tree-based growth model for even-aged stands against the maximum size–density 
 * relationship: Some insights from balsam fir stands in Quebec, Canada. The Forestry Chronicle 90(4): 503-515 
 * </a>
 */
public class MatapediaDbhIncrementPredictor extends REpiceaPredictor implements GrowthModel<MatapediaStand, MatapediaTree>{

	private static final long serialVersionUID = 20120911L;

	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public MatapediaDbhIncrementPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		init();
		oXVector = new Matrix(1,15);
	}

	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_AccroissementBeta.csv";
			String omegaFilename = path + "0_AccroissementOmega.csv";
			String covparmsFilename = path + "0_AccroissementCovParms.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			SymmetricMatrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			
			setParameterEstimates(new SASParameterEstimates(defaultBetaMean, defaultBetaVariance)); 
			
			Matrix covParms =  ParameterLoader.loadVectorFromFile(covparmsFilename).get();
			SymmetricMatrix plotRandomEffectVariance = SymmetricMatrix.convertToSymmetricIfPossible(covParms.getSubMatrix(0, 0, 0, 0));
			GaussianEstimate defRandomEffect = new GaussianEstimate(new Matrix(plotRandomEffectVariance.m_iRows,1), plotRandomEffectVariance);
			setDefaultRandomEffects(HierarchicalLevel.TREE, defRandomEffect);

			SymmetricMatrix residualErrorVariance = SymmetricMatrix.convertToSymmetricIfPossible(covParms.getSubMatrix(1, 1, 0, 0));
			setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(residualErrorVariance));
		} catch (Exception e) {
			System.out.println("MatapediaDbhIncrementPredictor.init() : Unable to initialize the diameter increment module!");
		}
		
	}

	/**
	 * This method predicts the annual dbh increment for the upcoming 5 years.
	 * @param stand a MatapediaStand instance
	 * @param tree a MatapediaTree instance
	 * @return the annual dbh increment (mm)
	 */
	@Override
	public double predictGrowth(MatapediaStand stand, MatapediaTree tree, Object... parms) {
		double prediction = fixedEffectsPrediction(tree, stand);
		double dbh = tree.getDbhCm();
		double randomEffect = getRandomEffectsForThisSubject(tree).scalarMultiply(dbh).getValueAt(0, 0);
		double residualError = getResidualError().getValueAt(0, 0); 
		prediction += randomEffect + residualError; 
		return prediction;
	}
	
	
	private synchronized double fixedEffectsPrediction(MatapediaTree tree, MatapediaStand stand) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(stand);
		
		int pointer = 0;
		
		oXVector.setValueAt(0, pointer, 1d);
		pointer++;
		
		MatapediaTreeSpecies species = tree.getMatapediaTreeSpecies();
		oXVector.setSubMatrix(species.getDummy(), 0, pointer);
		pointer += species.getDummy().m_iCols;

		double dbh = tree.getDbhCm();
		oXVector.setValueAt(0, pointer, dbh);
		pointer++;
		
		double dbh2 = tree.getSquaredDbhCm();
		oXVector.setValueAt(0, pointer, dbh2);
		pointer++;
		
		boolean isSBWComing = stand.isGoingToBeDefoliated();
		if (isSBWComing) {
			oXVector.setSubMatrix(species.getDummy(), 0, pointer);
		} 
		pointer += species.getDummy().m_iCols;

		double bal = tree.getBasalAreaLargerThanSubjectM2Ha();
		oXVector.setSubMatrix(species.getDummy().scalarMultiply(bal), 0, pointer);
		pointer += species.getDummy().m_iCols;

		double result = oXVector.multiply(beta).getValueAt(0, 0);
		return result;
	}

	
}
