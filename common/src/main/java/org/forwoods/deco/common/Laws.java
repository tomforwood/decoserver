package org.forwoods.deco.common;

/**
 * @author Tom
 * Equations about gas movement etc useful for multiple algos
 *
 */
public class Laws {
	
	
	/**
	 * The schreiner equation calculates the ending pp of inert gas at the end of a period where the inspired
	 * pp of that gas changes at a constant rate - e.g. when the diver ascends or descends at a constant rate
	 * @param initInspGasPres The inspired PP of the gas at the start of the movement
	 * @param gasChangeRate The rate at which the PP changes during the period
	 * @param segTime the time length of the period
	 * @param gasTimeConstant the gas constant of the compartment
	 * @param initGasPres the initial pp of gas in the compartment
	 * @return the ending pp of gas in the compartment
	 */
	public static double schreiner(double initInspGasPres, double gasChangeRate,double segTime,double gasTimeConstant, double initGasPres)
	{
		
		double result=initInspGasPres+gasChangeRate*(segTime - 1/gasTimeConstant) -
			(initInspGasPres - initGasPres - gasChangeRate/gasTimeConstant) *
			Math.exp(-gasTimeConstant * segTime);
		
		return (double)result;
	}
	
	
	/**
	 *	The Haldane equation is applied when calculating the uptake or 
		elimination of compartment gases during intervals at constant depth (the 
		outside ambient pressure does not change)
	 * @param initial_gas_pressure initial pp of intert gas in teh compartment
	 * @param inspired_gas_pressure pp of intert gas in inhaled gas
	 * @param gas_time_constant gas time constant of this compartment
	 * @param interval_time time spent at depth
	 * @return the end pp of gas in the compartment
	 */
	public static double haldane(double initial_gas_pressure, 
            double inspired_gas_pressure, 
            double gas_time_constant, 
            double interval_time){

			double ret_val = initial_gas_pressure + 
				(inspired_gas_pressure - initial_gas_pressure) * 
				(1d - Math.exp(-(gas_time_constant) * interval_time));
			return (double)ret_val;
		}
}
