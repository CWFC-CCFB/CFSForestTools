/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package quebecmrnfutility.treelogger.meristreelogger;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;

/**
 * A tree log category class for MerisTreeLogger.
 * @author Mathieu Fortin - December 2025
 */
@SuppressWarnings("serial")
public class MerisTreeLogCategory extends LogCategory {

	private transient MerisTreeLogCategoryPanel guiInterface;
	
	final List<String> speciesListInThisGroup;
	final boolean isBark;
	
	MerisTreeLogCategory(String logCategoryName, String speciesGroup) {
		super(logCategoryName, false);
		setSpecies(speciesGroup);
		isBark = logCategoryName.toLowerCase().contains("ecorce") || logCategoryName.toLowerCase().contains("\u00E9corce");
		speciesListInThisGroup = new ArrayList<String>();
	}
	
	void addSpecies(String species) {
		if (!speciesListInThisGroup.contains(species)) {
			speciesListInThisGroup.add(species);
		}
	}
	
	@Override
	public Component getUI() {
		if (guiInterface == null) {
			guiInterface = new MerisTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null ? guiInterface.isVisible() : false;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected List<? extends WoodPiece> extractFromTree(LoggableTree tree, Object... parms) {
		// TODO Auto-generated method stub
		return null;
	}

}
