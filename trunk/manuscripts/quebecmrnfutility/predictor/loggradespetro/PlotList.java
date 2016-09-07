package quebecmrnfutility.predictor.loggradespetro;

import java.util.ArrayList;

import repicea.math.Matrix;
import repicea.stats.estimates.HorvitzThompsonTauEstimate;

@SuppressWarnings("serial")
class PlotList extends ArrayList<Plot> {

	void setRealization(int id) {
		for (Plot plot : this) {
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				tree.setRealization(id);
			}
		}
	}
	
	HorvitzThompsonTauEstimate getHorvitzThompsonEstimate(int populationSize) {
		HorvitzThompsonTauEstimate estimate = new HorvitzThompsonTauEstimate(populationSize);
		
		for (Plot plot : this) {
			Matrix plotTotal = new Matrix(5,1);
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				plotTotal = plotTotal.add(tree.getRealizedValues());
			}
			estimate.addObservation(plotTotal, 1d/populationSize);
		}
		return estimate;
	}
	
}
