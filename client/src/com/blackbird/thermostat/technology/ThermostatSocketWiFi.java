package com.blackbird.thermostat.technology;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;

import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import com.thermostat.protocol.ThermostatSocket;
import com.thermostat.technology.GenericIPService;

class ThermostatSocketWiFi implements ThermostatSocket {

	private static final String TAG = ThermostatSocketWiFi.class.getSimpleName();

	private InetAddress address;
	
	private Socket socket;

	private InputStream inputStream = null;
	
	private OutputStream outputStream = null;
	
	private WifiLock wifiLock;
	
	protected ThermostatSocketWiFi(InetAddress address, WifiLock wifiLock) throws Exception {
		this.address = address;
		this.wifiLock = wifiLock;
		
		grabLock();
		try {
			socket = new Socket(address, GenericIPService.THERMOSTAT_SERVER_TCP_PORT);
			String ipVersion = (address instanceof Inet6Address) ? "IPv6" : "IPv4"; 
			Log.d(TAG, ipVersion + " socket created over WiFi to " + address.getHostAddress());
		} catch (Exception e) {
			releaseLock();
			throw e;
		}
	}

	private void grabLock() {
		synchronized (wifiLock) {
			Log.d(TAG, "Grabbing WiFi lock");
			wifiLock.acquire();
		}
	}
	
	private void releaseLock() {
		synchronized (wifiLock) {
			if (wifiLock.isHeld()) {
				wifiLock.release();
				Log.d(TAG, "Releasing WiFi lock, status: " + wifiLock.isHeld());
			}
		}
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = socket.getInputStream();
		}
		return inputStream;
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = socket.getOutputStream();
		}
		return outputStream;
	}
	
	@Override
	public void close() throws IOException {
		releaseLock();
		Log.d(TAG, "Closing " + toString());

		// Input and output streams are closed by ThermostatProtocol
		socket.close();
	}

	@Override
	public String toString() {
		return "WiFi socket to " + address.getHostAddress();
	}
}
