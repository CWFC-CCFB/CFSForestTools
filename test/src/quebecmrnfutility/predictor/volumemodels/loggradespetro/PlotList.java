package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import java.util.ArrayList;

import repicea.math.Matrix;
import repicea.stats.estimates.PopulationTotalEstimate;
import repicea.stats.sampling.PopulationUnitWithUnequalInclusionProbability;

@SuppressWarnings("serial")
class PlotList extends ArrayList<Plot> {

	void setRealization(int id) {
		for (Plot plot : this) {
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				tree.setRealization(id);
			}
		}
	}
	
	PopulationTotalEstimate getHorvitzThompsonEstimate(int populationSize) {
		PopulationTotalEstimate estimate = new PopulationTotalEstimate();
		
		for (Plot plot : this) {
			Matrix plotTotal = new Matrix(5,1);
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				plotTotal = plotTotal.add(tree.getRealizedValues());
			}
			estimate.addObservation(new PopulationUnitWithUnequalInclusionProbability(plot.toString(), plotTotal, 1d/populationSize));
		}
		return estimate;
	}
	
}
