/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.treelogger.petrotreelogger;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeType;
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
		getLogCategories().put(PetroGradeSpecies.BOJ.name(), productList);
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F1, PetroGradeSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F2, PetroGradeSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F3, PetroGradeSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F4, PetroGradeSpecies.BOJ.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_P, PetroGradeSpecies.BOJ.name()));
		
		productList = new ArrayList<PetroTreeLogCategory>();
		getLogCategories().put(PetroGradeSpecies.ERS.name(), productList);
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F1, PetroGradeSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F2, PetroGradeSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F3, PetroGradeSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_F4, PetroGradeSpecies.ERS.name()));
		productList.add(new PetroTreeLogCategory(PetroGradeType.PETRO_P, PetroGradeSpecies.ERS.name()));
		getLogCategories().put(PetroGradeSpecies.ERS.name(), productList);
		
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
