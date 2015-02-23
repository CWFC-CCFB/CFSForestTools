package quebecmrnfutility.predictor.stemtaper;

import repicea.math.Matrix;
import repicea.stats.estimates.Estimate;

public class StemTaperExample {

	
	public static void main(String[] args) throws Exception {
		StemTaperEquation ste = new StemTaperEquation();
		StemTaperTree tree = new StemTaperTreeImpl(200, 15.5);
		double bottomHeight = .3;
		double topHeight = 15.3;
		double spacing = .1;
		int numberOfRows = (int) ((topHeight - bottomHeight) / spacing) + 1;
		Matrix heights = new Matrix(numberOfRows, 1, bottomHeight, spacing);
		ste.setTree(tree);
		@SuppressWarnings("rawtypes")
		Estimate volumeEstimate = ste.predictVolume(heights);
		System.out.println("Volume = " + volumeEstimate.getMean().getSumOfElements());
		System.out.println("Variance = " + volumeEstimate.getVariance().getSumOfElements());
	}
	
}
