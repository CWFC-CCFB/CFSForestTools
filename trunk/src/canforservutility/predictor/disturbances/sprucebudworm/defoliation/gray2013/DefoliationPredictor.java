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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biosimclient.BioSimClient;
import biosimclient.BioSimClientException;
import biosimclient.BioSimDataSet;
import biosimclient.BioSimEnums.ClimateModel;
import biosimclient.BioSimEnums.RCP;
import biosimclient.BioSimParameterMap;
import biosimclient.BioSimPlot;
import biosimclient.BioSimServerException;
import biosimclient.Observation;
import repicea.math.Matrix;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.covariateproviders.plotlevel.SprayedAgainstDefoliatorProvider;

/**
 * This class implements the defoliation model found in Gray (2013).
 * @author Mathieu Fortin - May 2019
 * @see <a href=https://doi.org/10.1139/cjfr-2013-0240> Gray, D. R. 2013. The influence of forest composition and climate on outbreak
 * characteristics of the spruce budworm in eastern Canada. Canadian Journal of Forest Research 43: 1181-1195 
 * </a>
  */
@SuppressWarnings("serial")
public class DefoliationPredictor extends REpiceaBinaryEventPredictor<DefoliationPlot, Object> {

	
	private final double Over30 = 1d/30;
	
	private static final List<Integer> SpringMonths = new ArrayList<Integer>();
	static {
		SpringMonths.add(4); // April
		SpringMonths.add(5); // May	
	}
	
	private static final List<Integer> SummerMonths = new ArrayList<Integer>();
	static {
		SummerMonths.add(6); // June
		SummerMonths.add(7); // July
		SummerMonths.add(8); // August
	}
	
	
	
	
	boolean testPurposes;
	
	private Matrix MeanExplanatoryVariables;
	private Matrix stdExplanatoryVariables;
	private Matrix canonicalReg1Coef;
	private Matrix canonicalReg2Coef;
	private Matrix scoreDuration;
	private Matrix scoreSeverity;
	
	private final double nbYearsWithModerateToSevereDefoliation;
	private final Map<String, Matrix> climateMap;
	private final RCP rcp;
	private final ClimateModel climModel;
	private final BioSimParameterMap ddParms;
	
	
	/**
	 * Constructor. 
	 */
	public DefoliationPredictor(double nbYearsWithModerateToSevereDefoliation, RCP rcp, ClimateModel climModel) {
		super(false, false, true);	// residual variability must be set to true to ensure that predictEvent returns a boolean
		this.nbYearsWithModerateToSevereDefoliation = nbYearsWithModerateToSevereDefoliation;
		this.climateMap = new HashMap<String, Matrix>();
		this.rcp = rcp;
		this.climModel = climModel;
		ddParms = new BioSimParameterMap();
		ddParms.addParameter("LowerThreshold", 5);
		init();
		oXVector = new Matrix(1,8);
	}

	/**
	 * Constructor with default RCP (RCP 4.5) and climate model (RCM4). 
	 * @param nbYearsWithModerateToSevereDefoliation
	 */
	public DefoliationPredictor(double nbYearsWithModerateToSevereDefoliation) {
		this(nbYearsWithModerateToSevereDefoliation, RCP.RCP45, ClimateModel.RCM4);
	}	
	
	@Override
	protected void init() {
		MeanExplanatoryVariables = new Matrix(1,8);
		MeanExplanatoryVariables.m_afData[0][0] = 49.0485;
		MeanExplanatoryVariables.m_afData[0][1] = 38.7728;
		MeanExplanatoryVariables.m_afData[0][2] = 113.2317;
		MeanExplanatoryVariables.m_afData[0][3] = 5.8103;
		MeanExplanatoryVariables.m_afData[0][4] = 82.6894;
		MeanExplanatoryVariables.m_afData[0][5] = 41.5137;
		MeanExplanatoryVariables.m_afData[0][6] = 15.3037;
		MeanExplanatoryVariables.m_afData[0][7] = 0.5807;
		
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

	
	
	protected Matrix getClimateForThisInterval(DefoliationPlot plot, IntervalNestedInPlotDefinition subject) {
		if (testPurposes) {
			Matrix mat = new Matrix(4,1);
			mat.m_afData[0][0] = MeanExplanatoryVariables.m_afData[0][1];
			mat.m_afData[1][0] = MeanExplanatoryVariables.m_afData[0][2];
			mat.m_afData[2][0] = MeanExplanatoryVariables.m_afData[0][3];
			mat.m_afData[3][0] = MeanExplanatoryVariables.m_afData[0][4];
			return mat;
		} else if (!climateMap.containsKey(subject)) {
			List<BioSimPlot> plots = new ArrayList<BioSimPlot>();
			plots.add(plot);
			int initYear = (int) Math.floor((double) plot.getDateYr() * Over30) * 30 + 1;
			int finalYear = (int) Math.ceil((double) plot.getDateYr() * Over30) * 30;
			
			try {
				double yearFactor = 1d / (finalYear - initYear);

				Map<BioSimPlot, BioSimDataSet> dataSets = BioSimClient.getModelOutput(initYear, 
						finalYear,
						plots, 
						rcp,
						climModel,
						"Climatic_Monthly",
						null);
				BioSimDataSet dataSet = dataSets.get(plot);
				int indexTMin = dataSet.getFieldNames().indexOf("LowestTmin");
				int indexTMax = dataSet.getFieldNames().indexOf("HighestTmax");
				int indexMonth = dataSet.getFieldNames().indexOf("Month");
				
				double sp_emax = 0;
				double sm_emax = 0;
				double sm_emin = 0;
				for (int i = 0; i < dataSet.getNumberOfObservations(); i++) {
					Observation obs = dataSet.getObservations().get(i);
					Object[] array = obs.toArray();
					int month = (Integer) array[indexMonth];
					if (SpringMonths.contains(month)) {
						sp_emax += (Double) array[indexTMax] * yearFactor;
					} else if (SummerMonths.contains(month)) {
						sm_emax += (Double) array[indexTMax] * yearFactor;
						sm_emin += (Double) array[indexTMin] * yearFactor;
					}
				}

				
				dataSets = BioSimClient.getModelOutput(initYear, 
						finalYear,
						plots, 
						rcp,
						climModel,
						"DegreeDay_Monthly",
						ddParms);
				dataSet = dataSets.get(plot);
				double sp_dd = 0;
				indexMonth = dataSet.getFieldNames().indexOf("Month");
				int indexDD = dataSet.getFieldNames().indexOf("DD");
				for (int i = 0; i < dataSet.getNumberOfObservations(); i++) {
					Observation obs = dataSet.getObservations().get(i);
					Object[] array = obs.toArray();
					int month = (Integer) array[indexMonth];
					if (SpringMonths.contains(month)) {
						sp_dd += (Double) array[indexDD] * yearFactor;
					} 
				}

				Matrix mat = new Matrix(4,1);
				mat.m_afData[0][0] = sp_emax;
				mat.m_afData[1][0] = sp_dd;
				mat.m_afData[2][0] = sm_emin;
				mat.m_afData[3][0] = sm_emax;
				

				
				return mat;
			} catch (BioSimClientException | BioSimServerException e) {
				e.printStackTrace();
			}
			
		} 
		return climateMap.get(subject);
	}

	/**
	 * Returns the 
	 * @param plot
	 * @return
	 */
	public synchronized Matrix getDurationAndSeverityEstimate(DefoliationPlot plot) {
		IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(plot, plot.getDateYr());
		Matrix climate = getClimateForThisInterval(plot, interval);
		
		oXVector.resetMatrix();
		int i = 0;
		oXVector.m_afData[0][i] = plot.getLatitudeDeg();
		i++;
		oXVector.m_afData[0][i] = climate.m_afData[0][0]; // plot.getSpringSumMaxTemp();	// sp_emax
		i++;
		oXVector.m_afData[0][i] = climate.m_afData[1][0]; // plot.getSpringSumDegreeDays();	// sp_dd 
		i++;	
		oXVector.m_afData[0][i] = climate.m_afData[2][0]; // plot.getSummerSumMinTemp();	// sm_emin
		i++;
		oXVector.m_afData[0][i] = climate.m_afData[3][0]; // plot.getSummerSumMaxTemp();	// sm_emax
		i++;
		oXVector.m_afData[0][i] = plot.getVolumeM3HaOfBlackSpruce();
		i++;
		oXVector.m_afData[0][i] = plot.getVolumeM3HaOfFirAndOtherSpruces();
		i++;
		oXVector.m_afData[0][i] = plot.getProportionForestedArea();

		Matrix standardizedValues = oXVector.subtract(MeanExplanatoryVariables).elementWiseDivide(stdExplanatoryVariables);
		Matrix score = new Matrix(1,2);
		score.m_afData[0][0] = standardizedValues.multiply(canonicalReg1Coef).m_afData[0][0];
		score.m_afData[0][1] = standardizedValues.multiply(canonicalReg2Coef).m_afData[0][0];
		
		double durationResult = score.multiply(scoreDuration).m_afData[0][0] * 5.1463 + 4.7506;
		double severityResult = score.multiply(scoreSeverity).m_afData[0][0] * 20.4069 + 25.6180;
		double sinSeverity = Math.sin(severityResult); 
		severityResult = sinSeverity * sinSeverity * 100d;
		
//		SimpleEstimate estimate = new SimpleEstimate();
//		List<String> indices = new ArrayList<String>();
//		indices.add("Duration");
//		indices.add("Severity");
//		estimate.setRowIndex(indices);
		Matrix mean = new Matrix(2,1);
		mean.m_afData[0][0] = durationResult;
		mean.m_afData[1][0] = severityResult;
		return mean;
	}
	
	
	@Override
	public double predictEventProbability(DefoliationPlot plot, Object tree, Map<Integer, Object> parms) {
		if (plot instanceof SprayedAgainstDefoliatorProvider) {
			if (((SprayedAgainstDefoliatorProvider) plot).isSprayed()) {
				return 0d;
			}
		} 
		Matrix estimate = getDurationAndSeverityEstimate(plot);
		double durationResult = estimate.m_afData[0][0];
		double severityResult = estimate.m_afData[1][0];
		if (durationResult >= nbYearsWithModerateToSevereDefoliation) {
			double minSeverity = nbYearsWithModerateToSevereDefoliation * .65 + (durationResult - nbYearsWithModerateToSevereDefoliation) * .20;
			if (severityResult > minSeverity) {
				return 1d;
			} 
		} 
		return 0d;
	}
	

}
