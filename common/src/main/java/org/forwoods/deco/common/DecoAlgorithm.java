package org.forwoods.deco.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This class is a basis for implementing open circuit decompression algorithms
 * It's implemented methods will produce a plan which includes ascent and descent
 * segments at the prescribed rates and includes gas switches to ensure that the
 * optimum gas is used in each segment.
 * @author Tom
 *
 * @param <SETTINGS> Type parameter for the Algorithm settings used by this implementation 
 * @param <STATE> Type parameter for the saturation state tracked by this algorithm
 */
public abstract class DecoAlgorithm<SETTINGS extends DiveSettings, STATE extends AlgoState> {
	
	public final static double WATER_VAP_PRES=0.567f;//.493 in C
	public final static double UNITS_FACTOR = 10.1325f;//m/atmosphere
	
	public DivePlan<SETTINGS, STATE> calculateDive(DivePlan<SETTINGS, STATE> userPlan) {
		SETTINGS settings = userPlan.getAlgoSettings();
		List<Gas> gases = userPlan.getGases();
		
		List<PlanPoint<STATE>> resultPoints = new ArrayList<>();
		PlanPoint<STATE> start = initResultPlan(userPlan, resultPoints);
		
		//initialise state
		if (userPlan.getLastPlan()==null){
			//first dive in series
			initialise(settings, start);
		}
		else {
			//TODO calculate surface interval off-gassing
		}
		
		initGasVols(userPlan,resultPoints.get(0));

		DivePlan<SETTINGS, STATE> divePlan = new DivePlan<>();
		divePlan.algoSettings = settings;
		divePlan.setResultPoints(resultPoints);
		divePlan.setGases(gases);
		
		doPlan(settings,divePlan);
		
		return divePlan;
	}

	private PlanPoint<STATE> initResultPlan(DivePlan<SETTINGS, STATE> userPlan, List<PlanPoint<STATE>> resultPoints) {
		//copy user points to result points
		resultPoints.addAll(userPlan.getUserPoints());
		//adding in 0 start and end if required
		PlanPoint<STATE> end=resultPoints.get(resultPoints.size()-1);
		//all plans start and end at the surface
		if (end.getDepth()!=0)
		{
			resultPoints.add(new PlanPoint<>(0, 0, null, false, false, true));
		}
		PlanPoint<STATE> start=resultPoints.get(0);
		if (start.getDepth()!=0)
		{
			resultPoints.add(0,start=new PlanPoint<>(0, 0,null,false,false,true));
		}
		return start;
	}

	private void initGasVols(DivePlan<SETTINGS, STATE> userPlan, PlanPoint<STATE> planPoint) {
		List<Gas> gasses = userPlan.getGases();
		planPoint.gasUsed = new HashMap<>();
		for (Gas g:gasses) {
			planPoint.gasUsed.put(g, 0.0);
		}
	}

	private void doPlan(SETTINGS settings, DivePlan<SETTINGS, STATE> divePlan) {
		List<PlanPoint<STATE>> resultPoints = divePlan.getResultPoints();
		//for each point
		for (int i=1;i<resultPoints.size();i++) {
			PlanPoint<STATE> next = resultPoints.get(i);
			PlanPoint<STATE> last = resultPoints.get(i-1);
			
			double currentDepth=last.depth;
			double newDepth=next.depth;
			if (newDepth>currentDepth)
			{
				descending(settings, divePlan, i, next, last);
			}
			
			else if (newDepth==currentDepth) {
				flat(divePlan, next, last,i);
			}
			
			else {
				ascending(divePlan, next,last, i);
			}
			
			
		}
	}

	private void flat(DivePlan<SETTINGS, STATE> divePlan, PlanPoint<STATE> next, PlanPoint<STATE> last, int pos) {
		if (last.getGas()==null) {
			last.setGas(pickStartGas(last.depth,last.depth, divePlan));
		}
		
		if (next.durationPoint) {
			next.time=last.time+next.duration;
		}
		else if (next.time<last.time){
			next.time=last.time;
		}
		
		//TODO - enforce time interval for changes
		if (next.getGas()==null) {
			next.setGas(last.gas);
		}
		else if (next.gas!=last.gas) {
			//we need to break this up with a gas switch point
			float switchTime = divePlan.getAlgoSettings().switchTime;PlanPoint<STATE> switchPoint = new PlanPoint<>(last.depth,
					last.time+switchTime, true);
			switchPoint.gas= next.gas;
			switchPoint.time = last.time+switchTime;
			divePlan.getResultPoints().add(pos,switchPoint);
			if (next.durationPoint) {
				//take the switch time of the duration at this depth
				next.duration=Math.max(0,next.duration-switchTime);
				
				divePlan.getResultPoints().add(pos, switchPoint);
			}
			else {
				next.time = Math.max(next.time, switchPoint.time);
			}
			next = switchPoint;
		}
		flatSegment(last,next);
		trackGasUsed(last,next, divePlan.algoSettings);
	}

	private void descending(SETTINGS settings, DivePlan<SETTINGS, STATE> divePlan, int i, PlanPoint<STATE> next,
			PlanPoint<STATE> last) {
		//we are descending
		List<PlanPoint<STATE>> resultPoints = divePlan.getResultPoints();

		double currentDepth=last.depth;
		double newDepth=next.depth;
		
		double descentdistance = newDepth-currentDepth;
		double descentTime = descentdistance/settings.descentRate;//how long it will take to do the descent
		double descentArrival = last.time+descentTime;//time of arrival at depth
		
		//can we breath the current gas at the destination depth
		Gas startGas = last.gas;
		if (startGas==null) {
			startGas = last.gas = pickStartGas(currentDepth, newDepth, divePlan);
		}
		
		Gas endGas;
		
		if (!startGas.isBreathable(newDepth, settings)) {
			//the gas we are starting this segment with isn't breathable at our destination
			endGas = next.gas = Gas.pickBest(newDepth, settings, divePlan.getGases());
			
			//Add a duration point at the target depth for the switching
			//the descent code below will handle the descent bit
			newDepth = startGas.getMOD(settings);
			PlanPoint<STATE> switchPoint = new PlanPoint<>(newDepth,settings.switchTime, true);
			switchPoint.gas = endGas;
			resultPoints.add(i,switchPoint);
			
			descentdistance = newDepth-currentDepth;
			descentTime = descentdistance/settings.descentRate;//how long it will take to do the descent
			descentArrival = last.time+descentTime;//time of arrival at depth
			next = switchPoint;
			
		}
		else {
			endGas = next.gas;
			if (endGas==null) {
				endGas = next.gas = startGas;
			}
			if (!endGas.equals(startGas)) {
				//add a gas switch
				PlanPoint<STATE> switchPoint = new PlanPoint<>(newDepth,settings.switchTime, true);
				switchPoint.gas = endGas;
				resultPoints.add(i,switchPoint);
				next = switchPoint;
			}
		}
		
		if (next.isDurationPoint()) {
			//the bottom time of the next planned segment is fixed
			//so insert the descent segment and extend out the fixed segment
			PlanPoint<STATE> descentPoint = new PlanPoint<>(newDepth, descentArrival);
			descentPoint.gas = startGas;
			next.time = roundTime(descentArrival+next.duration);
			resultPoints.add(i,descentPoint);
			next = descentPoint;
		}
		
		else {
			if (next.time-descentArrival<.1) {
				//we can't get there that quickly - push the point later
				next.time = descentArrival;
			}
			else {
				//we get there sooner - add a descent point
				PlanPoint<STATE> descentPoint = new PlanPoint<>(newDepth, descentArrival);
				descentPoint.gas = startGas;
				resultPoints.add(i,descentPoint);
				next = descentPoint;
			}
		}
		
		decendingSegment(resultPoints, i, last, next);
		
		trackGasUsed(last,next, settings);
	}

	protected void trackGasUsed(PlanPoint<STATE> last, PlanPoint<STATE> next, SETTINGS settings) {
		next.gasUsed = new HashMap<>(last.gasUsed);
		
		double startDepth = last.depth;
		double endDepth = next.depth;
		double time = next.time - last.time;
		
		double breathed = breathed(startDepth, endDepth, time, 
				next.getAlgoState().isDeco()?settings.decoSAC:settings.bottomSAC,
				settings.atmosphericPressure);
		
		addGas(last.gas, last, next, breathed);
		if (!next.gas.equals(last.gas)) {
			//I'm going to assume for gas vol calcs that for the switching time
			//you are breathing both gasses simultaneously as the most conservative option
			addGas(next.gas, last, next, breathed);
		}
		
	}
	
	private void addGas(Gas gas, PlanPoint<STATE> last, PlanPoint<STATE> next, double breathed) {
		double vol = last.gasUsed.get(gas);
		vol+=breathed;
		next.gasUsed.put(gas, vol);
	}

	private double breathed(double startDepth, double endDepth, double time, double sac, float atmosphericPressure) {
		double avgDepth = (startDepth+endDepth)/2;
		double avgPresBar = metersToBar(avgDepth, atmosphericPressure);
		return avgPresBar*sac*time;
	}
	
	public static double metersToBar(double meters, double atmosphericPressure) {
		return (meters+atmosphericPressure) / UNITS_FACTOR;
	}

	/**
	 * @param currentDepth - the depth we are currently at
	 * @param newDepth - the depth we are movign to
	 * @param divePlan 
	 * @param gases - list of available gasses
	 * @return The appropriate gas to <b>start</b> on
	 */
	private Gas pickStartGas(double currentDepth, double newDepth, DivePlan<SETTINGS, STATE> divePlan ) {
		//we dont know what gas we should currently be breathing
		//pick the best gas for the end of the segment and use that
		//if it is usable here - because switching is a pain
		//if that gas isn't usable here then pick the best gas for now
		
		Gas end = Gas.pickBest(newDepth, divePlan.algoSettings, divePlan.getGases());
		
		if (end.isBreathable(currentDepth, divePlan.algoSettings)) {
			return end;
		}
		else return Gas.pickBest(currentDepth, divePlan.algoSettings, divePlan.getGases());
		
	}

	/**
	 * round times so durations always end on a whole minute
	 * @param d
	 * @return rounded time
	 */
	protected double roundTime(double d) {
		return Math.round(d);
	}
	
	/**
	 * round times up to whole minutes
	 * @param d
	 * @return
	 */
	protected double padTime(double d) {
		return Math.ceil(d-(1e-6));
	}

	/**
	 * calculate algorithm saturation state for a descending segment between last and next
	 * @param last The previous point
	 * @param next the point who's state should be calculated
	 */
	protected abstract void decendingSegment(List<PlanPoint<STATE>> resultPoints, int i, 
			PlanPoint<STATE> last,
			PlanPoint<STATE> next);

	/**
	 * calculate algorithm saturation state for a flat segment between last and next
	 * @param last The previous point
	 * @param next the point who's state should be calculated
	 */
	protected abstract void flatSegment(PlanPoint<STATE> last,
			PlanPoint<STATE> next) ;

	
	/**
	 * Initialise the state of the start point to the values for the first dive in a series
	 * e.g. saturated at surface pressure
	 * @param settings the settings for this plan
	 * @param start the point to initialise
	 */
	protected abstract void initialise(SETTINGS settings, PlanPoint<STATE> start);
	


	
	/**
	 * Manage the ascent from last point to next
	 * Most algos are going to want a lot more control over this than they do for the descent
	 * @param divePlan
	 * @param next
	 * @param last
	 */
	protected abstract void ascending(DivePlan<SETTINGS, STATE> divePlan, PlanPoint<STATE> next, PlanPoint<STATE> last, int pos);
}
