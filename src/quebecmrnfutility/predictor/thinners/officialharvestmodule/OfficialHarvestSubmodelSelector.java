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
 * Copyright (C) 2009-2017 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import quebecmrnfutility.predictor.QuebecGeneralSettings;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.gui.components.REpiceaMatchSelector;
import repicea.serial.MemorizerPackage;
import repicea.serial.SerializerChangeMonitor;
import repicea.serial.xml.XmlDeserializer;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;

public class OfficialHarvestSubmodelSelector extends REpiceaMatchSelector<OfficialHarvestTreatmentDefinition> {
		
	static {
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestSubmodelSelector",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector");
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestSubmodelSelector$Mode",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector$Mode");
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel$TreatmentType",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel$TreatmentType");
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestSubmodelSelector$ColumnID",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector$ColumnID");
	}
	
	protected static enum Mode implements TextableEnum {
		SingleTreatment("Single treatment", "Traitement unique"),
		TreatmentByPotentialVegetation("Treatement by potential vegetation", "Traitement par v\u00E9g\u00E9tation potentielle");

		Mode(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	private static enum ColumnID implements TextableEnum {
		PotentialVegetation("Potential vegetation", "V\u00E9g\u00E9tation potentielle"),
		SilviculturalTreatment("Treatment", "Traitement"),
		DelayBeforeReentry("Time before next treatment (yrs)", "Temps avant le prochain traitement (ann\u00E9es)"),
		;

		ColumnID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	
	
	protected Mode mode;
	protected OfficialHarvestTreatmentDefinition singleTreatment;
	protected OfficialHarvestSubmodelAreaLimitation areaLimitations;

	/**
	 * Official constructor.
	 */
	public OfficialHarvestSubmodelSelector() {
		super(QuebecGeneralSettings.POTENTIAL_VEGETATION_LIST.toArray(), 
				OfficialHarvestTreatmentDefinition.getDefinitionOfAllAvailableTreatment(),
				ColumnID.values());
		
		mode = Mode.SingleTreatment;
		for (OfficialHarvestTreatmentDefinition def : getPotentialMatches()) {
			if (def.isTotalHarvest()) {
				singleTreatment = def.copy();
				break;
			}
		}
	}
	
	protected OfficialHarvestSubmodelAreaLimitation getAreaLimitations() {
		if (areaLimitations == null) {
			areaLimitations = new OfficialHarvestSubmodelAreaLimitation(TreatmentType.values());
		}
		return areaLimitations;
	}
	
	
	protected Mode getMode() {return mode;}

	/*
	 * For extended visibility only (non-Javadoc)
	 * @see repicea.gui.components.REpiceaMatchSelector#getPotentialMatches()
	 */
	@Override
	protected List<OfficialHarvestTreatmentDefinition> getPotentialMatches() {
		return super.getPotentialMatches();
	}
	
		
	@SuppressWarnings("unchecked")
	@Override
	public OfficialHarvestSubmodelSelectorDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new OfficialHarvestSubmodelSelectorDialog(this, (Window) parent, columnNames);
		}
		return (OfficialHarvestSubmodelSelectorDialog) guiInterface;
	}

	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer;
		try {
			deserializer = new XmlDeserializer(filename);
		} catch (Exception e) {
			InputStream is = getClass().getResourceAsStream("/" + filename);
			if (is == null) {
				throw new IOException("The filename is not a file and cannot be converted into a stream!");
			} else {
				deserializer = new XmlDeserializer(is);
			}
		}
		OfficialHarvestSubmodelSelector newloadedInstance;
		newloadedInstance = (OfficialHarvestSubmodelSelector) deserializer.readObject();
		unpackMemorizerPackage(newloadedInstance.getMemorizerPackage());
		setFilename(filename);
	}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = super.getMemorizerPackage();
		mp.add(mode);
		mp.add(singleTreatment);
		mp.add(areaLimitations);
		return mp;
	}


	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		super.unpackMemorizerPackage(wasMemorized);
		mode = (Mode) wasMemorized.get(2);
		OfficialHarvestTreatmentDefinition def = (OfficialHarvestTreatmentDefinition) wasMemorized.get(3);
		if (this.potentialMatches.contains(def)) {	// need to adjust to ensure backward compatibility here
			singleTreatment = def.copy();	
		} else {
			singleTreatment = def;
		}
		if (wasMemorized.size() >= 5) {
			getAreaLimitations().areaLimitationMap.clear();
			OfficialHarvestSubmodelAreaLimitation thatAreaLimitation = (OfficialHarvestSubmodelAreaLimitation) wasMemorized.get(4);
			if (thatAreaLimitation != null) {
				getAreaLimitations().areaLimitationMap.putAll(thatAreaLimitation.areaLimitationMap);
			}
		}
	}

	@Override
	public OfficialHarvestTreatmentDefinition getMatch(Object potentialVegetation) {
		if (mode == Mode.SingleTreatment) {
			return singleTreatment;
		} else {
			return super.getMatch(potentialVegetation);
		}
	}
	
	
	public static void main(String[] args) {
		REpiceaTranslator.setCurrentLanguage(Language.French);
		OfficialHarvestSubmodelSelector selector = new OfficialHarvestSubmodelSelector();
		selector.showUI(null);
		boolean cancelled = selector.getUI(null).hasBeenCancelled();
		System.out.println("The dialog has been cancelled : " + cancelled);
		System.exit(0);
	}

}
