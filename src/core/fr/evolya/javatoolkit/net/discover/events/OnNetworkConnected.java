package fr.evolya.javatoolkit.net.discover.events;

import java.net.InetAddress;

import fr.evolya.javatoolkit.net.discover.TypeInterface;
import fr.evolya.javatoolkit.net.discover.TypeNetwork;

public interface OnNetworkConnected {
	
	public void onNetworkConnected(TypeNetwork network, TypeInterface iface, InetAddress addr);

}
