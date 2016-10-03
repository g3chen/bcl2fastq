package ca.on.oicr.pde.deciders.model;

import java.util.Collection;

import net.sourceforge.seqware.common.model.Lane;

public class RunPositionData {
	
	private int positionNumber;
	private Lane swLane;
	private Collection<IusData> ius;
	private String laneString;
	private String basesMask;
	
	public RunPositionData() {
		
	}
	
	public RunPositionData(int positionNumber, Lane swLane, Collection<IusData> ius) {
		this.positionNumber = positionNumber;
		this.swLane = swLane;
		this.ius = ius;
	}

	public int getPositionNumber() {
		return positionNumber;
	}

	public void setPositionNumber(int positionNumber) {
		this.positionNumber = positionNumber;
	}

	public Lane getSwLane() {
		return swLane;
	}

	public void setSwLane(Lane swLane) {
		this.swLane = swLane;
	}

	public Collection<IusData> getIus() {
		return ius;
	}

	public void setIus(Collection<IusData> ius) {
		this.ius = ius;
	}

	public String getBasesMask() {
		return basesMask;
	}

	public void setBasesMask(String basesMask) {
		this.basesMask = basesMask;
	}

	public String getLaneString() {
		return laneString;
	}

	public void setLaneString(String laneString) {
		this.laneString = laneString;
	}

}