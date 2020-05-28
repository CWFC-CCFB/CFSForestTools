package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

import org.junit.Assert;
import org.junit.Test;

import repicea.math.Matrix;

public class DefoliationTests {

	@Test
	public void simpleTestWithoutBioSIM() {
		DefoliationPredictor pred = new DefoliationPredictor(4);
		pred.testPurposes = true;
		DefoliationPlot plot = new DefoliationPlotImpl(48d, -70d, 200d, 15, 70, 1d, 1986);
		Matrix resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		double durationYrs = resultingEstimate.m_afData[0][0];
		Assert.assertEquals("Testing duration (yrs)", 16.485489351115977, durationYrs, 1E-8);
		double severity = resultingEstimate.m_afData[1][0];
		Assert.assertEquals("Testing severity (%)", 50.60942294501749, severity, 1E-8);
		plot = new DefoliationPlotImpl(51d, -70d, 200d, 70, 15, 1d, 1986);
		resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		durationYrs = resultingEstimate.m_afData[0][0];
		Assert.assertEquals("Testing duration (yrs)", 4.084573173775008, durationYrs, 1E-8);
		severity = resultingEstimate.m_afData[1][0];
		Assert.assertEquals("Testing severity (%)", 5.866373971170846, severity, 1E-8);
	}
	
	@Test
	public void simpleTestWithBioSIM() {
		DefoliationPredictor pred = new DefoliationPredictor(4);
		DefoliationPlot plot = new DefoliationPlotImpl(48d, -70d, 350d, 15, 70, 1d, 1986);
		Matrix resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		double durationYrs = resultingEstimate.m_afData[0][0];
		Assert.assertEquals("Testing duration (yrs)", 23.398588840191998, durationYrs, 0.1);
		double severity = resultingEstimate.m_afData[1][0];
		Assert.assertEquals("Testing severity (%)", 55.66199253209301, severity, 4);
		plot = new DefoliationPlotImpl(51d, -70d, 450d, 70, 15, 1d, 1986);
		resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		durationYrs = resultingEstimate.m_afData[0][0];
		Assert.assertEquals("Testing duration (yrs)", 25.53337946419431, durationYrs, 0.1);
		severity = resultingEstimate.m_afData[1][0];
		Assert.assertEquals("Testing severity (%)", 74.81527267016763, severity, 6);
	}
	
	
	
	
	
}
