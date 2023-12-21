/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2015 Gouvernement du Quebec 
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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;

public class ParameterLoaderExt extends ParameterLoader {
	/**
	 * This method reads the file and retrieve a matrix of parameters.
	 * 
	 * @param filename the path of the file to be read (*.csv)
	 * @param startFieldIndex column index to start reading. Every column before
	 *            that will
	 *            be dismiss.
	 * @return a Matrix instance
	 * @throws IOException if something goes wrong while reading the file
	 */
	public static Matrix loadMatrixFromFile(final String filename, final int startFieldIndex) throws IOException {
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			final Matrix omega = new Matrix(reader.getRecordCount(), reader.getFieldCount() - startFieldIndex);
			Object[] lineRead = reader.nextRecord();
			int record = 0;
			while (lineRead != null) {
				for (int i = 0; i < reader.getFieldCount() - startFieldIndex; i++) {
					final double parameter = Double.parseDouble(lineRead[i + startFieldIndex].toString());
					omega.setValueAt(record, i, parameter);
				}
				lineRead = reader.nextRecord();
				record++;
			}
			reader.close();
			return omega;
		} catch (final Exception e) {
			if (reader != null) {
				reader.close();
			}
			throw new IOException("ParameterLoader.loadMatrixFromFile() : Unable to read table");
		}
	}

	/**
	 * This method reads a file and retrieve a vector of parameters.
	 * 
	 * @param filename the path of the file to be read (*.csv)
	 * @param indexCol index of the column you want to read
	 * @return a List of Double or String
	 * @throws IOException if something goes wrong while reading the file.
	 */
	public static <T extends Object> List<T> loadColumnVectorFromFile(final String filename, final int indexCol, Class<T> type) throws IOException {
		List<T> value = new ArrayList<T>();
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] lineRead = reader.nextRecord();
			while (lineRead != null) {
				if (lineRead[indexCol] != null) {
					String paramStr = lineRead[indexCol].toString();
					if (paramStr.trim().length() != 0) {
						if (Number.class.isAssignableFrom(type)) {
							final double parameter = Double.parseDouble(paramStr);
							value.add(type.cast(parameter));
						} else if (type.isInstance(new String())) {
							value.add(type.cast(paramStr));
						}
					}
				}
				lineRead = reader.nextRecord();
			}
			reader.close();
			return value;
		} catch (final Exception e) {
			if (reader != null) {
				reader.close();
			}
			throw new IOException("ParameterLoader.loadColumnVectorFromFile() : Unable to read table");
		}
	}

}
