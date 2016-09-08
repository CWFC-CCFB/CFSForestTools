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
		Object[] record = new Object[4*5];
		record[0] = trueTau.m_afData[0][0];
		record[1] = trueTau.m_afData[1][0];
		record[2] = trueTau.m_afData[2][0];
		record[3] = trueTau.m_afData[3][0];
		record[4] = trueTau.m_afData[4][0];
		record[5] = estTau.m_afData[0][0];
		record[6] = estTau.m_afData[1][0];
		record[7] = estTau.m_afData[2][0];
		record[8] = estTau.m_afData[3][0];
		record[9] = estTau.m_afData[4][0];
		record[10] = estVarianceUncorr.m_afData[0][0];
		record[11] = estVarianceUncorr.m_afData[1][1];
		record[12] = estVarianceUncorr.m_afData[2][2];
		record[13] = estVarianceUncorr.m_afData[3][3];
		record[14] = estVarianceUncorr.m_afData[4][4];
		record[15] = estVarianceCorr.m_afData[0][0];
		record[16] = estVarianceCorr.m_afData[1][1];
		record[17] = estVarianceCorr.m_afData[2][2];
		record[18] = estVarianceCorr.m_afData[3][3];
		record[19] = estVarianceCorr.m_afData[4][4];
		return record;
	}
	
	
}
