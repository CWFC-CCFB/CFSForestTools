package manuscript.stemtaperproject;

import java.util.Vector;

import quebecmrnfutility.predictor.stemtaper.StemTaperHeightSection;
import quebecmrnfutility.predictor.stemtaper.StemTaperStand;
import quebecmrnfutility.predictor.stemtaper.StemTaperTree;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

public class StemTaperTreeImpl implements StemTaperTree {

	private double dbh;
	private double height;
	private Vector<StemTaperHeightSection> heightSections;
	private int monteCarloID;
	private StemTaperStand stand;
	
	protected StemTaperTreeImpl(double dbh, double height) {
		this.dbh = dbh;
		this.height = height;
		this.heightSections = new Vector<StemTaperHeightSection>();
		stand = new StemTaperStandImpl();
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

	@Override
	public StemTaperStand getStand() {
		return stand;
	}

//	@Override
//	public Object getSubjectPlusMonteCarloSpecificId() {
//		return toString() + monteCarloID;
//	}

	@Override
	public int getSubjectId() {
		return this.hashCode();
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Tree;}

	@Override
	public void setMonteCarloRealizationId(int i) {
		monteCarloID = i;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloID;
	}

}
