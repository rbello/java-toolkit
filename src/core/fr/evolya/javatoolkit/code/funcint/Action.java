package fr.evolya.javatoolkit.code.funcint;

@FunctionalInterface
public interface Action<T> {
	
	void action(T arg);
	
}
