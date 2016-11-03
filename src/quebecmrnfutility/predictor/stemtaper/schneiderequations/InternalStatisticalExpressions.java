/*
 * English version follows
 * 
 * Ce fichier fait partie de la biblioth�que mrnf-foresttools.
 * Il est prot�g� par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du minist�re des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Qu�bec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.stemtaper.schneiderequations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperEquationSettings.Effect;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.predictor.QuebecGeneralSettings;
import repicea.stats.LinearStatisticalExpression;

/**
 * This class handles the linear (or nonlinear) terms included in the StemTaperEquation class.
 * @author Mathieu Fortin - October 2011 
 */
class InternalStatisticalExpressions {

	private final StemTaperSubModule stemTaperPredictor;
	private LinearStatisticalExpression firstLinearTerm;
	private AbstractMathematicalFunction secondLinearTerm;		// abstract because it can be linear or nonlinear sometimes
	
	private StemTaperTree lastTree;
	
	private List<Integer> indexOfChangingEffects = new ArrayList<Integer>();


	/**
	 * This class extends the LinearStatisticalExpression in order to add a nonlinear term that is required 
	 * in the PEG version model.
	 * @author Mathieu Fortin - January 2012
	 */
	@SuppressWarnings("serial")
	static class CustomizedNonlinearStatisticalExpression extends AbstractMathematicalFunction {
		
		protected Matrix gradient;
		protected Matrix hessian;
		
		@Override
		public Double getValue() {
			if (getNumberOfParameters() != (getNumberOfVariables() + 1)) {		// there is one additional parameter
				throw new IllegalArgumentException("Incompatible vectors");
			} 
			double productResult = 0;
			productResult +=  - Math.exp(- getParameterValue(0) * Math.pow(getVariableValue(0), getParameterValue(1)));
			for (int i = 1; i < getNumberOfVariables(); i++) {
				productResult += getVariableValue(i) * getParameterValue(i + 1);
			}
			return productResult;
		}

		@Override
		public Matrix getGradient() {
			if (gradient == null || gradient.m_iRows != getNumberOfParameters()) {			// create a gradient matrix only once or only if the number of variables in x changes
				gradient = new Matrix(getNumberOfParameters(),1);								
			}
			double powerExpression = Math.pow(getVariableValue(0), getParameterValue(1));
			double basicExpression = - Math.exp(- getParameterValue(0) * powerExpression);
			gradient.m_afData[0][0] = basicExpression * - powerExpression;							// update the value in the gradient matrix
			gradient.m_afData[1][0] = basicExpression * - getParameterValue(0) * powerExpression * Math.log(getVariableValue(0));							// update the value in the gradient matrix
			for (int i = 2; i < getNumberOfParameters(); i++) {
				gradient.m_afData[i][0] = getVariableValue(i - 1);							// update the value in the gradient matrix
			}
			
			return gradient;
		}

		@Override
		public Matrix getHessian() {
			if (hessian == null || hessian.m_iCols != getNumberOfParameters()) {				// create a hessian matrix only once or only if the number of variables in x changes
				hessian = new Matrix(getNumberOfParameters(), getNumberOfParameters());
			}
//			hessian.resetMatrix();	// all values are reset to 0
			double powerExpression = Math.pow(getVariableValue(0), getParameterValue(1));
			double basicExpression = - Math.exp(- getParameterValue(0) * powerExpression);
			double logx = Math.log(getVariableValue(0));
			double tmp = - getParameterValue(0) * powerExpression + 1;
			
			double d2y_d2b0 = basicExpression * powerExpression * powerExpression;
			double d2y_d2b1 = basicExpression * - getParameterValue(0) * powerExpression * logx * logx * tmp;
			double d2y_db0db1 = basicExpression * - powerExpression * logx * tmp;

			hessian.m_afData[0][0] = d2y_d2b0;
			hessian.m_afData[1][1] = d2y_d2b1;
			hessian.m_afData[0][1] = d2y_db0db1;
			hessian.m_afData[1][0] = d2y_db0db1;
			
			return hessian;
		}

	}
	
	/**
	 * This class extends the LinearStatisticalExpression in order to add a
	 * nonlinear term that is required
	 * in the PIB version model.
	 * 
	 * @author Denis Hache - Mai 2014
	 */
	@SuppressWarnings("serial")
	static class CustomizedNonlinearPibStatisticalExpression extends AbstractMathematicalFunction {

		protected Matrix	gradient;
		protected Matrix	hessian;

		@Override
		public Double getValue() {
			if (getNumberOfParameters() != (getNumberOfVariables())) {
				throw new IllegalArgumentException("Incompatible vectors");
			}
			double productResult = 0;
			productResult += Math.pow(getVariableValue(1), getParameterValue(1)) * Math.pow(getVariableValue(2), getParameterValue(2));
			productResult += getVariableValue(1) * getParameterValue(1);
			productResult += getVariableValue(3) * getParameterValue(3);

			return productResult;
		}

		@Override
		public Matrix getGradient() {
			if (gradient == null || gradient.m_iRows != getNumberOfParameters()) {			// create a gradient matrix only once or only if the number of variables in x changes
				gradient = new Matrix(getNumberOfParameters(), 1);
			}
			double powerExpression1 = Math.pow(getVariableValue(1), getParameterValue(1));
			double powerExpression2 = Math.pow(getVariableValue(2), getParameterValue(2));
			double log1 = Math.log(getVariableValue(1));
			double log2 = Math.log(getVariableValue(2));
			
			gradient.m_afData[0][0] = 0;							// update the value in the gradient matrix
			gradient.m_afData[1][0] = powerExpression1 * powerExpression2 * log1;
			gradient.m_afData[2][0] = powerExpression1 * powerExpression2 * log2;
			gradient.m_afData[3][0] = getVariableValue(3);						

			return gradient;
		}

		@Override
		public Matrix getHessian() {
			if (hessian == null || hessian.m_iCols != getNumberOfParameters()) {				// create a hessian matrix only once or only if the number of variables in x changes
				hessian = new Matrix(getNumberOfParameters(), getNumberOfParameters());
			}
//			hessian.resetMatrix();	// all values are reset to 0
			double powerExpression1 = Math.pow(getVariableValue(1), getParameterValue(1));
			double powerExpression2 = Math.pow(getVariableValue(2), getParameterValue(2));
			double log1 = Math.log(getVariableValue(1));
			double log2 = Math.log(getVariableValue(2));			
			
			hessian.m_afData[1][1] = powerExpression1 * powerExpression2 * log1 * log1;
			hessian.m_afData[1][2] = powerExpression1 * powerExpression2 * log1 * log2;
			hessian.m_afData[2][1] = powerExpression1 * powerExpression2 * log2 * log1;
			hessian.m_afData[2][2] = powerExpression1 * powerExpression2 * log2 * log2;
			return hessian;
		}

	}
	
	
	/**
	 * Constructor.
	 * @param stemTaperPredictor a StemTaperPredictor instance
	 */
	InternalStatisticalExpressions(StemTaperSubModule stemTaperSubModule, StemTaperTreeSpecies species) {
		this.stemTaperPredictor = stemTaperSubModule;

		firstLinearTerm = new LinearStatisticalExpression();
		firstLinearTerm.setVariableValue(0, 1d);		// only an intercept for the first term

		if (species == StemTaperTreeSpecies.PEG) {
			secondLinearTerm = new CustomizedNonlinearStatisticalExpression();
		} else if (species == StemTaperTreeSpecies.PIB) {
			secondLinearTerm = new CustomizedNonlinearPibStatisticalExpression();
		} else {
			secondLinearTerm = new LinearStatisticalExpression();		// the intercept is set in the updateSecondLinearTerm
		}
	}


	/**
	 * This method update the second linear term at height section i.
	 * @param heightIndex the index of the height section
	 */
	private void updateSecondLinearTerm(int heightIndex) {
		int variableIndex = 0;
		Map<String, Matrix> oMap;
		Matrix oMat;
		StemTaperTree currentTree = stemTaperPredictor.getTree();
		boolean isNewTree = false;
		if (!currentTree.equals(lastTree)) {
			isNewTree = true;
			indexOfChangingEffects.clear();
			lastTree = currentTree;
		}

		int formerIndex;
		StemTaperStand currentStand = currentTree.getStand();
		List<Effect> effects = StemTaperEquationSettings.EFFECTS_MAP.get(stemTaperPredictor.modelType).get(currentTree.getStemTaperTreeSpecies());
		for (Effect effect : effects) {
			if (isNewTree) {	
				formerIndex = variableIndex;
				switch (effect) {
				case Intercept:
					secondLinearTerm.setVariableValue(variableIndex++, 1d);
					break;
				case SubDomain:
					String subDomain = QuebecGeneralSettings.ECO_REGION_MAP.get(currentStand.getEcoRegion());
					oMap = StemTaperEquationSettings.SUBDOMAIN_DUMMY_MAP.get(currentTree.getStemTaperTreeSpecies());
					oMat = oMap.get(subDomain);
					for (int i = 0; i < oMat.m_iCols; i++) {
						secondLinearTerm.setVariableValue(variableIndex++, oMat.m_afData[0][i]);
					}
					break;
				case ExpSectionRelativeHeight:
					secondLinearTerm.setVariableValue(variableIndex++, Math.exp(stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]));
					break;
				case CoreExpression:
					secondLinearTerm.setVariableValue(variableIndex++, stemTaperPredictor.coreExpression.m_afData[heightIndex][0]);
					break;
				case LogCoreExpression:
					secondLinearTerm.setVariableValue(variableIndex++, Math.log(stemTaperPredictor.coreExpression.m_afData[heightIndex][0]));
					break;
				case SectionRelativeHeight_x_CoreExpression:
					secondLinearTerm.setVariableValue(variableIndex++, stemTaperPredictor.relativeHeights.m_afData[heightIndex][0] * stemTaperPredictor.coreExpression.m_afData[heightIndex][0]);
					break;
				case SectionRelativeHeight:
					secondLinearTerm.setVariableValue(variableIndex++, stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]);
					break;
				case LogSectionRelativeHeight:
					secondLinearTerm.setVariableValue(variableIndex++, Math.log(stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]));
					break;
				case OneMinusSectionRelativeHeight:
					secondLinearTerm.setVariableValue(variableIndex++, 1 - stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]);
					break;
				case Drainage:
					oMap = StemTaperEquationSettings.DRAINAGE_GROUP_DUMMY_MAP.get(currentTree.getStemTaperTreeSpecies());
					oMat = oMap.get(currentStand.getDrainageClass());
					for (int i = 0; i < oMat.m_iCols; i++) {
						secondLinearTerm.setVariableValue(variableIndex++, oMat.m_afData[0][i]);
					}
					break;
				case VegPot:
					String potentialVegetation = currentStand.getEcologicalType().substring(0,3).toUpperCase();
					oMap = StemTaperEquationSettings.POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.get(currentTree.getStemTaperTreeSpecies());
					oMat = oMap.get(potentialVegetation);
					for (int i = 0; i < oMat.m_iCols; i++) {
						secondLinearTerm.setVariableValue(variableIndex++, oMat.m_afData[0][i]);
					}
					break;
				case DbhOB:
					secondLinearTerm.setVariableValue(variableIndex++, currentTree.getDbhCm() * 10);
					break;
				case Height:
					secondLinearTerm.setVariableValue(variableIndex++, currentTree.getHeightM());
					break;
				case BasalAreaPerHa:
					secondLinearTerm.setVariableValue(variableIndex++, currentStand.getBasalAreaM2Ha());
					break;
				case NumberOfStemsPerHa:
					secondLinearTerm.setVariableValue(variableIndex++, currentStand.getNumberOfStemsHa());
					break;
				case Elevation:
					secondLinearTerm.setVariableValue(variableIndex++, currentStand.getElevationM());
					break;
				default:
					throw new RuntimeException("No effect was catch in this switch loop"); 
				}
				if (!effect.isATreeConstantEffect()) {
					for (int i = formerIndex; i < variableIndex; i++) {
						indexOfChangingEffects.add(i);
					}
				}
			} else if (!effect.isATreeConstantEffect()) {
				switch (effect) {
				case ExpSectionRelativeHeight:
					secondLinearTerm.setVariableValue(indexOfChangingEffects.get(variableIndex++), Math.exp(stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]));
					break;
				case CoreExpression:
					secondLinearTerm.setVariableValue(indexOfChangingEffects.get(variableIndex++), stemTaperPredictor.coreExpression.m_afData[heightIndex][0]);
					break;
				case LogCoreExpression:
					secondLinearTerm.setVariableValue(indexOfChangingEffects.get(variableIndex++), Math.log(stemTaperPredictor.coreExpression.m_afData[heightIndex][0]));
					break;
				case SectionRelativeHeight_x_CoreExpression:
					secondLinearTerm.setVariableValue(indexOfChangingEffects.get(variableIndex++), stemTaperPredictor.relativeHeights.m_afData[heightIndex][0] * stemTaperPredictor.coreExpression.m_afData[heightIndex][0]);
					break;
				case SectionRelativeHeight:
					secondLinearTerm.setVariableValue(indexOfChangingEffects.get(variableIndex++), stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]);
					break;
				case LogSectionRelativeHeight:
					secondLinearTerm.setVariableValue(indexOfChangingEffects.get(variableIndex++), Math.log(stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]));
					break;
				case OneMinusSectionRelativeHeight:
					secondLinearTerm.setVariableValue(indexOfChangingEffects.get(variableIndex++), 1 - stemTaperPredictor.relativeHeights.m_afData[heightIndex][0]);
					break;
				default:
					throw new RuntimeException("No effect was caught in this switch loop"); 
				}
			}
		}
	}

	protected Matrix getValues() {
		Matrix output = new Matrix(stemTaperPredictor.heights.m_iRows, 2);
		for (int i = 0; i < stemTaperPredictor.heights.m_iRows; i++) {
			updateSecondLinearTerm(i);
			output.m_afData[i][0] = firstLinearTerm.getValue();
			output.m_afData[i][1] = secondLinearTerm.getValue();
		}
		return output;
	}

	protected void setParameters(Matrix beta) {
		firstLinearTerm.setParameterValue(0, beta.m_afData[0][0]);
		for (int i = 1; i < beta.m_iRows; i++) {
			secondLinearTerm.setParameterValue(i - 1, beta.m_afData[i][0]);
		}
	}
	
	/**
	 * This method returns the derivatives of each height with respect to the first linear term (1st column) and the second linear term (2nd column).
	 * @return a nx2 matrix
	 */
	protected Matrix getGradients() {
		Matrix basicGradient = new Matrix(stemTaperPredictor.heights.m_iRows, 2);
		
		Matrix parameters = getValues();
		double exponent;
		double alpha;
		double dbh2 = stemTaperPredictor.getTree().getSquaredDbhCm() * 100;
		for (int i = 0; i < stemTaperPredictor.heights.m_iRows; i++) {
			alpha = parameters.m_afData[i][0];
			exponent = parameters.m_afData[i][1];
			basicGradient.m_afData[i][0] = dbh2 * stemTaperPredictor.coreExpression.m_afData[i][0] * Math.pow(stemTaperPredictor.heightsSectionRespectToDbh.m_afData[i][0], 2 - exponent);
			basicGradient.m_afData[i][1] = alpha * basicGradient.m_afData[i][0] * Math.log(stemTaperPredictor.heightsSectionRespectToDbh.m_afData[i][0]) * -1d;
		}
		
		int firstTermNumberOfParameters = firstLinearTerm.getNumberOfParameters();
		int secondTermNumberOfParameters = secondLinearTerm.getNumberOfParameters();
		
		Matrix gradients = new Matrix(stemTaperPredictor.heights.m_iRows, firstTermNumberOfParameters + secondTermNumberOfParameters);
		for (int i = 0; i < stemTaperPredictor.heights.m_iRows; i++) {
			updateSecondLinearTerm(i);
			gradients.setSubMatrix(firstLinearTerm.getGradient().transpose().scalarMultiply(basicGradient.m_afData[i][0]), i, 0);
			gradients.setSubMatrix(secondLinearTerm.getGradient().transpose().scalarMultiply(basicGradient.m_afData[i][1]), i, firstTermNumberOfParameters);
		}		
		
		return gradients;
	}

	
	
	/**
	 * This method returns the second derivatives for each height. 
	 * @return a Matrix instances
	 */
	protected Matrix getHessians() {
		Matrix basicHessian = new Matrix(stemTaperPredictor.heights.m_iRows, 4);

		Matrix parameters = getValues();
		double exponent;
		double alpha;
		double dbh2 = stemTaperPredictor.getTree().getSquaredDbhCm() * 100;
		for (int i = 0; i < stemTaperPredictor.heights.m_iRows; i++) {
			alpha = parameters.m_afData[i][0];
			exponent = parameters.m_afData[i][1];
			basicHessian.m_afData[i][0] = dbh2 * stemTaperPredictor.coreExpression.m_afData[i][0] * Math.pow(stemTaperPredictor.heightsSectionRespectToDbh.m_afData[i][0], 2 - exponent);
			basicHessian.m_afData[i][1] = basicHessian.m_afData[i][0] * Math.log(stemTaperPredictor.heightsSectionRespectToDbh.m_afData[i][0]) * -1d;
			basicHessian.m_afData[i][2] = alpha * basicHessian.m_afData[i][1] * Math.log(stemTaperPredictor.heightsSectionRespectToDbh.m_afData[i][0]) * -1d;
			basicHessian.m_afData[i][3] = alpha * basicHessian.m_afData[i][1];
		}
		
		int firstTermNumberOfParameters = firstLinearTerm.getNumberOfParameters();
		int secondTermNumberOfParameters = secondLinearTerm.getNumberOfParameters();
		int totalNumberOfParameters = firstTermNumberOfParameters + secondTermNumberOfParameters;

		Matrix hessians = new Matrix(stemTaperPredictor.heights.m_iRows, (totalNumberOfParameters + 1) * totalNumberOfParameters / 2);
		Matrix tmpHessian;
		Matrix gradient1;
		Matrix gradient2;
		
		Matrix block11;
		Matrix block12;
		Matrix block22;
		for (int i = 0; i < stemTaperPredictor.heights.m_iRows; i++) {
			updateSecondLinearTerm(i);
			gradient1 = firstLinearTerm.getGradient();
			gradient2 = secondLinearTerm.getGradient();
			
			block11 = firstLinearTerm.getHessian().scalarMultiply(basicHessian.m_afData[i][0]);
			block12 = gradient1.multiply(gradient2.transpose()).scalarMultiply(basicHessian.m_afData[i][1]);
			block22 = gradient2.multiply(gradient2.transpose()).scalarMultiply(basicHessian.m_afData[i][2]).add(secondLinearTerm.getHessian().scalarMultiply(basicHessian.m_afData[i][2]));
			
			tmpHessian = block11.matrixStack(block12, false).matrixStack(block12.transpose().matrixStack(block22, false), true);
			
			hessians.setSubMatrix(tmpHessian.symSquare().transpose(), i, 0);
		}		
	
		return hessians;
	}

	
	

}

