package com.thermostat.server.technology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.protocol.ThermostatProtocol;
import com.thermostat.server.protocol.ThermostatListenServer;
import com.thermostat.server.protocol.ThermostatServerProtocol;
import com.thermostat.technology.GenericIPService;

public class ListenServerIP extends ThermostatListenServer {

	private static Logger logger = Logger.getLogger(ThermostatListenServer.class.getSimpleName());

	private static ListenServerIP instance = null;

	private ServerSocket serverSocket;
	
	public static ListenServerIP getInstance() throws IOException {
		if (instance == null) {
			instance = new ListenServerIP();
		}
		return instance;
	}
	
	private ListenServerIP() throws IOException {
		serverSocket = new ServerSocket(GenericIPService.THERMOSTAT_SERVER_TCP_PORT);
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				logger.info("Incoming IP connection from " + socket.getInetAddress());
				ClientHandlerThread t = new ClientHandlerThread(socket);
				t.start();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error when accepting incoming IP connection", e);
				// Wait some time to prevent infinite log flooding
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {}
			}
		}
	}
	
	private class ClientHandlerThread extends Thread {

		private Socket socket;
		
		public ClientHandlerThread(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				ThermostatProtocol protocol = new ThermostatProtocol(new ThermostatSocketIP(socket));
				new ThermostatServerProtocol(protocol);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error when serving incoming IP connection", e);
			}
		}
	}	

}
