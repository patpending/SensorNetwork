package org.dongargon.sensornetwork;

public class SwitchEvent extends SensorData {

	private boolean on;

	public SwitchEvent(boolean state, String sensorID) {
		
	
		super(sensorID);
		
		if(state)
		{
			setReading(1.0);
		}
		else
		{
			setReading(0.0);
		}
		this.on = state;
	}

	@Override
	public String toString() {
		return "SwitchEvent [on=" + on + ", sensorID=" + sensorID + "]";
	}
	
}
