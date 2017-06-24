package fr.evolya.javatoolkit.net.discover;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

public class TypeInterface {
	
	public enum Kind {
		WIFI, ETHERNET, FIBER, TOKENRING, BLUETOOTH, VIRTUAL, UNKNOWN
	}

	private NetworkInterface _iface;
	
	private List<InetAddress> _addr = new ArrayList<InetAddress>();
	
	private boolean _enabled = false;

	private TypeNetwork _network = null;

	private Kind _type = Kind.UNKNOWN;

	public TypeInterface(NetworkInterface iface) {
		_iface = iface;
		final String name = iface.getName();
		if (name.startsWith("eth") || name.startsWith("lan")) _type = Kind.ETHERNET;
		else if (name.startsWith("wlan")) _type = Kind.WIFI;
		else if (name.startsWith("lo")) _type = Kind.VIRTUAL;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof NetworkInterface) {
			return equals((NetworkInterface) arg0);
		}
		if (arg0 instanceof TypeInterface) {
			return equals(((TypeInterface) arg0).getInterface());
		}
		return super.equals(arg0);
	}

	public boolean equals(NetworkInterface arg0) {
		return _iface.getName().equals(arg0.getName());
	}
	
	public NetworkInterface getInterface() {
		return _iface;
	}

	public boolean contains(InetAddress addr) {
		return _addr.contains(addr);
	}

	public List<InetAddress> getAddresses() {
		return _addr;
	}
	
	public void setAddresses(List<InetAddress> addr) {
		_addr = addr;
	}
	
	public static boolean contains(List<NetworkInterface> nets, NetworkInterface iface) {
		for (NetworkInterface item : nets) {
			if (item.getName().equals(iface.getName())) return true;
		}
		return false;
	}

	public boolean isEnabled() {
		return _enabled;
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	public String getName() {
		return _iface.getName();
	}
	
	public String getDisplayName() {
		return _iface.getDisplayName();
	}

	public boolean isConnected() {
		return _network  != null;
	}

	public TypeNetwork getNetwork() {
		return _network;
	}

	public void setNetwork(TypeNetwork network) {
		_network = network;
	}
	
	@Override
	public String toString() {
		try {
			return _iface.getName() + " (" + _iface.getDisplayName() + ")";
		} catch (Exception e) {
			return _iface.getName() + " (?)";
		}
	}

	public void disconnect() {
		_network = null;
	}

	public Kind getType() {
		return _type ;
	}


}
