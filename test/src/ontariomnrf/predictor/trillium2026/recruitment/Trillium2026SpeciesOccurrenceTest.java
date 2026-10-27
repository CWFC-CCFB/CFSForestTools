package ontariomnrf.predictor.trillium2026.recruitment;

import org.junit.Test;

import canforservutility.occupancyindex.OccupancyIndexCalculablePlot;
import canforservutility.occupancyindex.OccupancyIndexCalculator;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.estimates.GaussianEstimate;

public class Trillium2026SpeciesOccurrenceTest {

	@Test
	public void test01SimpleOccurrence() {
		OccupancyIndexCalculator occIndCalc = new OccupancyIndexCalculator(Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex(), 25, null);
		occIndCalc.registerPlots(Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex());

		int i = 0;
		for (OccupancyIndexCalculablePlot p : Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex()) {
			try {
				GaussianEstimate occIndex = occIndCalc.getOccupancyIndex(p, Species.Abies_balsamea);
				i++;
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		System.out.println("Occupancy index was calculated on " + i + " out of " + Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex().size() + " plots");
	}
}
