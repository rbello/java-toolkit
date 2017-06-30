package fr.evolya.javatoolkit.app.config;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NonPersistentConfiguration implements AppConfiguration {

	protected Map<String, String> _config = new HashMap<String, String>();
	
	public NonPersistentConfiguration() {
	}
	
	public NonPersistentConfiguration(AppConfiguration config) {
		addProperties(config.getProperties());
	}

	@Override
	public boolean save(File file) {
		return false;
	}

	@Override
	public boolean load(File file) {
		return false;
	}

	@Override
	public boolean containsKey(String key) {
		return _config.containsKey(key);
	}

	@Override
	public AppConfiguration setProperty(String key, String value) {
		_config.put(key, value);
		return this;
	}

	@Override
	public AppConfiguration addProperties(Map<String, String> set) {
		_config.putAll(set);
		return this;
	}

	@Override
	public Map<String, String> getProperties() {
		return _config;
	}

	@Override
	public String toString(String string, String... properties) {
		return String.format(string, Arrays.stream(properties).filter(p -> _config.containsKey(p)).map(p -> _config.get(p)).collect(Collectors.toList()).toArray());
	}

	@Override
	public String getProperty(String key) {
		return _config.get(key);
	}
	
	@Override
	public String getProperty(String key, String defaultValue) {
		return _config.containsKey(key) ? _config.get(key) : defaultValue;
	}

	@Override
	public long getPropertyInt(String key) {
		return Integer.parseInt(_config.get(key));
	}

	@Override
	public long getPropertyInt(String key, int defaultValue) {
		return _config.containsKey(key) ? Integer.parseInt(_config.get(key)) : defaultValue;
	}

}
