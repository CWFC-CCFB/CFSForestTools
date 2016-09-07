package quebecmrnfutility.predictor.loggradespetro;

import repicea.math.Matrix;

class Realization {
	final Matrix trueTau;
	final Matrix estTau;
	final Matrix estVarianceUncorr;
	final Matrix estVarianceCorr;
	
	Realization(Matrix trueTau, Matrix estTau, Matrix estVarianceUncorr, Matrix estVarianceCorr) {
		this.trueTau = trueTau;
		this.estTau = estTau;
		this.estVarianceUncorr = estVarianceUncorr;
		this.estVarianceCorr = estVarianceCorr;
	}
	
	Object[] getRecord() {
		Object[] record = new Object[4];
		record[0] = trueTau;
		record[1] = estTau;
		record[2] = estVarianceUncorr;
		record[3] = estVarianceCorr;
		return record;
	}
	
	
}
