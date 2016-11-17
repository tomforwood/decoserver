package org.forwoods.deco.common;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

public class TestNoDeco {

	private NoDecoAlgo algo;
	Offset<Double> rounding = Offset.offset(1e-5);

	@Before
	public void setUp() throws Exception {
		algo = new NoDecoAlgo();
	}

	@Test
	public void testDescentSlow() {
		//the algo should slow the descent to
		//the max descent rate
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(40, 0);
		List<PlanPoint<NoDecoState>> userplan = Collections.singletonList(bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		plan.getGases().add(Gas.AIR);
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		assertThat(outputPlan.getResultPoints().get(1).time).isEqualTo(40/settings.descentRate);
		
		//check gases are properly assigned
		//TODO
		//assertThat(outputPlan.getResultPoints()).extracting(pp->pp.getGas()).allMatch(g->g!=null);
		
	}
	
	@Test
	public void testDescentFaster() {
		//the algo should insert a point using the max descent rate
		//and leave the existing point
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(40, 5);
		List<PlanPoint<NoDecoState>> userplan = Collections.singletonList(bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		plan.getGases().add(Gas.AIR);
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		List<PlanPoint<NoDecoState>> resultPoints = outputPlan.getResultPoints();
		assertThat(resultPoints.get(1).time).isEqualTo(40/settings.descentRate);
		assertThat(resultPoints.get(1).depth).isEqualTo(40);
		assertThat(resultPoints.get(2).time).isEqualTo(5);	
		//TODO
				//assertThat(outputPlan.getResultPoints()).extracting(pp->pp.getGas()).allMatch(g->g!=null);
	}
	
	@Test
	public void testDescentDurationSlower() {
		//the user wants to descend to 40 and stay there for 1 minutes
		//the algo will insert a descent point and then extend the bottom time
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(40, 1);
		bottom.setDuration(1);
		List<PlanPoint<NoDecoState>> userplan = Collections.singletonList(bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		plan.getGases().add(Gas.AIR);
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		List<PlanPoint<NoDecoState>> resultPoints = outputPlan.getResultPoints();
		assertThat(resultPoints.get(1).time).isEqualTo(40/settings.descentRate);
		assertThat(resultPoints.get(1).depth).isEqualTo(40);
		assertThat(resultPoints.get(2).time).isEqualTo(1+40/settings.descentRate);
		assertThat(outputPlan.getResultPoints()).extracting(pp->pp.getGas()).allMatch(g->g!=null);
	}
	
	@Test
	public void testDescentDurationFaster() {
		//the user wants to descend to 40 and stay there for 5 minutes
		//the algo will insert a descent point and then extend the bottom time
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(40, 5);
		bottom.setDuration(5);
		List<PlanPoint<NoDecoState>> userplan = Collections.singletonList(bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		plan.getGases().add(Gas.AIR);
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		List<PlanPoint<NoDecoState>> resultPoints = outputPlan.getResultPoints();
		assertThat(resultPoints.get(1).time).isEqualTo(40/settings.descentRate);
		assertThat(resultPoints.get(1).depth).isEqualTo(40);
		assertThat(resultPoints.get(2).time).isEqualTo(5+40/settings.descentRate);
		assertThat(outputPlan.getResultPoints()).extracting(pp->pp.getGas()).allMatch(g->g!=null);
	}
	
	@Test
	public void testGasSwitchDown(){
		//test a gas switch on the way down
		List<Gas> gasses = new ArrayList<>();
		gasses.add(Gas.FIFTY);
		gasses.add(Gas.AIR);
		
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(40, 1);
		PlanPoint<NoDecoState> start = new PlanPoint<>(0,0);
		start.setGas(Gas.FIFTY);//start on 50%
		List<PlanPoint<NoDecoState>> userplan = Arrays.asList(start, bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		plan.gases = gasses;
		
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		
		List<PlanPoint<NoDecoState>> resultPoints = outputPlan.getResultPoints();
		assertThat(resultPoints.get(1).depth).isEqualTo(Gas.FIFTY.getMOD(settings));
		float switchStart = Gas.FIFTY.getMOD(settings)/settings.descentRate;
		assertThat(resultPoints.get(1).time).isEqualTo(switchStart, rounding);
		assertThat(resultPoints.get(1).gas).isEqualTo(Gas.FIFTY);
		
		assertThat(resultPoints.get(2).depth).isEqualTo(Gas.FIFTY.getMOD(settings));
		float switchEnd = switchStart + settings.switchTime;
		assertThat(resultPoints.get(2).time).isEqualTo(switchEnd, rounding);
		assertThat(resultPoints.get(2).gas).isEqualTo(Gas.AIR);
		
		assertThat(resultPoints.get(3).depth).isEqualTo(40);
		assertThat(resultPoints.get(3).time).isEqualTo(settings.switchTime+40/settings.descentRate, rounding);
		assertThat(resultPoints.get(3).gas).isEqualTo(Gas.AIR);
	
		assertThat(outputPlan.getResultPoints()).extracting(pp->pp.getGas()).allMatch(g->g!=null);
	}
	
	@Test
	public void testGasSwitchHypoxic(){
		//test a gas switch on the way down
		List<Gas> gasses = new ArrayList<>();
		gasses.add(Gas.FIFTY);
		Gas mix = new Gas(10, 60);
		gasses.add(mix);
		
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(80, 1);
		List<PlanPoint<NoDecoState>> userplan = Arrays.asList(bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		plan.gases = gasses;
		
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		List<PlanPoint<NoDecoState>> resultPoints = outputPlan.getResultPoints();
		assertThat(resultPoints.get(1).depth).isEqualTo(Gas.FIFTY.getMOD(settings));
		float switchStart = Gas.FIFTY.getMOD(settings)/settings.descentRate;
		assertThat(resultPoints.get(1).time).isEqualTo(switchStart, rounding);
		assertThat(resultPoints.get(1).gas).isEqualTo(Gas.FIFTY);
		
		assertThat(resultPoints.get(2).depth).isEqualTo(Gas.FIFTY.getMOD(settings));
		float switchEnd = switchStart + settings.switchTime;
		assertThat(resultPoints.get(2).time).isEqualTo(switchEnd, rounding);
		assertThat(resultPoints.get(2).gas).isEqualTo(mix);
		
		assertThat(resultPoints.get(3).depth).isEqualTo(80);
		assertThat(resultPoints.get(3).time).isEqualTo(settings.switchTime+80/settings.descentRate);
		assertThat(resultPoints.get(3).gas).isEqualTo(mix);
		
		assertThat(outputPlan.getResultPoints()).extracting(pp->pp.getGas()).allMatch(g->g!=null);
	}
	
	@Test 
	public void testManualGasSwitch() {
		List<Gas> gasses = new ArrayList<>();
		gasses.add(Gas.FIFTY);
		Gas mix = new Gas(10, 60);
		gasses.add(mix);
		//We are going to descend to 10m and do our swich there (1 minute)
		//and then spend another few (4) minutes up to a total of 5 minutes doing checks or whatever
		//we should insert an extra point to represent the end of the switch
		
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(80, 6);
		PlanPoint<NoDecoState> atTen = new PlanPoint<>(10, 5,true);
		atTen.gas = mix;
		List<PlanPoint<NoDecoState>> userplan = Arrays.asList(atTen, bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		plan.gases = gasses;
		
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		List<PlanPoint<NoDecoState>> resultPoints = outputPlan.getResultPoints();
		assertThat(resultPoints.get(1).depth).isEqualTo(10);
		assertThat(resultPoints.get(1).time).isEqualTo(10/settings.descentRate, rounding);
		assertThat(resultPoints.get(1).gas).isEqualTo(Gas.FIFTY);
		
		assertThat(resultPoints.get(2).depth).isEqualTo(10);
		assertThat(resultPoints.get(2).time).isEqualTo(10/settings.descentRate+settings.switchTime, rounding);
		assertThat(resultPoints.get(2).gas).isEqualTo(mix);
		
		assertThat(resultPoints.get(3).depth).isEqualTo(10);
		assertThat(resultPoints.get(3).time).isEqualTo(10/settings.descentRate+5, rounding);
		assertThat(resultPoints.get(3).gas).isEqualTo(mix);
		
		assertThat(resultPoints.get(4).depth).isEqualTo(80);
		assertThat(resultPoints.get(4).time).isEqualTo(80/settings.descentRate+5, rounding);
		assertThat(resultPoints.get(4).gas).isEqualTo(mix);
		
	}

}
