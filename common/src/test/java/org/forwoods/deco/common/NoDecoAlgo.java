package org.forwoods.deco.common;

import java.util.List;

/**
 * This algo knows that decompression is dangerous
 * and therefore never makes you do any
 * it does insist on a 3 minute safety stop at 6 though
 * if you go deeper than 10
 * @author Tom
 *
 */
public class NoDecoAlgo extends DecoAlgorithm<NoDecoSettings, NoDecoState> {

	@Override
	protected void initialise(NoDecoSettings settings, PlanPoint<NoDecoState> start) {
		start.algoState = new NoDecoState();
	}

	@Override
	protected void decendingSegment(List<PlanPoint<NoDecoState>> resultPoints, int i, PlanPoint<NoDecoState> last,
			PlanPoint<NoDecoState> next) {
		next.algoState = last.algoState;
	}
	
	
	@Override //to make testing easier
	protected double roundTime(double d) {
		return d;
	}
	protected double padTime(double d) {
		return d;
	}

	@Override
	protected void flatSegment(PlanPoint<NoDecoState> last,
			PlanPoint<NoDecoState> next) {
		next.algoState = last.algoState;
	}

	@Override
	protected void ascending(DivePlan<NoDecoSettings, NoDecoState> divePlan, PlanPoint<NoDecoState> next,
			PlanPoint<NoDecoState> last, int pos) {
		NoDecoSettings algoSettings = divePlan.getAlgoSettings();
		if (last.depth>10 && next.depth<6) {
			//woah there - you need to do a safety stop
			//otherwise it isn't safe
			List<PlanPoint<NoDecoState>> resultPoints = divePlan.getResultPoints();
			
			double ascentDist = last.depth - 6;
			double ascentTime = ascentDist / algoSettings.ascentRate; 
			PlanPoint<NoDecoState> stopStart = new PlanPoint<>(6, last.time + ascentTime);
			stopStart.gas = last.gas;
			stopStart.algoState = last.algoState;
			resultPoints.add(pos,stopStart);
			trackGasUsed(last, stopStart, algoSettings);
			
			Gas best = Gas.pickBest(6, algoSettings, divePlan.getGases());
			PlanPoint<NoDecoState> stopStop = new PlanPoint<>(6,3,true);
			stopStop.time = last.time + ascentTime + 3;
			stopStop.gas = best;
			resultPoints.add(pos+1, stopStop);
			
			next.gas = best;
			next.time = stopStop.time + 6/algoSettings.ascentRate;
			
		}
		else {
			//ascent directly
			next.gas = last.gas;
			next.time = last.time +(last.depth-next.depth)/algoSettings.ascentRate;
		}
	}

	

}
