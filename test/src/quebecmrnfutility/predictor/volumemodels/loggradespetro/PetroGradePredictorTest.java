package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider.QcTreeQuality;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcMarkingPriorityProvider.QcMarkingPriority;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcVigorClassProvider.QcVigorClass;
import repicea.math.Matrix;
import repicea.serial.xml.XmlMarshallException;
import repicea.stats.estimates.MonteCarloEstimate;

public class PetroGradePredictorTest {

	@Test
	public void testWithBasicVersion() throws FileNotFoundException, XmlMarshallException {
		
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 50000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		for (int i = 0; i < actual.m_iRows; i++) {
			relDiff = Math.abs(1 - actual.m_afData[i][0] / expected.m_afData[i][0]); 
			Assert.assertEquals(0, relDiff, .05);
		}
	}
	
	
	
	
	@Test
	public void testWithMSCR() throws FileNotFoundException, XmlMarshallException {
			
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50, QcMarkingPriority.C);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 10000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		for (int i = 0; i < actual.m_iRows; i++) {
			relDiff = Math.abs(1 - actual.m_afData[i][0] / expected.m_afData[i][0]); 
			Assert.assertEquals(0, relDiff, .065);
		}
	}

	
	@Test
	public void testWithABCD() throws FileNotFoundException, XmlMarshallException {
		
		
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50, QcTreeQuality.B);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 10000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		for (int i = 0; i < actual.m_iRows; i++) {
			relDiff = Math.abs(1 - actual.m_afData[i][0] / expected.m_afData[i][0]); 
			Assert.assertEquals(0, relDiff, .08);
		}
			
	}

	
	@Test
	public void testWithVigor() throws FileNotFoundException, XmlMarshallException {
		
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50, QcVigorClass.V2);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 10000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		double diff;
		for (int i = 0; i < actual.m_iRows; i++) {
			diff = Math.abs(actual.m_afData[i][0] - expected.m_afData[i][0]); 
			relDiff = Math.abs(1 - actual.m_afData[i][0] / expected.m_afData[i][0]); 
			Assert.assertTrue(relDiff < 0.05 || diff < 0.02);
		}
			
	}


}
