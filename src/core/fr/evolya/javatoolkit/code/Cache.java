package fr.evolya.javatoolkit.code;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.annotations.ToOverride;

public abstract class Cache<K, V> {
	
	/**
	 * Logger
	 */
	protected static final Logger LOGGER = Logs.getLogger("Cache");

	/**
	 * Les données en cache
	 */
	private Map<K, CacheObject> _data;
	
	/**
	 * Le temps de vie (en millisecondes) des objets du cache
	 */
	private float _ttl;
	
	/**
	 * Constructeur simple, avec un TTL de 10 secondes par défaut
	 */
	public Cache() {
		this(10);
	}
	
	/**
	 * Constructeur
	 * 
	 * @param ttl En secondes
	 */
	public Cache(float ttl) {
		_data = new HashMap<K, CacheObject>();
		this._ttl = ttl * 1000;
	}
	
	/**
	 * Modifie la valeur du cache pour l'objet ayant la clé donnée
	 */
	public void setValue(K key, V value) {
		_data.put(key, new CacheObject(value));
	}
	
	/**
	 * Renvoie TRUE si le cache contient un objet ayant la clé donnée.
	 * Cette méthode ne prends pas en compte si cet objet est expiré ou non.
	 */
	public boolean isCached(K key) {
		return _data.containsKey(key);
	}
	
	/**
	 * Renvoie TRUE si le cache contient un objet ayant la clé donnée, et
	 * si cet objet n'est pas expiré.
	 */
	public boolean isCachedAndValid(K key) {
		if (!_data.containsKey(key)) return false;
		CacheObject cache = _data.get(key);
		if (cache == null) return false;
		return !cache.isExpired();
	}
	
	/**
	 * Renvoie la donnée du cache pour la clé donnée, uniquement si elle est
	 * encore à jour.
	 */
	public V getCacheValue(K key) {
		if (!_data.containsKey(key)) return null;
		CacheObject cache = _data.get(key);
		if (cache == null) return null;
		if (cache.isExpired()) return null;
		return cache.getValue();
	}
	
	/**
	 * Renvoie la valeur pour la clé donnée.
	 * Si le cache peut être utilisé, alors c'est cette valeur qui sera renvoyée.
	 * Sinon on fera appel à la méthode updateValue() pour mettre à jour la valeur
	 * et la renvoyer.
	 */
	public V getValue(K key) {
		
		// Méfiance...
		if (key == null)
			throw new NullPointerException();
		
		// On demande la valeur dans le cache. Cette méthode renvoie la valeur
		// uniquement si l'objet est encore à jour
		V value = getCacheValue(key);
		
		// Si le cache ne contient pas la donnée ou si elle est expirée, on
		// demande la mise à jour de la valeur.
		if (value == null) {

			try {
				value = updateValue(key);
				setValue(key, value);
			}
			catch (Throwable t) {
				if (LOGGER.isLoggable(Logs.DEBUG)) {
					LOGGER.log(Logs.DEBUG, "Exception on cache update: " + key, t);
				}
				removeValue(key);
			}
			
		}
		
		// Et on renvoie la valeur
		return value;
		
	}
	
	private void removeValue(K key) {
		_data.remove(key);
	}

	/**
	 * Mise à jour de la valeur pour la clé donnée.
	 */
	@ToOverride
	protected abstract V updateValue(K key) throws Exception;

	/**
	 * Représente un objet dans le cache, contenant une donnée et une date
	 * de mise à jour.
	 */
	public class CacheObject {

		private V value;
		private long creation;

		public CacheObject(V value) {
			this.value = value;
			this.creation = new Date().getTime();
		}

		public V getValue() {
			return value;
		}

		public boolean isExpired() {
			return new Date().getTime() - creation > _ttl;
		}
		
	}

	public void invalidate(K key) {
		_data.get(key).creation = 0;
	}

	public void invalidate() {
		_data.clear();
	}
	
}
