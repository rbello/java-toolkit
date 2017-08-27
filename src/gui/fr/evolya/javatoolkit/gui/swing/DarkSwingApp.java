package fr.evolya.javatoolkit.gui.swing;

public class DarkSwingApp extends SwingApp {
	
	public DarkSwingApp() {
		super();
	}

	public DarkSwingApp(String[] args) {
		super(args);
	}

	@Override
	protected int initDebugLevel(String[] args) {
		
		int level = super.initDebugLevel(args);

		// Initialisations pour Swing 
    	SwingHelper.initLookAndFeel();
    	SwingHelper.initSwingAnimations();
    	SwingHelper.adjustGlobalFontSize(13);
    	
    	return level;
    	
	}
	
}
