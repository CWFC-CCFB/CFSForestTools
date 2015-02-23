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
package quebecmrnfutility.predictor.stemtaper;

/**
 * This class simply describe a height section in tree. It includes a height and a diameter. It also implements the Comparable
 * interface in order to sort the section within a given tree.
 * @author Mathieu Fortin - July 2011
 */
@SuppressWarnings("rawtypes")
@Deprecated
public class StemTaperHeightSection implements Comparable {

	private double height;
	private double diameter;

	/**
	 * Constructor.
	 * @param height the height of the section along the bole (m)
	 * @param diameter the diameter of the section (mm)
	 */
	public StemTaperHeightSection(double height, double diameter) {
		this.height = height;
		this.diameter = diameter;
	}

	@Override
	public int compareTo(Object arg0) {
		StemTaperHeightSection otherSection = (StemTaperHeightSection) arg0;
		if (height < otherSection.height) {
			return -1;
		} else if (height == otherSection.height) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * This method returns the height of the section (m).
	 * @return a double
	 */
	public double getSectionHeight() {return height;}
	
	/**
	 * This method returns the diameter of the section (mm).
	 * @return a double
	 */
	public double getSectionDiameter() {return diameter;}
	
}
