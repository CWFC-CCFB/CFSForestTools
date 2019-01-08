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
