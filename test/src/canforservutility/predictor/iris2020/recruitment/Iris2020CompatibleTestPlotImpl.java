package canforservutility.predictor.iris2020.recruitment;

import java.security.InvalidParameterException;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.math.Matrix;

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
	private final double basalAreaM2HaConiferous;
	private final double basalAreaM2HaBroadleaved;
	private final double slopeInclination;
	private final double slopeAspect;
	private final int dateYr;
	private final double dd;
	private final double prcp;
	private final SoilDepth soilDepth;
	private final DisturbanceType pastDist;
	private final DisturbanceType upcomingDist;
	private final OriginType origin;
	private final DrainageGroup drainageGroup;
	private final SoilTexture soilTexture;
	private final double pred;
	private final String id;
	private final Iris2020Species species;
	private final Matrix gSpGrMat;
		
	Iris2020CompatibleTestPlotImpl(String id,
			double growthStepLength,
			double basalAreaM2HaConiferous,
			double basalAreaM2HaBroadleaved,
			double slopeInclination,
			double slopeAspect,
			int dateYr,
			double dd,
			double prcp,
			SoilDepth soilDepth,
			DisturbanceType pastDist,
			DisturbanceType upcomingDist,
			OriginType origin,
			DrainageGroup drainageGroup,
			SoilTexture soilTexture,
			Iris2020Species species,
			double pred, 
			Double gSpGr) {
		if (drainageGroup == null) {
			throw new InvalidParameterException("The drainage group cannot be null!");
		}
		this.id = id;
		this.growthStepLength = growthStepLength;
		this.basalAreaM2HaConiferous = basalAreaM2HaConiferous;
		this.basalAreaM2HaBroadleaved = basalAreaM2HaBroadleaved;
		this.slopeInclination = slopeInclination;
		this.slopeAspect = slopeAspect;
		this.dateYr = dateYr;
		this.dd = dd;
		this.prcp = prcp;
		this.soilDepth = soilDepth;
		this.pastDist = pastDist;
		this.upcomingDist = upcomingDist;
		this.origin = origin;
		this.drainageGroup = drainageGroup;
		this.soilTexture = soilTexture;
		this.species = species;
		this.pred = pred;
		gSpGrMat = new Matrix(1, Iris2020Species.values().length);
		gSpGrMat.m_afData[0][species.ordinal()] = gSpGr;
	}
	
	

	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getGrowthStepLengthYr() {return growthStepLength;}

	@Override
	public double getSlopeInclinationPercent() {return slopeInclination;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public double getMeanDegreeDaysOverThePeriod() {return dd;}

	@Override
	public double getMeanPrecipitationOverThePeriod() {return prcp;}

	@Override
	public SoilDepth getSoilDepth() {return soilDepth;}

	@Override
	public DrainageGroup getDrainageGroup() {return drainageGroup;}

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

	@Override
	public Matrix getBasalAreaM2HaBySpecies() {return gSpGrMat;}



	@Override
	public double getBasalAreaOfConiferousSpecies() {return basalAreaM2HaConiferous;}

	@Override
	public double getBasalAreaOfBroadleavedSpecies() {return basalAreaM2HaBroadleaved;}

	@Override
	public double getSlopeAspect() {return slopeAspect;}
	
}
