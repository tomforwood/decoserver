package org.forwoods.deco.common;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;

public class Gas implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum GAS {O2,HE,N2};
	
	
	EnumMap<GAS, Float> percentages = new EnumMap<>(GAS.class);
	
	private int odMax=-1;//Maximum operating depth
	private int odMin=-1;//Minimum operating depth
	
	private float cylinderSize;
	
	private boolean decoGas;//Can possibly fail
	
	public static final Gas AIR=new Gas(21,0);
	public static final Gas FIFTY=new Gas(50,0);
	public static final Gas HUNDRED = new Gas(100,0);
	
	public Gas(int o2P, int heP)
	{
		percentages.put(GAS.O2,o2P/100f);
		percentages.put(GAS.HE,heP/100f);
		percentages.put(GAS.N2,(100-o2P-heP)/100f);
	}
	
	public int getMOD(DiveSettings settings)
	{
		if (odMax>0) return odMax;
		if (settings==null) return -1;
		return (int)((settings.ppO2Max/percentages.get(GAS.O2)-1)*10/settings.stepSize)*settings.stepSize;
	}
	
	public void setMod(int mod)
	{
		odMax=mod;
	}
	
	public int getMinod(DiveSettings settings)
	{
		if (odMin>0) return odMin;
		if (settings==null) return -1;
		return (int)Math.ceil((settings.ppO2Min/percentages.get(GAS.O2)-1)*10);//round min od's up
	}
	
	public void setMinod(int minod)
	{
		odMin=minod;
	}
	
	public String toString()
	{
		return (int)(percentages.get(GAS.O2)*100)+"/"+(int)(percentages.get(GAS.HE)*100);
	}
	
	public float getPercent(GAS gas)
	{
		return percentages.get(gas);
	}
	
	public void setPercent(GAS gas, float percent)
	{
		percentages.put(gas,percent);
		percentages.put(GAS.N2,1-percentages.get(GAS.O2)-percentages.get(GAS.HE));
	}

	public void setCylinderSize(float cylinderSize) {
		this.cylinderSize = cylinderSize;
	}

	public float getCylinderSize() {
		return cylinderSize;
	}

	public void setDecoGas(boolean decoGas) {
		this.decoGas = decoGas;
	}

	public boolean isDecoGas() {
		return decoGas;
	}

	public static Gas pickBest(double depth, DiveSettings settings, List<Gas> gasses) {
		Gas best=null;
		float bestO2=0;
		
		for (Gas g:gasses) {
			//is this gas breathable
			if (g.isBreathable(depth, settings)) {
				//it is breathable - is it better (higher o2 percent)
				if (g.getPercent(GAS.O2)>bestO2) {
					best=g;
				}
			}
		}
		return best;
		
	}

	public boolean isBreathable(double depth, DiveSettings settings) {
		int mod = getMOD(settings);
		return mod>depth && getMinod(settings)<depth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((percentages == null) ? 0 : percentages.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gas other = (Gas) obj;
		if (percentages == null) {
			if (other.percentages != null)
				return false;
		} else if (!percentages.equals(other.percentages))
			return false;
		return true;
	}
	
	
	
	
}
