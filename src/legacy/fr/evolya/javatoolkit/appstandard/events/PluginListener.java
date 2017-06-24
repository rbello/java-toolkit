package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.AppPlugin;
import fr.evolya.javatoolkit.events.attr.EventListener;

public interface PluginListener extends EventListener {
	
	void onPluginConnected(AppPlugin plugin, App app);

}
