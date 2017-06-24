package fr.evolya.javatoolkit.net.discover;

import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.IncaLogger;

public class NetworkWatcherDebug extends NetworkWatcherAdapter {

	public static final Logger LOGGER = IncaLogger.getLogger("Network");
	
	@Override
	public void onInterfaceDetected(TypeInterface net, boolean enabled) {
		if (enabled) LOGGER.log(IncaLogger.DEBUG, "New interface: " + net);
	}

	@Override
	public void onInterfaceEnabled(TypeInterface net) {
		LOGGER.log(IncaLogger.DEBUG, "Interface enabled: " + net);
	}

	@Override
	public void onInterfaceDisabled(TypeInterface net) {
		LOGGER.log(IncaLogger.DEBUG, "Interface disabled: " + net);
	}

	@Override
	public void onNetworkConnected(TypeNetwork network, TypeInterface iface, InetAddress addr) {
		LOGGER.log(IncaLogger.INFO, "Network connected: " + network);
		LOGGER.log(IncaLogger.DEBUG, "  Interface : " + iface.getDisplayName() + " (" + iface.getName() + ")");
		LOGGER.log(IncaLogger.DEBUG, "       Type : " + iface.getType());
		LOGGER.log(IncaLogger.DEBUG, "         IP : " + addr);
		LOGGER.log(IncaLogger.DEBUG, "    Submask : " + network.getSubmask());
		LOGGER.log(IncaLogger.DEBUG, "    Gateway : " + network.getGateway());
		LOGGER.log(IncaLogger.DEBUG, "    Network : " + network.getName());
		LOGGER.log(IncaLogger.DEBUG, "       GUID : " + network.getGUID());
	}

	@Override
	public void onNetworkDisconnected(TypeNetwork network, TypeInterface iface, InetAddress addr) {
		LOGGER.log(IncaLogger.INFO, "Lost network: " + network);
	}

	@Override
	public void onConnected(TypeNetwork network) {
		LOGGER.log(IncaLogger.DEBUG, "Connected");
	}

	@Override
	public void onDisconnected(TypeNetwork network) {
		LOGGER.log(IncaLogger.DEBUG, "Disconnected");
	}

	@Override
	public void onInternetAvailable(TypeNetwork network) {
		LOGGER.log(IncaLogger.INFO, "Internet is available via " + network);
		for (Map.Entry<String, String> attr : network.getAttributes()) {
			LOGGER.log(IncaLogger.DEBUG, "  " + attr.getKey() + " : " + attr.getValue());
		}
	}

	@Override
	public void onInternetUnavailable(TypeNetwork network) {
		LOGGER.log(IncaLogger.INFO, "Internet link lost");
	}

}
