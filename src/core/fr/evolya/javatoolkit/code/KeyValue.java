package fr.evolya.javatoolkit.code;

public class KeyValue<K, V> {

	private K key;
	private V value;
	
	public KeyValue() {
		this(null, null);
	}
	
	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return key + " => " + value;
	}
	
}
