package quebecmrnfutility.biosim;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ClimateVariables implements Serializable {

	public static enum Variable {
		MeanAnnualTempC,
		MeanAnnualPrecMm;
	}

	private final Map<Variable, Double> variableMap;
	private final String plotID;
	
	public ClimateVariables(String plotID) {
		this.plotID = plotID;
		variableMap = new HashMap<Variable, Double>();
	}

	/**
	 * This method returns a climate variable generated by BioSim.
	 * @param enumVar the variable
	 * @return its value, a Double instance if it exists or NaN if it does not 
	 */
	public Double getVariable(Variable enumVar) {
		Double value = variableMap.get(enumVar);
		if (value == null) {
			value = Double.NaN;
		}
		return value;
	}
	
	public void setVariable(Variable enumVar, double value) {
		variableMap.put(enumVar, value);
	}
	
	public String getPlotId() {return plotID;}
	
	@Override
	public String toString() {
		return "Plot: " + plotID + "; " 
				+ Variable.MeanAnnualTempC.name() + " = " + getVariable(Variable.MeanAnnualTempC) + "; "
				+ Variable.MeanAnnualPrecMm.name() + " = " + getVariable(Variable.MeanAnnualPrecMm);
	}
}