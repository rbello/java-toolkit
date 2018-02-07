package fr.evolya.javatoolkit.app.config;

import java.io.File;
import java.util.Map;

public interface AppConfiguration {

	public boolean save(File file);
	
	public boolean load(File file);

	public boolean containsKey(String key);

	public AppConfiguration setProperty(String key, String value);
	
	public void setPropertyIfUndefined(String key);

	public void setPropertyIfUndefined(String key, Object value);
	
	public AppConfiguration addProperties(Map<String, String> set);
	
	public Map<String, String> getProperties();

	public String toString(String string, String... properties);
	
	public String getProperty(String key);
	
	public String getProperty(String key, String defaultValue);

	public long getPropertyInt(String key);
	
	public long getPropertyInt(String key, int defaultValue);

}
