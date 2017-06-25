package fr.evolya.javatoolkit.net.discover;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.evolya.javatoolkit.appstandard.AbstractThreadedService;
import fr.evolya.javatoolkit.appstandard.bridge.services.ELocalServiceType;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.utils.StringUtils;
import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.threading.worker.TimerOperation;

public class NetworkWatcher extends AbstractThreadedService 
	implements Runnable {

	/**
	 * Pointeur vers l'opération répétitive, pour pouvoir l'annuler.
	 */
	private TimerOperation _job;
	
	/**
	 * La liste des interfaces réseaux.
	 */
	private List<TypeInterface> _ifaces = new ArrayList<TypeInterface>();
	
	/**
	 * Le réseau connecté.
	 * Pour l'instant on ne peut en détecter qu'un.
	 */
	private TypeNetwork _connected = null;
	
	/**
	 * Fréquence de mise à jour, en millisecondes.
	 */
	private long _updateFrequency;
	
	/**
	 * Constructeur.
	 */
	public NetworkWatcher() {
		this(false, true, 3000);
	}
	
	/**
	 * Constructeur.
	 * 
	 * @param updateFrequency En millisecondes.
	 */
	public NetworkWatcher(long updateFrequency) {
		this(false, true, updateFrequency);
	}
	
	/**
	 * Constructeur.
	 * 
	 * @param isServicePublished Publication de service.
	 * @param autoStart Lancement automatique au démarrage de l'application.
	 * @param updateFrequency En millisecondes.
	 */
	public NetworkWatcher(boolean isServicePublished, boolean autoStart, long updateFrequency) {
		
		// On fabrique le service
		super(NetworkWatcherListener.class, isServicePublished, autoStart);
		
		// On sauvegarde la fréquence de mise à jour
		_updateFrequency = updateFrequency;
		
	}
	
	/**
	 * Renvoie le temps en millisecondes entre les mise à jour du watcher.
	 */
	public long getUpdateFrequency() {
		return _updateFrequency;
	}

	/**
	 * Modifie la fréquence de mise à jour du watcher.
	 * 
	 * @param updateFrequency En millisecondes.
	 */
	public void setUpdateFrequency(long updateFrequency) {
		_updateFrequency = updateFrequency;
		if (_job != null) {
			_job.setFrequency(updateFrequency);
		}
	}

	@Override
	public void onStart() throws Exception {
		_job = invokePeriodic(this, _updateFrequency);
	}

	@Override
	public void onStop() throws Exception {
		_job.cancel();
	}
	
	public boolean isConnected() {
		return _connected != null;
	}
	
	@Override
	public void run() {

		try {
			
			// On recupère les interfaces actuelles du système
			// Cette opération peut lever une SocketException
			List<NetworkInterface> ifaces = getNetworkInterfaces();
			
			// On effectue la détection des nouvelles interfaces et de celles qui sont supprimées
			detectInterfaces(ifaces);
			
			// On parse le résultat de la commande 'route'
			ParseRoute rt = new ParseRoute();
			
			// Détection d'une connexion
			if (rt.getLocalIPAddress() != null && !isConnected()) {
				
				// On recupère l'interface correspondant et l'IP de connexion
				Object[] tmp = findInterfaceByIP(rt.getLocalIPAddress());
				TypeInterface iface = (TypeInterface) tmp[0];
				InetAddress addr = (InetAddress) tmp[1];
				
				// On fabrique le réseau
				// Par défaut le nom correspond à l'interface
				TypeNetwork network = new TypeNetwork(iface.getName(), TypeNetwork.DEFAULT_BSSID, addr, iface);
				
				// On donne l'adresse de la passerelle
				try {
					network.setGateway(InetAddress.getByName(rt.getGateway()));
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				// On associe le réseau à l'interface
				iface.setNetwork(network);
				
				// On l'enregistre
				_connected = network;
				
				// On fait une recherche en profondeur
				detectNetwork(network);
				
				// On envoie un event ciblé sur le réseau
				getEventsService().trigger("onNetworkConnected", network, iface, addr);
				
				// On envoie un event global
				getEventsService().trigger("onConnected", network);
				
				// On fait une détection de la connexion Internet
				// On envoie un event pour l'accès à internet
				if (detectInternet(network) == 1) {
					getEventsService().trigger("onInternetAvailable", network);
				}
				
			}
			else if (rt.getLocalIPAddress() == null && isConnected()) {
				
				// On effectue la déconnexion
				TypeNetwork old = _connected;
				_connected = null;
				old.getInterface().setNetwork(null);
				old.setInternetAvailable(false);
				
				getEventsService().trigger("onInternetUnavailable", old);
				
				// On envoie un event ciblé sur le réseau
				getEventsService().trigger("onNetworkDisconnected", old, old.getInterface(), old.getAddress());
				
				// On envoie un event global
				getEventsService().trigger("onDisconnected", old);
				
			}
			else if (isConnected()) {
				// On fait une détection de la connexion Internet
				int r = detectInternet(_connected);
				// On envoie un event pour l'accès à internet
				if (r == 1) {
					getEventsService().trigger("onInternetAvailable", _connected);
				}
				else if (r == -1) {
					getEventsService().trigger("onInternetUnavailable", _connected);
				}
			}
		}
		catch (SocketException ex) {
			if (LOGGER.isLoggable(Logs.WARNING)) {
				LOGGER.log(Logs.WARNING, "Unable to list network interfaces: " + ex.getMessage());
			}
		}

	}

	private void detectNetwork(TypeNetwork network) {
		NetworkDeepInfo deep = new Win32DeepInfo(); // TODO
		deep.findNetworkInfo(network, this);
	}

	public Object[] findInterfaceByIP(String ip) {
		for (TypeInterface iface : _ifaces) {
			for (InetAddress addr : iface.getAddresses()) {
				if (addr instanceof Inet4Address) {
					String name = addr.toString();
					if (name.startsWith(StringUtils.SLASH)) name = name.substring(1);
					if (name.equals(ip))
					{
						return new Object[]{ iface, addr };
					}
				}
			}
		}
		return null;
	}

	public static List<NetworkInterface> getNetworkInterfaces() throws SocketException {
		return Collections.list(NetworkInterface.getNetworkInterfaces());
	}
	
	public static List<NetworkInterface> getActivesNetworkInterfaces() {
		List<NetworkInterface> list = new ArrayList<NetworkInterface>();
		try {
			for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (iface.isUp() && !iface.isLoopback()) {
					list.add(iface);
				}
			}
		}
		catch (Throwable ex) { }
		return list;
	}
	
	/**
	 * Cherche un interface à partir de son nom court.
	 * Ex: lo, eth0, wlan0, ...
	 */
	public TypeInterface getInterfaceByName(String name) {
		for (TypeInterface iface : _ifaces) {
			if (iface.getName().equals(name)) return iface;
		}
		return null;
	}
	
	/**
	 * Cherche une interface réseau à partir de son nom d'affichage.
	 * Ce nom n'est pas forcément unique, cette méthode renvoie la première
	 * interface à porter ce nom.
	 */
	public TypeInterface getInterfaceByDisplayName(String name) {
		for (TypeInterface iface : _ifaces) {
			if (iface.getDisplayName().equals(name)) return iface;
		}
		return null;
	}

	/**
	 * Détecter les différences (ajout/supression) entre la liste des interfaces du
	 * système (nets) et la liste interne à cet objet (_ifaces).
	 */
	private void detectInterfaces(List<NetworkInterface> nets) throws SocketException {
		
		// On parcours les interfaces actuelles, pour rechercher celles qui ne sont
		// pas déjà connues.
		for (NetworkInterface net : nets) {
			TypeInterface ct = getByNetworkInterface(net);
		
			// Nouvellement détectées
			if (ct == null) {
				ct = new TypeInterface(net);
				_ifaces.add(ct);
				ct.setEnabled(net.isUp());
				getEventsService().trigger("onInterfaceDetected", ct, net.isUp());
			}
			
			// Changement de status
			else if (net.isUp() != ct.isEnabled()) {
				ct.setEnabled(net.isUp());
				getEventsService().trigger(net.isUp() ? "onInterfaceEnabled" : "onInterfaceDisabled", ct);
				
				if (!ct.isEnabled() && ct.isConnected()) {
					// TODO Lever un event de déconnexion au réseau ?
					ct.disconnect();
				}
			}
			
		}
		
		// On parcours ensuite une copie de la liste interne des interfaces
		// pour détecter celles qui ne sont plus dans les interfaces actuelles
		for (TypeInterface netint : _ifaces) {
			if (!TypeInterface.contains(nets, netint.getInterface()) && netint.isEnabled()) {
				netint.setEnabled(false);
				getEventsService().trigger("onInterfaceDisabled", netint);
			}
			// On met à jour la liste d'adresses
			else {
				netint.setAddresses(Collections.list(netint.getInterface().getInetAddresses()));
			}
		}
		
	}
	
	/**
	 * Renvoie l'interface à partir de son équivalent NetworkInterface.
	 */
	public TypeInterface getByNetworkInterface(NetworkInterface net) {
		for (TypeInterface iface : _ifaces) {
			if (iface.equals(net)) return iface;
		}
		return null;
	}

	/**
	 * Surchargé pour donner le bon type de listener.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EventSource<NetworkWatcherListener> getEventsService() {
		return (EventSource<NetworkWatcherListener>) super.getEventsService();
	}
	
	@Override
	public ELocalServiceType getServiceType() {
		return ELocalServiceType.NETWORK_MONITOR;
	}
	
	public boolean isInternetAvailable() {
		return _connected != null && _connected.isInternetAvailable();
	}

	private static int detectInternet(TypeNetwork network) {
		
		// TODO Implémenter un délais plus long pour ce type de recherche
		
		try {
			String[] data = Utils.readAll(new URL("http://ip.evolya.fr/isp/")).split(StringUtils.NL);
//			String[] data = "RemoteAddress=78.218.214.148\nRemoteHost=cof16-1-78-218-214-148.fbx.proxad.net\nISP=AS12322 Free SAS\nGeoipCountryCode=FR\nGeoipCountryName=France\nGeoipRegionCode=B3\nGeoipCityName=Montgiscard\nGeoipLat=43.460701\nGeoipLng=1.567400".split(Util.NL);
//			if (false) throw new IOException();
			
			// On décode le message
			for (String line : data) {
				String key = line.substring(0, line.indexOf("="));
				String value = line.substring(line.indexOf("=") + 1);
				network.setAttribute(key, value);
//				switch (key) {
//				case "RemoteAddress" : break;
//				case "RemoteHost" : break;
//				case "ISP" : break;
//				case "GeoipCountryCode" : break;
//				case "GeoipCountryName" : break;
//				case "GeoipRegionCode" : break;
//				case "GeoipCityName" : break;
//				case "GeoipLat" : break;
//				case "GeoipLng" : break;
//				}
			}
			
			// On vient d'être connecté
			if (!network.isInternetAvailable()) {
				network.setInternetAvailable(true);
				return 1;
			}
			
		}
		catch (IOException ex) {
			// On vient d'être déconnecté
			if (network.isInternetAvailable()) {
				network.setInternetAvailable(false);
				return -1;
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		
		return 0;
		
	}

	public TypeNetwork getCurrentNetwork() {
		return _connected;
	}
	
}
