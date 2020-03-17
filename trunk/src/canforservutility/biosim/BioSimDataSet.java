/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Wood Fibre Centre
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.biosim;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.List;

import canforservutility.biosim.BioSimEnums.Month;
import canforservutility.biosim.BioSimEnums.Variable;
import repicea.stats.data.DataSet;
import repicea.stats.data.Observation;

/**
 * Handles the server replies. This class derived from DataSet includes
 * a function that makes it possible to convert the DataSet instance into
 * a Map provided that there is no repeated entry. Should it be the case,
 * tjhe getMap method would throw an Exception.
 *
 * @author Mathieu Fortin - March 2020
 *
 */
public class BioSimDataSet extends DataSet {

	/**
	 * Only constructor with the field names.
	 * @param fieldNames a List of String instances
	 */
	public BioSimDataSet(List<String> fieldNames) {
		super(fieldNames);
	}

	/**
	 * Converts the DataSet instance into a Map. There should not be any deplicate entry. 
	 * Otherwise the method returns an Exception.
	 * @return a LinkedHashMap with embedded LinkedHashMap if there are more than two fields.
	 */
	public LinkedHashMap getMap() {
		LinkedHashMap outputMap = new LinkedHashMap();
		Object[] rec;
		LinkedHashMap currentMap;
		for (Observation obs : getObservations()) {
			rec = obs.toArray();
			currentMap = outputMap;
			for (int i = 0; i < rec.length - 1; i++) {
				if (i == rec.length - 2) {
					currentMap.put(rec[i], rec[i+1]);
				} else if (!currentMap.containsKey(rec[i])) {
					currentMap.put(rec[i], new LinkedHashMap());
					currentMap = (LinkedHashMap) currentMap.get(rec[i]);
				} else {
					throw new InvalidParameterException();
				}
			}
		}
		return outputMap;
	}
	
	final BioSimDataSet getMonthDataSet(List<Month> months, List<Variable> variables) {
		BioSimMonthMap monthMap = new BioSimMonthMap(this);
		return monthMap.getMeanForTheseMonths(months, variables);
	}

}
