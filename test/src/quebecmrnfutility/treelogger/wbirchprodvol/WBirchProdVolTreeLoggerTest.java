package quebecmrnfutility.treelogger.wbirchprodvol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;
import quebecmrnfutility.predictor.volumemodels.wbirchloggrades.WBirchLogGradesPredictor;
import quebecmrnfutility.predictor.volumemodels.wbirchloggrades.WBirchLogGradesPredictorTest;
import quebecmrnfutility.predictor.volumemodels.wbirchloggrades.WBirchLogGradesStandImpl;
import repicea.simulation.treelogger.WoodPiece;
import quebecmrnfutility.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerParameters.ProductID;

public class WBirchProdVolTreeLoggerTest {

	@Test
	public void testTreeLoggerWithDeterministicPred() {
		Map<String, WBirchLogGradesStandImpl> stands = WBirchLogGradesPredictorTest.readStands();
		WBirchProdVolTreeLogger treeLogger = new WBirchProdVolTreeLogger(false);
		treeLogger.setTreeLoggerParameters(treeLogger.createDefaultTreeLoggerParameters());
		Collection<WBirchProdVolLoggableTree> trees = new ArrayList<WBirchProdVolLoggableTree>();
		
		for (WBirchLogGradesStandImpl stand : stands.values()) {
			trees.addAll(stand.getTrees().values());
		}
		
		treeLogger.init(trees);
		treeLogger.run();
		
		Map<String, Double> refMap = new HashMap<String, Double>();
		for (Collection<WoodPiece> woodPieces : treeLogger.getWoodPieces().values()) {
			for (WoodPiece piece : woodPieces) {
				String name = piece.getLogCategory().getName();
				if (!refMap.containsKey(name)) {
					refMap.put(name, 0d);
				}
				refMap.put(name, refMap.get(name) + piece.getWoodVolumeM3());
			}
		}

		Matrix pred = new Matrix(7,1);
		WBirchLogGradesPredictor predictor = new WBirchLogGradesPredictor(false);
		for (WBirchProdVolLoggableTree tree : trees) {
			if (treeLogger.getEligible(tree) != null) {
				pred = pred.add(predictor.getLogGradeUnderbarkVolumePredictions(tree.getStand(), tree));
			}
		}
		
		double observed;
		double expected;
		for (ProductID product : ProductID.values()) {
			observed = refMap.get(product.toString());
			expected = pred.getValueAt(product.getIndex(), 0);
			Assert.assertEquals("Comparing product " + product.name(), expected, observed, 1E-6);
		}
	}
	
}