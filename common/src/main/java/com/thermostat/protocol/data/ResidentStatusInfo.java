package com.thermostat.protocol.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Contains resident status info for a given period of time.
 * Status info is read from phone sensors and broadcast events. 
 */
public class ResidentStatusInfo implements Serializable {

	private static final long serialVersionUID = 8704530475284242478L;

	/** The end of the data collection period */
	private long timestamp;
	
	/** List of broadcast events captured during the period */
	private List<BroadcastEvent> events;
	
	/** Sensor name -> average sensor value */
	private Map<String, Float> sensorValues;
	
	public ResidentStatusInfo() {
		// Required for serialization 
	}
	
	public ResidentStatusInfo(List<BroadcastEvent> events, Map<String, Float> sensorValues) {
		this.events = events;
		this.sensorValues = sensorValues;
		timestamp = (new Date()).getTime();
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public List<BroadcastEvent> getEvents() {
		return events;
	}
	
	public Map<String, Float> getSensorValues() {
		return sensorValues;
	}
}
