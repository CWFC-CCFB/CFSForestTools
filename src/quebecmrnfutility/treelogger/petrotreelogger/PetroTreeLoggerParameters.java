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
package quebecmrnfutility.treelogger.petrotreelogger;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.treelogger.petrotreelogger.PetroLoggableTree.PetroLoggerSpecies;
import quebecmrnfutility.treelogger.petrotreelogger.PetroTreeLogCategory.ProductType;
import repicea.simulation.treelogger.TreeLoggerParameters;

@SuppressWarnings("serial")
public final class PetroTreeLoggerParameters extends TreeLoggerParameters<PetroTreeLogCategory> {

	private transient PetroTreeLoggerParametersDialog guiInterface;
	
	/**
	 * Protected constructor. The only valid PetroTreeLoggerParameter instance is the one provided by the method 
	 * initializeDefaultCategory.
	 */
	protected PetroTreeLoggerParameters() {
		super(PetroTreeLogger.class);
	}

	/**
	 * This method is already called in the super constructor
	 */
	public void initializeDefaultLogCategories() {
		getLogCategories().clear();
		
		List<PetroTreeLogCategory> productList = new ArrayList<PetroTreeLogCategory>();
		getLogCategories().put(PetroLoggerSpecies.BOJ.name(), productList);
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F1, PetroLoggerSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F2, PetroLoggerSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F3, PetroLoggerSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F4, PetroLoggerSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_P, PetroLoggerSpecies.BOJ.name()));
		
		productList = new ArrayList<PetroTreeLogCategory>();
		getLogCategories().put(PetroLoggerSpecies.ERS.name(), productList);
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F1, PetroLoggerSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F2, PetroLoggerSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F3, PetroLoggerSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_F4, PetroLoggerSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(ProductType.PETRO_P, PetroLoggerSpecies.ERS.name()));
		getLogCategories().put(PetroLoggerSpecies.ERS.name(), productList);
		
		setFilename("");
	}

	@Override
	public boolean isCorrect() {return true;}		// the default log categories are always valid

	@Override
	public PetroTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new PetroTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	/*
	 * For testing.
	 */
	public static void main(String[] args) {
		PetroTreeLoggerParameters stlp = new PetroTreeLoggerParameters();
		stlp.initializeDefaultLogCategories();
		stlp.showUI(null);
		stlp.showUI(null);
		System.exit(0);
	}

}
