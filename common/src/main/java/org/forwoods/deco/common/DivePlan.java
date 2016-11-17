package org.forwoods.deco.common;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DivePlan<SETTINGS extends DiveSettings, STATE extends AlgoState> {
	
	double surfaceInterval;
	DivePlan<SETTINGS,STATE> lastPlan;
	
	List<PlanPoint<STATE>> userPoints;
	List<Gas> gases;
	private List<PlanPoint<STATE>> resultPoints;
	STATE finalState;
	SETTINGS algoSettings;
	Map<Gas, Double> gasVols;
	
	public void setGasVols(Map<Gas, Double> gasVols) {
		this.gasVols = gasVols;
	}

	public DivePlan()
	{
		gases=new ArrayList<Gas>();
	}
	
	public List<PlanPoint<STATE>> getUserPoints() {
		return userPoints;
	}
	public void setUserPoints(List<PlanPoint<STATE>> userPoints) {
		this.userPoints = userPoints;
	}
	public List<Gas> getGases() {
		return gases;
	}
	public void setGases(List<Gas> gases) {
		this.gases = gases;
		//need to change gases for user points
		if (userPoints!=null)
		{
			for (PlanPoint<STATE> p:userPoints)
			{
				if (!gases.contains(p.gas))
					p.setGas(null);
			}
		}
	}
	
	public void clearResultPoints()
	{
		setResultPoints(null);
	}

	public List<PlanPoint<STATE>> getResultPoints() {
		return resultPoints;
	}
	public SETTINGS getAlgoSettings() {
		return algoSettings;
	}
	public Map<Gas, Double> getGasVols() {
		return gasVols;
	}
	
	public STATE getFinalState() {
		return finalState;
	}

	public void setFinalState(STATE finalState) {
		this.finalState = finalState;
	}
	
	public double getSurfaceInterval() {
		return surfaceInterval;
	}

	public void setSurfaceInterval(double surfaceInterval) {
		this.surfaceInterval = surfaceInterval;
	}

	public DivePlan<SETTINGS, STATE> getLastPlan() {
		return lastPlan;
	}

	public void setLastPlan(DivePlan<SETTINGS, STATE> lastPlan) {
		this.lastPlan = lastPlan;
	}

	public void prettyPrint(OutputStream out) {
		PrintStream pout=new PrintStream(out);
		pout.println("depth\ttime\truntime\tgas");
		for (PlanPoint<STATE> p:getResultPoints()) {
			if (p.isMovePoint()) continue;
			pout.print(p.depth);
			pout.print('\t');
			pout.print(p.duration);
			pout.print('\t');
			pout.print(p.time);
			pout.print('\t');
			pout.println(p.gas);
		}
	}

	public void setResultPoints(List<PlanPoint<STATE>> resultPoints) {
		this.resultPoints = resultPoints;
	}

	
}
