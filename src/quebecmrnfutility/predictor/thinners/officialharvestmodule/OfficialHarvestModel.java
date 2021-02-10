/*
 * English version follows
 * 
 * Ce fichier fait partie de la biblioth�que mrnf-foresttools.
 * Il est prot�g� par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du minist�re des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Qu�bec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;

/**
 * This method implement an automated harvest model that applies to Quebec's commercial forest.
 * Based on database provided by Gordon Weber on March 1st, 2010.
 * @author M. Fortin - August 2010
 */
public final class OfficialHarvestModel extends REpiceaBinaryEventPredictor<OfficialHarvestableStand, OfficialHarvestableTree> {
	
	 
	
	private static final long serialVersionUID = 20100905L;

	/**
	 * This Enum class contains all the available treatment for this harvester.
	 * @author M. Fortin - September 2010
	 */
	public static enum TreatmentType implements TreatmentEnum {
		
		CA("Sanitary harvesting", "Coupe d'am\u00E9lioration"), 
		CE("Crop tree harvesting", "Coupe d'\u00E9claircie"), 
		CJ_1997("Selection cutting before 1997", "Coupe de jardinage avant 1997"), 
		CJ_2004CERF("Selection cutting 1997-2004 adapted for deer", "Coupe de jardinage 1997-2004 Cerf"), 
		CJ_2004("Selection cutting 1997-2004", "Coupe de jardinage 1997-2004"), 
		CJMSCRCERF("Selection cutting after 2004 adapted for deer", "Coupe de jardinage apr\u00E8s 2004 Cerf"), 
		CJMSCR("Selection cutting after 2004", "Coupe de jardinage apr\u00E8s 2004"), 
		CP("Shelterwood cutting", "Coupe progressive"), 
		EC("Commercial thinning", "\u00C9claircie commerciale"), 		
		ES("Selective thinning", "\u00C9claircie s\u00E9lective"),
		CP_35("Partial cutting 35%", "Coupe partielle 35% chantier for\u00eat feuillue R-06"),
		CP_45("Partial cutting 45%", "Coupe partielle 45% chantier for\u00eat feuillue R-06"),
		CPI_CP("CPI_CP Outaouais", "CPI_CP Outaouais"),
		CPI_RL("CPI_RL Outaouais", "CPI_RL Outaouais"),
		CRS("CRS Outaouais", "CRS Outaouais"),
		CJP("Selection cutting CIMOTFF","Coupes jardinage CIMOTFF"),
		CJPG_QM("Selection cutting group of trees CIMOTFF","Coupe jardinage par groupe d'arbres CIMOTFF"),
		CPI_CP_CIMOTF("CPI_CP CIMOTFF", "Coupe progressive irr\u00E9guli\u00E8re couvert permanent CIMOTFF"),
		CPI_RL_CIMOTF("CPI_RL CIMOTFF","Coupe progressive irr\u00E9guli\u00E8re \u00E0 r\u00E9g\u00E9n\u00E9ration lente CIMOTFF"),
		CPRS("Harvesting with soil and regeneration protection", "CPRS - Coupe avec protection de la r\u00E9g\u00E9n\u00E9ration et des sols"),
		PROTECTION("No harvest", "Aucune coupe")
		;		
		TreatmentType(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
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
	private OfficialHarvestSubmodelSelector selector;
	
	
	/**
	 * General constructor that enables the stochastic implementation. NOTE: there is no random effect variability in this model.
	 * @param isParameterVariabilityEnabled = a boolean that enables the parameter variability
	 */
	public OfficialHarvestModel(boolean isParameterVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isParameterVariabilityEnabled);
		modelParametersLibrary = new HashMap<TreatmentType, OfficialHarvestSubmodel>();
		init();
		xVectorFactory = new FixedEffectVectorFactory();
	}
	
	/**
	 * Default constructor with all uncertainty sources disabled.
	 */
	public OfficialHarvestModel() {
		this(false);
	}
	
	/**
	 * This method sets a selector for the different treatments.
	 * @param selector an OfficialHarvestSubmodelSelector instance
	 */
	public void setOfficialHarvestSubmodelSelector(OfficialHarvestSubmodelSelector selector) {
		this.selector = selector;
	}
	
	/**
	 * This method provides access to the selector of treatments.
	 * @return an OfficialHarvestSubmodelSelector instance
	 */
	public OfficialHarvestSubmodelSelector getSelector() {return selector;}
	
	
	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
//			InputStream isParameters = ClassLoader.getSystemResourceAsStream(path + "parmsNew.txt");
			InputStream isParameters = getClass().getResourceAsStream("/" + path + "parmsNew.txt");
//			InputStream isOmega = ClassLoader.getSystemResourceAsStream(path + "covbNew.txt");
			InputStream isOmega = getClass().getResourceAsStream("/" + path + "covbNew.txt");
			String speciesPath = path + "species.csv";

			Map<TreatmentType, Matrix> betas = loadFileIntDoubleToMapIntMatrix(isParameters, false);		// is not a variance matrix 
			Map<TreatmentType, Matrix> variances = loadFileIntDoubleToMapIntMatrix(isOmega, true);			// is a variance matrix
			speciesMap = loadSpecies(speciesPath);			
			
			for (TreatmentType treatment : TreatmentType.values()) {
				if (betas.containsKey(treatment) && variances.containsKey(treatment)) {
					OfficialHarvestSubmodel calculatorForThisTreatment = new OfficialHarvestSubmodel(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
					calculatorForThisTreatment.setParameterEstimates(betas.get(treatment), variances.get(treatment));
					modelParametersLibrary.put(treatment, calculatorForThisTreatment);
				}
			}
			
		} catch (Exception e) {
			System.out.println("AutomatedHarvestModel.init() : Error while initializing the module!");
			e.printStackTrace();
		}
	}	
	
	@Override
	public synchronized double predictEventProbability(OfficialHarvestableStand stand, OfficialHarvestableTree tree, Map<Integer, Object> parms) {
		Enum<?> treatment;
		int modifier;
		if (selector == null) {
			treatment = (Enum<?>) parms.get(DisturbanceParameter.ParmTreatment);
			modifier = (Integer) parms.get(DisturbanceParameter.ParmModulation);
		} else {
			treatment = selector.getMatch(stand.getPotentialVegetation());
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
			double xBeta = oXVector.multiply(modelParameters).m_afData[0][0];
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

//	@Override
//	public void clearDeviates() {
//		for (OfficialHarvestSubmodel p : modelParametersLibrary.values()) {
//			p.clearDeviates();
//		}
//	}

//	/**
//	 * For algorithm testing.
//	 * @param args
//	 */
//	public static void main (String[] args) {
//		try {
//			@SuppressWarnings("unused")
//			OfficialHarvestModel model = new OfficialHarvestModel();
//			@SuppressWarnings("unused")
//			int u = 0;
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Problems!!!");
//		}
//		System.out.println("Done.");
//	}
	
	
}

