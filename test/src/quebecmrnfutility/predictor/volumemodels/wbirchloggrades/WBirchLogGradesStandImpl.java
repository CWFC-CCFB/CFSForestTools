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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades;

import java.util.HashMap;
import java.util.Map;

public class WBirchLogGradesStandImpl implements WBirchLogGradesStand {

	private final String plotID;
	private final double elevation;
	private final Map<Integer, WBirchLogGradesTreeImpl> trees;
	private int monteCarloId;
	
	WBirchLogGradesStandImpl(String plotID, double elevation) {
		this.plotID = plotID;
		this.elevation = elevation;
		this.trees = new HashMap<Integer, WBirchLogGradesTreeImpl>();
	}
	
	@Override
	public String getSubjectId() {return plotID;}

	protected void setMonteCarloRealizationId(int i) {monteCarloId = i;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloId;}

	@Override
	public double getElevationM() {return elevation;}
	
	public Map<Integer, WBirchLogGradesTreeImpl> getTrees() {return trees;}

	@Override
	public String toString() {
		return "Plot " + plotID;
	}

}
