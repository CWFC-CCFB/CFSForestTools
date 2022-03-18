/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
 * Copyright (C) 2019 Mathieu Fortin for Canadian Forest Service
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
package quebecmrnfutility.predictor.thinners.melothinner;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.simulation.covariateproviders.plotlevel.QcSlopeClassProvider.QcSlopeClass;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.plotlevel.LandOwnershipProvider;
import repicea.simulation.covariateproviders.plotlevel.LandOwnershipProvider.LandOwnership;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.simulation.thinners.REpiceaThinner;
import repicea.simulation.thinners.REpiceaTreatmentDefinition;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.util.ObjectUtility;

/**
 * This class implements the thinning model designed and fitted by Lara Melo in her thesis. 
 * 
 * @author Mathieu Fortin - March 2019
 * @see <a href=https://doi.org/10.1139/cjfr-2016-0498> Melo, L.C., R. Schneider, R. Manso, J.-P.
 * Saucier and M. Fortin. 2017. Using survival analysis to predict the harvesting of forest stands in 
 * Quebec, Canada. Canadian Journal of Forest Research 47: 1066-1074 
 * </a>
 */
@SuppressWarnings("serial")
public final class MeloThinnerPredictor extends REpiceaThinner<MeloThinnerPlot, Object> implements REpiceaShowableUIWithParent {

	
	class EmbeddedFunction extends AbstractMathematicalFunction {
		@Override
		public Double getValue() {
			double conditionalSurvival = getParameterValue(0) * getVariableValue(0);
			double u = getParameterValue(1) * getVariableValue(1);
			return Math.pow(conditionalSurvival, Math.exp(u));
		}

		@Override
		public Matrix getGradient() {return null;}

		@Override
		public Matrix getHessian() {return null;}
	}
	
	private static final List<Integer> ParametersToIntegrate = new ArrayList<Integer>();
	static {
		ParametersToIntegrate.add(1);
	}
	
	private boolean quadratureEnabled = true;
	
	private Map<QcSlopeClass, Matrix> slopeClassDummy;
	private Map<String, Matrix> dynamicTypeDummy;
	private final GaussHermiteQuadrature ghq = new GaussHermiteQuadrature(NumberOfPoints.N5);
	private final EmbeddedFunction embeddedFunction;
	protected Double targetAACPerHa;
	private transient MeloThinnerPredictorDialog dialog;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled a boolean that enables or disables the stochastic mode
	 */
	public MeloThinnerPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		init();
		embeddedFunction = new EmbeddedFunction();
		embeddedFunction.setVariableValue(0, 1);
		embeddedFunction.setVariableValue(1, 1);
		embeddedFunction.setParameterValue(0, 0);
		embeddedFunction.setParameterValue(1, 0);
	}

	/**
	 * Set the annual allowance cut volume. The aac parameter must be the annual
	 * allowance cut volume per hectare. It has to be positive or null. If null,
	 * the aac is derived from past aac.
	 * @param aac a Double
	 */
	public void setTargetAACPerHa(Double aac) {
		if (aac != null && (aac < 0 || Double.isNaN(aac))) {
			throw new InvalidParameterException("The aac parameter must be a positive double!");
		}
		targetAACPerHa = aac;
	}
	
	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_HarvestBeta.csv";
			String omegaFilename = path + "0_HarvestOmega.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix randomEffectVariance = defaultBetaMean.getSubMatrix(11, 11, 0, 0);
			defaultBetaMean = defaultBetaMean.getSubMatrix(0, 10, 0, 0);
			
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			defaultBetaVariance = defaultBetaVariance.getSubMatrix(0, 10, 0, 10);
			Matrix meanRandomEffect = new Matrix(1,1);
			setDefaultRandomEffects(HierarchicalLevel.CRUISE_LINE, new GaussianEstimate(meanRandomEffect, randomEffectVariance));
			ModelParameterEstimates estimate = new SASParameterEstimates(defaultBetaMean, defaultBetaVariance);
			setParameterEstimates(estimate); 
			oXVector = new Matrix(1, estimate.getMean().m_iRows);
			
		} catch (Exception e) {
			System.out.println("MeloThinnerPredictor.init() : : Unable to read parameter files!");
		}

	}

	
	
	
	
	
	/**
	 *  {@inheritDoc}
	 *  For this class, the tree parameter should be null. The parms argument is a map
	 *  whose keys and values can be <br>
	 *  <ul> 
	 *  <li>DisturbanceParameter.ParmAAC: an array of annual allowance cut volumes (m3/ha/yr)</li>
	 *  <li>DisturbanceParameter.ParmYear0: the date at the beginning of the time step</li>
	 *  <li>DisturbanceParameter.ParmYear1: the date at the end of the time step</li>
	 *  <li>DisturbanceParameter.ParmModulation: a modulation factor (optional)
	 *  </ul>
	 *  If the ParmAAC parameter is specified, the other three are not taken into account. 
	 *  If the ParmAAC parameter is missing, then the ParmYear0 and ParmYear1 are
	 *  mandatory. However, the modulation factor is optional. It must be range between -1 (exclusive)
	 *  and +1 (inclusive) which are interpreted as -100% and +100% of the AAC. Values beyond this range 
	 *  are not considered and no modulation factor is then used. <br>
	 *  <br>
	 *  If the ParmAAC parameter is missing but the AAC has been set through the 
	 *  setFixedAACPerHa method then the modulation factor is discarded. 
	 *  
	 */
	@Override
	public synchronized double predictEventProbability(MeloThinnerPlot stand, Object tree, Map<String, Object> parms) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(stand);
		double proportionalPart = getProportionalPart(stand, beta);
		double[] aac;
		double modulationFactor = 0d;
		if (parms.containsKey(DisturbanceParameter.ParmAAC)) {
			aac = (double[]) parms.get(DisturbanceParameter.ParmAAC);
		} else {
			int year0 = (Integer) parms.get(DisturbanceParameter.ParmYear0);
			int year1 = (Integer) parms.get(DisturbanceParameter.ParmYear1);

			if (parms.containsKey(DisturbanceParameter.ParmModulation)) {
				modulationFactor = (Double) parms.get(DisturbanceParameter.ParmModulation);
				if (modulationFactor <= -1d || modulationFactor > 1d) {
					modulationFactor = 0;
				}
			}
			
			LandOwnership ownership;
			if (stand instanceof LandOwnershipProvider) {
				ownership = ((LandOwnershipProvider) stand).getLandOwnership();
			} else {
				ownership = LandOwnership.Public;
			}
			if (this.targetAACPerHa != null) {
				aac = new double[year1 - year0];
				for (int i = 0; i < aac.length; i++) {
					aac[i] = targetAACPerHa;		// modulation factor should not be taken into account here 
				}
			} else {
				aac = MeloThinnerAACProvider.getInstance().getAACValues(stand.getQuebecForestRegion(),
						ownership, 
						year0,
						year1,
						modulationFactor);
			}
		}
		double baseline = getBaseline(beta, aac);

		double conditionalSurvival = Math.exp(-proportionalPart * baseline);
		embeddedFunction.setParameterValue(0, conditionalSurvival);
		
		double survival;
		if (isRandomEffectsVariabilityEnabled) {
			String cruiseLineID = stand.getCruiseLineID();
			if (cruiseLineID == null) {
				cruiseLineID = stand.getSubjectId();
			}
			CruiseLine cruiseLine = getCruiseLineForThisSubject(cruiseLineID, stand);
			Matrix cruiseLineRandomEffect = getRandomEffectsForThisSubject(cruiseLine);
			double u = cruiseLineRandomEffect.getValueAt(0, 0);
			embeddedFunction.setParameterValue(1, u);
			survival = embeddedFunction.getValue();
		} else {
			if (quadratureEnabled) {
				Matrix lowerCholeskyTriangle = getDefaultRandomEffects(HierarchicalLevel.CRUISE_LINE).getVariance().getLowerCholTriangle();
				survival = ghq.getIntegralApproximation(embeddedFunction, ParametersToIntegrate, lowerCholeskyTriangle);
			} else {
				embeddedFunction.setParameterValue(1, 0);
				survival = embeddedFunction.getValue();
			}
		}
		double harvestProb = 1 - survival;
		return harvestProb;
	}

	
	/**
	 *  {@inheritDoc}
	 *  For this class, the tree parameter should be null. The parms argument is a map
	 *  whose keys and values can be <br>
	 *  <ul> 
	 *  <li>DisturbanceParameter.ParmAAC: an array of annual allowance cut volumes (m3/ha/yr)</li>
	 *  <li>DisturbanceParameter.ParmYear0: the date at the beginning of the time step</li>
	 *  <li>DisturbanceParameter.ParmYear1: the date at the end of the time step</li>
	 *  <li>DisturbanceParameter.ParmModulation: a modulation factor (optional)
	 *  </ul>
	 *  If the ParmAAC parameter is specified, the other three are not taken into account.
	 *  If the ParmAAC parameter is missing, then the ParmYear0 and ParmYear1 are
	 *  mandatory. However, the modulation factor is optional. It must be range between -1 (exclusive)
	 *  and +1 (inclusive) which are interpreted as -100% and +100% of the AAC. 
	 *  Values beyond this range are not considered and no modulation factor is then used.
	 */
	@Override
	public Object predictEvent(MeloThinnerPlot stand, Object tree, Map<String, Object> parms) {
		return super.predictEvent(stand, tree, parms);
	}
	
	private double getBaseline(Matrix beta, double[] aac) {
		
		double gamma0 = beta.getValueAt(9, 0);
		double gamma1 = beta.getValueAt(10, 0);
		
		double baselineResult = 0;
		for (double v : aac) {
			baselineResult += Math.exp(gamma0 + gamma1 * v);
		}
		
		return baselineResult;
	}

	private double getProportionalPart(MeloThinnerPlot stand, Matrix beta) {
		int index = 0;
		oXVector.setValueAt(0, index, Math.log(stand.getBasalAreaM2Ha()));
		index++;
		
		oXVector.setValueAt(0, index, stand.getNumberOfStemsHa());
		index++;
		
		Matrix slopeClassDummy = getDummySlopeClass(stand.getSlopeClass());
		oXVector.setSubMatrix(slopeClassDummy, 0, index);
		index += slopeClassDummy.m_iCols;
		
		Matrix dynamicTypeDummy = getDynamicTypeDummy(stand.getEcologicalType());
		oXVector.setSubMatrix(dynamicTypeDummy, 0, index);
		index += dynamicTypeDummy.m_iCols;
		
		Matrix xBeta = oXVector.multiply(beta);
		return Math.exp(xBeta.getValueAt(0, 0));
	}
	
	
	private Matrix getDummySlopeClass(QcSlopeClass slopeClass) {
		if (slopeClassDummy == null) {
			slopeClassDummy = new HashMap<QcSlopeClass, Matrix>();
			Matrix dummy;
			for (QcSlopeClass sc : QcSlopeClass.values()) {
				dummy = new Matrix(1,5);
				if (sc.ordinal() > 0) {
					if (sc == QcSlopeClass.S) 
						dummy.setValueAt(0, QcSlopeClass.F.ordinal() - 1, 1d);	// Class S is given the same dummy as Class F
					else
						dummy.setValueAt(0, sc.ordinal() - 1, 1d);
				}
				slopeClassDummy.put(sc, dummy);
			}
		}
		return slopeClassDummy.get(slopeClass);
	}
	
	private Matrix getDynamicTypeDummy(String ecologicalType) {
		if (dynamicTypeDummy == null) {
			dynamicTypeDummy = new HashMap<String, Matrix>();
			Matrix dummy = new Matrix(1,2);
			dummy.setValueAt(0, 0, 1d);
			dynamicTypeDummy.put("F", dummy);
			
			dummy = new Matrix(1,2);
			dummy.setValueAt(0, 1, 1d);
			dynamicTypeDummy.put("M", dummy);
			
			dummy = new Matrix(1,2);
			dynamicTypeDummy.put("R", dummy);
		}
		return dynamicTypeDummy.get(ecologicalType.substring(0, 1));
	}

	/*
	 * For test purpuse. Not to be disabled.
	 */
	void setGaussianQuadrature(boolean quadEnabled) {
		this.quadratureEnabled = quadEnabled;
	}

	@Override
	public MeloThinnerPredictorDialog getUI(Container parent) {
		if (dialog == null) {
			dialog = new MeloThinnerPredictorDialog(this, (Window) parent);
		}
		return dialog;
	}

	@Override
	public boolean isVisible() {
		if (dialog != null && dialog.isVisible()) {
			return true;
		}
		return false;
	}

	@Override
	public void showUI(Window parent) {
		getUI(parent).setVisible(true);
	}

	/**
	 * Return the target annual allowance cut (AAC) volume per ha. 
	 * @return a Double or null if the AAC has not been set.
	 */
	public Double getTargetAACPerHa() {
		return targetAACPerHa;
	}

	@Override
	public REpiceaTreatmentDefinition getTreatmentDefinitionForThisHarvestedStand(MeloThinnerPlot stand) {return null;}

	/*
	 * This class does not make any distinction as to the treatment applied in the plots.
	 */
	@Override
	public List<Enum> getTreatmentList() {return null;}

}
