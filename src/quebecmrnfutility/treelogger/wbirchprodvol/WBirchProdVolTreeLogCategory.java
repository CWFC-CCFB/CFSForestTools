/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package quebecmrnfutility.treelogger.wbirchprodvol;

import java.util.List;

import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class WBirchProdVolTreeLogCategory extends LogCategory {

	protected final double lengthM;
	protected final Double minimumSmallEndDiameterCm;
	protected final Double maximumDecayDiameterCm;
	protected final String eligibleLogGrade;
	
	private transient WBirchProdVolTreeLogCategoryPanel guiInterface;
	
	protected WBirchProdVolTreeLogCategory(String name,
			String speciesName, 
			String eligibleLogGrade,
			double lengthM, 
			Double minimumSmallEndDiameterCm, 
			Double maximumDecayDiameterCm) {
		super(name, false);
		this.setSpecies(speciesName);
		this.eligibleLogGrade = eligibleLogGrade;
		this.lengthM = lengthM;
		this.minimumSmallEndDiameterCm = minimumSmallEndDiameterCm;
		this.maximumDecayDiameterCm = maximumDecayDiameterCm;
	}

	@Override
	public WBirchProdVolTreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new WBirchProdVolTreeLogCategoryPanel(this);
		} 
		return guiInterface;
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}


	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		return 1d;
	}

	/*
	 * Useless for this module (non-Javadoc)
	 * @see repicea.simulation.treelogger.LogCategory#extractFromTree(repicea.simulation.treelogger.LoggableTree, java.lang.Object[])
	 */
	@Override
	protected List<WoodPiece> extractFromTree(LoggableTree tree, Object... parms) {
		return null;
	}

}
