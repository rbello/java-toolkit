package fr.evolya.javatoolkit.net.discover;

import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.Logs;

public class NetworkWatcherDebug extends NetworkWatcherAdapter {

	public static final Logger LOGGER = Logs.getLogger("Network");
	
	@Override
	public void onInterfaceDetected(TypeInterface net, boolean enabled) {
		if (enabled) LOGGER.log(Logs.DEBUG, "New interface: " + net);
	}

	@Override
	public void onInterfaceEnabled(TypeInterface net) {
		LOGGER.log(Logs.DEBUG, "Interface enabled: " + net);
	}

	@Override
	public void onInterfaceDisabled(TypeInterface net) {
		LOGGER.log(Logs.DEBUG, "Interface disabled: " + net);
	}

	@Override
	public void onNetworkConnected(TypeNetwork network, TypeInterface iface, InetAddress addr) {
		LOGGER.log(Logs.INFO, "Network connected: " + network);
		LOGGER.log(Logs.DEBUG, "  Interface : " + iface.getDisplayName() + " (" + iface.getName() + ")");
		LOGGER.log(Logs.DEBUG, "       Type : " + iface.getType());
		LOGGER.log(Logs.DEBUG, "         IP : " + addr);
		LOGGER.log(Logs.DEBUG, "    Submask : " + network.getSubmask());
		LOGGER.log(Logs.DEBUG, "    Gateway : " + network.getGateway());
		LOGGER.log(Logs.DEBUG, "    Network : " + network.getName());
		LOGGER.log(Logs.DEBUG, "       GUID : " + network.getGUID());
	}

	@Override
	public void onNetworkDisconnected(TypeNetwork network, TypeInterface iface, InetAddress addr) {
		LOGGER.log(Logs.INFO, "Lost network: " + network);
	}

	@Override
	public void onConnected(TypeNetwork network) {
		LOGGER.log(Logs.DEBUG, "Connected");
	}

	@Override
	public void onDisconnected(TypeNetwork network) {
		LOGGER.log(Logs.DEBUG, "Disconnected");
	}

	@Override
	public void onInternetAvailable(TypeNetwork network) {
		LOGGER.log(Logs.INFO, "Internet is available via " + network);
		for (Map.Entry<String, String> attr : network.getAttributes()) {
			LOGGER.log(Logs.DEBUG, "  " + attr.getKey() + " : " + attr.getValue());
		}
	}

	@Override
	public void onInternetUnavailable(TypeNetwork network) {
		LOGGER.log(Logs.INFO, "Internet link lost");
	}

}
