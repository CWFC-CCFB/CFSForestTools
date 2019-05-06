/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Forest Service
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
package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.estimates.SimpleEstimate;

/**
 * This class implements the defoliation model found in Gray (2013).
 * @author Mathieu Fortin - May 2019
 * @see <a href=https://doi.org/10.1139/cjfr-2013-0240> Gray, D. R. 2013. The influence of forest composition and climate on outbreak
 * characteristics of the spruce budworm in eastern Canada. Canadian Journal of Forest Research 43: 1181-1195 
 * </a>
  */
@SuppressWarnings("serial")
public class DefoliationPredictor extends REpiceaPredictor {

	private Matrix meanExplanatoryVariables;
	private Matrix stdExplanatoryVariables;
	private Matrix canonicalReg1Coef;
	private Matrix canonicalReg2Coef;
	private Matrix scoreDuration;
	private Matrix scoreSeverity;
	
	
	/**
	 * Constructor. Only works in deterministic mode at the moment.
	 */
	public DefoliationPredictor() {
		super(false, false, false);
		init();
		oXVector = new Matrix(1,8);
	}

	@Override
	protected void init() {
		meanExplanatoryVariables = new Matrix(1,8);
		meanExplanatoryVariables.m_afData[0][0] = 49.0485;
		meanExplanatoryVariables.m_afData[0][1] = 38.7728;
		meanExplanatoryVariables.m_afData[0][2] = 113.2317;
		meanExplanatoryVariables.m_afData[0][3] = 5.8103;
		meanExplanatoryVariables.m_afData[0][4] = 82.6894;
		meanExplanatoryVariables.m_afData[0][5] = 41.5137;
		meanExplanatoryVariables.m_afData[0][6] = 15.3037;
		meanExplanatoryVariables.m_afData[0][7] = 0.5807;
		
		stdExplanatoryVariables = new Matrix(1,8);
		stdExplanatoryVariables.m_afData[0][0] = 1.8838;
		stdExplanatoryVariables.m_afData[0][1] = 4.3036;
		stdExplanatoryVariables.m_afData[0][2] = 51.4563;
		stdExplanatoryVariables.m_afData[0][3] = 4.3139;
		stdExplanatoryVariables.m_afData[0][4] = 4.6368;
		stdExplanatoryVariables.m_afData[0][5] = 25.1083;
		stdExplanatoryVariables.m_afData[0][6] = 11.0932;
		stdExplanatoryVariables.m_afData[0][7] = 0.2069;
		
		canonicalReg1Coef = new Matrix(8,1);
		canonicalReg1Coef.m_afData[0][0] = 0.8760;
		canonicalReg1Coef.m_afData[1][0] = -1.0791;
		canonicalReg1Coef.m_afData[2][0] = 1.6679;
		canonicalReg1Coef.m_afData[3][0] = -0.1370;
		canonicalReg1Coef.m_afData[4][0] = -0.2757;
		canonicalReg1Coef.m_afData[5][0] = 0.0242;
		canonicalReg1Coef.m_afData[6][0] = -0.2754;
		canonicalReg1Coef.m_afData[7][0] = -0.4493;

		canonicalReg2Coef = new Matrix(8,1);
		canonicalReg2Coef.m_afData[0][0] = 0.1586;
		canonicalReg2Coef.m_afData[1][0] = 0.5446;
		canonicalReg2Coef.m_afData[2][0] = 2.4593;
		canonicalReg2Coef.m_afData[3][0] = -1.4997;
		canonicalReg2Coef.m_afData[4][0] = -1.0369;
		canonicalReg2Coef.m_afData[5][0] = 1.0801;
		canonicalReg2Coef.m_afData[6][0] = 0.2540;
		canonicalReg2Coef.m_afData[7][0] = -0.1964;
		
		scoreDuration = new Matrix(2,1);
		scoreDuration.m_afData[0][0] = -0.8056;
		scoreDuration.m_afData[1][0] = -0.1053;
		
		scoreSeverity = new Matrix(2,1);
		scoreSeverity.m_afData[0][0] = -0.6920;
		scoreSeverity.m_afData[1][0] = 0.1668;
	}

	
	public synchronized SimpleEstimate predictDefoliation(DefoliationPlot plot) {
		oXVector.resetMatrix();
		int i = 0;
		oXVector.m_afData[0][i] = plot.getLatitudeDeg();
		i++;
		oXVector.m_afData[0][i] = meanExplanatoryVariables.m_afData[0][i];	// TODO call BioSIM here
		i++;
		oXVector.m_afData[0][i] = meanExplanatoryVariables.m_afData[0][i];	// TODO call BioSIM here
		i++;	
		oXVector.m_afData[0][i] = meanExplanatoryVariables.m_afData[0][i];	// TODO call BioSIM here
		i++;
		oXVector.m_afData[0][i] = meanExplanatoryVariables.m_afData[0][i];	// TODO call BioSIM here
		i++;
		oXVector.m_afData[0][i] = plot.getVolumeM3HaOfBlackSpruce();
		i++;
		oXVector.m_afData[0][i] = plot.getVolumeM3HaOfFirAndOtherSpruces();
		i++;
		oXVector.m_afData[0][i] = plot.getProportionForestedArea();

		Matrix standardizedValues = oXVector.subtract(meanExplanatoryVariables).elementWiseDivide(stdExplanatoryVariables);
		Matrix score = new Matrix(1,2);
		score.m_afData[0][0] = standardizedValues.multiply(canonicalReg1Coef).m_afData[0][0];
		score.m_afData[0][1] = standardizedValues.multiply(canonicalReg2Coef).m_afData[0][0];
		
		double durationResult = score.multiply(scoreDuration).m_afData[0][0] * 5.1463 + 4.7506;
		double severityResult = score.multiply(scoreSeverity).m_afData[0][0] * 20.4069 + 25.6180;
		double sinSeverity = Math.sin(severityResult); 
		severityResult = sinSeverity * sinSeverity * 100d;
		SimpleEstimate estimate = new SimpleEstimate();
		List<String> indices = new ArrayList<String>();
		indices.add("Duration");
		indices.add("Severity");
		estimate.setRowIndex(indices);
		Matrix mean = new Matrix(2,1);
		mean.m_afData[0][0] = durationResult;
		mean.m_afData[1][0] = severityResult;
		estimate.setMean(mean);
		Matrix variance = new Matrix(2,2);
		estimate.setVariance(variance);		// TODO implement the variance here
		return estimate;
	}
	
}
