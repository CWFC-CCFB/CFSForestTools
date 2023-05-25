/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Mathieu Fortin
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import biosimclient.BioSimClient;
import biosimclient.BioSimException;
import repicea.math.Matrix;

public class DefoliationTest {

	@BeforeClass
	public static void initializeTest() {
		BioSimClient.setTestModeEnabled(true);
	}
	
	@AfterClass
	public static void finalizeTest() {
		BioSimClient.setTestModeEnabled(false);
	}
	
	@Test
	public void simpleTestWithoutBioSIM() throws BioSimException {
		DefoliationPredictor pred = new DefoliationPredictor(4);
		pred.testPurposes = true;
		DefoliationPlot plot = new DefoliationPlotImpl(48d, -70d, 200d, 15, 70, 1d, 1986);
		Matrix resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		double durationYrs = resultingEstimate.getValueAt(0, 0);
		Assert.assertEquals("Testing duration (yrs)", 16.485489351115977, durationYrs, 1E-8);
		double severity = resultingEstimate.getValueAt(1, 0);
		Assert.assertEquals("Testing severity (%)", 50.60942294501749, severity, 1E-8);
		plot = new DefoliationPlotImpl(51d, -70d, 200d, 70, 15, 1d, 1986);
		resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		durationYrs = resultingEstimate.getValueAt(0, 0);
		Assert.assertEquals("Testing duration (yrs)", 4.084573173775008, durationYrs, 1E-8);
		severity = resultingEstimate.getValueAt(1, 0);
		Assert.assertEquals("Testing severity (%)", 5.866373971170846, severity, 1E-8);
	}
	
	// This GrayDefoliator class must be improved. It is simply too long.
	@Ignore
	@Test
	public void simpleTestWithBioSIM() throws BioSimException {
		DefoliationPredictor pred = new DefoliationPredictor(4);
		DefoliationPlot plot = new DefoliationPlotImpl(48d, -70d, 350d, 15, 70, 1d, 1986);
		Matrix resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		double durationYrs = resultingEstimate.getValueAt(0, 0);
		Assert.assertEquals("Testing duration (yrs)", 19.927, durationYrs, 0.2);
		double severity = resultingEstimate.getValueAt(1, 0);
		Assert.assertEquals("Testing severity (%)", 8.942412817277273, severity, 8);
		System.out.println("Duration = " + durationYrs + "; severity = " + severity);
		plot = new DefoliationPlotImpl(51d, -70d, 450d, 70, 15, 1d, 1986);
		resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
		durationYrs = resultingEstimate.getValueAt(0, 0);
		Assert.assertEquals("Testing duration (yrs)", 9.282, durationYrs, 0.2);
		severity = resultingEstimate.getValueAt(1, 0);
		Assert.assertEquals("Testing severity (%)", 15.756, severity, 11);
		System.out.println("Duration = " + durationYrs + "; severity = " + severity);
		
		
//		plot = new DefoliationPlotImpl(49d, -70d, 450d, 0, 100, 1d, 1986);
//		resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
//		durationYrs = resultingEstimate.getValueAt(0, 0);
//		severity = resultingEstimate.getValueAt(1, 0);
//		System.out.println("Duration = " + durationYrs + "; severity = " + severity);
//
//		plot = new DefoliationPlotImpl(47d, -71d, 600d, 0, 100, 1d, 1986);
//		resultingEstimate = pred.getDurationAndSeverityEstimate(plot);
//		durationYrs = resultingEstimate.getValueAt(0, 0);
//		severity = resultingEstimate.getValueAt(1, 0);
//		System.out.println("Duration = " + durationYrs + "; severity = " + severity);

	}
	
	
	
	
	
}
