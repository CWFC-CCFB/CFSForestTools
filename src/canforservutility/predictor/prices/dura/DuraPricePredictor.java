/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin - Canadian Forest Service
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
package canforservutility.predictor.prices.dura;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * An implementation of Helin Dura's price model for 
 * lumber and OSB products.
 * @author Mathieu Fortin - February 2025
 */
@SuppressWarnings("serial")
public final class DuraPricePredictor extends REpiceaPredictor {

	
	final class DuraPriceSubPredictor extends REpiceaPredictor {

		final List<Effect> effectList;
		final WoodProduct woodProduct;
		Matrix xVector;
		double varParm;
		double corrParm;
		double resVariance;
		
		protected DuraPriceSubPredictor(WoodProduct wp, boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
			super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
			woodProduct = wp;
			effectList = new ArrayList<Effect>();
			init();
		}

		@Override
		protected void init() {}
		
		synchronized double predictPrice(DuraPriceContext dpc) {
			Matrix beta = getParametersForThisRealization(dpc);
			setXVector(dpc);
			double pred = xVector.multiply(beta).getValueAt(0, 0);
			if (isResidualVariabilityEnabled) {
				Matrix res = getResidualErrorForThisSubject(dpc, ErrorTermGroup.Default);
				pred += res.getValueAt(res.m_iRows - 1, 0);
			}
			return pred;
		}

		SymmetricMatrix getResidualErrorCovMatrix(DuraPriceContext dpc) {
			return getDefaultResidualError(ErrorTermGroup.Default).getVariance(getGaussianErrorTerms(dpc));
		}
		
		private void setXVector(DuraPriceContext dpc) {
			xVector.resetMatrix();
			int index = 0;
			for (Effect effect : effectList) {
				switch(effect) {
				case Intercept:
					xVector.setValueAt(0, index++, 1d);
					break;
				case EXCAUSLag4:
					xVector.setValueAt(0, index++, dpc.getExchangeRateRatioCANToUSA_lag4());
					break;
				case CLIMCOSTLAG:
					xVector.setValueAt(0, index++, dpc.getClimateCost_BillionDollars());
					break;
				case FEDFUNDSLag1:
					xVector.setValueAt(0, index++, dpc.getFederalFundsRate_lag1());
					break;
				case PSAVERTLag4:
					xVector.setValueAt(0, index++, dpc.getPersonalSavingRate_lag4());
					break;
				case FEDFUNDSLag3:
					xVector.setValueAt(0, index++, dpc.getFederalFundsRate_lag3());
					break;
				case DummyCovid1:
					double value = dpc instanceof CovidPeriodProvider && ((CovidPeriodProvider) dpc).isCovidPeriod() ? 1d : 0d;  
					xVector.setValueAt(0, index++, value);
					break;
				case HOUST:
					xVector.setValueAt(0, index++, dpc.getHousingStartNumber_ThousandUnits());
					break;
				case EXCAUSLag1:
					xVector.setValueAt(0, index++, dpc.getEchangeRateRatioCANToUSA_lag1());
					break; 
				}
			}
		}
		
		/*
		 * For extended visibility
		 */
		@Override
		protected void setParameterEstimates(ModelParameterEstimates gaussianEstimate) {
			super.setParameterEstimates(gaussianEstimate);
		}
		
		void setEffectList(List<Effect> effects) {
			effectList.addAll(effects);
			xVector = new Matrix(1, effectList.size());
		}
		
		void setCovParms(Matrix vector) {
			corrParm = vector.getValueAt(0, 0);
			double resStdDev = vector.getValueAt(1, 0);
			resVariance = resStdDev * resStdDev;
			varParm = vector.getValueAt(2, 0);		
		}

		protected void setDefaultResidualError() {
			GaussianErrorTermEstimate gete = new GaussianErrorTermEstimate(SymmetricMatrix.getIdentityMatrix(1).scalarMultiply(resVariance),
					corrParm,
					TypeMatrixR.POWER);
			setDefaultResidualError(ErrorTermGroup.Default, gete);
		}
		
	}
	
	static ParameterMap BETA_VECTORS;
	static ParameterMap COVB_MATRICES;
	static Map<Integer, List<Effect>> EFFECT_LIST;
	static ParameterMap COVPARMS_VECTORS;
	
	static final Object LOCK = new Object();
	
	/**
	 * An enum that stands for the units associated with the
	 * different products addressed in 
	 * Helin Dura's models of wood product price.
	 */
	public static enum WoodProductUnit implements TextableEnum {
		MBF("Thousand board feet", "Mille pieds mesure de planche"),
		MSF("Thousand square feet", "Mille pieds carr\u00E9s");

		WoodProductUnit(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	/**
	 * An enum that stands for the different products addressed in 
	 * Helin Dura's models of wood product price.
	 */
	public static enum WoodProduct implements TextableEnum {
		LUMBER_1X3(WoodProductUnit.MBF, "Lumber 1x3", "Sciage 1x3"),
		LUMBER_1X4(WoodProductUnit.MBF, "Lumber 1x4", "Sciage 1x4"),
		LUMBER_2X3(WoodProductUnit.MBF, "Lumber 2x3", "Sciage 2x3"),
		LUMBER_2X4(WoodProductUnit.MBF, "Lumber 2x4", "Sciage 2x4"),
		LUMBER_2X6(WoodProductUnit.MBF, "Lumber 2x6", "Sciage 2x6"),
		LUMBER_2X8(WoodProductUnit.MBF, "Lumber 2x8", "Sciage 2x8"),
		LUMBER_2X10(WoodProductUnit.MBF, "Lumber 2x10", "Sciage 2x10"),
		PANEL_OSB(WoodProductUnit.MSF, "OSB panel", "Panneau OSB");
		
		private WoodProductUnit unit;
		
		WoodProduct(WoodProductUnit unit, String englishText, String frenchText) {
			this.unit = unit;
			setText(englishText, frenchText);
		}
		
		/**
		 * Provide the units associated with the WoodProduct enum
		 * @return a WoodProductUnit enum
		 */
		public WoodProductUnit getUnit() {return unit;}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	private static enum Effect {
		Intercept,
		EXCAUSLag4,
		CLIMCOSTLAG,
		FEDFUNDSLag1,
		PSAVERTLag4,
		FEDFUNDSLag3,
		DummyCovid1,
		HOUST, 
		EXCAUSLag1; 
	}
	
	final Map<WoodProduct, DuraPriceSubPredictor> subPredictorMap;
	
	/**
	 * General constructor.
	 * @param isParametersVariabilityEnabled true to enable the variability in the parameter estimates
	 * @param isResidualVariabilityEnabled true to enable the variability due to the residual error
	 */
	public DuraPricePredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled); // no random effect in this model
		subPredictorMap = new HashMap<WoodProduct, DuraPriceSubPredictor>();
		init();
	}

	/**
	 * Constructor in deterministic mode.
	 */
	public DuraPricePredictor() {
		this(false,false);
	}
	
	@Override
	protected void init() {
		synchronized (LOCK) {
			if (BETA_VECTORS == null) {
				CSVReader reader = null;
				try {
					String path = ObjectUtility.getRelativePackagePath(getClass());
					String filename = path + "0_beta.csv";
					BETA_VECTORS = ParameterLoader.loadVectorFromFile(1, filename);
					filename = path + "0_vcov.csv";
					COVB_MATRICES = ParameterLoader.loadVectorFromFile(1, filename);
					filename = path + "0_covParms.csv";
					COVPARMS_VECTORS = ParameterLoader.loadVectorFromFile(1, filename);
					filename = path + "0_effectList.csv";
					reader = new CSVReader(filename);
					EFFECT_LIST = new HashMap<Integer, List<Effect>>();
					Object[] record;
					while ((record = reader.nextRecord()) != null) {
						Integer model = Integer.parseInt(record[0].toString());
						if (!EFFECT_LIST.containsKey(model)) {
							EFFECT_LIST.put(model, new ArrayList<Effect>());
						}
						EFFECT_LIST.get(model).add(Effect.valueOf(record[1].toString()));
					}
					reader.close();
				} catch (Exception e) {
					if (reader != null) {
						reader.close();
					}
					throw new InvalidParameterException(e.getMessage());
				}
			}
		}
		
		for (WoodProduct wp : WoodProduct.values()) {
			DuraPriceSubPredictor sp = new DuraPriceSubPredictor(wp, isParametersVariabilityEnabled, isResidualVariabilityEnabled);
			subPredictorMap.put(wp, sp);
			Matrix mean = BETA_VECTORS.get(wp.ordinal());
			SymmetricMatrix vcov = COVB_MATRICES.get(wp.ordinal()).squareSym();
			ModelParameterEstimates ge = new ModelParameterEstimates(mean, vcov);
			sp.setParameterEstimates(ge);
			sp.setEffectList(EFFECT_LIST.get(wp.ordinal()));
			sp.setCovParms(COVPARMS_VECTORS.get(wp.ordinal()));
			sp.setDefaultResidualError(); 
		}
	}

	/**
	 * Predict the market price given a particular economic context,
	 * @param wp a WoodProduct enum
	 * @param pc a DuraPriceContext instance
	 * @return the price 
	 */
	public double predictPriceForThisProduct(WoodProduct wp, DuraPriceContext pc) {
		return subPredictorMap.get(wp).predictPrice(pc);
	}
	
	
}
