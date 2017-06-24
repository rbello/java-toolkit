package fr.evolya.javatoolkit.net.discover;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.evolya.javatoolkit.code.annotations.InvariantClass;

/**
 * Représente un réseau informatique.
 */
@InvariantClass
public class TypeNetwork {

	public static final String DEFAULT_BSSID = "<no-bssid>";
	
	private String _name;
	private String _bssid;
	private TypeInterface _iface;
	private InetAddress _addr;
	
	private InetAddress _gateway = null;
	private String _guid = null;
	private boolean _internet = false;
	private Map<String, String> _attr = new HashMap<String, String>();
	
	/**
	 * Constructeur.
	 */
	public TypeNetwork(String name, String bssid, InetAddress addr, TypeInterface iface) {
		_name = name;
		_bssid = bssid;
		_iface = iface;
		_addr = addr;
	}
	
	public boolean equals(String bssid) {
		return bssid != null && bssid.equals(_bssid);
	}

	/**
	 * Renvoie le nom du réseau.
	 * 
	 * Sur un réseau sans-fil type Wifi il s'agit du SSID*, c'est à dire le nom
	 * porté par le réseau lui-même.
	 * Dans le cas d'un réseau filaire, le nom porté est celui de l'interface.
	 * * SSID = Service Set Identifier
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Renvoie le nom de 
	 * TODO
	 */
	public String getBSSID() {
		return _bssid;
	}

	/**
	 * Renvoie l'interface réseau qui est connectée au réseau. 
	 */
	public TypeInterface getInterface() {
		return _iface;
	}
	
	/**
	 * Renvoie l'adresse IP sur le réseau.
	 */
	public InetAddress getAddress() {
		return _addr;
	}
	
	/**
	 * Renvoie le mask du réseau.
	 */
	public String getSubmask() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Renvoie l'adresse de la passerelle.
	 */
	public InetAddress getGateway() {
		return _gateway;
	}
	
	@Override
	public String toString() {
		return _name + " (bssid=" + _bssid + " ip=" + _addr + " iface=" + _iface.getName() + ")";
	}

	public String getGUID() {
		return _guid;
	}

	public void setGateway(InetAddress addr) {
		_gateway = addr;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setBSSID(String bssid) {
		_bssid = bssid;
	}

	public void setGUID(String guid) {
		_guid = guid;
	}

	public boolean isInternetAvailable() {
		return _internet;
	}

	public void setInternetAvailable(boolean enabled) {
		_internet = enabled;
	}
	
	public void setAttribute(String key, String value) {
		_attr.put(key, value);
	}

	public String getAttribute(String key) {
		return _attr.containsKey(key) ? _attr.get(key) : null;
	}

	public Set<Map.Entry<String, String>> getAttributes() {
		return _attr.entrySet();
	}
	
	

}
