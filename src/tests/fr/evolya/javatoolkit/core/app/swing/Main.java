package fr.evolya.javatoolkit.core.app.swing;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.gui.swing.SwingApp;

public class Main {

	public static void main(String[] args) {
		
		// Create app
		App app = new SwingApp();
		
		// Set common properties
		app.get(AppConfiguration.class)
			.setProperty("App.Name", "MyApplication")
			.setProperty("App.Version", "1.0.0");
		
		// Change general log level
		app.setLogLevel(Logs.ALL);
		
		// Just add few components
		app.add(Model.class);
		app.add(View.class);
		app.add(Controller.class);
		
		// Run application
		app.start();
		
	}

}
