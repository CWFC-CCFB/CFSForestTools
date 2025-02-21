package canforservutility.predictor.prices.dura;

/**
 * An interface for test purposes.
 * @author Mathieu Fortin - February 2025
 */
interface CovidPeriodProvider {

	/**
	 * Inform whether the quarter is during a Covid pandemic.
	 * @return a boolean
	 */
	public boolean isCovidPeriod();

}
