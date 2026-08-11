/*
 * This file is part of the CFSForesttools library.
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
package allometry.stemtaper.schneider2013;


import java.util.List;

import repicea.math.Matrix;
import repicea.math.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.math.integral.GaussLegendreQuadrature;
import repicea.math.integral.TrapezoidalRule;
import repicea.simulation.stemtaper.StemTaperCrossSection;
import repicea.simulation.stemtaper.StemTaperSegment;
import repicea.simulation.stemtaper.StemTaperSegmentList;

class Schneider2013StemTaperTreeImpl implements Schneider2013StemTaperTree {

	Schneider2013StemTaperPlotImpl stand;
	StemTaperTreeSpecies species;
	double dbhcm;
	double heightm;
	
	Schneider2013StemTaperTreeImpl(StemTaperTreeSpecies species, Schneider2013StemTaperPlotImpl stand, double dbhcm, double heightm) {
		this.stand = stand;
		this.species = species;
		this.dbhcm = dbhcm;
		this.heightm = heightm;
	}
	
	
	@Override
	public String getSubjectId() {
		return "0";
	}

	@Override
	public Schneider2013StemTaperPlot getStand() {
		return stand;
	}

	@Override
	public double getDbhCm() {
		return dbhcm;
	}

	@Override
	public double getHeightM() {
		return heightm;
	}

	@Override
	public List<StemTaperCrossSection> getCrossSections() {
		return null;
	}


	@Override
	public StemTaperTreeSpecies getStemTaperTreeSpecies() {
		return species;
	}

	Matrix getHeightsForTaper() {
		return new Matrix(20,1,0.3,1); 
	}


	StemTaperSegmentList getSegments() {
		StemTaperSegment sts = new StemTaperSegment(0.3, 19.3, new TrapezoidalRule(1d));
		StemTaperSegmentList stemTaperSegments = new StemTaperSegmentList();
		stemTaperSegments.add(sts);
		return stemTaperSegments; 
	}
	
	StemTaperSegmentList getTrapezoidalBottomSegments() {
		StemTaperSegmentList stemTaperSegments = new StemTaperSegmentList();
		stemTaperSegments.add(new StemTaperSegment(0.3, 1.3, new TrapezoidalRule(.2)));
		stemTaperSegments.add(new StemTaperSegment(1.3, 2.3, new TrapezoidalRule(.2)));
		stemTaperSegments.add(new StemTaperSegment(2.3, 3.3, new TrapezoidalRule(.2)));
		stemTaperSegments.add(new StemTaperSegment(3.3, 4.3, new TrapezoidalRule(.2)));
		return stemTaperSegments; 
	}
	
	StemTaperSegmentList getGaussLegendreBottomSegments() {
		StemTaperSegmentList stemTaperSegments = new StemTaperSegmentList();
		stemTaperSegments.add(new StemTaperSegment(0.3, 1.3, new GaussLegendreQuadrature(NumberOfPoints.N5)));
		stemTaperSegments.add(new StemTaperSegment(1.3, 2.3, new GaussLegendreQuadrature(NumberOfPoints.N5)));
		stemTaperSegments.add(new StemTaperSegment(2.3, 3.3, new GaussLegendreQuadrature(NumberOfPoints.N5)));
		stemTaperSegments.add(new StemTaperSegment(3.3, 4.3, new GaussLegendreQuadrature(NumberOfPoints.N5)));
		return stemTaperSegments; 
	}

	


	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}


	
}
