package fr.evolya.javatoolkit.code.funcint;

public interface Consumer<I, E extends Exception> {

	void accept(I input) throws E;
	
}
