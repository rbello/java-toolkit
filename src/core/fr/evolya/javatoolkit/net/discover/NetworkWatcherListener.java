package fr.evolya.javatoolkit.net.discover;

import java.net.InetAddress;

import fr.evolya.javatoolkit.appstandard.events.ServiceListener;

public interface NetworkWatcherListener extends ServiceListener {
	
	/**
	 * Quand une interface réseau est découverte.
	 */
	public void onInterfaceDetected(TypeInterface net, boolean enabled);
	
	/**
	 * Quand une interface réseau est activée.
	 */
	public void onInterfaceEnabled(TypeInterface net);

	/**
	 * Quand une interface réseau est désactivée.
	 */
	public void onInterfaceDisabled(TypeInterface net);

	/**
	 * Quand une interface se connecte à un réseau.
	 * 
	 * @param net Le réseau connecté.
	 * @param iface L'interface associée.
	 * @param addr L'adresse sur le réseau.
	 */
	public void onNetworkConnected(TypeNetwork net, TypeInterface iface, InetAddress addr);
	
	/**
	 * Quand une interface se déconnecte d'un réseau.
	 * 
	 * @param net Le réseau connecté.
	 * @param iface L'interface associée.
	 * @param addr L'adresse sur le réseau.
	 */
	public void onNetworkDisconnected(TypeNetwork net, TypeInterface iface, InetAddress addr);

	/**
	 * Event global qui indique que la machine est connectée.
	 * 
	 * @param network
	 */
	public void onConnected(TypeNetwork network);
	
	/**
	 * Event global qui indique que la machine est déconnectée.
	 * 
	 * @param network
	 */
	public void onDisconnected(TypeNetwork network);
	
	public void onInternetAvailable(TypeNetwork network);
	
	public void onInternetUnavailable(TypeNetwork network);
	
}
