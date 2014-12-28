package com.blackbird.thermostat.state;

import com.thermostat.protocol.data.ResidentState;

public interface ControlStateProvider {

	ResidentState getCurrentState();
}
