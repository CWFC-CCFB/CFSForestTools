package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

import org.junit.Assert;
import org.junit.Test;

import repicea.stats.estimates.SimpleEstimate;

public class DefoliationTests {

	@Test
	public void simpleTest() {
		DefoliationPredictor pred = new DefoliationPredictor();
		DefoliationPlot plot = new DefoliationPlotImpl(48d, -70d, 200d, 15, 70, 1d);
		SimpleEstimate resultingEstimate = pred.predictDefoliation(plot);
		double durationYrs = resultingEstimate.getMean().m_afData[0][0];
		Assert.assertEquals("Testing duration (yrs)", 16.485489351115977, durationYrs, 1E-8);
		double severity = resultingEstimate.getMean().m_afData[1][0];
		Assert.assertEquals("Testing severity (%)", 50.60942294501749, severity, 1E-8);
		plot = new DefoliationPlotImpl(51d, -70d, 200d, 70, 15, 1d);
		resultingEstimate = pred.predictDefoliation(plot);
		durationYrs = resultingEstimate.getMean().m_afData[0][0];
		Assert.assertEquals("Testing duration (yrs)", 4.084573173775008, durationYrs, 1E-8);
		severity = resultingEstimate.getMean().m_afData[1][0];
		Assert.assertEquals("Testing severity (%)", 5.866373971170846, severity, 1E-8);
	}
	
	
	
	
	
	
}
