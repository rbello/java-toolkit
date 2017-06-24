# Network API

## Description

An API for interconnecting an application over a TCP / IP network.

## State

This API is currently Stable.

## Usage

Use of NetworkWatcher :

```java

// Create network watcher
NetworkWatcher watcher = new NetworkWatcher();
watcher.setUpdateFrequency(3000); // ms

// Add listener
watcher.getEventsService().bind(new NetworkWatcherListener {
	
	public void onInterfaceDetected(TypeInterface net, boolean enabled) {}
	
	public void onInterfaceEnabled(TypeInterface net) { }

	public void onInterfaceDisabled(TypeInterface net) { }

	public void onNetworkConnected(TypeNetwork net, TypeInterface iface, InetAddress addr) { }
	
	public void onNetworkDisconnected(TypeNetwork net, TypeInterface iface, InetAddress addr) { }

	public void onConnected(TypeNetwork network) { }
	
	public void onDisconnected(TypeNetwork network) { }
	
	public void onInternetAvailable(TypeNetwork network) { }
	
	public void onInternetUnavailable(TypeNetwork network) { }
	
});


```

## Credits

- Network watcher: R. Bello
- FTP connector: Carlo Pelliccia