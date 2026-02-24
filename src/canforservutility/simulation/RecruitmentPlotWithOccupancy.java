package canforservutility.simulation;

import repicea.simulation.MonteCarloSimulationCompliantObject;

public interface RecruitmentPlotWithOccupancy extends MonteCarloSimulationCompliantObject {

	/**
	 * Return the occupancy for a particular species. <p>
	 * @param sp an Enum that stands for the species
	 * @return either a double (if the occupancy is known or simulated) or 
	 * a GaussianEstimate instance (when it has been estimated).
	 */
	public Object getOccupancyForThisSpecies(Enum<?> sp);

	
	
}
