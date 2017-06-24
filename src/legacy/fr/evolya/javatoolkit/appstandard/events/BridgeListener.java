package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.bridge.IBridge;
import fr.evolya.javatoolkit.appstandard.bridge.IBridgeable;
import fr.evolya.javatoolkit.events.attr.EventListener;

public interface BridgeListener extends EventListener {
	
	public boolean beforeUnBridged(IBridge bridge, IBridgeable member);
	
	public boolean beforeBridged(IBridge bridge, IBridgeable member);
	
	public void afterBridged(IBridge bridge, IBridgeable member);

}
