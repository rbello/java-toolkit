package fr.evolya.javatoolkit.transactional;

public interface Action<T> {

	public void execute(T data);
	
}
