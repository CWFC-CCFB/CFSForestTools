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
package quebecmrnfutility.predictor.thinners.betaharvestmodule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import quebecmrnfutility.predictor.thinners.betaharvestmodule.BetaHarvestableTree.BetaHarvestableSpecies;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.TreatmentEnum;
import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.simulation.thinners.REpiceaThinner;
import repicea.simulation.thinners.REpiceaTreatmentDefinition;
import repicea.stats.StatisticalUtility;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;

/**
 * This method implement a preliminary harvest module. Not to be used on a practical basis. Just for tests.
 * @author Mathieu Fortin - October 2009
 */
@Deprecated
public final class BetaHarvestModel extends REpiceaThinner<BetaHarvestableStand, BetaHarvestableTree>{
	
	private static final long serialVersionUID = 20100905L;


	/**
	 * This Enum class defines the possible treatments in this harvest module.
	 * @author M. Fortin - Septembre 2010
	 */
	public enum Treatment implements TreatmentEnum {
		COUPE_JARDINAGE("Selection cutting", "Coupe de jardinage"),
		COUPE_PROGRESSIVE("Shelterwood cutting", "Coupe progressive"),
		ECLAIRCIE_COMMERCIALE("Commercial thinning", "\u00C9claircie commerciale"),
		ECLAIRCIE_SELECTIVE("Selective thinning", "\u00C9claircie s\u00E9lective");
		
		private Matrix dummy;
		
		Treatment(String englishText, String frenchText) {
			setText(englishText, frenchText);
			dummy = new Matrix(1,4);
			dummy.setValueAt(0, ordinal(), 1d);
		}
		
		public Matrix getDummy() {return dummy;}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}

		@Override
		public String getCompleteName() {
			return getClass().getName().concat(".") + name();
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

	}
	
	private final Map<BetaHarvestableSpecies, Map<Treatment, Matrix>> speciesTreatmentDummy;
	@SuppressWarnings("unused")
	private boolean isCorrectlyInitialized;

	/**
	 * General constructor that enables the stochastic implementation. NOTE: there is no random effect variability in this model.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	@Deprecated
	public BetaHarvestModel(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);
		isCorrectlyInitialized = false;
		speciesTreatmentDummy = new TreeMap<BetaHarvestableSpecies, Map<Treatment, Matrix>>();
		init();
		oXVector = new Matrix(1,getParameterEstimates().getMean().m_iRows);
		isCorrectlyInitialized = true;
	}
	
	/**
	 * Default constructor with all uncertainty sources disabled.
	 */
	@Deprecated
	public BetaHarvestModel() {
		this(false);
	}
	
	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_HarvestGenericBeta.csv";
			String omegaFilename = path + "0_HarvestGenericOmega.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			
			setParameterEstimates(new SASParameterEstimates(defaultBetaMean, defaultBetaVariance)); 
			
			Vector<Integer> index = new Vector<Integer>();
			index.add(31);						// an element that has to be omitted because there was no occurrence in the fitting data (PEU x CP)
			for (BetaHarvestableSpecies species : BetaHarvestableSpecies.values()) {
				Matrix spDummy = species.getDummy();
				Map<Treatment, Matrix> oMap = new TreeMap<Treatment, Matrix>();
				for (Treatment trt : Treatment.values()) {
					Matrix spTrtDummy = StatisticalUtility.combineMatrices(spDummy, trt.getDummy()).removeElements(index);
					oMap.put(trt, spTrtDummy);
				}
				speciesTreatmentDummy.put(species, oMap);
			}
		} catch (Exception e) {
			System.out.println("GeneralHarvestModel.init() : Unable to initialize the general harvest module");
		}
	}	
	
		
	
	@Override
	public synchronized double predictEventProbability(BetaHarvestableStand stand, BetaHarvestableTree t, Map<String, Object> parms) {
		Enum<?> treatment = (Enum<?>) parms.get(DisturbanceParameter.ParmTreatment);
		Matrix modelParameters = getParametersForThisRealization(stand);
		double basalAreaHA = stand.getBasalAreaM2Ha();
		double densityHA = stand.getNumberOfStemsHa();

		oXVector.resetMatrix();
		int pointeur = 0;
		BetaHarvestableSpecies species = t.getBetaHarvestableTreeSpecies();
		double dbh = t.getDbhCm();
		int dummy_dbh = (dbh < 23.1)? 0 : 1;
		
		double lndbhHD = t.getLnDbhCmPlus1();
		Matrix dummySpecies = species.getDummy();
		Map<Treatment, Matrix> treatmentDummyMap = speciesTreatmentDummy.get(species);
		Matrix dummySpeciesTreatment = treatmentDummyMap.get(treatment);
			
		oXVector.setValueAt(0, pointeur, 1d);
		pointeur++;

		oXVector.setSubMatrix(dummySpecies, 0, pointeur);
		pointeur += dummySpecies.m_iCols;
		
		oXVector.setValueAt(0, pointeur, basalAreaHA);
		pointeur++;
		
		oXVector.setValueAt(0, pointeur, basalAreaHA * dummy_dbh);
		pointeur++;

		oXVector.setValueAt(0, pointeur, densityHA * dummy_dbh);
		pointeur++;
		
		oXVector.setSubMatrix(dummySpeciesTreatment.scalarMultiply(dbh), 0, pointeur);
		pointeur += dummySpeciesTreatment.m_iCols;
		
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(dbh * dummy_dbh), 0, pointeur);
		pointeur += dummySpecies.m_iCols;
		
		oXVector.setSubMatrix(dummySpeciesTreatment.scalarMultiply(lndbhHD), 0, pointeur);
		pointeur += dummySpeciesTreatment.m_iCols;
		
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lndbhHD * dummy_dbh), 0, pointeur);
		pointeur += dummySpecies.m_iCols;

		double xBeta = oXVector.multiply(modelParameters).getValueAt(0, 0);
		
		double fExpXBeta = Math.exp(xBeta);
		return fExpXBeta / (1.0 + fExpXBeta);
		
	}

	@Override
	public REpiceaTreatmentDefinition getTreatmentDefinitionForThisHarvestedStand(BetaHarvestableStand stand) {return null;}

	@Override
	public List<Enum> getTreatmentList() {
		return Arrays.asList(Treatment.values());
	}
	

//	/**
//	 * For testing purpose.
//	 */
//	public static void main (String[] args) {
//		try {
//			BetaHarvestModel test = new BetaHarvestModel();
//			if (!test.isCorrectlyInitialized) {
//				throw new Exception();
//			}
//			System.out.println("Module loads without problems.");
//		} catch (Exception e) {
//			System.out.println("Problems while loading!!!!!");
//			e.printStackTrace();
//		}
//	}


}

