/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
 * Copyright (C) 2021-2024 His Majestry the King in Right of Canada
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.simulation.thinners.REpiceaThinner;
import repicea.simulation.thinners.REpiceaThinningOccurrenceProvider;
import repicea.simulation.thinners.REpiceaTreatmentEnum;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;

/**
 * This method implement an automated harvest model that applies to Quebec's commercial forest.
 * Based on database provided by Gordon Weber on March 1st, 2010.
 * @author M. Fortin - August 2010
 */
public final class OfficialHarvestModel extends REpiceaThinner<OfficialHarvestableStand, OfficialHarvestableTree> {
	
	 
	
	private static final long serialVersionUID = 20100905L;

	/**
	 * This Enum class contains all the available treatment for this harvester.
	 * @author M. Fortin - September 2010
	 */
	public static enum TreatmentType implements REpiceaTreatmentEnum {
		
		CA("Sanitary harvesting", "Coupe d'am\u00E9lioration", null), 
		CE("Crop tree harvesting", "Coupe d'\u00E9claircie", new int[] {15, 30}), 
		CJ_1997("Selection cutting before 1997", "Coupe de jardinage avant 1997", null), 
		CJ_2004CERF("Selection cutting 1997-2004 adapted for deer", "Coupe de jardinage 1997-2004 Cerf", null), 
		CJ_2004("Selection cutting 1997-2004", "Coupe de jardinage 1997-2004", null), 
		CJMSCRCERF("Selection cutting after 2004 adapted for deer", "Coupe de jardinage apr\u00E8s 2004 Cerf", null), 
		CJMSCR("Selection cutting after 2004", "Coupe de jardinage apr\u00E8s 2004", null), 
		/**
		 * Shelterwood cutting. <p>
		 * 
		 * Final cut is assumed to occur 15-30 years after shelterwood cutting.
		 * @see <a href=https://forestierenchef.gouv.qc.ca/wp-content/uploads/099-102_MDPF_CPR.pdf>  
		 * Bureau du Forestier en Chef du Qu\u00E9bec. Section 3.6 Coupe progressive irr\u00E9guli\u00E8re dans 
		 * Manuel de d\u00E9termination des possibilit\u00E9s foresti\u00E8res 2013-2018.
		 * </a>
		 */
		CP("Shelterwood cutting", "Coupe progressive", new int[] {15, 30}), 
		EC("Commercial thinning", "\u00C9claircie commerciale", new int[] {15, 30}),
		ES("Selective thinning", "\u00C9claircie s\u00E9lective", new int[] {15, 30}),
		CP_35("Partial cutting 35%", "Coupe partielle 35% chantier for\u00eat feuillue R-06", new int[] {15, 30}),
		CP_45("Partial cutting 45%", "Coupe partielle 45% chantier for\u00eat feuillue R-06", new int[] {15, 30}),
		CPI_CP("CPI_CP Outaouais", "CPI_CP Outaouais", null),
		CPI_RL("CPI_RL Outaouais", "CPI_RL Outaouais", null),
		CRS("CRS Outaouais", "CRS Outaouais", null),
		CJP("Selection cutting CIMOTFF","Coupes jardinage CIMOTFF", null),
		CJPG_QM("Selection cutting group of trees CIMOTFF","Coupe jardinage par groupe d'arbres CIMOTFF", null),
		CPI_CP_CIMOTF("CPI_CP CIMOTFF", "Coupe progressive irr\u00E9guli\u00E8re couvert permanent CIMOTFF", null),
		CPI_RL_CIMOTF("CPI_RL CIMOTFF","Coupe progressive irr\u00E9guli\u00E8re \u00E0 r\u00E9g\u00E9n\u00E9ration lente CIMOTFF", null),
		CPRS("Harvesting with soil and regeneration protection", "CPRS - Coupe avec protection de la r\u00E9g\u00E9n\u00E9ration et des sols", null),
		PROTECTION("No harvest", "Aucune coupe", null)
		;		

		private static List<TreatmentType> FinalTreatments = Arrays.asList(CRS, CPRS);

		int[] finalCutRange;
		
		
		TreatmentType(String englishText, String frenchText, int[] finalCutRange) {
			setText(englishText, frenchText);
			this.finalCutRange = finalCutRange;
		}

		/**
		 * Indicate that a final cut has to be schedule whenever the treatment 
		 * is applied.
		 * @return a boolean
		 */
		boolean doesFinalCutHaveToBeScheduled() {
			return finalCutRange != null;
		}
		

		@Override
		public boolean isFinalCut() {return FinalTreatments.contains(this);}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}

		@Override
		public String getCompleteName() {
			return this.getClass().getName().concat(".") + name();
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
	}
	
	private FixedEffectVectorFactory xVectorFactory;
	private Map<TreatmentType, OfficialHarvestSubmodel> modelParametersLibrary;
	protected static Map<TreatmentType, Map<String, String>> speciesMap;
	private final OfficialHarvestSubmodelSelector selector;
	
	
	/**
	 * General constructor that enables the stochastic implementation. NOTE: there is no random effect variability in this model.
	 * @param isParameterVariabilityEnabled = a boolean that enables the parameter variability
	 */
	public OfficialHarvestModel(boolean isParameterVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isParameterVariabilityEnabled);
		modelParametersLibrary = new HashMap<TreatmentType, OfficialHarvestSubmodel>();
		init();
		xVectorFactory = new FixedEffectVectorFactory();
		selector = new OfficialHarvestSubmodelSelector();
	}
	
	/**
	 * Default constructor with all uncertainty sources disabled.
	 */
	public OfficialHarvestModel() {
		this(false);
	}
	
//	/**
//	 * This method sets a selector for the different treatments.
//	 * @param selector an OfficialHarvestSubmodelSelector instance
//	 */
//	public void setOfficialHarvestSubmodelSelector(OfficialHarvestSubmodelSelector selector) {
//		this.selector = selector;
//	}
	
	/**
	 * Provide access to the treatment selector.
	 * @return an OfficialHarvestSubmodelSelector instance
	 */
	public OfficialHarvestSubmodelSelector getSelector() {return selector;}
	
	
	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			InputStream isParameters = getClass().getResourceAsStream("/" + path + "parmsNew.txt");
			InputStream isOmega = getClass().getResourceAsStream("/" + path + "covbNew.txt");
			String speciesPath = path + "species.csv";

			Map<TreatmentType, Matrix> betas = loadFileIntDoubleToMapIntMatrix(isParameters, false);		// is not a variance matrix 
			Map<TreatmentType, Matrix> variances = loadFileIntDoubleToMapIntMatrix(isOmega, true);			// is a variance matrix
			speciesMap = loadSpecies(speciesPath);			
			
			for (TreatmentType treatment : TreatmentType.values()) {
				if (betas.containsKey(treatment) && variances.containsKey(treatment)) {
					OfficialHarvestSubmodel calculatorForThisTreatment = new OfficialHarvestSubmodel(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
					calculatorForThisTreatment.setParameterEstimates(betas.get(treatment), SymmetricMatrix.convertToSymmetricIfPossible(variances.get(treatment)));
					modelParametersLibrary.put(treatment, calculatorForThisTreatment);
				}
			}
			
		} catch (Exception e) {
			System.out.println("AutomatedHarvestModel.init() : Error while initializing the module!");
			e.printStackTrace();
		}
	}	
	
	@Override
	public synchronized double predictEventProbability(OfficialHarvestableStand stand, OfficialHarvestableTree tree, Map<String, Object> parms) {
		Enum<?> treatment;
		int modifier;
		if (parms != null && parms.containsKey(DisturbanceParameter.ParmTreatment)) {
			treatment = (Enum<?>) parms.get(DisturbanceParameter.ParmTreatment);
			modifier = parms.containsKey(DisturbanceParameter.ParmModulation) ? 
				(Integer) parms.get(DisturbanceParameter.ParmModulation) :
					0;
		} else {
			treatment = getTreatmentDefinitionForThisHarvestedStand(stand).getTreatmentType();
			modifier = 0;
		}

		double eventProbability;
		if (treatment == TreatmentType.CPRS) {
			eventProbability = 1d;
		} else if (treatment == TreatmentType.PROTECTION) {
			eventProbability = 0d;
		} else {
			OfficialHarvestSubmodel submodel = modelParametersLibrary.get(treatment);		
			Matrix modelParameters = submodel.getSubParametersForThisStand(stand);
			oXVector = xVectorFactory.getFixedEffectVector(stand, tree, (TreatmentType) treatment);
			double xBeta = oXVector.multiply(modelParameters).getValueAt(0, 0);
			double fExpXBeta = Math.exp(xBeta);
			eventProbability = fExpXBeta / (1.0 + fExpXBeta);
			
			double modifierFactor = 1;
			
			
			if (modifier != 0) {
				modifierFactor += (double) modifier / 100;
			}
			
			eventProbability *= modifierFactor;
			
			if (eventProbability > 1) {
				eventProbability = 1;
			} else if (eventProbability < 0) {
				eventProbability = 0;
			}
		} 
		return eventProbability;
	}
	
	/**
	 * This method serves to load the parameters of the model.
	 */
	private Map<TreatmentType, Matrix> loadFileIntDoubleToMapIntMatrix(InputStream iStream, boolean isVarianceMatrix) throws Exception {
		Map<TreatmentType, Matrix> oFinalMap = new HashMap<TreatmentType,Matrix>();
		Map<TreatmentType,Vector<Double>> oMap = new HashMap<TreatmentType,Vector<Double>>();

		int modelVersion;
		double modelParm;
		
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(iStream));
				String str = in.readLine();
				while (str != null) {

					// comment / blank line : goes to next line
					if (!str.startsWith("#") && str.trim().length() != 0) {
						// System.err.println (str);
						String token1 = null;
						String token2 = null;
						
						StringTokenizer tkz = new StringTokenizer(str, ";");

						token1 = tkz.nextToken();
						modelVersion = Integer.parseInt(token1);
						TreatmentType treatment = TreatmentType.values()[modelVersion];
						
						token2 = tkz.nextToken();
						modelParm = Double.parseDouble(token2);

						Vector<Double> oVectorTmp = oMap.get(treatment);
						if (oVectorTmp==null) {
							oVectorTmp = new Vector<Double>();
							oMap.put(treatment, oVectorTmp);
						}
						oVectorTmp.add(modelParm);
					}
					str = in.readLine();
				}
				
				for (TreatmentType treatment : oMap.keySet()) {
					Matrix oMatTmp = new Matrix(oMap.get(treatment));
					if (isVarianceMatrix) {
						oMatTmp = oMatTmp.squareSym();
					}
					oFinalMap.put(treatment, oMatTmp);
				}
				return oFinalMap;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
	}
	
	/**
	 * Method loadSpecies allows to load species files.
	 * Loads the following files:
	 * species.csv	-		The species list	
	 * @param strFilename the path of the file to be read
	 * @return a Map instance
	 * @throws IOException if an exception is thrown during the reading of the file
	 */
	private Map<TreatmentType, Map<String, String>> loadSpecies(String strFilename) throws IOException {
		CSVReader reader = null;
		try {
			Map<TreatmentType, Map<String, String>> oMap = new HashMap<TreatmentType, Map<String, String>>();
			int id;
			String speciesKey;
			String speciesMatch;
			reader = new CSVReader(strFilename);
			Object[] lineRead = reader.nextRecord();
			while (lineRead != null) {
				id = Integer.parseInt(lineRead[0].toString());
				speciesKey = lineRead[1].toString();
				speciesMatch = lineRead[2].toString();				
				TreatmentType treatment = TreatmentType.values()[id];
				Map<String, String> speciesMap = oMap.get(treatment);
				if(speciesMap == null){
					speciesMap = new HashMap<String, String>();
					oMap.put(treatment, speciesMap);
				}
				speciesMap.put(speciesKey, speciesMatch);			
				lineRead = reader.nextRecord();
			}
			reader.close();
			return oMap;
		} catch (IOException e) {
			if (reader != null) {
				reader.close();
			}
			e.printStackTrace();
			throw e;
		} 
	}

	@Override
	public OfficialHarvestTreatmentDefinition getTreatmentDefinitionForThisHarvestedStand(OfficialHarvestableStand stand) {
		OfficialHarvestTreatmentDefinition currentSelection = selector.getMatch(stand.getLandUse(), stand.getPotentialVegetation()).getDeepClone();
		REpiceaThinningOccurrenceProvider thinningOcc = stand.getThinningOccurrence();
		if (thinningOcc != null) {
			if (((OfficialHarvestTreatmentDefinition) thinningOcc.getTreatmentDefinition()).doesFinalCutHaveToBeScheduled()) {
				OfficialHarvestTreatmentDefinition defCPRS = new OfficialHarvestTreatmentDefinition(TreatmentType.CPRS, currentSelection.getDelayBeforeReentryYrs());
				return defCPRS;
			}
		}
		return currentSelection;
	}

	@Override
	public List<REpiceaTreatmentEnum> getTreatmentList() {return Arrays.asList(TreatmentType.values());}

	/**
	 * Return the maximum annual area for this treatment.
	 * 
	 * @param treatment a TreatmentType enum
	 * @return a double if the treatment is listed. Otherwise, it must return 0d, which means there is no maximum.
	 */
	public double getMaxAnnualAreaHaForThisTreatment(TreatmentType treatment) {
		Double maxAreaHa = selector.getAreaLimitations().areaLimitationMap.get(treatment);
		return maxAreaHa == null ? 0d : maxAreaHa;
	}
	
}

