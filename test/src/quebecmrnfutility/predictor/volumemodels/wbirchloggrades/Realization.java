package quebecmrnfutility.predictor.volumemodels.wbirchloggrades;

import repicea.math.Matrix;

class Realization {
	final Matrix trueTau;
	final Matrix estTau;
	final Matrix estVarianceUncorr;
	final Matrix estVarianceCorr;
	final Matrix samplingPart;
	final Matrix modelPart;
	
	Realization(Matrix trueTau, Matrix estTau, Matrix estVarianceUncorr, Matrix estVarianceCorr, Matrix samplingPart, Matrix modelPart) {
		this.trueTau = trueTau;
		this.estTau = estTau;
		this.estVarianceUncorr = estVarianceUncorr;
		this.estVarianceCorr = estVarianceCorr;
		this.samplingPart = samplingPart;
		this.modelPart = modelPart;
	}
	
	Object[] getRecord() {
		Object[] record = new Object[6*7];
		record[0] = trueTau.getValueAt(0, 0);
		record[1] = estTau.getValueAt(0, 0);
		record[2] = estVarianceUncorr.getValueAt(0, 0);
		record[3] = estVarianceCorr.getValueAt(0, 0);
		record[4] = samplingPart.getValueAt(0, 0);
		record[5] = modelPart.getValueAt(0, 0);

		record[6] = trueTau.getValueAt(1, 0);
		record[7] = estTau.getValueAt(1, 0);
		record[8] = estVarianceUncorr.getValueAt(1, 1);
		record[9] = estVarianceCorr.getValueAt(1, 1);
		record[10] = samplingPart.getValueAt(1, 1);
		record[11] = modelPart.getValueAt(1, 1);

		record[12] = trueTau.getValueAt(2, 0);
		record[13] = estTau.getValueAt(2, 0);
		record[14] = estVarianceUncorr.getValueAt(2, 2);
		record[15] = estVarianceCorr.getValueAt(2, 2);
		record[16] = samplingPart.getValueAt(2, 2);
		record[17] = modelPart.getValueAt(2, 2);
		
		record[18] = trueTau.getValueAt(3, 0);
		record[19] = estTau.getValueAt(3, 0);
		record[20] = estVarianceUncorr.getValueAt(3, 3);
		record[21] = estVarianceCorr.getValueAt(3, 3);
		record[22] = samplingPart.getValueAt(3, 3);
		record[23] = modelPart.getValueAt(3, 3);

		record[24] = trueTau.getValueAt(4, 0);
		record[25] = estTau.getValueAt(4, 0);
		record[26] = estVarianceUncorr.getValueAt(4, 4);
		record[27] = estVarianceCorr.getValueAt(4, 4);
		record[28] = samplingPart.getValueAt(4, 4);
		record[29] = modelPart.getValueAt(4, 4);
		
		record[30] = trueTau.getValueAt(5, 0);
		record[31] = estTau.getValueAt(5, 0);
		record[32] = estVarianceUncorr.getValueAt(5, 5);
		record[33] = estVarianceCorr.getValueAt(5, 5);
		record[34] = samplingPart.getValueAt(5, 5);
		record[35] = modelPart.getValueAt(5, 5);

		record[36] = trueTau.getValueAt(6, 0);
		record[37] = estTau.getValueAt(6, 0);
		record[38] = estVarianceUncorr.getValueAt(6, 6);
		record[39] = estVarianceCorr.getValueAt(6, 6);
		record[40] = samplingPart.getValueAt(6, 6);
		record[41] = modelPart.getValueAt(6, 6);

		return record;
	}
	
	
}
