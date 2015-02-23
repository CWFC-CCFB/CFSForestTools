/*
 * English version follows
 * 
 * Ce fichier fait partie de la bibliothèque mrnf-foresttools.
 * Il est protégé par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du ministère des Ressources naturelles et de la Faune du Gouvernement du 
 * Québec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Québec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Québec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Québec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.stemtaper;

import repicea.math.Matrix;
import repicea.stats.LinearStatisticalExpression;

/**
 * This class handles the linear terms included in the StemTaperEquation class.
 * @author Mathieu Fortin - October 2011 
 */
@Deprecated
class InternalLinearExpressions {

	private StemTaperEquation stemTaperEquation;
	private LinearStatisticalExpression firstLinearTerm;
	private LinearStatisticalExpression secondLinearTerm;

	/**
	 * Constructor.
	 * @param stemTaperEquation a StemTaperEquation instance
	 */
	protected InternalLinearExpressions(StemTaperEquation stemTaperEquation) {
		this.stemTaperEquation = stemTaperEquation;

		firstLinearTerm = new LinearStatisticalExpression();
		firstLinearTerm.setVariableValue(0, 1d);		// only an intercept for the first term

		secondLinearTerm = new LinearStatisticalExpression();
		secondLinearTerm.setVariableValue(0, 1d);		// intercept for the second term

	}

	private void updateLinearTerms(int i) {
		secondLinearTerm.setVariableValue(1, stemTaperEquation.coreExpression.m_afData[i][0]);
		secondLinearTerm.setVariableValue(2, Math.log(stemTaperEquation.coreExpression.m_afData[i][0]));
		secondLinearTerm.setVariableValue(3, Math.exp(stemTaperEquation.relativeHeights.m_afData[i][0]));
		secondLinearTerm.setVariableValue(4, stemTaperEquation.treeDbh);
		secondLinearTerm.setVariableValue(5, stemTaperEquation.coreExpression.m_afData[i][0] * stemTaperEquation.relativeHeights.m_afData[i][0]);
	}

	protected Matrix getValues() {
		Matrix output = new Matrix(stemTaperEquation.heights.m_iRows, 2);
		for (int i = 0; i < stemTaperEquation.heights.m_iRows; i++) {
			updateLinearTerms(i);
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
		Matrix basicGradient = new Matrix(stemTaperEquation.heights.m_iRows, 2);
		
		Matrix parameters = getValues();
		double exponent;
		double alpha;
		double dbh2 = stemTaperEquation.treeDbh * stemTaperEquation.treeDbh;
		for (int i = 0; i < stemTaperEquation.heights.m_iRows; i++) {
			alpha = parameters.m_afData[i][0];
			exponent = parameters.m_afData[i][1];
			basicGradient.m_afData[i][0] = dbh2 * stemTaperEquation.coreExpression.m_afData[i][0] * Math.pow(stemTaperEquation.heightsSectionRespectToDbh.m_afData[i][0], 2 - exponent);
			basicGradient.m_afData[i][1] = alpha * basicGradient.m_afData[i][0] * Math.log(stemTaperEquation.heightsSectionRespectToDbh.m_afData[i][0]) * -1d;
		}
		
		int firstTermNumberOfParameters = firstLinearTerm.getNumberOfParameters();
		int secondTermNumberOfParameters = secondLinearTerm.getNumberOfParameters();
		
		Matrix gradients = new Matrix(stemTaperEquation.heights.m_iRows, firstTermNumberOfParameters + secondTermNumberOfParameters);
		for (int i = 0; i < stemTaperEquation.heights.m_iRows; i++) {
			updateLinearTerms(i);
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
		Matrix basicHessian = new Matrix(stemTaperEquation.heights.m_iRows, 4);

		Matrix parameters = getValues();
		double exponent;
		double alpha;
		double dbh2 = stemTaperEquation.treeDbh * stemTaperEquation.treeDbh;
		for (int i = 0; i < stemTaperEquation.heights.m_iRows; i++) {
			alpha = parameters.m_afData[i][0];
			exponent = parameters.m_afData[i][1];
			basicHessian.m_afData[i][0] = dbh2 * stemTaperEquation.coreExpression.m_afData[i][0] * Math.pow(stemTaperEquation.heightsSectionRespectToDbh.m_afData[i][0], 2 - exponent);
			basicHessian.m_afData[i][1] = basicHessian.m_afData[i][0] * Math.log(stemTaperEquation.heightsSectionRespectToDbh.m_afData[i][0]) * -1d;
			basicHessian.m_afData[i][2] = alpha * basicHessian.m_afData[i][1] * Math.log(stemTaperEquation.heightsSectionRespectToDbh.m_afData[i][0]) * -1d;
			basicHessian.m_afData[i][3] = alpha * basicHessian.m_afData[i][1];
		}
		
		int firstTermNumberOfParameters = firstLinearTerm.getNumberOfParameters();
		int secondTermNumberOfParameters = secondLinearTerm.getNumberOfParameters();
		int totalNumberOfParameters = firstTermNumberOfParameters + secondTermNumberOfParameters;

		Matrix hessians = new Matrix(stemTaperEquation.heights.m_iRows, (totalNumberOfParameters + 1) * totalNumberOfParameters / 2);
		Matrix tmpHessian;
		Matrix gradient1;
		Matrix gradient2;
		
		Matrix block11;
		Matrix block12;
		Matrix block22;
		for (int i = 0; i < stemTaperEquation.heights.m_iRows; i++) {
			updateLinearTerms(i);
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

