package org.forwoods.deco.common;

import java.util.Map;

public class PlanPoint<STATE extends AlgoState> {
	
	public double depth;//in metres
	public double time;//in minutes since start of dive
	public double duration;
	
	public Gas gas;
	public boolean durationPoint;
	//A duration point will have its duration maintained while its runtime is changed.  
	//I.E. you can use this to put in a stop of a minute to change gas
	//The algorithm will work out where to put it
	
	final boolean userPoint;//Whether this point is user created
	//user created points are much less likely to be moved 
	
	final boolean movePoint;
	//Move points are inserted to make the plan into a set of straight lines
	//they will never be displayed to the user in table format.
	
	//gas loading state
	public STATE algoState;

	public double OTU;
	public double CNS;
	public Map<Gas, Double> gasUsed;
	
	
	/**The user will create plan points using the first two constructors*/
	public PlanPoint(double depth, double time)
	{
		this.depth=depth;
		this.time=time;
		this.userPoint=true;
		movePoint=false;
	}
	
	public PlanPoint(double depth, double duration, boolean durationPoint)
	{
		this.depth=depth;
		if (durationPoint)
			this.duration=duration;
		else
			this.time=duration;
		this.userPoint=true;
		this.durationPoint=durationPoint;
		movePoint=false;
	}	
	
	public PlanPoint(double depth, double time, Gas gas, boolean userPoint, boolean durationPoint, boolean movePoint)
	{
		this.depth=depth;
		if (durationPoint)
		{
			this.duration=time;
		}
		else
		{
			this.time=time;
		}
		this.gas=gas;
		this.userPoint=userPoint;
		this.durationPoint=durationPoint;
		this.movePoint=movePoint;
		
	}
	
	public String toString()
	{
		return depth+"-"+time+"("+gas+")";
		//return OTU+"-"+CNS;
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
		durationPoint=false;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
		durationPoint=true;
	}

	public Gas getGas() {
		return gas;
	}

	public void setGas(Gas gas) {
		this.gas = gas;
	}

	public boolean isUserPoint() {
		return userPoint;
	}

	/*public void setUserPoint(boolean userPoint) {
		this.userPoint = userPoint;
	}*/

	public boolean isDurationPoint() {
		return durationPoint;
	}

	@Deprecated 
	/**
	 * not really being used properly at present
	 * @return
	 */
	public boolean isMovePoint() {
		return movePoint;
	}

	public double getOTU() {
		return OTU;
	}

	public double getCNS() {
		return CNS;
	}

	boolean special;

	public void setSpecial() {
		special = true;
	}
	
	public STATE getAlgoState() {
		return algoState;
	}

	public void setAlgoState(STATE algoState) {
		this.algoState = algoState;
	}
	
	

}
