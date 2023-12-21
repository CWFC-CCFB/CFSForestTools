/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge Epicea
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
package quebecmrnfutility.util;

import java.security.InvalidParameterException;
import java.util.List;

import repicea.serial.DeprecatedObject;


/**
 * This class implement most of the basic function in linear algebra
 * Authors: Jean-Francois Lavoie and Mathieu Fortin (June 2009)
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
				newMat.setValueAt(i, j, m_afData[i][j]);
			}
		}
		return newMat;
	}
	
	
}
