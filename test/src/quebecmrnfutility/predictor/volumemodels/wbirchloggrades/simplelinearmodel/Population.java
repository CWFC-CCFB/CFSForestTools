/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades.simplelinearmodel;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.stats.sampling.BootstrapHybridPointEstimate;
import repicea.stats.sampling.BootstrapHybridPointEstimate.VarianceEstimatorImplementation;
import repicea.stats.sampling.FinitePopulationEstimate;
import repicea.util.ObjectUtility;

public class Population {

	final PlotList sampleUnits;
	Random random = new Random();
	final SimpleLinearModel superModel = new SimpleLinearModel(false, true);	// no randomness on parameters since this is the superpopulation model
	
	Population(int populationSize) {
		sampleUnits = new PlotList();
		SamplePlot p;
		for (int i = 0; i < populationSize; i++) {
			double x = (2 + random.nextDouble() * 6d);
			p = new SamplePlot(i + "", x);
			sampleUnits.add(p);
		}
	}
	
	PlotList getSample(int sampleSize) {
		List<Integer> sampleIndex = new ArrayList<Integer>();
		int index;
		while (sampleIndex.size() < sampleSize) {
			index = (int) Math.floor(random.nextDouble() * sampleUnits.size());
			if (!sampleIndex.contains(index)) {
				sampleIndex.add(index);
			}
		}
		PlotList sample = new PlotList();
		for (Integer ind : sampleIndex) {
			sample.add(sampleUnits.get(ind));
		}
		return sample;
	}
	
	static void setRealizedValues(List<SamplePlot> plots, SimpleLinearModel model) {
		for (SamplePlot plot : plots) {
			plot.setY(model.predictY(plot));
		}
	}
	
	Matrix getTotal() {
		Matrix total = new Matrix(1,1);
		for (SamplePlot plot : sampleUnits) {
			total = total.add(plot.getY());
		}
		return total;
	}
	
	
	public static void main(String[] args) throws IOException, CloneNotSupportedException {
		boolean isCompleteBootstrap = true;
		boolean simpleReplacement = true;		// default is true
		SimpleLinearModel.R2_95Version = false;	// default is false
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		long start = System.currentTimeMillis();
		int populationSize = 1000;
		Population pop = new Population(populationSize);
		int nbRealizations = 10000; 
		int nbInternalReal = 1000;
		int sampleSize = 25;
		String filename;
		if (SimpleLinearModel.R2_95Version) {
			filename = ObjectUtility.getPackagePath(Population.class) + "simulationR2_95_" + sampleSize + ".csv";
		} else if (!simpleReplacement) {
			filename = ObjectUtility.getPackagePath(Population.class) + "fromDataset_" + sampleSize + ".csv";
		} else if (isCompleteBootstrap) {
			filename = ObjectUtility.getPackagePath(Population.class) + "compBootstrap_" + sampleSize + ".csv";
		} else {
			filename = ObjectUtility.getPackagePath(Population.class) + "simulation" + sampleSize + ".csv";
		}
		filename = filename.replace("bin", "manuscripts");
		
		CSVWriter writer = new CSVWriter(new File(filename), false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("TrueTau"));
		fields.add(new CSVField("EstTau"));
		fields.add(new CSVField("UncorrVar"));
		fields.add(new CSVField("CorrVar"));
		fields.add(new CSVField("Samp"));
		fields.add(new CSVField("Model"));
		writer.setFields(fields);

		String filenameSingleSimulation = ObjectUtility.getPackagePath(Population.class) + "stabilize" + sampleSize + ".csv";
		filenameSingleSimulation = filenameSingleSimulation.replace("bin", "manuscripts");
		CSVWriter writerStabilizer = new CSVWriter(new File(filenameSingleSimulation), false);
		List<FormatField> fieldsStabilizer = new ArrayList<FormatField>();
		fieldsStabilizer.add(new CSVField("RealID"));
		fieldsStabilizer.add(new CSVField("Mean"));
		fieldsStabilizer.add(new CSVField("Var"));
		writerStabilizer.setFields(fieldsStabilizer);
		boolean isWriterStabilizerOpen = true;

		Object[] recordStabilizer = new Object[3];
		List<Realization> realizations = new ArrayList<Realization>();
		long timeDiff;
		for (int real = 0; real < nbRealizations; real++) {
			setRealizedValues(pop.sampleUnits, pop.superModel);
			Matrix total = pop.getTotal();
			SimpleLinearModel currentModel = new SimpleLinearModel(true, true); // the current model must account for the errors in the parameter estimates
			PlotList sample;
			if (simpleReplacement) {
				currentModel.replaceModelParameters();	// the parameter estimates are drawn at random in the distribution
				sample = pop.getSample(sampleSize);
			} else {
				sample = pop.getSample(sampleSize + 100);
				PlotList dataSet = new PlotList();
				for (int i = 0; i < 100; i++) {
					dataSet.add(sample.remove(0));
				}
				currentModel.replaceModelParameters(dataSet);
			}
			BootstrapHybridPointEstimate hybHTEstimate = new BootstrapHybridPointEstimate();
			PlotList referenceSample = sample;
			for (int internalReal = 0; internalReal < nbInternalReal; internalReal++) {
				if (isCompleteBootstrap) {
					sample = referenceSample.getBootstrapSample();
				} else {
					sample = referenceSample;
				}
				sample.setRealization(internalReal);
				setRealizedValues(sample, currentModel);
				FinitePopulationEstimate htEstimator = sample.getHorvitzThompsonEstimate(populationSize);
				hybHTEstimate.addPointEstimate(htEstimator);
				if (!SimpleLinearModel.R2_95Version && real == 0 && internalReal >= 1) {
					recordStabilizer[0] = internalReal;
					recordStabilizer[1] = hybHTEstimate.getMean().getValueAt(0, 0);
					recordStabilizer[2] = hybHTEstimate.getVariance().getValueAt(0, 0);
					writerStabilizer.addRecord(recordStabilizer);
				} else if (real > 0 && isWriterStabilizerOpen) {
					writerStabilizer.close();
					isWriterStabilizerOpen = false;
				}
			}
			Matrix correctedVarEstimate = hybHTEstimate.getVariance();
			hybHTEstimate.setVarianceEstimatorImplementation(VarianceEstimatorImplementation.RegularMultipleImputation);
			double uncorrectedVariance = hybHTEstimate.getVariance().getValueAt(0, 0);
			if (isCompleteBootstrap) {
				uncorrectedVariance = hybHTEstimate.getModelRelatedVariance().getValueAt(0, 0);  // only the variance of the point estimates for the complete bootstrap
			}
			hybHTEstimate.setVarianceEstimatorImplementation(VarianceEstimatorImplementation.Corrected);
			Realization thisRealization = new Realization(total.getValueAt(0, 0), 
					hybHTEstimate.getMean().getValueAt(0, 0), 
					uncorrectedVariance, 
					correctedVarEstimate.getValueAt(0, 0), 
					hybHTEstimate.getSamplingRelatedVariance().getValueAt(0, 0), 
					hybHTEstimate.getModelRelatedVariance().getValueAt(0, 0));
			realizations.add(thisRealization);
			writer.addRecord(thisRealization.getRecord());
			timeDiff = System.currentTimeMillis() - start;
		    double timeByReal = timeDiff / (real + 1);
		    double remainingTime = timeByReal * (nbRealizations - (real + 1)) * 0.001 / 60;
			System.out.println("Running realization " + real +"; Remaining time " + nf.format(remainingTime) + " min.");
		}
		writer.close();
	}
	
}
