package com.blackbird.thermostat.state;

import com.thermostat.protocol.data.ResidentState;

/**
 * Listener interface to get control state update notifications.
 */
public interface ControlStateListener {

	/**
	 * Called when control state changes.
	 * 
	 * @param oldState
	 * @param newState
	 */
	void stateUpdate(ResidentState oldState, ResidentState newState);
}
