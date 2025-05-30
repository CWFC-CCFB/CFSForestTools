/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package quebecmrnfutility.predictor.artemis2009;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.allometrycalculator.AllometryCalculator;
import repicea.simulation.allometrycalculator.LightAllometryCalculableTree;

class Artemis2009CompatibleStandImpl implements Artemis2009CompatibleStand {

	private int monteCarloRealizationId = 0;
	private final int dateYr;
	private final double totalAnnualPrecipitationMm;
	private final double meanAnnualTemperatureC;
	private final double elevationM;
	private final double latitude;
	private final double longitude;
	private final String idString;
	private boolean isGoingToBeDefoliated;
	private final AllometryCalculator calculator = new AllometryCalculator();
	private double basalAreaM2Ha;
	private double meanQuadraticDiameterCm;
	private final String potentialVegetation;
	
	private boolean isInterventionResult;
	
	private final List<Artemis2009CompatibleTree> trees = new ArrayList<Artemis2009CompatibleTree>();
	private double numberOfStemsHa;
	
	private Matrix numberOfStemsBySpeciesGroups;
	private Matrix basalAreaBySpeciesGroups;
	
	protected Artemis2009CompatibleStandImpl(String idString, 
			int dateYr,
			double latitude,
			double longitude,
			double elevationM,
			double meanAnnualTemperatureC,
			double meanAnnualPrecipitationMm,
			String potentialVegetation) {
		this.dateYr = dateYr;
		this.idString = idString;
		this.elevationM = elevationM;
		this.latitude = latitude;
		this.longitude = longitude;
		this.meanAnnualTemperatureC = meanAnnualTemperatureC;
		this.totalAnnualPrecipitationMm = meanAnnualPrecipitationMm;
		this.potentialVegetation = potentialVegetation;
	}
	
	protected List<Artemis2009CompatibleTree> getTrees() {return trees;}
	
	@Override
	public String getPotentialVegetation() {return potentialVegetation;}

	@Override
	public boolean isInterventionResult() {return isInterventionResult;}

	protected void setBasalAreaM2Ha() {basalAreaM2Ha = calculator.getBasalAreaM2(trees) / getAreaHa();}
	protected void setMQDCm() {meanQuadraticDiameterCm = calculator.getMeanQuadraticDiameterCm(trees);}
	protected void setNumberOfStemsHa() {numberOfStemsHa = calculator.getNumberOfTrees(trees) / getAreaHa();}

	protected void updatePlotVariables() {
		setBasalAreaM2Ha();
		setMQDCm();
		setNumberOfStemsHa();
		setBAL();
		numberOfStemsBySpeciesGroups = ParameterDispatcher.getGroupEssGorN(this, trees, false); // false for the number of stems
		basalAreaBySpeciesGroups = ParameterDispatcher.getGroupEssGorN(this, trees, true); // true for the number of stems
	}
	
	
	protected void setMonteCarloRealization(int i) {
		monteCarloRealizationId = i;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setBAL() {
		for (Artemis2009CompatibleTree tree : getTrees()) {
			Collection<LightAllometryCalculableTree> largerTrees = calculator.getTreesLarger((Collection) getTrees(), tree.getDbhCm());
			((Artemis2009CompatibleTreeImpl) tree).setBAL(calculator.getBasalAreaM2((Collection<LightAllometryCalculableTree>) largerTrees) / getAreaHa());
		}
	}

	@Override
	public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

	@Override
	public double getMeanQuadraticDiameterCm() {return meanQuadraticDiameterCm;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public double getTotalAnnualPrecipitationMm() {return totalAnnualPrecipitationMm;}

	@Override
	public double getMeanAnnualTemperatureCelsius() {return meanAnnualTemperatureC;}

	@Override
	public double getElevationM() {return elevationM;}

	@Override
	public double getLatitudeDeg() {return latitude;}

	@Override
	public double getLongitudeDeg() {return longitude;}

	@Override
	public double getNumberOfStemsHa() {return numberOfStemsHa;}

	@Override
	public double getAreaHa() {return 0.04;}

	@Override
	public String getSubjectId() {return idString;}

//	@Override
//	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloRealizationId;}

	@Override
	public boolean isGoingToBeDefoliated() {return isGoingToBeDefoliated;}

	@Override
	public double getGrowthStepLengthYr() {return 10;}

	@Override
	public boolean isInitialStand() {return true;}

	@Override
	public Matrix getNumberOfStemsBySpeciesGroup() {return numberOfStemsBySpeciesGroups;}

	@Override
	public Matrix getBasalAreaBySpeciesGroup() {return this.basalAreaBySpeciesGroups;}

	@Override
	public boolean isGoingToBeHarvested() {return false;}


}
