package fr.evolya.javatoolkit.app.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.code.IncaLogger;

public class PropertiesFileConfiguration implements AppConfiguration {

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
			if (App.LOGGER.isLoggable(IncaLogger.INFO)) {
				App.LOGGER.log(IncaLogger.INFO, "Unable to load: " + file
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

	@Override
	public boolean containsKey(String key) {
		return _config.containsKey(key);
	}

	@Override
	public String getProperty(String key) {
		return _config.getProperty(key);
	}

	@Override
	public AppConfiguration setProperty(String key, String value) {
		_config.setProperty(key, value);
		return this;
	}

	@Override
	public AppConfiguration addProperties(Map<String, String> set) {
		_config.putAll(set);
		return this;
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> out = new HashMap<String, String>();
		for (Entry<Object, Object> e : _config.entrySet()) {
			out.put((String)e.getKey(), (String)e.getValue());
		}
		return out;
	}

	@Override
	public String toString(String string, String... properties) {
		return String.format(string, Arrays.stream(properties).filter(p -> _config.containsKey(p)).map(p -> _config.get(p)).collect(Collectors.toList()).toArray());
	}

}
