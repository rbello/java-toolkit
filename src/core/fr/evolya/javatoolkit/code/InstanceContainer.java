package fr.evolya.javatoolkit.code;

public class InstanceContainer<T> {

	private T instance;
	
	public InstanceContainer() {
		this(null);
	}
	
	public InstanceContainer(T instance) {
		this.instance = instance;
	}
	
	public void set(T value) {
		this.instance = value;
	}
	
	public T get() {
		return this.instance;
	}
	
}
