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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades.simplelinearmodel;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;

class SamplePlot implements MonteCarloSimulationCompliantObject, Cloneable {

	private final String id;
	private final double x;
	private double y;
	private int realization;
	
	SamplePlot(String id, double x) {
		this.id = id;
		this.x = x;
	}
	
	protected double getX() {return x;}
	
	protected Matrix getY() {
		Matrix pred = new Matrix(1,1);
		pred.setValueAt(0, 0, y);
		return pred;
	}
	
	protected void setY(double y) {this.y = y;}

	@Override
	public String getSubjectId() {
		return id;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.PLOT;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return realization;
	}
	
	protected void setMonteCarloRealizationId(int realization) {this.realization = realization;}
	
	@Override
	public SamplePlot clone() throws CloneNotSupportedException {
		return (SamplePlot) super.clone();
	}
}
