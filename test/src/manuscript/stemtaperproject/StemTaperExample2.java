package manuscript.stemtaperproject;

import quebecmrnfutility.predictor.stemtaper.StemTaperEquation;
import quebecmrnfutility.predictor.stemtaper.StemTaperTree;
import repicea.math.Matrix;
import repicea.stats.estimates.Estimate;

public class StemTaperExample2 {

	public static void main(String[] args) throws Exception {
		StemTaperEquation ste = new StemTaperEquation();
		StemTaperTree tree = new StemTaperTreeImpl(250,19);
		ste.setTree(tree);
		Matrix mat = new Matrix(5,1,0.3,1);
		Estimate<?> volume = ste.predictVolume(mat);
		double vol1 = volume.getMean().getSubMatrix(0, 3, 0, 0).getSumOfElements();
		double vol2 = volume.getMean().getSubMatrix(4, 7, 0, 0).getSumOfElements();
		
		
		double var1 = volume.getVariance().getSubMatrix(0, 3, 0, 3).getSumOfElements();
		double var2 = volume.getVariance().getSubMatrix(4, 7, 4, 7).getSumOfElements();
		double cov1 = volume.getVariance().getSubMatrix(0, 3, 4, 7).getSumOfElements();
		
		int u = 0;
	}
	
}
