package fr.evolya.javatoolkit.code.funcint;

@FunctionalInterface
public interface Func<I, O> {
	
	O function(I arg);
	
}
