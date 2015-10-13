/*
 * English version follows
 * 
 * Ce fichier fait partie de la bibliothèque mrnf-foresttools.
 * Il est protégé par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du ministère des Ressources naturelles et de la Faune du Gouvernement du 
 * Québec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Québec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Québec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Québec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.officialharvestmodule;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestableTree.OfficialHarvestableSpecies;
import repicea.math.Matrix;

/**
 * This private class handles all the effects and generates X vector required by the AutomatedHarvestedModel.
 * @author M. Fortin - August 2010
 */
class FixedEffectVectorFactory implements Serializable {
	
	private static final long serialVersionUID = 20101018L;

	private static enum Effect{
		Intercept,
		Species,
		OffsetDbh,
		OffsetDbhXDummyDbh,
		OffsetDbhXOffsetDbh,
		OffsetDbhSquared,
		OffsetDbhXOffsetDbhXDummyDbh,
		LogNbHa,
		SpeciesXLogNbHa,
		SpeciesXOffsetDbh,
		SpeciesXOffsetDbhXDummyDbh,
		SpeciesXOffsetDbhXOffsetDbh,
		SpeciesXOffsetDbhXOffsetDbhXDummyDbh,
		SpeciesXBasalAreaHa,
	}
	
	private Map<TreatmentType, ArrayList<Effect>> effects;
	private Matrix xVector = new Matrix(1,200);
	
	protected FixedEffectVectorFactory() {
		init();
	}
	
	private void init() {
		effects = new HashMap<TreatmentType, ArrayList<Effect>>();
		ArrayList<Effect> effectList = null;
		for (TreatmentType treatment : TreatmentType.values()) {
			switch (treatment) {
			case CA:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.OffsetDbhXOffsetDbh);
				effectList.add(Effect.OffsetDbhXOffsetDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case CE:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.OffsetDbhXDummyDbh);
				effectList.add(Effect.OffsetDbhXOffsetDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case CJ_1997:
			case CJ_2004:
			case CJMSCR:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case CP:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.OffsetDbhXOffsetDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case EC:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				break;
			case ES:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.OffsetDbh);
				effectList.add(Effect.OffsetDbhXOffsetDbh);
				break;
			case CJ_2004CERF:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case CJMSCRCERF:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case CP_35:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.OffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);				
				effectList.add(Effect.LogNbHa);
				break;
			case CP_45:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbhXDummyDbh);				
				effectList.add(Effect.LogNbHa);
				break;
			case CPI_CP:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);				
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);				
				effectList.add(Effect.SpeciesXLogNbHa);
				break;
			case CPI_RL:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXBasalAreaHa);
				break;
			case CRS:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.SpeciesXOffsetDbh);				
				effectList.add(Effect.OffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXLogNbHa);							
				break;
			case CJP:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.OffsetDbh);				
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.LogNbHa);							
				break;
			case CJPG_QM:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);				
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.LogNbHa);							
				break;
			case CPI_CP_CIMOTF:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXOffsetDbh);				
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);	
				break;
			case CPI_RL_CIMOTF:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.OffsetDbh);				
				effectList.add(Effect.SpeciesXOffsetDbhXDummyDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbh);
				effectList.add(Effect.SpeciesXOffsetDbhXOffsetDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);							
				break;
			}
			effects.put(treatment, effectList);
		}
	}
	
	/**
	 * This method returns the xVector corresponding to a particular combination of tree, stand and treatment.
	 */
	protected Matrix getFixedEffectVector(OfficialHarvestableStand stand, 
			OfficialHarvestableTree tree,
			TreatmentType treatment) {
	
		xVector.resetMatrix();
		int pointer = 0;
		ArrayList<Effect> effectsToBeSet = effects.get(treatment);
		
		for (Effect effect : effectsToBeSet) {
			pointer = setEffect(tree, stand, effect, pointer, treatment);
		}
	
		return xVector.getSubMatrix(0, 0, 0, pointer - 1);
	}
	
	
	/**
	 * This private method sets a particular effect into the XVector and returns the new position 
	 * of the vector.
	 */
	private int setEffect(OfficialHarvestableTree tree,
			OfficialHarvestableStand stand, 
			Effect effect, 
			int pointer,
			TreatmentType treatment) {
		
		int dummyDbh;
		int output = 0;
		
		
		OfficialHarvestableSpecies species = tree.getOfficialHarvestableTreeSpecies(treatment);
		double dbh = tree.getDbhCm();
		double offsetDbh =  dbh - 23;
		double offsetDbh2 = tree.getSquaredDbhCm() - 2 * 23 * dbh + 23 * 23;
		double numberOfStemsHa = stand.getNumberOfStemsHa();
		double basalArea = stand.getBasalAreaM2Ha();
		
		switch (effect) {
		
		case Intercept:
			xVector.m_afData[0][pointer] = 1d;
			output = 1;
			break;
	
		case Species:
			xVector.setSubMatrix(species.getDummy(treatment), 0, pointer);
			output = species.getDummy(treatment).m_iCols;
			break;

		case SpeciesXOffsetDbh:
			xVector.setSubMatrix(species.getDummy(treatment).scalarMultiply(offsetDbh), 0, pointer);
			output = species.getDummy(treatment).m_iCols;
			break;

		case SpeciesXOffsetDbhXDummyDbh:
			dummyDbh = (offsetDbh > 0d) ? 1 : 0;
			if (dummyDbh !=0) {
				xVector.setSubMatrix(species.getDummy(treatment).scalarMultiply(offsetDbh), 0, pointer);
			}
			output = species.getDummy(treatment).m_iCols;
			break;

		case SpeciesXOffsetDbhXOffsetDbh:
			xVector.setSubMatrix(species.getDummy(treatment).scalarMultiply(offsetDbh2), 0, pointer);
			output = species.getDummy(treatment).m_iCols;
			break;
			
		case SpeciesXOffsetDbhXOffsetDbhXDummyDbh:
			dummyDbh = (offsetDbh > 0d) ? 1 : 0;
			if (dummyDbh !=0) {
				xVector.setSubMatrix(species.getDummy(treatment).scalarMultiply(offsetDbh2), 0, pointer);
			}
			output = species.getDummy(treatment).m_iCols;
			break;
			
		case OffsetDbh:
			xVector.m_afData[0][pointer] = offsetDbh;
			output = 1;
			break;
			
		case OffsetDbhXDummyDbh:
			dummyDbh = (offsetDbh > 0d) ? 1 : 0;
			if (dummyDbh !=0) {
				xVector.m_afData[0][pointer] = offsetDbh;
			}
			output = 1;
			break;

		case OffsetDbhXOffsetDbh:
			xVector.m_afData[0][pointer] = offsetDbh2;
			output = 1;
			break;

		case OffsetDbhXOffsetDbhXDummyDbh:
			dummyDbh = (offsetDbh > 0d) ? 1 : 0;
			if (dummyDbh !=0) {
				xVector.m_afData[0][pointer] = offsetDbh2;
			}
			output = 1;
			break;
			
		case LogNbHa:
			double logNbHa = Math.log(numberOfStemsHa + 1);
			xVector.m_afData[0][pointer] = logNbHa;
			output = 1;
			break;
		case SpeciesXLogNbHa:
			logNbHa = Math.log(numberOfStemsHa + 1);
			xVector.setSubMatrix(species.getDummy(treatment).scalarMultiply(logNbHa), 0, pointer);
			output = species.getDummy(treatment).m_iCols;
			break;
		case SpeciesXBasalAreaHa:
			xVector.setSubMatrix(species.getDummy(treatment).scalarMultiply(basalArea), 0, pointer);
			output = species.getDummy(treatment).m_iCols;
			break;
//		case NbHaXDummyDbh:
//			dummyDbh = (offsetDbh > 0d) ? 1 : 0;
//			if (dummyDbh !=0) {
//				xVector.m_afData[0][pointer] = numberOfStemsHa;
//			}
//			output = 1;
//			break;
		
		
		default:
			throw new InvalidParameterException("Unknown effect : " + effect.name());
			
		}
		
		return pointer += output;
	}
	
	
	
	
}
