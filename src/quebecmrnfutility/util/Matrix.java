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
package quebecmrnfutility.util;

import java.security.InvalidParameterException;
import java.util.List;

import repicea.serial.DeprecatedObject;


/**
 * This class implement most of the basic function in linear algebra
 * Authors: Jean-Fran�ois Lavoie and Mathieu Fortin (June 2009)
 */
@Deprecated
class Matrix implements DeprecatedObject {

	private static final long serialVersionUID = 20100804L;
	
	public double[][] m_afData;
	public int m_iRows;
	public int m_iCols;
	
	/**
	 * Constructor. Creates a matrix from a two-dimension array.
	 */
	Matrix(double data[][]) {
		this(data.length, data[0].length);
		for (int i = 0; i < m_iRows; i++)
			for (int j = 0; j < m_iCols; j++)
				m_afData[i][j] = data[i][j];
	}
	
	/**
	 * Constructor. Creates a row vector from an array of double
	 * @param data an array of double instances.
	 */
	Matrix(double data[]) {
		this(data.length, 1);
		for (int i = 0; i < m_iRows; i++)
			m_afData[i][0] = data[i];
	}
	
	/**
	 * Constructor. Creates a row vector with all the values found in the parameter.
	 * @param list a List instance
	 */
	@SuppressWarnings("rawtypes")
	Matrix(List list) {
		this(list.size(), 1);
		if (!(list.get(0) instanceof Number)) {
			throw new InvalidParameterException("The list should contain Number instances!");
		}
		Number number;
		for (int i = 0; i < m_iRows; i++) {
			number = (Number) list.get(i);
			m_afData[i][0] = number.doubleValue();
		}
	}

	
	/**
	 * Constructor. Creates a matrix with all elements set to 0.
	 * @param iRows number of rows
	 * @param iCols number of columns
	 */
	Matrix(int iRows, int iCols) {
		m_afData = new double[iRows][iCols];
		m_iRows = iRows;
		m_iCols = iCols;
	}
	
	/**
	 * Constructor
	 * @param iRows number of rows
	 * @param iCols number of columns
	 * @param from first element of the matrix
	 * @param iIncrement increment for the next elements
	 */
	Matrix(int iRows, int iCols, double from, double iIncrement) {
		this(iRows,iCols);
		double value = from;
		for (int i = 0; i < m_iRows; i++) {
			for (int j = 0; j < m_iCols; j++) {
				m_afData[i][j] = value;
				value += iIncrement;
			}
		}
	}
		

	@Override
	public Object convertIntoAppropriateClass() {
		repicea.math.Matrix newMat = new repicea.math.Matrix(m_iRows, m_iCols);
		for (int i = 0; i < m_iRows; i++) {
			for (int j= 0; j < m_iCols; j++) {
				newMat.m_afData[i][j] = m_afData[i][j];
			}
		}
		return newMat;
	}
	
	
}
