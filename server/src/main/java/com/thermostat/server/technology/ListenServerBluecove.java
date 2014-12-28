package com.thermostat.server.technology;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.thermostat.protocol.ThermostatProtocol;
import com.thermostat.server.protocol.ThermostatListenServer;
import com.thermostat.server.protocol.ThermostatServerProtocol;
import com.thermostat.technology.BluetoothService;

public class ListenServerBluecove extends ThermostatListenServer {

	private static Logger logger = Logger.getLogger(ListenServerBluecove.class.getSimpleName());

	/** The UUID of the keep alive service running on the smartphone side */
	private static UUID serviceId = new UUID(BluetoothService.SERVICE_ID.replaceAll("-", ""), false);

	private static String serviceUrl = "btspp://localhost:" + serviceId.toString() + ";authenticate=true;encrypt=true;master=true";

	private static ListenServerBluecove instance;
	
	public static ListenServerBluecove getInstance() throws IOException {
		if (instance == null) {
			instance = new ListenServerBluecove();
		}
		return instance;
	}
	
	private ListenServerBluecove() throws IOException {
		// Make sure that server's Bluetooth adapter is visible
		LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
		logger.info("Thermostat controller Bluetooth address: " + LocalDevice.getLocalDevice().getBluetoothAddress());
	}
	
	@Override
	public void run() {
		StreamConnectionNotifier notifier = null;
		try {
			notifier = (StreamConnectionNotifier) Connector.open(serviceUrl);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error when opening " + serviceUrl, e);
			return;
		}
		while (true) {
			try {
				StreamConnection connection = notifier.acceptAndOpen();
				logger.info("Incoming Bluetooth connection");
				ClientHandlerThread t = new ClientHandlerThread(connection);
				t.start();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error when accepting incoming Bluetooth connection", e);
				// Wait some time to prevent infinite log flooding
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {}
			}
		}
	}
	
	private class ClientHandlerThread extends Thread {

		private StreamConnection connection;
		
		public ClientHandlerThread(StreamConnection connection) {
			this.connection = connection;
		}
		
		@Override
		public void run() {
			try {
				ThermostatProtocol protocol = new ThermostatProtocol(new ThermostatSocketBluecove(connection));
				new ThermostatServerProtocol(protocol);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error when handling incoming bluetooth connection", e);
			}
		}
	}	
}
