package com.blackbird.thermostat.technology;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.Map;

import com.blackbird.thermostat.discovery.IpDiscoveryClient;
import com.blackbird.thermostat.protocol.ThermostatServerIdentifier;
import com.blackbird.thermostat.store.ThermostatProfile;
import com.blackbird.thermostat.store.ThermostatProfileStore;
import com.thermostat.protocol.ThermostatSocket;
import com.thermostat.security.util.InetUtils;
import com.thermostat.technology.Technology;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Tracks availability of different technologies (e.g., Bluetooth, WiFi) and provides communication channel to thermostat servers accordingly. 
 */
public class TechnologySelector {

	private static final String TAG = TechnologySelector.class.getSimpleName();

	private Context context;

	private EnumSet<Technology> availableTechnologies = EnumSet.noneOf(Technology.class);

	// Bluetooth specific stuff
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothBoradcastReceiver bluetoothBroadcastReceiver;

	// WiFi specific stuff
	private WifiManager wifiManager;
	private WiFiBroadcastReceiver wifiBroadcastReceiver;
	private String ssid;

	private IpDiscoveryClient ipDiscoveryClient;
	private boolean ipv6Enabled = false;
	
	// Access details about registered thermostat servers
	private ThermostatProfileStore thermostatProfileStore;

	public TechnologySelector(Context context, ThermostatProfileStore thermostatProfileStore) throws IOException {
		this.context = context;
		this.thermostatProfileStore = thermostatProfileStore;
		
		// Check Bluetooth state
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Log.i(TAG, "Bluetooth is not available on this terminal");
		} else {
			if (bluetoothAdapter.isEnabled()) {
				Log.i(TAG, "Bluetooth is enabled");
				availableTechnologies.add(Technology.BLUETOOTH_RFCOMM);
			} else {
				Log.i(TAG, "Bluetooth is disabled");
			}
			// Register for broadcasts on BluetoothAdapter state change
		    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		    bluetoothBroadcastReceiver = new BluetoothBoradcastReceiver();
		    context.registerReceiver(bluetoothBroadcastReceiver, filter);
		}
		
		// Register for broadcasts on WiFi state changes
	    IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	    filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    wifiBroadcastReceiver = new WiFiBroadcastReceiver();
	    context.registerReceiver(wifiBroadcastReceiver, filter);
	    
		// Check WiFi state and initialize WiFi lock
		wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		Log.d(TAG, "Creating WiFi lock");
		if (wifiManager.isWifiEnabled()) {
			wifiBroadcastReceiver.wifiEnabled();
		} else {
			wifiBroadcastReceiver.wifiDisabled();
		}
		
	    // Check IPv6 capability of the terminal
		if (InetUtils.isIPv6Enabled()) {
			ipv6Enabled = true;
		}
		
		// Initialize IP discovery client
		ipDiscoveryClient = new IpDiscoveryClient(context);
	}
	
	public void onDestroy() {
		context.unregisterReceiver(bluetoothBroadcastReceiver);
		context.unregisterReceiver(wifiBroadcastReceiver);
	}
	
	/**
	 * Performs dynamic address discovery for currently enabled technologies.
	 */
	public void startDiscovery() {
		// currently, proactive discovery makes sense only for IPv4 addresses over WiFi 
		if (availableTechnologies.contains(Technology.WiFi_IP) && isHomeSSID(ssid)) {
			ipDiscoveryClient.startDiscovery(thermostatProfileStore.getThermostatProfiles(), true);
		}
	}
	
	private boolean isHomeSSID(String ssid) {
		for (ThermostatProfile thermostat : thermostatProfileStore.getThermostatProfiles()) {
			ServerIdentifierIP id = (ServerIdentifierIP)thermostat.getServerIdentifiers().get(Technology.WiFi_IP);
			if (id != null && id.getSSIDs().contains(ssid)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check whether local radio communication links are available.
	 * 
	 * @return true if there are available radio technologies (does not necessarily mean that thermostats are actually reachable) false otherwise.
	 */
	public boolean hasLocalConnectivity() {
		return bluetoothAdapter.isEnabled() || 
				availableTechnologies.contains(Technology.WiFi_IP) && isHomeSSID(ssid);
	}
	
	/**
	 * Creates a thermostat socket using the following priority order for available technologies:
	 * 1) WiFi IPv6
	 * 2) WiFi IPv4
	 * 3) Bluetooth RFCOMM
	 * 
	 * @param thermostat to be connected via the socket
	 * @return the socket to the thermostat or null if there are no available communication technologies
	 */
	public ThermostatSocket getThermostatSocket(ThermostatProfile thermostat) {
		Map<Technology, ThermostatServerIdentifier> idMap = thermostat.getServerIdentifiers(); 
		String fingerprint = thermostat.getFingerprint();
		
		// Try WiFi IP 
		if (availableTechnologies.contains(Technology.WiFi_IP)) {
			Log.d(TAG, "WiFi is available, trying to create IP socket...");
			ServerIdentifierIP id = (ServerIdentifierIP)idMap.get(Technology.WiFi_IP);
			if (id != null) {
				try {
					ThermostatSocket socket = getWifiSocket(id, fingerprint);
					if (socket != null) {
						return socket;
					}
				} catch (Exception e) {
					Log.e(TAG, "Error when creating WiFi socket to " + fingerprint, e);
				}
			} else {
				Log.d(TAG, "IP addresses are not available for thermostat " + fingerprint);
			}
		}
		
		// Try Bluetooth RFCOMM
		if (availableTechnologies.contains(Technology.BLUETOOTH_RFCOMM)) {
			Log.d(TAG, "Bluetooth is available, trying to create RFCOMM socket...");
			ServerIdentifierBluetooth id = (ServerIdentifierBluetooth)idMap.get(Technology.BLUETOOTH_RFCOMM);
			if (id != null) {
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(id.getBluetoothAddress());
				try {
					return new ThermostatSocketBluetooth(device);
				} catch (Exception e) {
					Log.e(TAG, "Error when creating Bluetooth socket to " + fingerprint, e);
				}
			} else {
				Log.d(TAG, "IP addresses are not available for thermostat " + fingerprint);
			}
		}

		return null;
	}
	
	private ThermostatSocket getWifiSocket(ServerIdentifierIP id, String fingerprint) throws Exception {
		// Try IPv6 first, if not available, fall back to IPv4
		if (ssid != null && id.getSSIDs().contains(ssid)) {
			if (ipv6Enabled && id.getIpv6Address() != null) {
				// Get the correct scope ID for the IPv6 address
				int ipv6scope = InetUtils.getLinkLocalScopeId();
				byte[] ipv6address = id.getIpv6Address().getAddress();
				return new ThermostatSocketWiFi(Inet6Address.getByAddress("", ipv6address, ipv6scope), 
						wifiManager.createWifiLock("Thermostat WiFi lock"));
			} else {
				// Lookup IPv4 address of the server
				InetAddress serverAddress = ipDiscoveryClient.getAddress(fingerprint);
				if (serverAddress != null) {
					return new ThermostatSocketWiFi(serverAddress, 
							wifiManager.createWifiLock("Thermostat WiFi lock"));
				} else {
					Log.w(TAG, "Could not resolve IPv4 address of thermostat server " + fingerprint);
				}
			}
		} else {
			Log.d(TAG, "SSID " + ssid + " not part of home SSIDs " + id.getSSIDs());
		}
		
		return null;
	}
	
	protected void enableBluetooth() {
		if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
		    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    enableBluetoothIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.startActivity(enableBluetoothIntent);
		}
	}

	protected void enableWifi() {
		// TODO
	}
	
	private class BluetoothBoradcastReceiver extends BroadcastReceiver {

		@Override
	    public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();

	        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
	            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	            if (state == BluetoothAdapter.STATE_TURNING_OFF) {
	            	Log.i(TAG, "Bluetooth is turning off");
	            	availableTechnologies.remove(Technology.BLUETOOTH_RFCOMM);
	            } else if (state == BluetoothAdapter.STATE_OFF) {
	            	Log.i(TAG, "Bluetooth is turned off");
	            	availableTechnologies.remove(Technology.BLUETOOTH_RFCOMM);
	            } else if (state == BluetoothAdapter.STATE_TURNING_ON) {
	            	Log.i(TAG, "Bluetooth is being turned back on");
	            } else if (state == BluetoothAdapter.STATE_ON) {
	            	Log.i(TAG, "Bluetooth is turned back on");
	            	availableTechnologies.add(Technology.BLUETOOTH_RFCOMM);
	            }
	        }
	    }
	}
	
	private class WiFiBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();

	        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
	        	NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	        	Log.i(TAG, "New network state: " + networkInfo.getDetailedState());
	        	if (networkInfo.getDetailedState() == DetailedState.CONNECTED) {
	        		wifiEnabled();
	        	} else {
	        		wifiDisabled();
	        	}
	        } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
	        	int newState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
	        	if (newState == WifiManager.WIFI_STATE_ENABLED) {
	        		wifiEnabled();
	        	} else {
	        		wifiDisabled();
	        	}
	        }
		}
		
		private synchronized void wifiEnabled() {
			Log.i(TAG, "WiFi enabled");
    		availableTechnologies.add(Technology.WiFi_IP);
    		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    		if (wifiInfo != null) {
    			String s = wifiInfo.getSSID();
    			if (s != null) {
    				ssid = s.replace("\"", "");
    				Log.i(TAG, "SSID: " + ssid);
    			}
    		}
		}
		
		private synchronized void wifiDisabled() {
			Log.i(TAG, "WiFi disabled");
    		availableTechnologies.remove(Technology.WiFi_IP);
    		ssid = null;
		}
	}
}
