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
