/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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
