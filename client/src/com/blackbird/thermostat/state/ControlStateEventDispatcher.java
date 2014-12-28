package com.blackbird.thermostat.state;

import java.util.ArrayList;
import java.util.List;

import com.thermostat.protocol.data.ResidentState;

/**
 * Dispatches control state information updates for subscribed listeners. 
 */
public class ControlStateEventDispatcher {

	private List<ControlStateListener> listeners = new ArrayList<ControlStateListener>();

	private static ControlStateEventDispatcher instance = null;
	
	public static ControlStateEventDispatcher getInstance() {
		if (instance == null) {
			instance = new ControlStateEventDispatcher();
		}
		return instance;
	}
	
	public void addControlStateListener(ControlStateListener listener) {
		listeners.add(listener);
	}
	
	public void removeControlStateListener(ControlStateListener listener) {
		listeners.remove(listener);
	}
	
	protected void stateUpdate(ResidentState oldState, ResidentState newState) {
		for (ControlStateListener listener : listeners) {
			listener.stateUpdate(oldState, newState);
		}
	}	
}
