package quebecmrnfutility.predictor.generalhdrelation2009;

import java.util.ArrayList;
import java.util.Collection;

import repicea.simulation.covariateproviders.standlevel.MeanQuadraticDiameterCmProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

public class Heightable2009StandImpl implements Heightable2009Stand, MeanQuadraticDiameterCmProvider {

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
	final Collection<Heightable2009Tree> trees;
	
	Heightable2009StandImpl(String subjectID,
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
		this.trees = new ArrayList<Heightable2009Tree>();
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

//	@Override
//	public double getElevationM() {
//		return elevationM;
//	}

	@Override
	public boolean isSBWDefoliated() {
		return isDefoliated;
	}

//	@Override
//	public double getNumberOfStemsHa() {
//		return numberOfStemsHa;
//	}

//	@Override
//	public double getMeanAnnualPrecipitationMm() {
//		return meanAnnualPrecipitationMm;
//	}
//
	@Override
	public double getMeanQuadraticDiameterCm() {
		return meanQuadraticDiameter;
	}

//	@SuppressWarnings("rawtypes")
//	@Override
//	public Collection getTrees() {
//		return trees;
//	}


	@SuppressWarnings("rawtypes")
	@Override
	public Collection getTrees(StatusClass statusClass) {
		return trees;
	}


	@Override
	public String getDrainageClass() {
		return "3";			// mesic for the sake of simplicity
	}


//	@Override
//	public List<HDRelationshipStand> getAllHDStands() {
//		List<HDRelationshipStand> stands = new ArrayList<HDRelationshipStand>();
//		stands.add(this);
//		return stands;
//	}

}
