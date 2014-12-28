package com.thermostat.protocol;

public abstract class ThermostatMessageListener {

	private ThermostatProtocol protocol;
	
	public ThermostatMessageListener(ThermostatProtocol protocol) {
		this.protocol = protocol;
		protocol.registerMessageListener(this);
	}
	
	/**
	 * Called when receiving a message from the communication partner 
	 * 
	 * @param reply the received message
	 * @param request the previous request message for which this message was sent as a reply or null if this is the first message.
	 */
	protected abstract void receiveMessage(ThermostatMessage reply, ThermostatMessage request);
	
	/**
	 * Called when an exception occured when receiving message.
	 * 
	 * @param e
	 * @param request the previous request message for which this message was sent as a reply or null if this is the first message.
	 */
	protected abstract void failure(Exception e, ThermostatMessage request);
	
	public ThermostatProtocol getProtocol() {
		return protocol;
	}
}
