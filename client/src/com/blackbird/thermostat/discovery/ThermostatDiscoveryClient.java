package com.blackbird.thermostat.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import com.thermostat.security.util.InetUtils;
import com.thermostat.technology.GenericIPService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;

/**
 * Provides discovery of thermostat servers on the local network 
 */
public class ThermostatDiscoveryClient {

	private static final String TAG = ThermostatDiscoveryClient.class.getSimpleName();
	
	private Context context;
	
	private List<String> homeSSIDs;
	
	private boolean enabled = false;
	
	private ConnectivityManager connectivitymanager;
	
	private WifiManager wifiManager;
	
	private MulticastLock lock = null;
	
	private WiFiBroadcastReceiver wifiBroadcastReceiver;
	
	private JmDNS jmdns;
	
	private ServiceListener jmdnsListener;
	
	public ThermostatDiscoveryClient(Context context, List<String> homeSSIDs) {
		this.context = context;
		this.homeSSIDs = homeSSIDs;

		connectivitymanager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

		// Register for broadcasts on WiFi state changes
	    IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	    filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    wifiBroadcastReceiver = new WiFiBroadcastReceiver();
	    context.registerReceiver(wifiBroadcastReceiver, filter);
	}

	/**
	 * Start service discovery.
	 * Should not be called from the main application thread.
	 */
	public synchronized void enable() {
		if (enabled) {
			Log.d(TAG, "Discovery already enabled");
			return;
		}
		
		// Check whether WiFi is enabled
		if (!wifiManager.isWifiEnabled()) {
			Log.w(TAG, "WiFi is disabled");
			return;
		}
		
		// Check WiFi connectivity (live connection to one of the specified home SSIDs)
		NetworkInfo networkInfo = connectivitymanager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (!networkInfo.isConnected()) {
			Log.w(TAG, "Not connected to WiFi network");
			return;
		}
		if (!homeSSIDs.contains(wifiInfo.getSSID().replace("\"", ""))) {
			Log.w(TAG, wifiInfo.getSSID().replace("\"", "") + " is not registered among home SSIDs: " + homeSSIDs);
			return;
		}

		// Check for a valid IP address
		InetAddress address = InetUtils.getLocalInetAddress();
		if (address == null) {
			Log.w(TAG, "No valid IP addresses found on this device");
			return;
		}
		Log.i(TAG, "Start service discovery using the IP address: " + address.getHostAddress());
		
        Log.d(TAG, "Grabbing WiFi multicast lock");

		// Grab WiFi multicast lock
		lock = wifiManager.createMulticastLock("ThermostatWifiMulticastLock");
        lock.setReferenceCounted(true);
        lock.acquire();
        
        // Subscriber for JmDNS notifications
        if (jmdns == null) {						
        	try {
        		Log.i(TAG, "Initialize JmDNS service discovery instance");
				jmdns = JmDNS.create(address);
			} catch (IOException e) {
				Log.e(TAG, "Error when initializing JmDNS", e);
				return;
			}
        }
		jmdnsListener = new JmdnsServiceListener();
		jmdns.addServiceListener(GenericIPService.MDNS_SERVICE_TYPE, jmdnsListener);
		jmdns.requestServiceInfo(GenericIPService.MDNS_SERVICE_TYPE, GenericIPService.MDNS_SERVICE_NAME, true, 500);
        Log.d(TAG, "JmDNS service info requested");
        
		enabled = true;
	}
	
	public synchronized void disable() {
		if (!enabled) {
			Log.d(TAG, "Discovery already disabled");
			return;
		}
		
		// Unsubscribe JmDNS listener
		jmdns.removeServiceListener(GenericIPService.MDNS_SERVICE_TYPE, jmdnsListener);
		jmdnsListener = null;

		// Release WiFi multicast lock
		if (lock != null) {
			lock.release();
			lock = null;
		} 
		
		enabled = false;
	}
	
	private class WiFiBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();

	        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
	        	NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	        	Log.i(TAG, "New network state: " + networkInfo.getDetailedState());
	        	if (networkInfo.getDetailedState() == DetailedState.CONNECTED) {
	        		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	        		String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
	        		Log.i(TAG, "BSSID: " + bssid + ", SSID: " + wifiInfo.getSSID());
	        	}
	        } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
	        	int newState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
	        	Log.i(TAG, "New WiFi state: " + newState);
	        }
		}
	}
	
	private class JmdnsServiceListener implements ServiceListener {

		@Override
		public void serviceAdded(ServiceEvent event) {
			// Required to force serviceResolved to be called again (after the first search)
            jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
            Log.i(TAG, "Service added: " + event.getName());
		}

		@Override
		public void serviceRemoved(ServiceEvent event) {
			Log.i(TAG, "Service removed: " + event.getName());
		}

		@Override
		public void serviceResolved(ServiceEvent event) {
			Log.i(TAG, "Service resolved: " + " type: " + event.getType() + " name: " + event.getName() + 
					event.getInfo().getQualifiedName() + ", IP: " + Arrays.asList(event.getInfo().getHostAddresses()) + ", port:" + event.getInfo().getPort());
		}
	}	
}
