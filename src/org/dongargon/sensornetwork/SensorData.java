package org.dongargon.sensornetwork;

public abstract class SensorData {
	private double reading;
	String sensorID;
	public SensorData(double reading,String sensorID) {
		super();
		this.reading = reading;
		this.sensorID = sensorID;
	}
	
	public SensorData(String sensorID) {
		this.sensorID = sensorID;
	}

	public String getSensorID() {
		return sensorID;
	}
	
	public double getReading() {
		return reading;
	}
	public void setReading(double reading) {
		this.reading = reading;
	}

	@Override
	public String toString() {
		return "SensorData [reading=" + reading + ", sensorID=" + sensorID
				+ ", getClass()=" + getClass() + "]";
	}

}
