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

class Artemis2009CompatibleTreeImpl implements Artemis2009CompatibleTree {

	private Artemis2009CompatibleStandImpl stand;
	private int id;
	private double dbhCm;
	private double number;
	private double bal;
	private String speciesGroupName;
	

	protected Artemis2009CompatibleTreeImpl(Artemis2009CompatibleStandImpl stand, double dbhCm, String speciesName, double number) {
		this.stand = stand;
		speciesName = ParameterDispatcher.getInstance().getInitPrioriSpeciesGrouping(speciesName);
		this.dbhCm = dbhCm;
		this.number = number;
		if (ParameterDispatcher.getInstance().isRecognizedSpecies(speciesName)) {
			this.speciesGroupName = ParameterDispatcher.getInstance().getSpeciesGroupName(stand, speciesName);
			id = stand.getTrees().size();
			stand.getTrees().add(this);
		}
	}

	/**
	 * Fake constructor for recruits
	 * @param speciesGroupName
	 */
	protected Artemis2009CompatibleTreeImpl(String speciesGroupName) {
		this.speciesGroupName = speciesGroupName;
	}
	
	protected void setBAL(double bal) {this.bal = bal;}
	
	@Override
	public double getBasalAreaLargerThanSubjectM2Ha() {return bal;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCm() {return Math.log(dbhCm);}

	@Override
	public double getSquaredDbhCm() {return dbhCm * dbhCm;}

	@Override
	public String getSpeciesGroupName() {return speciesGroupName;}
	
	@Override
	public String getSubjectId() {return ((Integer) id).toString();}

//	@Override
//	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

//	@Override
//	public void setMonteCarloRealizationId(int i) {stand.setMonteCarloRealizationId(i);}

	@Override
	public int getMonteCarloRealizationId() {return stand.getMonteCarloRealizationId();}

	@Override
	public double getNumber() {return number;}

	@Override
	public double getStemBasalAreaM2() {return Math.PI * getSquaredDbhCm() * 0.000025;}

	@Override
	public int getErrorTermIndex() {
		return 0;
	}

}
