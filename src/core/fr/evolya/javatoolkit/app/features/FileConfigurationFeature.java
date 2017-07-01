package fr.evolya.javatoolkit.app.features;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Properties;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.app.event.ApplicationBuilding;
import fr.evolya.javatoolkit.app.event.ApplicationStopping;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.events.fi.BindOnEvent;

public class FileConfigurationFeature {

	@BindOnEvent(ApplicationBuilding.class)
	public void load(App app) {
		File file = getConfigFile(app);
		if (file == null) {
			App.LOGGER.log(Logs.WARNING, "Missing property 'Config.File' "
					+ "defined in configuration");
			return;
		}
		// Load properties
		FileInputStream input = null;
		Properties properties = null;
		try {
			input = new FileInputStream(file);
			properties = new Properties();
			properties.load(input);
		}
		catch (Exception ex) {
			App.LOGGER.log(Logs.INFO, "Unable to load: " + file
						+ " (" + ex.getClass().getSimpleName() + " : " + ex.getMessage() + ")");
			return;
		}
		finally {
			try {
				if (input != null) input.close();
			} catch (IOException e) { }
		}
		// On ajoute les properties à la config en place
		int i = 0;
		AppConfiguration config = app.get(AppConfiguration.class);
		for (Entry<Object, Object> prop : properties.entrySet()) {
			config.setProperty("" + prop.getKey(), "" + prop.getValue());
			i++;
		}
		if (App.LOGGER.isLoggable(Logs.DEBUG)) {
			App.LOGGER.log(Logs.DEBUG, "Load " + i + " properties from file: " + file);
		}
	}

	@BindOnEvent(ApplicationStopping.class)
	public void save(App app) {
		try {
			Properties config = new Properties();
			config.putAll(app.get(AppConfiguration.class).getProperties());
			File file = getConfigFile(app);
			try (FileOutputStream stream = new FileOutputStream(file)) {
				config.store(stream, null);
			}
		}
		catch (Throwable e) {
			if (App.LOGGER.isLoggable(Logs.WARNING)) {
				App.LOGGER.log(Logs.WARNING, "Unable to save configuration ("
						+ e.getClass().getSimpleName() + " : " + e.getMessage() + ")");
			}
		}
	}

	public File getConfigFile(App app) {
		String path = app.get(AppConfiguration.class)
				.getProperty("Config.File", null);
		if (path == null) {
			return null;
		}
		path = path.replace("${cd}", System.getProperty("user.dir"));
		path = path.replace("${rd}", Paths.get("").toAbsolutePath().toString());
		return new File(path);
	}	

}
