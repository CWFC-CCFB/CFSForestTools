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

import java.util.List;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeType;
import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;

public class PetroTreeLogCategory extends LogCategory {

	private static final long serialVersionUID = 20100804L;

	protected PetroGradeType productType;

	private transient PetroTreeLogCategoryPanel guiInterface;
	
	/**
	 * Constructor.
	 * @param productType
	 */
	protected PetroTreeLogCategory(PetroGradeType productType, String species) {
		super(productType.getName(), false);
		setSpecies(species);
		this.productType = productType;
	}

	public PetroGradeType getProductType() {
		return productType;
	}
	
	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		// TODO: find a more realistic value
		return 0.9;
	}

	@Override
	public PetroTreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new PetroTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	/*
	 * Useless for this logger (non-Javadoc)
	 * @see repicea.simulation.treelogger.LogCategory#extractFromTree(repicea.simulation.treelogger.LoggableTree, java.lang.Object[])
	 */
	@Override
	protected List<WoodPiece> extractFromTree(LoggableTree tree, Object... parms) {
		return null;
	}
	
}
