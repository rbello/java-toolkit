package fr.evolya.javatoolkit.code;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Une map de type <String, Object> ou <String, Object[]>
 * 
 * Cette map a deux particularités :
 *  - Il est possible de typer les objets en lecture avec get(String, Class)
 *  - Il est possible de définir une callback lors de l'écriture
 * 
 * Utiliser set(String,Object) pour enregistrer, et get(String, Class) pour lire.
 */
public class MutableMap implements Map<String, Object> {

	protected Map<String, Object> data;
	protected Map<String, Setter<Object>> setters;
	
	public MutableMap() {
		data = new HashMap<String, Object>();
		setters = new HashMap<String, Setter<Object>>();
	}
	
	public MutableMap set(String key, Object value) {
		put(key, value);
		if (setters.containsKey(key)) {
			setters.get(key).set(value);
		}
		return this;
	}

	public MutableMap set(String key, Object[] values) {
		put(key, values);
		if (setters.containsKey(key)) {
			setters.get(key).set(values);
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public MutableMap set(String key, Object value, Setter<?> setter) {
		put(key, value);
		setters.put(key, (Setter<Object>) setter);
		((Setter<Object>)setter).set(value);
		return this;
	}

	@Override
	public void clear() {
		data.clear();
		setters.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return data.entrySet();
	}

	@Override
	public Object get(Object key) {
		if (data.containsKey(key)) {
			return data.get(key);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> c) {
		if (data.containsKey(key)) {
			return (T) data.get(key);
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return data.keySet();
	}

	@Override
	public Object put(String key, Object value) {
		return data.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		data.putAll(m);
	}

	@Override
	public Object remove(Object key) {
		if (data.containsKey(key)) {
			setters.remove(key);
			return data.remove(key);
		}
		return null;
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public Collection<Object> values() {
		return data.values();
	}
	
	public interface Setter<T> {
		public void set(T value);
	}

}
