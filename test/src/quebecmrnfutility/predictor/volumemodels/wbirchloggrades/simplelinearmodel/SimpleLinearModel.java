package quebecmrnfutility.predictor.volumemodels.wbirchloggrades.simplelinearmodel;

import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.distributions.ChiSquaredDistribution;
import repicea.stats.estimates.GaussianErrorTermEstimate;

@SuppressWarnings("serial")
class SimpleLinearModel extends REpiceaPredictor {

	private ChiSquaredDistribution distributionForVCovRandomDeviates;
	
	protected static boolean R2_95Version = false;
	
	protected SimpleLinearModel(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		init();
	}

	@Override
	protected void init() {
		Matrix beta = new Matrix(2,1);
		beta.setValueAt(0, 0, 4d);
		beta.setValueAt(1, 0, 3d);
		Matrix omega = new Matrix(2,2);
		omega.setValueAt(0, 0, 0.025);
		omega.setValueAt(1, 1, 0.0005);
		omega.setValueAt(0, 1, Math.sqrt(omega.getValueAt(0, 0) * omega.getValueAt(1, 1)) * .1);
		omega.setValueAt(1, 0, omega.getValueAt(0, 1));
		setParameterEstimates(new ModelParameterEstimates(beta, omega));
		Matrix residualVariance = new Matrix(1,1);
		if (R2_95Version) {
			residualVariance.setValueAt(0, 0, .284);			// to ensure a R2 of 0.95
		} else {
			residualVariance.setValueAt(0, 0, 2d);
		}
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(residualVariance));
		oXVector = new Matrix(1, beta.m_iRows);
	}
	
	protected double predictY(SamplePlot plot) {
		Matrix currentBeta = getParametersForThisRealization(plot);
		oXVector.resetMatrix();
		oXVector.setValueAt(0, 0, 1d);
		oXVector.setValueAt(0, 1, plot.getX());
		double pred = oXVector.multiply(currentBeta).getValueAt(0, 0);
		pred += getResidualError().getValueAt(0, 0) * Math.sqrt(plot.getX());
		return pred;
	}

	
	/*
	 * For manuscript purposes.
	 */
	void replaceModelParameters() {
		int degreesOfFreedom = 98;		// assumption of 100 observations - 2 parameters
		Matrix newMean = getParameterEstimates().getRandomDeviate();
		Matrix variance = getParameterEstimates().getVariance();
		if (distributionForVCovRandomDeviates == null) {
			distributionForVCovRandomDeviates = new ChiSquaredDistribution(degreesOfFreedom, variance);
		}
		Matrix newVariance = distributionForVCovRandomDeviates.getRandomRealization();
		setParameterEstimates(new ModelParameterEstimates(newMean, newVariance));
		
		Matrix residualVariance = this.getDefaultResidualError(ErrorTermGroup.Default).getVariance();
		ChiSquaredDistribution residualVarianceDistribution = new ChiSquaredDistribution(degreesOfFreedom, residualVariance);
		Matrix newResidualVariance = residualVarianceDistribution.getRandomRealization();
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(newResidualVariance));
	}
	
	
	void replaceModelParameters(PlotList sample) {
		int degreesOfFreedom = sample.size() - 2;
		Matrix matX = new Matrix(sample.size(), 2);
		Matrix matY = new Matrix(sample.size(), 1);
		Matrix matW = new Matrix(sample.size(), sample.size());
		for (int i = 0; i < sample.size(); i++) {
			SamplePlot plot = sample.get(i);
			matX.setValueAt(i, 0, 1d);
			matX.setValueAt(i, 1, plot.getX());
			matY.setSubMatrix(plot.getY(), i, 0);
			matW.setValueAt(i, i, plot.getX());
		}
		Matrix invW = matW.getInverseMatrix();
		Matrix invXWX = matX.transpose().multiply(invW).multiply(matX).getInverseMatrix();
		Matrix newMean = invXWX.multiply(matX.transpose()).multiply(invW).multiply(matY);
		Matrix res = matY.subtract(matX.multiply(newMean));
		Matrix newResidualVariance = res.transpose().multiply(invW).multiply(res).scalarMultiply(1d/(degreesOfFreedom));
		Matrix newVariance = invXWX.scalarMultiply(newResidualVariance.getValueAt(0, 0));
		
		setParameterEstimates(new ModelParameterEstimates(newMean, newVariance));
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(newResidualVariance));
	}


}
