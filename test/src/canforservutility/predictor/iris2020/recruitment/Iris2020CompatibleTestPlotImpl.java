package canforservutility.predictor.iris2020.recruitment;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;

public class Iris2020CompatibleTestPlotImpl implements Iris2020CompatiblePlot {

	class Iris2020CompatibleTestTreeImpl implements Iris2020CompatibleTree {
		
		final Iris2020Species species;
		
		Iris2020CompatibleTestTreeImpl(Iris2020Species species) {
			this.species = species;
		}
		
		@Override
		public double getBasalAreaLargerThanSubjectM2Ha() {return 0;}

		@Override
		public double getDbhCm() {return 0;}

		@Override
		public double getSquaredDbhCm() {return 0;}

		@Override
		public double getStemBasalAreaM2() {return 0;}

		@Override
		public double getLnDbhCm() {return 0;}

		@Override
		public String getSubjectId() {return null;}

		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public int getErrorTermIndex() {return 0;}

		@Override
		public Iris2020Species getSpecies() {return species;}
		
	}
	
	
	
	private final double growthStepLength;
	private final double basalAreaM2Ha;
	private final double stemDensity;
	private final double slope;
	private final int dateYr;
	private final double dd;
	private final double prcp;
	private final double length;
	private final double frostDay;
	private final double lowestTmin;
	private final SoilDepth soilDepth;
	private final DisturbanceType pastDist;
	private final DisturbanceType upcomingDist;
	private final OriginType origin;
	private final boolean isOrganicSoil;
	private final SoilTexture soilTexture;
	private final double pred;
	private final String id;
	private final Iris2020Species species;
		
	Iris2020CompatibleTestPlotImpl(String id,
			double growthStepLength,
			double basalAreaM2Ha,
			double stemDensity,
			double slope,
			int dateYr,
			double dd,
			double prcp,
			double length,
			double frostDay,
			double lowestTmin,
			SoilDepth soilDepth,
			DisturbanceType pastDist,
			DisturbanceType upcomingDist,
			OriginType origin,
			boolean isOrganicSoil,
			SoilTexture soilTexture,
			Iris2020Species species,
			double pred) {
		this.id = id;
		this.growthStepLength = growthStepLength;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.stemDensity = stemDensity;
		this.slope = slope;
		this.dateYr = dateYr;
		this.dd = dd;
		this.prcp = prcp;
		this.length = length;
		this.frostDay = frostDay;
		this.lowestTmin = lowestTmin;
		this.soilDepth = soilDepth;
		this.pastDist = pastDist;
		this.upcomingDist = upcomingDist;
		this.origin = origin;
		this.isOrganicSoil = isOrganicSoil;
		this.soilTexture = soilTexture;
		this.species = species;
		this.pred = pred;
	}
	
	
	
	@Override
	public double getAreaHa() {
		return 0.04;
	}

	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getGrowthStepLengthYr() {return growthStepLength;}

	@Override
	public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

	@Override
	public double getNumberOfStemsHa() {return stemDensity;}

	@Override
	public double getSlopeInclinationPercent() {return slope;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public double getMeanDegreeDaysOverThePeriod() {return dd;}

	@Override
	public double getMeanPrecipitationOverThePeriod() {return prcp;}

	@Override
	public double getMeanGrowingSeasonLengthOverThePeriod() {return length;}

	@Override
	public double getMeanFrostDaysOverThePeriod() {return frostDay;}

	@Override
	public double getMeanLowestTminOverThePeriod() {return lowestTmin;}

	@Override
	public SoilDepth getSoilDepth() {return soilDepth;}

	@Override
	public boolean isOrganicSoil() {return isOrganicSoil;}

	@Override
	public DisturbanceType getPastDisturbance() {return pastDist;}

	@Override
	public DisturbanceType getUpcomingDisturbance() {return upcomingDist;}

	@Override
	public OriginType getOrigin() {return origin;}

	@Override
	public SoilTexture getSoilTexture() {return soilTexture;}
	
	double getPredProb() {return pred;}

	Iris2020CompatibleTree getTreeInstance() {
		return new Iris2020CompatibleTestTreeImpl(species); 
	}
	
}
