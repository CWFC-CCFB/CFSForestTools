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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.Heightable2014Tree.Hd2014Species;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.hdrelationships.HeightPredictor;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * An implementation of the model of height-diameter relationships
 * published in Auger (2016).
 * 
 * @author Hache Denis - Fev 2014
 * @see <a href=https://mffp.gouv.qc.ca/nos-publications/nouvelle-relation-hauteur-diametre>  
 * Auger, I. 2016. Une nouvelle relation hauteur-diametre tenant compte de l’influence de la 
 * station et du climat pour 27 essences commerciales du Quebec. Gouvernement du Quebec, 
 * Ministere des Forets, de la Faune et des Parcs, Direction de 
 * la recherche forestiere. Note de recherche forestiere no 146. 31 p.
 * </a>
 */
public class GeneralHeight2014Predictor extends REpiceaPredictor implements HeightPredictor<Heightable2014Stand, Heightable2014Tree> {


	protected static class BetaHeightableStandMonteCarlo implements MonteCarloSimulationCompliantObject {
		private final int monteCarloRealization;
		private String subjectID;
		private HierarchicalLevel	hieraLevel;

		protected BetaHeightableStandMonteCarlo(MonteCarloSimulationCompliantObject subject, Hd2014Species species) {
			String id = (subject.getMonteCarloRealizationId() + "_" + species.ordinal());
			this.monteCarloRealization = id.hashCode();
			subjectID = subject.getSubjectId();
			hieraLevel = subject.getHierarchicalLevel();
		}

		@Override
		public String getSubjectId() {
			return subjectID;
		}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return hieraLevel;
		}


		@Override
		public int getMonteCarloRealizationId() {
			return monteCarloRealization;
		}

	}

	private static final long	serialVersionUID	= -8023375730600740497L;
	
	
	protected static enum DisturbanceType{
		INT,
		MOY,
		NON;		
	}
	
	/**
	 * This enum variable contains all the possible effects
	 * @author Denis hache - Fev 2014
	 */
	protected static enum Effect {
		LogDbh("ldhp"),
		LogDbh2("ldhp2"),
		LogDbh_basalArea("ldhp*st"), 
		LogDbh_basalAreaGreaterThan("ldhp*stgt"),
		LogDbh2_basalAreaGreaterThan("ldhp2*stgt"),
		LogDbh_ratioDbh("ldhp*rdhp"),
		LogDbh2_ratioDbh("ldhp2*rdhp"), 
		LogDbh_SubDom("ldhp*sdom"), 
		LogDbh_PotVeg("ldhp*vp"), 
		LogDbh_Elevation("ldhp*alt"),
		LogDbh_EcoType("ldhp*milieu"),
		LogDbh_Disturb("ldhp*pert"),
		LogDbh_Dens("ldhp*dens"),
		LogDbh_meanT("ldhp*Tmoy"),
		LogDbh_pTot("ldhp*Ptot"),
		LogDbh_Is("ldhp*is"),
		LogDbh_NotOuest("ldhp*ouest");
		
		private String nameEffect;
		
		Effect(String pNameEffect) {
			this.nameEffect = pNameEffect;
		}

		public static Effect fromString(String text) {
			if (text != null) {
				for (Effect b : Effect.values()) {
					if (text.equalsIgnoreCase(b.nameEffect)) {
						return b;
					}
				}
			}
			throw new IllegalArgumentException("No constant with text " + text + " found");
		}
	}

	private final Map<Hd2014Species, GeneralHeight2014InternalPredictor> internalPredictors;
	
	public GeneralHeight2014Predictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		internalPredictors = new HashMap<Hd2014Species, GeneralHeight2014InternalPredictor>();
		init();
	}

	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public GeneralHeight2014Predictor() {
		this(false);
	}

	
	private Matrix setMatrixAndValue(List<?> oList, Object obj) {
		if (oList != null && !oList.isEmpty()) {
			Matrix oMat = new Matrix(1, oList.size());
			if(oList.indexOf(obj) != -1){
				oMat.setValueAt(0, oList.indexOf(obj), 1d);
			}
			return oMat;
		} else {
			return null;
		}
	}
	
	
	@Override
	protected final void init() {
		try {
			for (Hd2014Species species : Hd2014Species.values()) {		
				GeneralHeight2014InternalPredictor internalPredictor = new GeneralHeight2014InternalPredictor(species, 
						isParametersVariabilityEnabled);
				internalPredictors.put(species, internalPredictor);
				String path = ObjectUtility.getRelativePackagePath(getClass()) + species.name().toLowerCase() + "/";
				String suffix = species.name().toUpperCase().concat(".csv");
				
				String parameterFilename = path + "parameters".concat(suffix);
				String omegaFilename = path + "omega".concat(suffix);
				String plotRandomEffectsFilename = path + "randomEffects".concat(suffix);
				String listSdomFilename = path + "listeSdom".concat(suffix);
				String listVpFilename = path + "listeVp2".concat(suffix);
				String listEcoTypeFilename = path + "listeTypeEco4".concat(suffix);
				String listEffectFilename = path + "Effects".concat(suffix);
				String listDisturbFileName = path + "listePerturb".concat(suffix);

				Matrix beta = ParameterLoaderExt.loadVectorFromFile(parameterFilename).get();
				SymmetricMatrix omega = SymmetricMatrix.convertToSymmetricIfPossible(ParameterLoaderExt.loadMatrixFromFile(omegaFilename, 6));				
				try {
					omega.getLowerCholTriangle();
				} catch (Exception e) {
					System.err.println("Error can't use getLowerCholTriangle on omega for " + species);
				}

				SASParameterEstimates defaultBeta = new SASParameterEstimates(beta, omega);
				internalPredictor.setParameterEstimates(defaultBeta);

				Matrix randomEffects = ParameterLoaderExt.loadVectorFromFile(plotRandomEffectsFilename).get();
				SymmetricMatrix matrixG = SymmetricMatrix.convertToSymmetricIfPossible(randomEffects.getSubMatrix(0, 0, 0, 0));
				Matrix defaultRandomEffectsMean = new Matrix(matrixG.m_iRows, 1);
				internalPredictor.setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
				
				SymmetricMatrix sigma2 = SymmetricMatrix.convertToSymmetricIfPossible(randomEffects.getSubMatrix(2, 2, 0, 0));
				double phi = randomEffects.getValueAt(1, 0);//tree
				GaussianErrorTermEstimate estimate = new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.POWER);
				setDefaultResidualError(ErrorTermGroup.Default, estimate);
				internalPredictor.setDefaultResidualError(ErrorTermGroup.Default, estimate);

				Matrix oMat;
				List<String> sdomM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 3, String.class);
				List<String> sdomBio = ParameterLoaderExt.loadColumnVectorFromFile(listSdomFilename, 0, String.class);
				List<String> sdom = ParameterLoaderExt.loadColumnVectorFromFile(listSdomFilename, 1, String.class);
				Map<String, Matrix> oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < sdomBio.size(); i++) {
					String sdomBioKey = sdomBio.get(i);
					String sdomValue = sdom.get(i);
					oMat = setMatrixAndValue(sdomM, sdomValue);
					if (oMat != null) {
						oMap.put(sdomBioKey, oMat);
					}
				}
				internalPredictor.setSubDomainDummyMap(oMap);

				List<String> vegPotM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 4, String.class);
				List<String> vegPotKeys = ParameterLoaderExt.loadColumnVectorFromFile(listVpFilename, 0, String.class);
				List<String> vp = ParameterLoaderExt.loadColumnVectorFromFile(listVpFilename, 1, String.class);
				oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < vegPotKeys.size(); i++) {
					String vegPotKey = vegPotKeys.get(i);
					String vpValue = vp.get(i);
					oMat = setMatrixAndValue(vegPotM, vpValue);
					if (oMat != null) {
						oMap.put(vegPotKey, oMat);
					}
				}
				internalPredictor.setVegPotDummyMap(oMap);

				List<String> ecoTypeM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 5, String.class);
				List<String> ecoTypeKeys = ParameterLoaderExt.loadColumnVectorFromFile(listEcoTypeFilename, 0, String.class);
				List<String> milieu = ParameterLoaderExt.loadColumnVectorFromFile(listEcoTypeFilename, 1, String.class);
				oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < ecoTypeKeys.size(); i++) {
					String ecoTypeKey = ecoTypeKeys.get(i);
					String milieuValue = milieu.get(i);
					oMat = setMatrixAndValue(ecoTypeM, milieuValue);
					if (oMat != null) {
						oMap.put(ecoTypeKey, oMat);
					}
				}
				internalPredictor.setEcoTypeDummyMap(oMap);
				
				
				List<String> disturbM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 2, String.class);
				List<String> disturbKeys = ParameterLoaderExt.loadColumnVectorFromFile(listDisturbFileName, 0, String.class);
				List<String> disturb = ParameterLoaderExt.loadColumnVectorFromFile(listDisturbFileName, 1, String.class);
				oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < disturbKeys.size(); i++) {
					String ecoTypeKey = disturbKeys.get(i);
					String disturbValue = disturb.get(i);
					oMat = setMatrixAndValue(disturbM, disturbValue);
					if (oMat != null) {
						oMap.put(ecoTypeKey, oMat);
					}
				}
				internalPredictor.setDisturbanceDummyMap(oMap);

				List<String> effects = ParameterLoaderExt.loadColumnVectorFromFile(listEffectFilename, 0, String.class);
				List<Effect> listEffect = new ArrayList<Effect>();
				for (String effect : effects) {
					listEffect.add(Effect.fromString(effect));
				}
				internalPredictor.setEffectList(listEffect);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("BetaHeightPredictor Class : Unable to initialize the beta height-diameter relationship");
		}
	}

	@Override
	public synchronized double predictHeightM(Heightable2014Stand stand, Heightable2014Tree tree) {
		Hd2014Species species = tree.getHeightable2014TreeSpecies();
		GeneralHeight2014InternalPredictor internalPredictor = internalPredictors.get(species); 
		double height = internalPredictor.predictHeightM(stand, tree);
		if(height < 3.0d) {
			height = 3.0d;
		}
		return height;
	}

	
//	/**
//	 * For testing purpose.
//	 * 
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		ArrayList<Integer> outputVec = new ArrayList<Integer>();
//		for (int year = 2013; year <= 2013 + 10 * 20; year += 10) {
//			outputVec.add(year);
//		}
//
//		BetaHeightPredictor intance = new BetaHeightPredictor(false, false, false, outputVec);
//	}
//		

}
