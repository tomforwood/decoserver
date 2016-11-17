package org.forwoods.deco.common;

/**
 * @author Tom
 * Class that represents settings that apply to all dives computed by algorithms
 */
public class DiveSettings{
	

	public float atmosphericPressure=10.1325f;//m seawater equivalent
	
	public float bottomSAC=18;//l/min
	public float decoSAC=15;//l/min
	
	public float descentRate=20.0f;//m/min
	public float ascentRate=10f;//m/min
	
	public float ppO2Max=1.6f;
	public float ppO2Min=0.18f;
	
	public float switchTime=1;//mins to switch gas
	public int stepSize=3;//m
	public float minStop=1;//minimum stop time
	public float lastStopDepth=6;
	public float lastAscentTime=1;

}
