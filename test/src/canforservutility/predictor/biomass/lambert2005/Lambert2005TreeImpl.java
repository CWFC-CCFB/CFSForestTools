package canforservutility.predictor.biomass.lambert2005;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;

public class Lambert2005TreeImpl implements Lambert2005Tree {

	Lambert2005Species species;
	double dbhcm;
	double hm;	
	
	Lambert2005TreeImpl(Lambert2005Species _species, double _dbhcm, double _hm) {
		species = _species;
		dbhcm = _dbhcm;
		hm = _hm;		
	}
	
	@Override
	public double getDbhCm() {
		// TODO Auto-generated method stub
		return dbhcm;
	}

	@Override
	public double getHeightM() {
		// TODO Auto-generated method stub
		return hm;
	}

	@Override
	public String getSubjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMonteCarloRealizationId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Lambert2005Species getLambert2005Species() {
		// TODO Auto-generated method stub
		return species;
	}
}
