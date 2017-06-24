package fr.evolya.javatoolkit.appstandard;

import java.io.File;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.IncaLogger;
import fr.evolya.javatoolkit.xmlconfig.XmlConfig;

@Deprecated
public class AppLauncher {

	protected static final Logger LOGGER = IncaLogger.getLogger("inca.code.app.Launcher");

	/**
	 * Point d'entr�e du programme en mode CLI.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Usage
		if (args == null || args.length == 0) {
			System.err.println("Usage: eva.jar <app.xml>");
			System.exit(101);
		}
		
		// Code de retour
		int r = 0;

		// Execution du fichier de config XML
		try {
			executeConfig(args[0]);
		}
		catch (RuntimeException ex) {
			if (LOGGER.isLoggable(IncaLogger.ERROR)) {
				LOGGER.log(IncaLogger.ERROR, "Exception: " + ex.getClass().getCanonicalName() + " : " + ex.getMessage());
				ex.printStackTrace();
			}
		}
		catch (Exception ex) {
			if (LOGGER.isLoggable(IncaLogger.ERROR)) {
				LOGGER.log(IncaLogger.ERROR, "Exception: " + ex.getClass().getCanonicalName() + " : " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	
		// Exit
		if (r != 0) {
			System.exit(r);
		}
		
	}
	
	public static void executeConfig(String pathToXmlAppFile) throws Exception {
			
		// Chargement de la config
		//inca.code.config.XmlConfig config = new inca.code.config.XmlConfig1(new File(pathToXmlAppFile));
		XmlConfig config = new XmlConfig(new File(pathToXmlAppFile));
		
		// R�cup�ration de l'instance de l'application
		Object bean = config.getBean("mainApp");
		
		// On test l'instance
		if (bean == null || !(bean instanceof App)) {
			throw new NullPointerException("No bean with name 'mainApp' found");
		}
		
	}

}
