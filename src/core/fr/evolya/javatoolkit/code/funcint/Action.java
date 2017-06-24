package fr.evolya.javatoolkit.code.funcint;

@FunctionalInterface
public interface Action<A, B> {
	
	void action(A a, B b);
	
}
