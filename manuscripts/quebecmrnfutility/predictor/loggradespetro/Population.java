package quebecmrnfutility.predictor.loggradespetro;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import quebecmrnfutility.predictor.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import repicea.math.Matrix;
import repicea.stats.estimates.HorvitzThompsonTauEstimate;

public class Population {

	final PlotList sampleUnits;
	Random random = new Random();
	final PetroGradePredictor superModel = new PetroGradePredictor(false, true);	// no randomness on parameters since this is the superpopulation model
	
	Population(int populationSize) {
		sampleUnits = new PlotList();
		Plot p;
		for (int i = 0; i < populationSize; i++) {
			int nbTrees = (int) (2 + Math.floor(random.nextDouble() * 21d));
			p = new Plot(i);
			sampleUnits.add(p);
			double dbhCm;
			for (int j = 0; j < nbTrees; j++) {
				dbhCm = 24 + Math.floor(random.nextDouble() * 27d);
				p.addTree(new PetroGradeTreeImpl(PetroGradeSpecies.ERS, dbhCm));
			}
		}
	}
	
	PlotList getSample(int sampleSize) {
		List<Integer> sampleIndex = new ArrayList<Integer>();
		int index;
		while (sampleIndex.size() < sampleSize) {
			index = (int) Math.floor(random.nextDouble() * 1000);
			if (!sampleIndex.contains(index)) {
				sampleIndex.add(index);
			}
		}
		PlotList sample = new PlotList();
		for (Integer ind : sampleIndex) {
			sample.add(sampleUnits.get(ind));
		}
		return sample;
	}
	
	static void setRealizedValues(List<Plot> plots, PetroGradePredictor model) {
		for (Plot plot : plots) {
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				tree.setRealizedValues(model.getPredictedGradeVolumes(tree));
			}
		}
	}
	
	Matrix getTotal() {
		Matrix total = new Matrix(5,1);
		for (Plot plot : sampleUnits) {
			for (PetroGradeTreeImpl tree : plot.getTrees()) {
				total = total.add(tree.getRealizedValues());
			}
		}
		return total;
	}
	
	
	public static void main(String[] args) {
		int populationSize = 1000;
		Population pop = new Population(populationSize);
		int nbRealizations = 100;
		int nbInternalReal = 1000;
		int sampleSize = 10;
		for (int real = 0; real < nbRealizations; real++) {
			setRealizedValues(pop.sampleUnits, pop.superModel);
			Matrix total = pop.getTotal();
			PetroGradePredictor currentModel = new PetroGradePredictor(true, true); // the current model must account for the errors in the parameter estimates
			currentModel.replaceBeta();	// the parameter estimates are drawn at random in the distribution
			PlotList sample = pop.getSample(sampleSize);
			for (int internalReal = 0; internalReal < nbInternalReal; internalReal++) {
				sample.setRealization(internalReal);
				setRealizedValues(sample, currentModel);
				HorvitzThompsonTauEstimate estimate = sample.getHorvitzThompsonEstimate(populationSize);
				Matrix totalEstimated = estimate.getTotal();
				int u = 0;
			}
			System.out.println("Running realization " + real);
		}
	}
	
}
