package quebecmrnfutility.predictor.stemtaper;

import java.util.Vector;

import quebecmrnfutility.predictor.stemtaper.StemTaperHeightSection;
import quebecmrnfutility.predictor.stemtaper.StemTaperStand;
import quebecmrnfutility.predictor.stemtaper.StemTaperTree;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

@SuppressWarnings("deprecation")
public class StemTaperTreeImpl implements StemTaperTree {

	private double dbh;
	private double height;
	private Vector<StemTaperHeightSection> heightSections;
	
	protected StemTaperTreeImpl(double dbh, double height) {
		this.dbh = dbh;
		this.height = height;
		this.heightSections = new Vector<StemTaperHeightSection>();
	}

	@Override
	public double getDbh() {return this.dbh;}

	@Override
	public double getDbh2() {return getDbh() * getDbh();}

	@Override
	public double getHeight() {return height;}

	@Override
	public Vector<StemTaperHeightSection> getHeightSections() {return heightSections;}

	@Override
	public void addHeightSection(StemTaperHeightSection heightSection) {getHeightSections().add(heightSection);}

//	@Override
//	public Object getSubjectPlusMonteCarloSpecificId() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public int getSubjectId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StemTaperStand getStand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMonteCarloRealizationId(int i) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getMonteCarloRealizationId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
