package fr.evolya.javatoolkit.lexer.v3;

public abstract class Pattern<T> {

	public Pattern(String regex, String name) {
		// TODO Auto-generated constructor stub
	}
	
	public abstract T getValue(String value);

}
