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
package quebecmrnfutility.predictor.formerofficialharvestmodule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import quebecmrnfutility.predictor.officialharvestmodule.TreatmentEnum;
import repicea.math.Matrix;
import repicea.simulation.REpiceaLogisticPredictor;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;

/**
 * This method implement an automated harvest model that applies to Quebec's commercial forest.
 * Based on database provided by Gordon Weber on March 1st, 2010.
 * @author M. Fortin - August 2010
 */
@Deprecated
public class FormerOfficialHarvestModel extends REpiceaLogisticPredictor<FormerOfficialHarvestableStand, FormerOfficialHarvestableTree> {
	
	private static final long serialVersionUID = 20100905L;

	/**
	 * This Enum class contains all the available treatment for this harvester.
	 * @author M. Fortin - September 2010
	 */
	@Deprecated
	public static enum Treatment implements TreatmentEnum {
		CA("Sanitary harvesting", "Coupe d'am\u00E9lioration"), 
		CE("Crop tree harvesting", "Coupe d'\u00E9claircie"), 
		CJ("Selection cutting", "Coupe de jardinage"), 
		CP("Shelterwood cutting", "Coupe progressive"), 
		EC("Commercial thinning", "\u00C9claircie commerciale"), 
		ES("Selective thinning", "\u00C9claircie s\u00E9lective");
		
		Treatment(String englishText, String frenchText) {
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
	private Map<Treatment, FormerOfficialHarvestSubmodel> modelParametersLibrary;
	
	/**
	 * General constructor that enables the stochastic implementation. NOTE: there is no random effect variability in this model.
	 * @param isVariabilityEnabled = a boolean that enables the parameter variability
	 */
	@Deprecated
	public FormerOfficialHarvestModel(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);
		modelParametersLibrary = new HashMap<Treatment, FormerOfficialHarvestSubmodel>();
		init();
		xVectorFactory = new FixedEffectVectorFactory();
	}
	
	/**
	 * Default constructor with all uncertainty sources disabled.
	 */
	@Deprecated
	public FormerOfficialHarvestModel() {
		this(false);
	}
	
	/**
	 * This method initializes the model parameters of this class
	 */
	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			InputStream isParameters = ClassLoader.getSystemResourceAsStream(path + "parms.txt");
			InputStream isOmega = ClassLoader.getSystemResourceAsStream(path + "covb.txt");

			Map<Treatment, Matrix> betas = loadFileIntDoubleToMapIntMatrix(isParameters);
			Map<Treatment, Matrix> variances = loadFileIntDoubleToMapIntMatrix(isOmega); 
			
			for (Treatment treatment : Treatment.values()) {
				if (betas.containsKey(treatment) && variances.containsKey(treatment)) {
					FormerOfficialHarvestSubmodel calculatorForThisTreatment = new FormerOfficialHarvestSubmodel(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
					calculatorForThisTreatment.setDefaultBeta(betas.get(treatment), variances.get(treatment).squareSym());
					modelParametersLibrary.put(treatment, calculatorForThisTreatment);
				}
			}
			
		} catch (Exception e) {
			System.out.println("AutomatedHarvestModel.init() : Error while initializing the module!");
			e.printStackTrace();
		}
	}	
	
	
	@Override
	public synchronized double predictEventProbability(FormerOfficialHarvestableStand stand, FormerOfficialHarvestableTree tree, Object... parms) {
		Enum<?> treatment = (Enum<?>) parms[0];
		FormerOfficialHarvestSubmodel submodel = modelParametersLibrary.get(treatment);
		Matrix modelParameters = submodel.getSubParametersForThisStand(stand);
		oXVector = xVectorFactory.getFixedEffectVector(stand, tree, (Treatment) treatment);
		double xBeta = oXVector.multiply(modelParameters).m_afData[0][0];
		double fExpXBeta = Math.exp(xBeta);
		return fExpXBeta / (1.0 + fExpXBeta);
	}
	
	/**
	 * This method serves to load the parameters of the model.
	 */
	private Map<Treatment, Matrix> loadFileIntDoubleToMapIntMatrix(InputStream iStream) throws Exception {
		Map<Treatment, Matrix> oFinalMap = new HashMap<Treatment, Matrix>();
		Map<Treatment, Vector<Double>> oMap = new HashMap<Treatment, Vector<Double>>();

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
						Treatment treatment = Treatment.values()[modelVersion];
						
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
				
				for (Treatment treatment : oMap.keySet()) {
					Matrix oMatTmp = new Matrix(oMap.get(treatment));
					oFinalMap.put(treatment, oMatTmp);
				}
				return oFinalMap;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
	}

	
	@Override
	public void clearDeviates() {
		for (FormerOfficialHarvestSubmodel p : modelParametersLibrary.values()) {
			p.clearDeviates();
		}
	}

//	/**
//	 * For algorithm testing.
//	 * @param args
//	 */
//	public static void main (String[] args) {
//		try {
//			@SuppressWarnings("unused")
//			FormerOfficialHarvestModel model = new FormerOfficialHarvestModel();
//			@SuppressWarnings("unused")
//			int u = 0;
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Problems!!!");
//		}
//		System.out.println("Done.");
//	}
	
	
}

