package fr.evolya.javatoolkit.code.funcint;

@FunctionalInterface
public interface Action<T> {
	
	void call(T arg);
	
}
