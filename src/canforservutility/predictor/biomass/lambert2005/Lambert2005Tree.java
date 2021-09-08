package canforservutility.predictor.biomass.lambert2005;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;

/**
 * 
 * 
 * @author Jean-Francois Lavoie, Aug 2021
 */

public interface Lambert2005Tree extends DbhCmProvider, HeightMProvider, MonteCarloSimulationCompliantObject {
	
	public enum Lambert2005Species {	
		Any,
		Coniferous,
		Broadleaved,
		AbiesBalsamea,
		PopulusBalsamifera,
		FraxinusNigra,
		PrunusSerotina,
		PiceaMariana,
		TsugaCanadensis,
		ThujaOccidentalis,
		PinusStrobus,
		BetulaPopulifolia,
		PinusContorta,
		PinusResinosa,
		AcerSaccharinum,
		AcerSaccharum,
		LarixLaricina,
		BetulaPapyrifera,
		QuercusAlba,
		PiceaGlauca,
		AbiesLasiocarpa,
		TiliaAmericana,
		FagusGrandifolia,
		JuniperusVirginiana,
		CaryaSp,
		OstryaVirginiana,
		PinusBanksiana,
		PopulusGrandidentata,
		FraxinusPennsylvanica,
		AcerRubrum,
		QuercusRubra,
		PiceaRubens,
		PopulusTremuloides,
		FraxinusAmericana,
		UlmusAmericana,
		BetulaAlleghaniensis		
	}
	
	public Lambert2005Species getLambert2005Species();
}
