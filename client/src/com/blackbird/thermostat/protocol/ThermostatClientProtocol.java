package com.blackbird.thermostat.protocol;

import java.io.IOException;
import java.security.GeneralSecurityException;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.blackbird.thermostat.Constants;
import com.blackbird.thermostat.ThermostatController;
import com.blackbird.thermostat.security.ThermostatClientSecurityManager;
import com.thermostat.protocol.ThermostatMessage;
import com.thermostat.protocol.ThermostatMessageListener;
import com.thermostat.protocol.ThermostatProtocol;
import com.thermostat.protocol.data.ResidentState;
import com.thermostat.protocol.data.ResidentStatusInfo;
import com.thermostat.protocol.data.ZoneData;
import com.thermostat.protocol.message.CloseMessage;
import com.thermostat.protocol.message.StateUpdateMessage;
import com.thermostat.protocol.message.ZoneActionMessage;
import com.thermostat.protocol.message.ZoneDataMessage;

public class ThermostatClientProtocol extends ThermostatMessageListener {

	private static final String TAG = ThermostatClientProtocol.class.getSimpleName();

	private Context context;
	
	private ThermostatClientSecurityManager securityManager;
	
	public ThermostatClientProtocol(Context context, ThermostatProtocol protocol, ThermostatClientSecurityManager securityManager) {
		super(protocol);
		this.context = context;
		this.securityManager = securityManager;
	}

	@Override
	protected void failure(Exception e, ThermostatMessage request) {
		Log.e(TAG, "Exception when receiving message, close protocol sequence: ", e);
	}

	@Override
	protected void receiveMessage(ThermostatMessage reply, ThermostatMessage request) {
		Log.d(TAG, "Received message " + reply);
		switch (reply.getType()) {
		case CLOSE:
			getProtocol().close();
			break;
		case ZONE_INFO:
			handleZoneInfo((ZoneDataMessage)reply);
			break;
		default:
			throw new IllegalArgumentException("Message type " + reply.getType() + " not yet handled");
		}
	}
	
	private void handleZoneInfo(ZoneDataMessage zoneDataMessage) {
		// Update states in the controller
		for (ZoneData zone : zoneDataMessage.getZones()) {
			ThermostatController.getInstance().updateZone(zone);
		}

		// Send data in local broadcast to the GUI
		Intent localIntent = new Intent(Constants.ZONE_REFRESH_ACTION);
		localIntent.putExtra(Constants.EXTENDED_ZONE_DATA, zoneDataMessage.getZones());
		LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

		// Initiate closing this message sequence
		try {
			getProtocol().send(new CloseMessage());
		} catch (IOException e) {
			Log.e(TAG, "Error when sending close message", e);
		}
		getProtocol().close();
	}
	
	public void sendStateUpdate(ResidentState state, ResidentStatusInfo status) throws GeneralSecurityException, IOException {
		String fingerprint = securityManager.getPublicKeyFingerprint();
		StateUpdateMessage message = new StateUpdateMessage(fingerprint, state, status);
		getProtocol().send(message);
	}
	
	public void sendZoneAction(ZoneData zone) {
		try {
			String fingerprint = securityManager.getPublicKeyFingerprint();
			ZoneActionMessage actionMessage = new ZoneActionMessage(fingerprint, zone);
			getProtocol().send(actionMessage);
		} catch (Exception e) {
			Log.e(TAG, "Exception when sending zone action message", e);
		}
	}
}
