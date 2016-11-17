package org.forwoods.deco.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

public class TestGasConsumption {

	private NoDecoAlgo algo;
	Offset<Double> rounding = Offset.offset(1e-5);

	@Before
	public void setUp() throws Exception {
		algo = new NoDecoAlgo();
	}

	@Test
	public void testGasSwitchDown(){
		//test a gas switch on the way down
		List<Gas> gasses = new ArrayList<>();
		gasses.add(Gas.FIFTY);
		gasses.add(Gas.AIR);
		
		PlanPoint<NoDecoState> bottom = new PlanPoint<>(40, 1);
		PlanPoint<NoDecoState> atTen = new PlanPoint<>(10, 0);
		PlanPoint<NoDecoState> start = new PlanPoint<>(0, 0);
		atTen.setGas(Gas.AIR);
		start.setGas(Gas.FIFTY);
		List<PlanPoint<NoDecoState>> userplan = Arrays.asList(start,atTen, bottom);
		DivePlan<NoDecoSettings, NoDecoState> plan = new DivePlan<>();
		NoDecoSettings settings = new NoDecoSettings();
		plan.algoSettings = settings;
		plan.userPoints = userplan;
		
		plan.gases = gasses;
		
		DivePlan<NoDecoSettings, NoDecoState> outputPlan = algo.calculateDive(plan);
		
		List<PlanPoint<NoDecoState>> resultPoints = outputPlan.getResultPoints();
		double fiftyUsedDesc = DecoAlgorithm.metersToBar((0+10)/2, settings.atmosphericPressure) 
				* 10 /settings.descentRate * settings.bottomSAC; 
		double gasSwitch = DecoAlgorithm.metersToBar(10, settings.atmosphericPressure) 
				* settings.switchTime * settings.bottomSAC;
		
		assertThat(resultPoints.get(1).gasUsed.get(Gas.FIFTY)).isEqualTo(fiftyUsedDesc);
		assertThat(resultPoints.get(2).gasUsed.get(Gas.FIFTY)).isEqualTo(fiftyUsedDesc+gasSwitch);
		assertThat(resultPoints.get(2).gasUsed.get(Gas.AIR)).isEqualTo(gasSwitch);
	}
		
		

}
