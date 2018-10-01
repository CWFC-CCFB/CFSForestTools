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

import quebecmrnfutility.predictor.loggradespetro.PetroGradeTree.PetroGradeType;
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
	protected WoodPiece extractFromTree(LoggableTree tree, Object... parms) {
		return null;
	}
	
}
