package com.thermostat.server.zone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.protocol.data.ZoneData;
import com.thermostat.server.actuation.Actuator;
import com.thermostat.server.actuation.GpioRelayActuator;
import com.thermostat.server.actuation.GpioMotorizedValveActuator;
import com.thermostat.server.configuration.Configuration;
import com.thermostat.server.configuration.ConfigurationException;
import com.thermostat.server.temperature.OneWireTemperatureSensor;
import com.thermostat.server.temperature.TemperatureSensor;

/**
 * Zone registry implementation using configuration in properties file. 
 */
public class ZoneRegistryPropertiesFile implements ZoneRegistry {
	
	private static Logger logger = Logger.getLogger(ZoneRegistryPropertiesFile.class.getSimpleName());

	private static final String ZONE_CONF_DIR = "zones";
	
	private static final String ZONE_NAME_PROPERTY_NAME = "name";

	private static final String ZONE_ACTUATOR_TYPE_PROPERTY_NAME = "actuator.type";

	private static final String ZONE_ACTUATOR_ID_PROPERTY_NAME = "actuator.id";

	private static final String ZONE_SENSOR_ID_PROPERTY_NAME = "sensor.id";
	
	private static final String ZONE_SENSOR_CORRECTION_PROPERTY_NAME = "sensor.correction";
	
	private static final String ACTUATOR_TYPE_GPIO_RELAY = "GPIO_RELAY";

	private static final String ACTUATOR_TYPE_GPIO_MOTORIZED_VALVE = "GPIO_MOTORIZED_VALVE";

	private Map<String, ZoneManager> zoneConfigMap = new HashMap<String, ZoneManager>();
	
	protected ZoneRegistryPropertiesFile() throws IOException, ConfigurationException, NumberFormatException {
		File baseDirectory = Configuration.getConfigPath(ZONE_CONF_DIR);
		if (!baseDirectory.exists()) {
			throw new ConfigurationException("Zone configuration directory " + baseDirectory.getAbsolutePath() + " does not exist");
		}
		for (String zoneConfFile : baseDirectory.list()) {
			Properties p = new Properties();
			p.load(new FileInputStream(new File(baseDirectory, zoneConfFile)));
			try {
				// Read zone name
				String name = p.getProperty(ZONE_NAME_PROPERTY_NAME);
				if (name == null) {
					throw new ConfigurationException("Property " + ZONE_NAME_PROPERTY_NAME + " not found in " + zoneConfFile);
				}
				
				// Read actuator configuration
				Actuator actuator = null;
				String actuatorType = p.getProperty(ZONE_ACTUATOR_TYPE_PROPERTY_NAME);
				if (actuatorType == null) {
					throw new ConfigurationException("Property " + ZONE_ACTUATOR_TYPE_PROPERTY_NAME + " not found in " + zoneConfFile);
				} else if (actuatorType.equalsIgnoreCase(ACTUATOR_TYPE_GPIO_RELAY)) {
					String actuatorId = p.getProperty(ZONE_ACTUATOR_ID_PROPERTY_NAME);
					if (actuatorId == null) {
						throw new ConfigurationException("Property " + ZONE_ACTUATOR_ID_PROPERTY_NAME + " not found in " + zoneConfFile);
					}
					actuator = new GpioRelayActuator(Integer.parseInt(actuatorId));
				} else if (actuatorType.equalsIgnoreCase(ACTUATOR_TYPE_GPIO_MOTORIZED_VALVE)) {
					actuator = new GpioMotorizedValveActuator();
				}
				
				// Read temperature sensor configuration
				String sensorId = p.getProperty(ZONE_SENSOR_ID_PROPERTY_NAME);
				if (sensorId == null) {
					throw new ConfigurationException("Property " + ZONE_SENSOR_ID_PROPERTY_NAME + " not found in " + zoneConfFile);
				}
				String t = p.getProperty(ZONE_SENSOR_CORRECTION_PROPERTY_NAME);
				float temperatureCorrection = t == null ? 0.0f : Float.parseFloat(t);
				TemperatureSensor sensor = new OneWireTemperatureSensor(sensorId, temperatureCorrection);

				ZoneManager zoneManager = new ZoneManager(name, actuator, sensor);
				zoneConfigMap.put(zoneManager.getZoneName(), zoneManager);
				logger.info("Started zone manager for zone " + name + ", sensorId = " + sensorId + ", actuator = " + actuator);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error when initializing zone manager specified in " + zoneConfFile, e);
			}
		}
	}
	
	public ZoneManager getZoneConfiguration(String name) {
		return zoneConfigMap.get(name);
	}

	public Set<String> getZoneNames() {
		return Collections.unmodifiableSet(zoneConfigMap.keySet());
	}
	
	public Collection<ZoneManager> getZoneManagers() {
		return Collections.unmodifiableCollection(zoneConfigMap.values());
	}
	
	public ArrayList<ZoneData> getZoneData() {
		ArrayList<ZoneData> zoneData = new ArrayList<ZoneData>();
		for (ZoneManager z : zoneConfigMap.values()) {
			zoneData.add(z.getZoneData());
		}
		return zoneData;
	}
}
