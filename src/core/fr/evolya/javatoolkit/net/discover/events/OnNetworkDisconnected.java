package fr.evolya.javatoolkit.net.discover.events;

import java.net.InetAddress;

import fr.evolya.javatoolkit.net.discover.TypeInterface;
import fr.evolya.javatoolkit.net.discover.TypeNetwork;

public interface OnNetworkDisconnected {
	
	public void onNetworkDisonnected(TypeNetwork network, TypeInterface iface, InetAddress addr);

}
