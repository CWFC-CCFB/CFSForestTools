package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014;

import java.util.ArrayList;
import java.util.Collection;

public class Heightable2014StandImpl implements Heightable2014Stand {

	final String subjectID;
	int monteCarloRealizationID;
	final double basalAreaM2Ha;
	final double meanQuadraticDiameter;
	final String ecoRegion;
	final String ecoType;
	boolean isInterventionResult;
	final double elevationM;
	final double meanAnnualTemperatureC;
	final double meanAnnualPrecipitationMm;
	boolean isDefoliated;
	final Collection<Heightable2014Tree> trees;
	
	Heightable2014StandImpl(String subjectID,
			double basalAreaM2Ha, 
			double meanQuadraticDiameter, 
			String ecoRegion, 
			String ecoType, 
			double elevationM, 
			double meanAnnualTemperatureC,
			double meanAnnualPrecipitationMm) {
		this.subjectID = subjectID;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.meanQuadraticDiameter = meanQuadraticDiameter;
		this.ecoRegion = ecoRegion;
		this.ecoType = ecoType;
		this.elevationM = elevationM;
		this.meanAnnualTemperatureC = meanAnnualTemperatureC;
		this.meanAnnualPrecipitationMm = meanAnnualPrecipitationMm;
		this.trees = new ArrayList<Heightable2014Tree>();
	}
	
	
	@Override
	public String getSubjectId() {return subjectID;}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloRealizationID;
	}

	@Override
	public double getBasalAreaM2Ha() {
		return basalAreaM2Ha;
	}

	@Override
	public double getMeanAnnualTemperatureC() {
		return meanAnnualTemperatureC;
	}

	@Override
	public String getEcoRegion() {
		return ecoRegion;
	}

	@Override
	public String getEcologicalType() {
		return ecoType;
	}

	@Override
	public boolean isInterventionResult() {
		return isInterventionResult;
	}

	@Override
	public double getElevationM() {
		return elevationM;
	}

	@Override
	public boolean isSBWDefoliated() {
		return isDefoliated;
	}

//	@Override
//	public double getNumberOfStemsHa() {
//		return numberOfStemsHa;
//	}

	@Override
	public double getMeanAnnualPrecipitationMm() {
		return meanAnnualPrecipitationMm;
	}

	@Override
	public double getMeanQuadraticDiameterCm() {
		return meanQuadraticDiameter;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection getTrees() {
		return trees;
	}


//	@Override
//	public List<HDRelationshipStand> getAllHDStands() {
//		List<HDRelationshipStand> stands = new ArrayList<HDRelationshipStand>();
//		stands.add(this);
//		return stands;
//	}

}
