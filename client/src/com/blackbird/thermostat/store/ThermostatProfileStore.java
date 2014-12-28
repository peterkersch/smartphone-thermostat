package com.blackbird.thermostat.store;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.blackbird.thermostat.protocol.ThermostatServerIdentifier;
import com.blackbird.thermostat.technology.ServerIdentifierBluetooth;
import com.blackbird.thermostat.technology.ServerIdentifierIP;
import com.thermostat.security.KeyHashStore;
import com.thermostat.technology.Technology;

/**
 * Provides persistent storage of information for registered thermostats. 
 */
public class ThermostatProfileStore implements KeyHashStore {

	private static final String TAG = ThermostatProfileStore.class.getSimpleName();
	
	/** Cryptographic ID -> profile */
	private Map<String, ThermostatProfile> thermostats = new HashMap<String, ThermostatProfile>();
	
	public ThermostatProfileStore() {
		// TODO: read from shared preferences instead of hardcoding
		
		// Kitchen
		Map<Technology, ThermostatServerIdentifier> serverIdentifiers = new HashMap<Technology, ThermostatServerIdentifier>();
		serverIdentifiers.put(Technology.BLUETOOTH_RFCOMM, new ServerIdentifierBluetooth("00:02:72:C5:BF:15", "raspberrypi-peti-2"));
		try {
			List<String> homeSSIDs = new ArrayList<String>();
			homeSSIDs.add("Lioranna");
			ServerIdentifierIP id = new ServerIdentifierIP(homeSSIDs,
					(Inet6Address)InetAddress.getByName("fe80::ca3a:35ff:fec6:4b77"));
			serverIdentifiers.put(Technology.WiFi_IP, id);
		} catch (IOException e) {
			Log.e(TAG, "IP address exception: ", e);
		}
		ThermostatProfile profile = new ThermostatProfile("18456022754539500063086999025756793969037293133103758160521088160611996566631288245343122636925517900598972550398810832708650947096260046211584023287296254319051059257124877857860894097615112601395600471910384962243558359466672373267034141124359287202700229975034225535739293850388702804927333787907098871650417652351803206143412944987629135897579283989130935159598480489276264918042524453649077867248454733366953820874640910393216619440261162878860386596811595387061286634171916257242846129835422924431688936106537692416685792339298946600118633588262207144012502665199055609543189301192269892459327581604190119202537,65537",
				"a27af4ac49f381387a8720fec01824c289e2271c8065363690e4e4db37313a98",
				serverIdentifiers);
		thermostats.put(profile.getFingerprint(), profile);
		
		// Living room and bedroom
		serverIdentifiers = new HashMap<Technology, ThermostatServerIdentifier>();
		serverIdentifiers.put(Technology.BLUETOOTH_RFCOMM, new ServerIdentifierBluetooth("00:02:72:DA:17:A7", "raspberrypi-peti-0"));
		try {
			List<String> homeSSIDs = new ArrayList<String>();
			homeSSIDs.add("Lioranna");
			ServerIdentifierIP id = new ServerIdentifierIP(homeSSIDs,
					(Inet6Address)InetAddress.getByName("fe80::ba27:ebff:fe97:7e47"));
			serverIdentifiers.put(Technology.WiFi_IP, id);
		} catch (IOException e) {
			Log.e(TAG, "IP address exception: ", e);
		}
		profile = new ThermostatProfile("19515359383891663440072071504006841465836337357999706516973358614645654426665601601509344280647209395447990506590962132020074926131817680048082368219169536471121935153010960867346434059926261012158330485369280515076748034058139631242395874818691519607070739256616864257301003548753855334892963429174321042395142495871127763981963884129312475014167192576159990052760174890979429596807072842559527875654022612922783190052733483007946288756773687103916204615410499399337014449369105512412988361919989565716130388441781885914631807235624468964147621664826499002624252287105867346575434888763300337988056943653708653787309,65537",
				"26f2d3d2444ed7b67a9be5e3737f4d26dfc0d780fe4313e85a9d634b75186f59",
				serverIdentifiers);
		thermostats.put(profile.getFingerprint(), profile);
	}
	
	public synchronized Set<String> getThermostatIds() {
		return Collections.unmodifiableSet(thermostats.keySet());
	}
	
	public synchronized Collection<ThermostatProfile> getThermostatProfiles() {
		return Collections.unmodifiableCollection(thermostats.values());
	}
	
	public synchronized ThermostatProfile getThermostatProfile(String id) {
		return thermostats.get(id);
	}

	@Override
	public String getPublicKey(String id) {
		ThermostatProfile profile = thermostats.get(id);
		return profile == null ? null : profile.getPublicKey();
	}
}
