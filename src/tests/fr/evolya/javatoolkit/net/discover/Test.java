package fr.evolya.javatoolkit.net.discover;

import fr.evolya.javatoolkit.code.IncaLogger;

public class Test {

	public static void main(String[] args) {
		IncaLogger.setGlobalLevel(IncaLogger.DEBUG);
		NetworkWatcher w = new NetworkWatcher();
		w.getEventsService().bind(new NetworkWatcherDebug());
		w.start();
	}

}
