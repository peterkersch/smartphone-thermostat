package com.blackbird.thermostat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.blackbird.thermostat.state.ControlStateProvider;
import com.thermostat.protocol.data.ResidentState;
import com.thermostat.protocol.data.ZoneData;

/**
 * The main controller entity in the MVC pattern for this app 
 */
public class ThermostatController implements Serializable, ControlStateProvider {

	private static final long serialVersionUID = 8036617258781672677L;
	
	public static final int DEFAULT_AWAY_TIMEOUT_MIN = 5;
	
	private static ThermostatController instance;

	/** List of ZoneData objects describing temperature control status in each of these zones */
	private ArrayList<ZoneData> zones = new ArrayList<ZoneData>();

	/** List of available states */
	private ArrayList<ResidentState> states = new ArrayList<ResidentState>();
	
	private long lastZoneUpdate = -1;
	
	private int currentStateIndex;
	
	public static ThermostatController getInstance() {
		if (instance == null) {
			instance = new ThermostatController();
		}
		return instance;
	}
	
	public ThermostatController() {
		// TODO: read from configuration
		states.add(ResidentState.HOME_AWAKE);
		states.add(ResidentState.HOME_SLEEPING);
		states.add(new ResidentState("In bed"));
		currentStateIndex = 0;
	}
	
	/**
	 * Update temperature control information for the given zone
	 * 
	 * @param zone
	 */
	public synchronized void updateZone(ZoneData zone) {
		lastZoneUpdate = (new Date()).getTime();

		int i = zones.indexOf(zone);
		if (i<0) {
			zones.add(zone);
		} else {
			zones.set(i, zone);
		}
	}
	
	public int indexOf(ZoneData zone) {
		return zones.indexOf(zone);
	}
	
	public List<ZoneData> getZones() {
		return Collections.unmodifiableList(zones);
	}

	public List<ResidentState> getStates() {
		return Collections.unmodifiableList(states);
	}
	
	public int getCurrentStateIndex() {
		return currentStateIndex;
	}
	
	public void setCurrentStateIndex(int currentState) {
		this.currentStateIndex = currentState;
	}

	@Override
	public ResidentState getCurrentState() {
		return states.get(currentStateIndex);
	}
	
	public long getLastZoneUpdate() {
		return lastZoneUpdate;
	}
}
