/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2024 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package canforservutility.predictor.ungetal2009;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biosimclient.BioSimClient;
import biosimclient.BioSimClientException;
import biosimclient.BioSimDataSet;
import biosimclient.BioSimEnums.ClimateModel;
import biosimclient.BioSimEnums.Period;
import biosimclient.BioSimEnums.RCP;
import biosimclient.BioSimPlot;
import biosimclient.BioSimPlotImpl;
import biosimclient.BioSimServerException;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.data.DataSet;
import repicea.util.ObjectUtility;

/**
 * An implementation of Ung et al. (2009) model.<p>
 * 
 * Ung et al. (2009) presented two models, that are referred to as Nat1 and Nat2, for 25 species. The Nat1 model is a 
 * system of three equations that predict dominant height, basal area and merchantable volume. The Nat2 model is a single
 * equation that predicts merchantable volume. Both models rely on the degree-days (base 0C) and the mean annual precipitation 
 * for the period 1971-2000.<p> 
 * 
 * The implementation offers methods that rely on either geographical locations or climate variables. The methods relying on 
 * geographical locations use BioSIM Web API to retrieve the degree-days and mean annual precipitation. <p>
 * 
 * This model does NOT implement stochastic features. 
 * 
 * @author Mathieu Fortin - February 2024
 * @see <a href=https://doi.org/10.5558/tfc85057-1> Ung, C.-H., Bernier, P. Y., Guo, X. J. and Lambert, M.-C. 2009.
 * A simple growth and yield model for assessing changes in standing volume across Canada's forests. The Forestry
 * Chronicle 85(1):57-64. </a>
 */
@SuppressWarnings("serial")
public class UngEtAl2009Predictor extends REpiceaPredictor {

	public enum UngEtAl2009Species {
		ABIES_BALSAMEA,
		ABIES_LASIOCARPA,
		ACER_RUBRUM,
		ACER_SACCHARUM,
		BETULA_ALLEGHANIENSIS,
		BETULA_PAPYRIFERA,
		FAGUS_GRANDIFOLIA,
		LARIX_LARICINA,
		LARIX_OCCIDENTALIS,
		PICEA_ENGELMANNII,
		PICEA_GLAUCA,
		PICEA_MARIANA,
		PICEA_RUBENS,
		PINUS_BANKSIANA,
		PINUS_CONTORTA,
		PINUS_RESINOSA,
		PINUS_STROBUS,
		POPULUS_BALSAMIFERA,
		POPULUS_GRANDIDENTATA,
		POPULUS_TREMULOIDES,
		PSEUDOTSUGA_MENZIESII,
		QUERCUS_RUBRA,
		THUJA_OCCIDENTALIS,
		TSUGA_CANADENSIS,
		TSUGA_HETEROPHYLLA;
	}		
	
	private static enum ParmNat1 {
		h10,
		h11,
		h12,
		h20,
		h21,
		h22,
		g10,
		g11,
		g12,
		g20,
		g21,
		g22,
		v30,
		v31,
		v32,
		CDH,
		CDG,
		CDV;
	}

	private static enum ParmNat2 {
		v10,
		v11,
		v12,
		v20,
		v21,
		v22,
		Cd;
	}

	static Map<UngEtAl2009Species, Matrix> Nat1ParmsMap;
	static Map<UngEtAl2009Species, Matrix> Nat2ParmsMap;

	/**
	 * Constructor.
	 */
	public UngEtAl2009Predictor() {
		super(false, false, false); // the model is fully deterministic
		if (Nat1ParmsMap == null || Nat2ParmsMap == null) {
			init();
		}
	}

	@Override
	protected synchronized void init() {
		if (Nat1ParmsMap == null || Nat2ParmsMap == null) {
			setNat1ParmsMap();
			setNat2ParmsMap();
		}
	}

	private void setNat1ParmsMap() {
		Nat1ParmsMap = new HashMap<UngEtAl2009Species, Matrix>();
		String parmsFilename = ObjectUtility.getRelativePackagePath(getClass()) + "NatOneWide.csv";
		try {
			@SuppressWarnings("resource")
			CSVReader reader = new CSVReader(parmsFilename);
			Object[] record;
			while((record = reader.nextRecord()) != null) {
				String speciesName = record[0].toString();
				UngEtAl2009Species species = UngEtAl2009Species.valueOf(speciesName.trim().replace(" ", "_").toUpperCase());
				Matrix mat = new Matrix(record.length - 1, 1);
				for (ParmNat1 p : ParmNat1.values()) {
					try {
						double parmEst = Double.parseDouble(record[p.ordinal() + 1].toString());	
						mat.setValueAt(p.ordinal(), 0, parmEst);
					} catch (NumberFormatException e) {
						mat.setValueAt(p.ordinal(), 0, 0d);
					}
				}
				Nat1ParmsMap.put(species, mat);
			}
			reader.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void setNat2ParmsMap() {
		Nat2ParmsMap = new HashMap<UngEtAl2009Species, Matrix>();
		String parmsFilename = ObjectUtility.getRelativePackagePath(getClass()) + "NatTwoWide.csv";
		try {
			@SuppressWarnings("resource")
			CSVReader reader = new CSVReader(parmsFilename);
			Object[] record;
			while((record = reader.nextRecord()) != null) {
				String speciesName = record[0].toString();
				UngEtAl2009Species species = UngEtAl2009Species.valueOf(speciesName.trim().replace(" ", "_").toUpperCase());
				Matrix mat = new Matrix(record.length - 1, 1);
				for (ParmNat2 p : ParmNat2.values()) {
					try {
						double parmEst = Double.parseDouble(record[p.ordinal() + 1].toString());	
						mat.setValueAt(p.ordinal(), 0, parmEst);
					} catch (NumberFormatException e) {
						mat.setValueAt(p.ordinal(), 0, 0d);
					}
				}
				Nat2ParmsMap.put(species, mat);
			}
			reader.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private double getParm(UngEtAl2009Species species, ParmNat1 parmName) {
		return Nat1ParmsMap.get(species).getValueAt(parmName.ordinal(), 0);
	}

	private double getParm(UngEtAl2009Species species, ParmNat2 parmName) {
		return Nat2ParmsMap.get(species).getValueAt(parmName.ordinal(), 0);
	}

	private double predictLnHeightNat1(UngEtAl2009Species species, double ageYr, double degreeDaysRef0, double precipitationMM) {
		double h1 = 0;
		h1 += getParm(species, ParmNat1.h10);
		h1 += getParm(species, ParmNat1.h11) * degreeDaysRef0;
		h1 += getParm(species, ParmNat1.h12) * precipitationMM;
		double h2 = 0;
		h2 += getParm(species, ParmNat1.h20);
		h2 += getParm(species, ParmNat1.h21) * degreeDaysRef0;
		h2 += getParm(species, ParmNat1.h22) * precipitationMM;
		return h1 + h2 / ageYr;
	}

	private double predictLnBasalAreaNat1(UngEtAl2009Species species, double ageYr, double degreeDaysRef0, double precipitationMM) {
		double g1 = 0;
		g1 += getParm(species, ParmNat1.g10);
		g1 += getParm(species, ParmNat1.g11) * degreeDaysRef0;
		g1 += getParm(species, ParmNat1.g12) * precipitationMM;
		double g2 = 0;
		g2 += getParm(species, ParmNat1.g20);
		g2 += getParm(species, ParmNat1.g21) * degreeDaysRef0;
		g2 += getParm(species, ParmNat1.g22) * precipitationMM;
		return g1 + g2 / ageYr;
	}
	
	private double predictLnStandingVolumeNat1(UngEtAl2009Species species, double ageYr, double lnH, double lnG) {
		double lnV = getParm(species, ParmNat1.v30) + getParm(species, ParmNat1.v31) * lnH + getParm(species, ParmNat1.v32) * lnG;
		return lnV;
	}

	
	/**
 	 * Provide height, basal area and standing volume predictions using the Nat1 system of equations.<p>
 	 * 
 	 * The output Dataset instance has the following fields:
 	 * <ul>
	 * <li> AgeYr: plot age (yr) of LoggableTree instances that defines the trees to be processed;
	 * <li> degreeDaysRef0: annual degree-days (base 0C) for the period 1971-2000;
	 * <li> precipitationMM: mean annual precipitation (mm) for the period 1971-2000;
	 * <li> HeightM: plot dominant height (m);
	 * <li> BasalAreaM2Ha: plot basal area (m2/a)
	 * <li> MerchVolumeM3Ha: plot merchantable volume (m3/ha). 
	 * </ul>
	 * 
	 * @param species a UngEtAl2009Species enum
	 * @param upToAgeYr the upper limit of the time window in terms of plot age (yr). Must be equal to or greater than 1.
	 * @param latitudeDeg the latitude of the plot (degrees)
	 * @param longitudeDeg the longitude of the plot (degrees)
	 * @return a DataSet instance 
	 * @throws BioSimClientException if something goes wrong on the client side
	 * @throws BioSimServerException if something goes wrong on the server side
	 */
	public DataSet predictPlotStandingVolumeUsingNat1ModelWithGeography(UngEtAl2009Species species, int upToAgeYr, double latitudeDeg, double longitudeDeg) throws BioSimClientException, BioSimServerException {
//		long startTime = System.currentTimeMillis();
		if (upToAgeYr < 1) {
			throw new InvalidParameterException("The upToAgeYr argument must be equal to or greater than 1!");
		}
		double[] climVars = getDDandPrecipitation(latitudeDeg, longitudeDeg); 
		DataSet output = predictPlotStandingVolumeUsingNat1ModelWithClimateVariables(species, upToAgeYr, climVars[0], climVars[1]);
//		System.out.println("Elapsed time = " + (System.currentTimeMillis() - startTime));
		return output;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private double[] getDDandPrecipitation(double latitudeDeg, double longitudeDeg) throws BioSimClientException, BioSimServerException {
		BioSimPlot p = new BioSimPlotImpl(latitudeDeg, longitudeDeg, Double.NaN);
		List<BioSimPlot> biosimPlots = new ArrayList<BioSimPlot>();
		biosimPlots.add(p);
		BioSimDataSet ds = (BioSimDataSet) ((LinkedHashMap) BioSimClient.generateWeather(1971, 
				2000, 
				biosimPlots, 
				RCP.RCP45, 
				ClimateModel.GCM4, Arrays.asList(new String[]{"DegreeDay_Annual"}), null).get("DegreeDay_Annual")).get(p);
		List<Double> ddByYear = (List) ds.getFieldValues(ds.getFieldNames().indexOf("DD"));
		double degreeDaysRef0 = 0d;
		for (Double dd : ddByYear) {
			degreeDaysRef0 += dd;
		}
		degreeDaysRef0 /= ddByYear.size();
		ds = BioSimClient.getAnnualNormals(Period.FromNormals1971_2000, biosimPlots, RCP.RCP45, ClimateModel.GCM4).get(p);
		double precipitationMM = (Double) ds.getFieldValues(ds.getFieldNames().indexOf("P")).get(0);
		double[] output = new double[2];
		output[0] = degreeDaysRef0;
		output[1] = precipitationMM;
		return output;
	}
	
	/**
 	 * Provide height, basal area and standing volume predictions using the Nat1 system of equations.<p>
 	 * 
 	 * The output Dataset instance has the following fields:
 	 * <ul>
	 * <li> AgeYr: plot age (yr) of LoggableTree instances that defines the trees to be processed;
	 * <li> degreeDaysRef0: annual degree-days (base 0C) for the period 1971-2000;
	 * <li> precipitationMM: mean annual precipitation (mm) for the period 1971-2000;
	 * <li> HeightM: plot dominant height (m);
	 * <li> BasalAreaM2Ha: plot basal area (m2/a)
	 * <li> MerchVolumeM3Ha: plot merchantable volume (m3/ha). 
	 * </ul>
	 * 
	 * @param species a UngEtAl2009Species enum
	 * @param upToAgeYr the upper limit of the time window in terms of plot age (yr). Must be equal to or greater than 1.
	 * @param degreeDaysRef0 annual degree-days (base 0C) for the period 1971-2000
	 * @param precipitationMM mean annual precipitation (mm) for the period 1971-2000
	 * @return a DataSet instance 
	 */
	public DataSet predictPlotStandingVolumeUsingNat1ModelWithClimateVariables(UngEtAl2009Species species, int upToAgeYr, double degreeDaysRef0, double precipitationMM) {
		DataSet output = new DataSet(Arrays.asList(new String[] {"AgeYr", "degreeDaysRef0", "precipitationMM", "HeightM", "BasalAreaM2Ha", "MerchVolumeM3Ha"}));
		Object[] obs;
		for (int ageYr = 1; ageYr <= upToAgeYr; ageYr++) {
			double lnH = predictLnHeightNat1(species, ageYr, degreeDaysRef0, precipitationMM); 
			double lnG = predictLnBasalAreaNat1(species, ageYr, degreeDaysRef0, precipitationMM);
			double lnV = predictLnStandingVolumeNat1(species, ageYr, lnH, lnG);
			double H = Math.exp(lnH) * getParm(species, ParmNat1.CDH); 
			double G = Math.exp(lnG) * getParm(species, ParmNat1.CDG);
			double V = Math.exp(lnV) * getParm(species, ParmNat1.CDV);
			obs = new Object[6];
			obs[0] = ageYr;
			obs[1] = degreeDaysRef0;
			obs[2] = precipitationMM;
			obs[3] = H;
			obs[4] = G;
			obs[5] = V;
			output.addObservation(obs);
		}
		return output;
	}

	
	/**
 	 * Provide height, basal area and standing volume predictions using the Nat2 single equation.<p>
 	 * 
 	 * The output Dataset instance has the following fields:
 	 * <ul>
	 * <li> AgeYr: plot age (yr) of LoggableTree instances that defines the trees to be processed;
	 * <li> degreeDaysRef0: annual degree-days (base 0C) for the period 1971-2000;
	 * <li> precipitationMM: mean annual precipitation (mm) for the period 1971-2000;
	 * <li> MerchVolumeM3Ha: plot merchantable volume (m3/ha). 
	 * </ul>
	 * 
	 * @param species a UngEtAl2009Species enum
	 * @param upToAgeYr the upper limit of the time window in terms of plot age (yr). Must be equal to or greater than 1.
	 * @param degreeDaysRef0 annual degree-days (base 0C) for the period 1971-2000
	 * @param precipitationMM mean annual precipitation (mm) for the period 1971-2000
	 * @return a DataSet instance 
	 */
	public DataSet predictPlotStandingVolumeUsingNat2ModelWithClimateVariables(UngEtAl2009Species species, int upToAgeYr, double degreeDaysRef0, double precipitationMM) {
		DataSet output = new DataSet(Arrays.asList(new String[] {"AgeYr", "degreeDaysRef0", "precipitationMM", "MerchVolumeM3Ha"}));
		Object[] obs;
		for (int ageYr = 1; ageYr <= upToAgeYr; ageYr++) {
			double lnV = predictLnStandingVolumeNat2(species, ageYr, degreeDaysRef0, precipitationMM);
			double V = Math.exp(lnV) * getParm(species, ParmNat2.Cd);
			obs = new Object[6];
			obs[0] = ageYr;
			obs[1] = degreeDaysRef0;
			obs[2] = precipitationMM;
			obs[3] = V;
			output.addObservation(obs);
		}
		return output;
	}

	/**
 	 * Provide height, basal area and standing volume predictions using the Nat2 single equation.<p>
 	 * 
 	 * The output Dataset instance has the following fields:
 	 * <ul>
	 * <li> AgeYr: plot age (yr) of LoggableTree instances that defines the trees to be processed;
	 * <li> degreeDaysRef0: annual degree-days (base 0C) for the period 1971-2000;
	 * <li> precipitationMM: mean annual precipitation (mm) for the period 1971-2000;
	 * <li> MerchVolumeM3Ha: plot merchantable volume (m3/ha). 
	 * </ul>
	 * 
	 * @param species a UngEtAl2009Species enum
	 * @param upToAgeYr the upper limit of the time window in terms of plot age (yr). Must be equal to or greater than 1.
	 * @param latitudeDeg the latitude of the plot (degrees)
	 * @param longitudeDeg the longitude of the plot (degrees)
	 * @return a DataSet instance 
	 * @throws BioSimClientException if something goes wrong on the client side
	 * @throws BioSimServerException if something goes wrong on the server side
	 */
	public DataSet predictPlotStandingVolumeUsingNat2ModelWithGeography(UngEtAl2009Species species, int upToAgeYr, double latitudeDeg, double longitudeDeg) throws BioSimClientException, BioSimServerException {
		long startTime = System.currentTimeMillis();
		if (upToAgeYr < 1) {
			throw new InvalidParameterException("The upToAgeYr argument must be equal to or greater than 1!");
		}
		double[] climVars = getDDandPrecipitation(latitudeDeg, longitudeDeg); 
		DataSet output = predictPlotStandingVolumeUsingNat2ModelWithClimateVariables(species, upToAgeYr, climVars[0], climVars[1]);
		System.out.println("Elapsed time = " + (System.currentTimeMillis() - startTime));
		return output;
	}

	private double predictLnStandingVolumeNat2(UngEtAl2009Species species, int ageYr, double degreeDaysRef0, double precipitationMM) {
		double lnV = getParm(species, ParmNat2.v10) + getParm(species, ParmNat2.v11) * degreeDaysRef0 + getParm(species, ParmNat2.v12) * precipitationMM +
				(getParm(species, ParmNat2.v20) + getParm(species, ParmNat2.v21) * degreeDaysRef0 + getParm(species, ParmNat2.v22) * precipitationMM) / ageYr;
		return lnV;
	}
	
//	public static void main(String[] args) throws BioSimClientException, BioSimServerException {
//		DataSet pred = new UngEtAl2009Predictor().predictPlotStandingVolumeUsingNat1ModelWithGeography(UngEtAl2009Species.PICEA_MARIANA, 60, 47, -78);
//		DataSet pred2 = new UngEtAl2009Predictor().predictPlotStandingVolumeUsingNat2ModelWithGeography(UngEtAl2009Species.PICEA_MARIANA, 60, 47, -78);
//		int u = 0;
//	}
	
}
