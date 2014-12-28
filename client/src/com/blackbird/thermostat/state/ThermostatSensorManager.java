package com.blackbird.thermostat.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ThermostatSensorManager extends BroadcastReceiver implements SensorEventListener {

	private static final String TAG = ThermostatSensorManager.class.getSimpleName();
	
	private static final String SENSOR_TYPE_AMBIENT_LIGHT = "Light";
	private static final String SENSOR_TYPE_MOTION = "Motion";
	private static final String SENSOR_TYPE_WIFI_RSSI = "WiFi_RSSI";

	private SensorManager sensorManager;
	
	/**
	 * Sensors accessed via sensor manager.
	 * Thermostat naming -> sensor object
	 */
	private Map<String, Sensor> sensors = new HashMap<String, Sensor>();
	
	/**
	 * Sensor information retrieved from broadcast events.
	 * Broadcast intent action name -> thermostat naming
	 */
	private Map<String, String> broadcastSensors = new HashMap<String, String>();
	
	private Map<String, List<Float>> sensorValues = new HashMap<String, List<Float>>();
	
	public ThermostatSensorManager(ContextWrapper context) {
		// Register sensors to be watched via the SensorManager
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		Log.d(TAG, "Available sensors:");
		for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
			Log.d(TAG, "Sensor " + sensor.getName() + ", type=" + sensor.getType() + ", power=" + sensor.getPower() + sensor.getResolution());
		}
		Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (lightSensor != null) {
			sensors.put(SENSOR_TYPE_AMBIENT_LIGHT, lightSensor);
		}
		Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (accelerometerSensor != null) {
			sensors.put(SENSOR_TYPE_MOTION, accelerometerSensor);
		}
		
		// Register for sensor information from broadcast events
		broadcastSensors.put(WifiManager.RSSI_CHANGED_ACTION, SENSOR_TYPE_WIFI_RSSI);
		IntentFilter filter = new IntentFilter();
		for (String eventName : broadcastSensors.keySet()) {
			filter.addAction(eventName);
		}
		context.registerReceiver(this, filter);
	}
	
	/**
	 * Checks availability of sensor data for all registered sensors
	 * 
	 * @return true if at least one data sample is available for all selected sensors.
	 */
	public boolean hasData() {
		for (String s : sensors.keySet()) {
			List<Float> values = sensorValues.get(s);
			if (values == null || values.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Starts sensor data collection.
	 */
	public void start() {
		for (Entry<String, Sensor> e : sensors.entrySet()) {
			sensorManager.registerListener(this, e.getValue(), SensorManager.SENSOR_DELAY_FASTEST);
		}
	}
	
	/**
	 * Stops sensor data collection and create statistics from accumulated sensor readings
	 * 
	 * @return a map mapping sensor name to average value during data collection period
	 */
	public synchronized Map<String, Float> getValues() {
		// Unregister sensor listener
		sensorManager.unregisterListener(this);

		// Calculate average value of readings per sensor
		Map<String, Float> readings = new HashMap<String, Float>();
		for (Entry<String, List<Float>> e : sensorValues.entrySet()) {
			float sum = 0;
			int n = 0;
			for (Float v : e.getValue()) {
				sum += v;
				n++;
			}
			if (n > 0) {
				readings.put(e.getKey(), sum / n);
			}
		}
		
		// Reset statistics
		sensorValues.clear();
		for (String s : sensors.keySet()) {
			sensorValues.put(s, new ArrayList<Float>());
		}
		for (String s : broadcastSensors.values()) {
			sensorValues.put(s, new ArrayList<Float>());
		}
		
		return readings;
	}
	
	private synchronized void registerSensorValue(String key, Float value) {
		if (key == null || value == null) {
			return;
		}
		List<Float> values = sensorValues.get(key);
		if (values == null) {
			values = new ArrayList<Float>();
			sensorValues.put(key, values);
		}
		values.add(value);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		String key = sensorType2String(event.sensor.getType());
		Float value = sensorEvent2Float(event);
		registerSensorValue(key, value);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String key = broadcastSensors.get(intent.getAction());
		Float value = broadcastEvent2Float(intent);
		registerSensorValue(key, value);
	}

	private String sensorType2String(int type) {
		switch (type) {
		case Sensor.TYPE_ACCELEROMETER: return SENSOR_TYPE_MOTION;
		case Sensor.TYPE_LIGHT: return SENSOR_TYPE_AMBIENT_LIGHT;
		default: return null;
		}
	}
	
	private Float sensorEvent2Float(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER: return (float)Math.sqrt((event.values[0])*(event.values[0]) + (event.values[1])*(event.values[1]) + (event.values[2]-(float)9.81)*(event.values[2]-(float)9.81));
		case Sensor.TYPE_LIGHT: return event.values[0];
		default: return null;
		}
	}
	
	private Float broadcastEvent2Float(Intent intent) {
		String action = intent.getAction();
		if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			return (float)intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0);
		} else {
			return null;
		}
	}

}
