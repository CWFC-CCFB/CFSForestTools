/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package allometry.volume.honer1983;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;

/**
 * The HonerTotalVolumePredictor class implements the underbark volume model developed 
 * by Honer 1983. It provides the total tree volume underbark based on tree dbh and height. <br>
 * <br>
 * IMPORTANT: The implementation is only deterministic since the covariance matrix and the variance of
 * the residual error terms are not provided.
 * @author Mathieu Fortin - March 2013, July 2026
 * @see <a href=https://ostrnrcan-dostrncan.canada.ca/entities/publication/53af9fe6-4849-4279-a129-7e67196b0927>  
 * Honer, T.G., Ker, M.F., and Alemdag, I.S. 1983. Metric timber tables for commercial
 * tree species of central and eastern Canada. Environment Canada. Canadian Forestry Service.
 * Maritimes Forest Research Centre. Information Report M-X-140.
 * </a> 
 */
@SuppressWarnings("serial")
@SimulationModule(type = ModuleType.Volume, scope = SpeciesLocale.Canada)
public class Honer1983VolumePredictor extends REpiceaPredictor implements REpiceaSpeciesCompliantObject {

	/*
	 * Parameter estimates for the imperial system taken from Honer et al. (1983), Table 3
	 */
	private static final Map<Species, double[]> ParmsMap = new HashMap<Species, double[]>();
	static {
		ParmsMap.put(Species.Pinus_strobus, new double[] {			0.184, 	0.691, 	363.676});
		ParmsMap.put(Species.Pinus_resinosa, new double[] {			0.151, 	0.710, 	355.623});
		ParmsMap.put(Species.Pinus_banksiana, new double[] {		0.151, 	0.897, 	348.530});
		ParmsMap.put(Species.Pinus_contorta, new double[] {			0.118, 	0.694, 	343.896});
		ParmsMap.put(Species.Picea_mariana, new double[] {			0.164, 	1.588, 	333.364});
		ParmsMap.put(Species.Picea_rubens, new double[] {			0.169, 	1.226, 	315.832});
		ParmsMap.put(Species.Picea_glauca, new double[] {			0.176, 	1.440, 	342.175});
		ParmsMap.put(Species.Abies_balsamea, new double[] {			0.152, 	2.139, 	301.634});
		ParmsMap.put(Species.Thuja_occidentalis, new double[] {		0.155, 	4.167, 	244.906});
		ParmsMap.put(Species.Tsuga_canadensis, new double[] {		0.155, 	1.112, 	350.092});
		ParmsMap.put(Species.Populus_tremuloides, new double[] {	0.127, 	-0.312, 436.683});
		ParmsMap.put(Species.Populus_balsamifera, new double[] {	0.127, 	0.420, 	394.644});
		ParmsMap.put(Species.Betula_papyrifera, new double[] {		0.176, 	2.222, 	300.373});
		ParmsMap.put(Species.Betula_alleghaniensis, new double[] {	0.181, 	1.449, 	344.754});
		ParmsMap.put(Species.Acer_spp, new double[] {				0.145, 	1.046, 	383.972});
		ParmsMap.put(Species.Tilia_americana, new double[] {		0.145, 	0.948, 	401.456});
		ParmsMap.put(Species.Fagus_grandifolia, new double[] {		0.145, 	0.959, 	334.829});
		ParmsMap.put(Species.Prunus_serotina, new double[] {		0.145, 	0.033, 	393.336});
		ParmsMap.put(Species.Ulmus_americana, new double[] {		0.145, 	0.634, 	440.496});
		ParmsMap.put(Species.Ostrya_virginiana, new double[] {		0.145, 	1.877, 	332.585});
		ParmsMap.put(Species.Quercus_rubra, new double[] {			0.145, 	1.512, 	336.509});
	}
	
	/*
	 * Parameter estimates converted to the metric system using Honer et al. (1983), Eq. 14
	 */
	private static final Map<Species, Matrix> FormattedParmsMap = new HashMap<Species, Matrix>();
	static {
		for (Species sp : ParmsMap.keySet()) {
			Matrix mat = new Matrix(3,1);  // slot 0 = c1, slot 1 = c2, slot 2 = b2
			double[] parms = ParmsMap.get(sp);
			double tmp = 1 - 0.04365 * parms[0];
			double b2_formatted = 0.0043891 * tmp * tmp;
			double c1_formatted = parms[1];
			double c2_formatted = 0.3048 * parms[2]; 
					
			mat.setValueAt(0, 0, c1_formatted);
			mat.setValueAt(1, 0, c2_formatted);
			mat.setValueAt(2, 0, b2_formatted);
			FormattedParmsMap.put(sp, mat);
		}
	}

	private static List<Species> EligibleSpecies;
	static {
		List<Species> speciesList = new ArrayList<Species>(ParmsMap.keySet());
		Collections.sort(speciesList);
		EligibleSpecies = Collections.unmodifiableList(speciesList);
	}
	
	private final ConcurrentHashMap<Species, Species> surrogateMap;
	
	/**
	 * General constructor for this class. 
	 */
	public Honer1983VolumePredictor() {
		super(false, false,false);
		surrogateMap = new ConcurrentHashMap<Species, Species>();
		setSurrogateMapToDefaultValue();
		init();
	}
	
	@Override
	protected final void init() {}
	
	/**
	 * This method returns the total volume of a particular tree.
	 * @param tree a HonerTotalVolumeTree instance
	 * @return the total volume (m3)
	 */
	public double predictTreeTotalUnderbarkVolumeM3(Honer1983VolumableTree tree) {
		Species sp = convertToEligibleSpecies(tree.getREpiceaSpecies());
		Matrix beta = FormattedParmsMap.get(sp);
		
		double c1 = beta.getValueAt(0, 0);
		double c2 = beta.getValueAt(1, 0);
		double b2 = beta.getValueAt(2, 0);
		double h = tree.getHeightM();
		
		return b2 * tree.getSquaredDbhCm() / (c1 + c2 / h);
	}

	@Override
	public List<Species> getEligibleSpecies() {return EligibleSpecies;}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Canada;}

	@Override
	public ConcurrentHashMap<Species, Species> getSurrogateMap() {return surrogateMap;}

	@Override
	public void setSurrogateMapToDefaultValue() {
		// TODO Auto-generated method stub
		
	}
	

}
