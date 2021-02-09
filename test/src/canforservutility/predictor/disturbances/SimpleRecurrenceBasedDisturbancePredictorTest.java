package canforservutility.predictor.disturbances;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.predictor.disturbances.SimpleRecurrenceBasedDisturbancePredictor.SimpleRecurrenceBasedDisturbanceParameters;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;

public class SimpleRecurrenceBasedDisturbancePredictorTest {

	static class Stand implements MonteCarloSimulationCompliantObject {

		final String id;
		int monteCarloRealizationId;
		
		Stand(String id) {
			this.id = id;
		}
		
		@Override
		public String getSubjectId() {return id;}

		/*
		 * Useless in this case (non-Javadoc)
		 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getHierarchicalLevel()
		 */
		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return null;
		}

		@Override
		public int getMonteCarloRealizationId() {return monteCarloRealizationId;}
		
	}
	
	/*
	 * This test checks if the predictor in stochastic mode properly records the event.
	 */
	@Test
	public void simpleRecordingOfEvent() {
		SimpleRecurrenceBasedDisturbancePredictor predictor = new SimpleRecurrenceBasedDisturbancePredictor(true, true);
		Stand s = new Stand("myStand");
		SimpleRecurrenceBasedDisturbanceParameters p = new SimpleRecurrenceBasedDisturbanceParameters(35,0);	// recurrence of 35 years without variance
		Object reference = null;
//		int currentDateYr = 2000;
		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put(SimpleRecurrenceBasedDisturbancePredictor.ParmCurrentDateYr, 2000);
		parms.put(SimpleRecurrenceBasedDisturbancePredictor.ParmSimpleRecurrenceBasedParameters, p);
		for (int i = 0; i < 1000; i++) {
			Object result = predictor.predictEvent(s, null, parms);
			Assert.assertTrue("Testing if the same instance of internal predictor is used", predictor.internalPredictorMap.size() == 1);
			if (reference == null) {
				reference = result;
			} else {
				Assert.assertTrue("Testing if the result is the same", result.equals(reference));
			}
		}
	}
}
