package fr.evolya.javatoolkit.net.discover;

import java.net.InetAddress;

import fr.evolya.javatoolkit.appstandard.AppService;

public abstract class NetworkWatcherAdapter implements NetworkWatcherListener {

	@Override
	public void onServiceStarted(AppService service) {
	}

	@Override
	public void onServiceStopped(AppService service) {
	}

	@Override
	public void onInterfaceDetected(TypeInterface net, boolean enabled) {
	}

	@Override
	public void onInterfaceEnabled(TypeInterface net) {
	}

	@Override
	public void onInterfaceDisabled(TypeInterface net) {
	}

	@Override
	public void onNetworkConnected(TypeNetwork net, TypeInterface iface,
			InetAddress addr) {
	}

	@Override
	public void onNetworkDisconnected(TypeNetwork net, TypeInterface iface,
			InetAddress addr) {
	}

	@Override
	public void onConnected(TypeNetwork network) {
	}

	@Override
	public void onDisconnected(TypeNetwork network) {
	}
	
	@Override
	public void onInternetAvailable(TypeNetwork network) {
	}
	
	@Override
	public void onInternetUnavailable(TypeNetwork network) {
	}

}
