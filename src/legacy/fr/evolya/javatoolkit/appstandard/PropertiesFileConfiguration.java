package fr.evolya.javatoolkit.appstandard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.config.NonPersistentConfiguration;
import fr.evolya.javatoolkit.code.Logs;

@Deprecated
public class PropertiesFileConfiguration extends NonPersistentConfiguration {

	private Properties _config = new Properties();
	
	@Override
	public boolean save(File file) {
		try {
			_config.store(new FileOutputStream(file), null);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	@Override
	public boolean load(File file) {
		FileInputStream input = null;
		Properties properties = null;
		try {
			input = new FileInputStream(file);
			properties = new Properties();
			properties.load(input);
		}
		catch (Exception ex) {
			// Log
			if (App.LOGGER.isLoggable(Logs.INFO)) {
				App.LOGGER.log(Logs.INFO, "Unable to load: " + file
						+ " (" + ex.getClass().getSimpleName() + " : " + ex.getMessage() + ")");
			}
			return false;
		}
		// Dans tous les cas on ferme l'input
		finally {
			try {
				if (input != null) input.close();
			} catch (IOException e) { }
		}
		// On ajoute les properties à la config en place
		_config.putAll(properties);
		return true;
	}

}
