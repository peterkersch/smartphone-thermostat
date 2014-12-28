package com.thermostat.server.technology;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.thermostat.protocol.ThermostatSocket;

public class ThermostatSocketIP implements ThermostatSocket {

	private Socket socket;
	
	private InputStream inputStream = null;
	
	private OutputStream outputStream = null;

	public ThermostatSocketIP(Socket connection) throws IOException {
		this.socket = connection;
	}

	public InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = socket.getInputStream();
		}
		return inputStream;
	}
	
	public OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = socket.getOutputStream();
		}
		return outputStream;
	}
	
	public void close() throws IOException {
		// Input and output streams are closed by ThermostatProtocol
		socket.close();
	}

}
