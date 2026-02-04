package ontariomnrf.predictor.trillium2026;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class Trillium2026MortalityPredictor extends REpiceaBinaryEventPredictor<Trillium2026Plot, Trillium2026Tree> {

	private static Map<String, Species> SpeciesLookupMap = new HashMap<String, Species>();
	static {
		Species[] species = new Species[] {Species.Abies_balsamea, Species.Acer_pensylvanicum, Species.Acer_rubrum,
				Species.Acer_saccharinum, Species.Acer_saccharum, Species.Betula_alleghaniensis,
				Species.Betula_papyrifera, Species.Fagus_grandifolia, Species.Fraxinus_americana,
				Species.Fraxinus_nigra, Species.Larix_laricina, Species.Ostrya_virginiana,
				Species.Picea_glauca, Species.Picea_mariana, Species.Pinus_banksiana,
				Species.Pinus_resinosa, Species.Pinus_strobus, Species.Populus_balsamifera,
				Species.Populus_grandidentata, Species.Populus_tremuloides, Species.Prunus_pensylvanica,
				Species.Prunus_serotina, Species.Quercus_rubra, Species.Thuja_occidentalis,
				Species.Tilia_americana, Species.Tsuga_canadensis};
		for (Species sp : species) {
			SpeciesLookupMap.put(sp.getLatinName(), sp);
		}
		SpeciesLookupMap.put("Carya sp.", Species.Carya_spp);
		SpeciesLookupMap.put("Juglans sp.", Species.Juglans_spp);
		SpeciesLookupMap.put("Fraxinus pennsylvanica", Species.Fraxinus_pensylvanica); // this one has a typo in R 
		SpeciesLookupMap.put("Meridional species", Species.Other_broadleaved);
		SpeciesLookupMap.put("Quercus sp.", Species.Quercus_spp);
		SpeciesLookupMap.put("Shrubs", Species.Broadleaved_shrubs);
		SpeciesLookupMap.put("Ulmus sp.", Species.Ulmus_spp);
	}
	
	
	private static HashMap<Species, List<Double>> CoefLists;
	private static HashMap<Species, List<Double>> EffectLists;
	private static HashMap<Species, List<Double>> VCovLists;
	private static HashMap<Species, List<Double>> RanefVarLists;
	
	private final Map<Species, Trillium2026MortalityInternalPredictor> internalPredictorMap;
	
	protected Trillium2026MortalityPredictor(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		internalPredictorMap = new HashMap<Species, Trillium2026MortalityInternalPredictor>();
		init();
	}
	
	/**
	 * General predictor
	 * @param isVariabilityEnabled true to run in stochastic model or false for deterministic
	 */
	public Trillium2026MortalityPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
	}

	@Override
	public double predictEventProbability(Trillium2026Plot plot, Trillium2026Tree tree, Map<String, Object> parms) {
		Species species = tree.getTrillium2026TreeSpecies();
		if (!SpeciesLookupMap.values().contains(species)) {
			throw new UnsupportedOperationException("The mortality model of Trillium 2026 does not support species: " + species.getLatinName());
		}
		return internalPredictorMap.get(species).predictEventProbability(plot, tree);
	}
	
	/**
	 * Provide the list of eligible species for this module.
	 * @return a List of Species enums
	 */
	public List<Species> getEligibleSpecies() {
		List<Species> species = new ArrayList<Species>(SpeciesLookupMap.values());
		Collections.sort(species);
		return species;
	}

	@Override
	protected synchronized void init() {
		if (CoefLists == null) {
			String path = ObjectUtility.getRelativePackagePath(getClass());

			EffectLists = new HashMap<Species, List<Double>>();
			String effectFilename = path + "0_mort_effectlist.csv";
			readFile(EffectLists, effectFilename);
			
			CoefLists = new HashMap<Species, List<Double>>();
			String paramFilename = path + "0_mort_coefs.csv";
			readFile(CoefLists, paramFilename);
			
			VCovLists = new HashMap<Species, List<Double>>();
			String vcovFilename = path + "0_mort_vcov.csv";
			readFile(VCovLists, vcovFilename);

			RanefVarLists = new HashMap<Species, List<Double>>();
			String ranefVarFilename = path + "0_mort_ranefVar.csv";
			readFile(RanefVarLists, ranefVarFilename);
		}
		
		for (Species sp : EffectLists.keySet()) {
			internalPredictorMap.put(sp, new Trillium2026MortalityInternalPredictor(
					this.isParametersVariabilityEnabled,
					this.isRandomEffectsVariabilityEnabled,
					this.isResidualVariabilityEnabled,
					EffectLists.get(sp),
					CoefLists.get(sp),
					VCovLists.get(sp),
					RanefVarLists.get(sp)));
		}
	}

	static Species getSpeciesFromString(String speciesName) {
		Species species = SpeciesLookupMap.get(speciesName);
		if (species == null) {
			throw new UnsupportedOperationException("The mortality model of Trillium 2026 does not support species: " + speciesName);
		}
		return species;
	}
	
	private void readFile(Map<Species, List<Double>> oMap, String filename) {
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String speciesName = record[0].toString();
				double parm = Double.parseDouble(record[1].toString());
				Species species = getSpeciesFromString(speciesName);
				if (!oMap.containsKey(species)) {
					oMap.put(species, new ArrayList<Double>());
				}
				oMap.get(species).add(parm);
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static void main(String[] args) {
		new Trillium2026MortalityPredictor(false);
	}
}
