package fr.evolya.javatoolkit.net.discover;

import fr.evolya.javatoolkit.code.Logs;

public class Test {

	public static void main(String[] args) {
		Logs.setGlobalLevel(Logs.DEBUG);
		NetworkWatcher w = new NetworkWatcher();
		w.getEventsService().bind(new NetworkWatcherDebug());
		w.start();
	}

}
