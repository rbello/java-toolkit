package fr.evolya.javatoolkit.lexer.v3;

import fr.evolya.javatoolkit.lexer.v3.IExpressionRule.AbstractExpressionRule;

public abstract class Pattern<T> extends AbstractExpressionRule {

	private java.util.regex.Pattern regex;
	
	public Pattern(String regex, String name) {
		super(name);
		this.regex = java.util.regex.Pattern.compile(regex);
	}

	@Override
	public boolean accept(StringBuffer buffer) {
		return regex.matcher(buffer).matches();
	}
	
	public abstract T getValue(String value);
	
}
