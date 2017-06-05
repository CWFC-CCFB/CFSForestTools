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
package quebecmrnfutility.predictor.officialharvestmodule;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import repicea.gui.REpiceaShowableUIWithParent;
import repicea.gui.components.REpiceaTableModel;
import repicea.io.IOUserInterfaceableObject;
import repicea.io.GFileFilter.FileType;
import repicea.predictor.QuebecGeneralSettings;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.serial.xml.XmlSerializer;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class OfficialHarvestSubmodelSelector implements REpiceaShowableUIWithParent, 
														TableModelListener, 
														IOUserInterfaceableObject, 
														Memorizable {


//	public static enum BasicDefaultTreatment implements TextableEnum {
//		CPRS("Harvesting with soil and regeneration protection", "Coupe avec protection de la r\u00E9g\u00E9n\u00E9ration et des sols (CPRS)"),
//		CPPTM("Harvesting with advanced regeneration protection (HARP)", "Coupe avec protection des petites tiges marchandes (CPPTM)");
//		
//		BasicDefaultTreatment(String englishText, String frenchText) {
//			setText(englishText, frenchText);
//		}
//		
//		@Override
//		public void setText(String englishText, String frenchText) {
//			REpiceaTranslator.setString(this, englishText, frenchText);
//		}
//		
//		@Override
//		public String toString() {return REpiceaTranslator.getString(this);}
//	}
		
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
	
	
	
	
	protected final Map<String, Enum<?>> treatmentMatchMap;
	protected final List<Enum<?>> potentialTreatments;
	private String filename;
	protected Mode mode;
	protected Enum<?> singleTreatment;
	private transient OfficialHarvestSubmodelSelectorDialog guiInterface;

	/**
	 * Official constructor.
	 */
	public OfficialHarvestSubmodelSelector() {
		potentialTreatments = new ArrayList<Enum<?>>();
		mode = Mode.SingleTreatment;
		addPotentialTreatments(OfficialHarvestModel.TreatmentType.values());
		singleTreatment = potentialTreatments.get(potentialTreatments.size() - 1);
		treatmentMatchMap = new TreeMap<String, Enum<?>>();
		for (String potentialVegetation : QuebecGeneralSettings.POTENTIAL_VEGETATION_LIST) {
			treatmentMatchMap.put(potentialVegetation, potentialTreatments.get(potentialTreatments.size() - 1));
		}
	}
	
	protected Mode getMode() {return mode;}

	/**
	 * This method adds a potential treatment to the list of available treatments
	 * @param enumValues an array of enum variable 
	 */
	public void addPotentialTreatments(Enum<?>[] enumValues) {
		for (Enum<?> enumValue : enumValues) {
			if (!potentialTreatments.contains(enumValue)) {
				potentialTreatments.add(enumValue);
			}
		}
	}
	
	
	
	@Override
	public OfficialHarvestSubmodelSelectorDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new OfficialHarvestSubmodelSelectorDialog(this, (Window) parent);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		if (guiInterface != null && guiInterface.isVisible()) {
			return true;
		}
		return false;
	}

	@Override
	public void showUI(Window parent) {
		getUI(parent).setVisible(true);
	}

	
	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE) {
			if (e.getSource() instanceof REpiceaTableModel) {
				REpiceaTableModel model = (REpiceaTableModel) e.getSource();
				String potentialVegetation = (String) model.getValueAt(e.getLastRow(), 0);
				Enum<?> treatement = (Enum<?>) model.getValueAt(e.getLastRow(), 1);
				treatmentMatchMap.put(potentialVegetation, treatement);
			}
		}
	}


	@Override
	public void save(String filename) throws IOException {
		setFilename(filename);
		XmlSerializer serializer = new XmlSerializer(filename);
		try {
			serializer.writeObject(this);
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while saving the file!");
		}
	}
	
	private void setFilename(String filename) {this.filename = filename;}


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
	public FileFilter getFileFilter() {return FileType.XML.getFileFilter();}


	@Override
	public String getFilename() {return filename;}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add((Serializable) treatmentMatchMap);
		mp.add((Serializable) potentialTreatments);
		mp.add(mode);
		mp.add(singleTreatment);
		return mp;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		treatmentMatchMap.clear();
		treatmentMatchMap.putAll((Map) wasMemorized.get(0));
		potentialTreatments.clear();
		potentialTreatments.addAll((List) wasMemorized.get(1));
		mode = (Mode) wasMemorized.get(2);
		singleTreatment = (Enum<?>) wasMemorized.get(3);
	}

	protected Enum<?> getTreatment(String potentialVegetation) {
		if (mode == Mode.SingleTreatment) {
			return singleTreatment;
		} else {
			return treatmentMatchMap.get(potentialVegetation);
		}
	}
	
	
	public static void main(String[] args) {
		new OfficialHarvestSubmodelSelector().showUI(null);
	}

}
