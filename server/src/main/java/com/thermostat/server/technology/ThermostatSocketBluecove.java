package com.thermostat.server.technology;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import com.thermostat.protocol.ThermostatSocket;

public class ThermostatSocketBluecove implements ThermostatSocket {

	private StreamConnection connection;
	
	private InputStream inputStream = null;
	
	private OutputStream outputStream = null;

	public ThermostatSocketBluecove(StreamConnection connection) throws IOException {
		this.connection = connection;
	}

	public InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = connection.openInputStream();
		}
		return inputStream;
	}
	
	public OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = connection.openOutputStream();
		}
		return outputStream;
	}
	
	public void close() throws IOException {
		// Input and output streams are closed by ThermostatProtocol
		connection.close();
	}
}
