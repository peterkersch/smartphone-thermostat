package com.thermostat.server.protocol;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.protocol.ThermostatMessage;
import com.thermostat.protocol.ThermostatMessageListener;
import com.thermostat.protocol.ThermostatProtocol;
import com.thermostat.protocol.data.ResidentState;
import com.thermostat.protocol.data.ZoneData;
import com.thermostat.protocol.message.CloseMessage;
import com.thermostat.protocol.message.RegistrationMessage;
import com.thermostat.protocol.message.StateUpdateMessage;
import com.thermostat.protocol.message.ZoneActionMessage;
import com.thermostat.protocol.message.ZoneDataMessage;
import com.thermostat.server.profile.ResidentProfile;
import com.thermostat.server.profile.ResidentProfileStore;
import com.thermostat.server.profile.ResidentProfileStoreFactory;
import com.thermostat.server.profile.ResidentStatusLogger;
import com.thermostat.server.security.ThermostatServerSecurityManager;
import com.thermostat.server.zone.ZoneManager;
import com.thermostat.server.zone.ZoneRegistry;
import com.thermostat.server.zone.ZoneRegistryFactory;

/**
 * Manages communication between the thermostat server and the smartphone application. 
 */
public class ThermostatServerProtocol extends ThermostatMessageListener {

	private static Logger logger = Logger.getLogger(ThermostatServerProtocol.class.getSimpleName());

	private ResidentProfileStore profileStore;
	
	private ZoneRegistry zoneRegistry;

	private ThermostatServerSecurityManager securityManager;
	
	private ThermostatServerState state;
	
	public ThermostatServerProtocol(ThermostatProtocol protocol) throws IOException, GeneralSecurityException {
		super(protocol);
		profileStore = ResidentProfileStoreFactory.getResidentProfileStore();
		zoneRegistry = ZoneRegistryFactory.getZoneRegistry();
		securityManager = ThermostatServerSecurityManager.getInstance();
		state = ThermostatServerState.WAITING_FOR_FIRST_MESSAGE;
	}

	protected void receiveMessage(ThermostatMessage reply, ThermostatMessage request) {
		logger.fine("Received message " + reply);
		switch (reply.getType()) {
		case CLOSE:
			getProtocol().close();
			break;
		case REGISTRATION:
			handleRegistration((RegistrationMessage)reply);
			break;
		case STATE_UPDATE: 
			handleStateUpdate((StateUpdateMessage)reply); 
			break;
		case ZONE_ACTION:
			handleZoneAction((ZoneActionMessage)reply);
			break;
		default:
			throw new IllegalArgumentException("Message type " + reply.getType() + " not yet handled");
		}
	}

	protected void failure(Exception e, ThermostatMessage request) {
		logger.log(Level.SEVERE, "Exception when receiving message, closing protocol sequence", e);
	}
	
	private void handleRegistration(RegistrationMessage registrationMessage) {
		try {
			if (state != ThermostatServerState.WAITING_FOR_FIRST_MESSAGE) {
				throw new IllegalArgumentException("Received RegistrationMessage in " + state + " state.");
			}
			String ownPublicKey = securityManager.getPublicKeyString();
			// TODO: fill in parameters in registration message
			RegistrationMessage registrationReplyMessage = new RegistrationMessage(ownPublicKey, "thermostat", "", "", "");
			getProtocol().send(registrationReplyMessage);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException when handling registration message", e);
		} catch (GeneralSecurityException e) {
			logger.log(Level.SEVERE, "GeneralSecurityException when handling registration message", e);
			getProtocol().close();
		}
	}
	
	private void handleStateUpdate(StateUpdateMessage message) {
		String fingerprint = message.getFingerprint();
		ResidentState state = message.getState();
		logger.info("State update message from " + fingerprint + ", state: " + state.getName());
		try {
			ResidentStatusLogger.getInstance().write(fingerprint, message.getStatus());
			ResidentStatusLogger.getInstance().write(fingerprint, state);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not write resident status log file", e);
		}
		ResidentProfile profile = profileStore.getProfile(fingerprint);
		if (profile != null) {
			logger.info("State update sender Bluetooth address " + profile.getBluetoothName());
			profile.updateState(state);
			// Updates zones
			for (ZoneManager zoneManager : zoneRegistry.getZoneManagers()) {
				try {
					zoneManager.update();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Error when aqpplying new zone settings", e);
				}
			}
		}
		try {
			getProtocol().send(new ZoneDataMessage(zoneRegistry.getZoneData()));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error when sending zone info message", e);
		}
	}
	
	private void handleZoneAction(ZoneActionMessage message) {
		String fingerprint = message.getFingerprint();
		logger.info("Zone action message from " + fingerprint);
		ZoneData zoneData = message.getZone();
		logger.info("New boost temperature for zone " + zoneData.displayName + ": " + zoneData.boostTemperature);
		ZoneManager zoneManager = zoneRegistry.getZoneConfiguration(zoneData.displayName);
		if (zoneManager != null) {
			zoneManager.setBoostTemperature(zoneData.boostTemperature);
			try {
				zoneManager.update();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error when applying new zone settings", e);
			}
		} else {
			logger.warning("Zone manager not found for zone " + zoneData.displayName);
		}
		
		try {
			getProtocol().send(new CloseMessage());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error when sending close message", e);
		}
		getProtocol().close();
	}
}
