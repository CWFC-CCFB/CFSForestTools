package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;


import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.stemtaper.StemTaperCrossSection;
import repicea.simulation.stemtaper.StemTaperSegment;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.stats.integral.GaussLegendreQuadrature;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.stats.integral.TrapezoidalRule;

class StemTaperTreeImpl implements StemTaperTree {

	StemTaperStandImpl stand;
	StemTaperTreeSpecies species;
	double dbhcm;
	double heightm;
	
	StemTaperTreeImpl(StemTaperTreeSpecies species, StemTaperStandImpl stand, double dbhcm, double heightm) {
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
	public StemTaperStand getStand() {
		return stand;
	}

	@Override
	public double getDbhCm() {
		return dbhcm;
	}

	@Override
	public double getSquaredDbhCm() {
		return getDbhCm() * getDbhCm();
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
