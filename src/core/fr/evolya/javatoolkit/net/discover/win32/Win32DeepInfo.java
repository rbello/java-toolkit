package fr.evolya.javatoolkit.net.discover.win32;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import fr.evolya.javatoolkit.net.discover.NetworkDeepInfo;
import fr.evolya.javatoolkit.net.discover.NetworkWatcher;
import fr.evolya.javatoolkit.net.discover.TypeInterface;
import fr.evolya.javatoolkit.net.discover.TypeNetwork;

public class Win32DeepInfo implements NetworkDeepInfo {

	@Override
	public void findNetworkInfo(TypeNetwork network, NetworkWatcher watcher) {

		try {
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "netsh wlan show interfaces");
		    builder.redirectErrorStream(true);
		    Process p = builder.start();
		    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    
		    String iface = null;
		    String ssid = null;
		    String bssid = null;
		    String guid = null;
		    
		    String line;
		    while ((line = r.readLine()) != null) {
//		    	System.err.println(line);
			    if (line.contains("Description")) {
			    	iface = line.split(":")[1].trim();
			    }
			    else if (line.contains("BSSID")) {
			    	bssid = line.replaceFirst(":", "::::").split("::::")[1].trim();
			    	//System.out.println("Found " + ssid + " (" +bssid+") on " + iface);
			    	
			    	TypeInterface ifa = watcher.getInterfaceByDisplayName(iface);
			    	if (ifa == null) {
			    		// TODO
			    		return;
			    	}
			    	
//			    	InetAddress addr = ifa.getInterface().getInetAddresses().nextElement();
			    	
			    	if (ifa.getNetwork() == network && network.getBSSID() == TypeNetwork.DEFAULT_BSSID) {
			    		network.setName(ssid);
			    		network.setBSSID(bssid);
			    		network.setGUID(guid);
			    	}
			    	
			    	iface = null;
			    	ssid = null;
			    	bssid = null;
			    }
			    else if (line.contains("SSID")) {
			    	ssid = line.split(":")[1].trim();
			    }
			    else if (line.contains("GUID")) {
			    	guid = line.split(":")[1].trim();
			    }
		    }
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		
	}

}
