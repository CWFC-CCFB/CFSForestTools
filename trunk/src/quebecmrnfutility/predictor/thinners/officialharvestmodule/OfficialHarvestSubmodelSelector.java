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
import repicea.gui.components.REpiceaMatchSelector;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class OfficialHarvestSubmodelSelector extends REpiceaMatchSelector<OfficialHarvestModel.TreatmentType> {
		
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
		SilviculturalTreatment("Treatment", "Traitement");

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

	
	
//	protected final Map<String, Enum<?>> treatmentMatchMap;
//	protected final List<Enum<?>> potentialTreatments;
//	private String filename;
	protected Mode mode;
	protected Enum<OfficialHarvestModel.TreatmentType> singleTreatment;

	/**
	 * Official constructor.
	 */
	public OfficialHarvestSubmodelSelector() {
		super(QuebecGeneralSettings.POTENTIAL_VEGETATION_LIST.toArray(), 
				OfficialHarvestModel.TreatmentType.values(), 
				OfficialHarvestModel.TreatmentType.CPRS, 
				ColumnID.values());
		mode = Mode.SingleTreatment;
		singleTreatment = OfficialHarvestModel.TreatmentType.CPRS;
	}
	
	protected Mode getMode() {return mode;}

	/*
	 * For extended visibility only (non-Javadoc)
	 * @see repicea.gui.components.REpiceaMatchSelector#getPotentialMatches()
	 */
	@Override
	protected List<Enum<OfficialHarvestModel.TreatmentType>> getPotentialMatches() {
		return super.getPotentialMatches();
	}
	
//	/**
//	 * This method adds a potential treatment to the list of available treatments
//	 * @param enumValues an array of enum variable 
//	 */
//	public void addPotentialTreatments(Enum<?>[] enumValues) {
//		for (Enum<?> enumValue : enumValues) {
//			if (!potentialTreatments.contains(enumValue)) {
//				potentialTreatments.add(enumValue);
//			}
//		}
//	}
		
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
			InputStream is = ClassLoader.getSystemResourceAsStream(filename);
			if (is == null) {
				throw new IOException("The filename is not a file and cannot be converted into a stream!");
			} else {
				deserializer = new XmlDeserializer(is);
			}
		}
		OfficialHarvestSubmodelSelector newloadedInstance;
		try {
			newloadedInstance = (OfficialHarvestSubmodelSelector) deserializer.readObject();
			unpackMemorizerPackage(newloadedInstance.getMemorizerPackage());
			setFilename(filename);
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while loading the file!");
		}
	}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = super.getMemorizerPackage();
		mp.add(mode);
		mp.add(singleTreatment);
		return mp;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		super.unpackMemorizerPackage(wasMemorized);
		mode = (Mode) wasMemorized.get(2);
		singleTreatment = (Enum<OfficialHarvestModel.TreatmentType>) wasMemorized.get(3);
	}

	@Override
	public Enum<OfficialHarvestModel.TreatmentType> getMatch(Object potentialVegetation) {
		if (mode == Mode.SingleTreatment) {
			return singleTreatment;
		} else {
			return super.getMatch(potentialVegetation);
		}
	}
	
	
	public static void main(String[] args) {
		OfficialHarvestSubmodelSelector selector = new OfficialHarvestSubmodelSelector();
		selector.showUI(null);
		boolean cancelled = selector.getUI(null).hasBeenCancelled();
		System.out.println("The dialog has been cancelled : " + cancelled);
	}

}
