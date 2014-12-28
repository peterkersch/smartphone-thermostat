package com.blackbird.thermostat.technology;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.thermostat.protocol.ThermostatSocket;
import com.thermostat.technology.BluetoothService;

class ThermostatSocketBluetooth implements ThermostatSocket {

	private static final String TAG = ThermostatSocketBluetooth.class.getSimpleName();

	private static UUID serviceId = UUID.fromString(BluetoothService.SERVICE_ID);
	
	private BluetoothSocket socket;

	private InputStream inputStream = null;
	
	private OutputStream outputStream = null;
	
	protected ThermostatSocketBluetooth(BluetoothDevice device) throws IOException {
		socket = device.createRfcommSocketToServiceRecord(serviceId);
		Log.d(TAG, "Bluetooth RFCOMM socket created to " + device.getAddress());
		// TODO: make sure that service discovery is not ongoing
		socket.connect();
		Log.d(TAG, "Bluetooth RFCOMM socket connected");
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
		Log.d(TAG, "Closing " + toString());

		// Input and output streams are closed by ThermostatProtocol
		socket.close();
	}
	
	@Override
	public String toString() {
		return "Bluetooth RFCOMM socket to " + socket.getRemoteDevice().getAddress();
	}
}
