package fr.evolya.javatoolkit.core.app.swing;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.gui.swing.SwingApp;

public class Main {

	public static void main(String[] args) {
		
		App app = new SwingApp();
		
		app.setLogLevel(Logs.ALL);
		
		app.add(Model.class);
		app.add(View.class);
		app.add(Controller.class);
		
		app.start();
		
	}

}
