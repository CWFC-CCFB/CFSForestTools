/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Forest Service
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
package canforservutility.predictor.disturbances.sprucebudworm.occurrence.boulangerarsenault2004;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.disturbances.DisturbanceTypeProvider.DisturbanceType;

/**
 * This class is a basic implementation of the SpruceBudwormOutbreakOccurrencePlot interface.
 * @author Mathieu Fortin - March 2019
 */
public class SpruceBudwormOutbreakOccurrencePlotImpl implements SpruceBudwormOutbreakOccurrencePlot {

	
	private final Integer timeSinceLastOutbreakYrs;
	private int timeSinceInitialKnownDate;
	private int monteCarloId;

	/**
	 * Constructor.
	 * @param timeSinceLastOutbreakYrs an Integer instance (can be null)
	 * @param timeSinceInitialKnownDate an integer
	 */
	public SpruceBudwormOutbreakOccurrencePlotImpl(Integer timeSinceLastOutbreakYrs, int timeSinceInitialKnownDate) {
		this.timeSinceLastOutbreakYrs = timeSinceLastOutbreakYrs;
		this.timeSinceInitialKnownDate = timeSinceInitialKnownDate;
	}

	@Override
	public Integer getTimeSinceLastDisturbanceYrs(DisturbanceType type, int currentDateYrs) {
		return timeSinceLastOutbreakYrs;
	}

	@Override
	public int getTimeSinceFirstKnownDateYrs(int currentDateYrs) {
		return timeSinceInitialKnownDate;
	}

	/*
	 * Useless for this predictor (non-Javadoc)
	 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getSubjectId()
	 */
	@Override
	public String getSubjectId() {return null;}

	/*
	 * Useless for this predictor (non-Javadoc)
	 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getSubjectId()
	 */
	@Override
	public HierarchicalLevel getHierarchicalLevel() {return null;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloId;}

	protected void setMonteCarloId(int monteCarloId) {
		this.monteCarloId = monteCarloId;
	}
}
