package fr.evolya.javatoolkit.lexer.v3;

import fr.evolya.javatoolkit.lexer.v3.IElement.Element;
import fr.evolya.javatoolkit.lexer.v3.IExpressionRule.AbstractExpressionRule;

public abstract class Pattern<T> extends AbstractExpressionRule {

	private java.util.regex.Pattern regex;
	
	public Pattern(String regex, String name) {
		super(name);
		this.regex = java.util.regex.Pattern.compile(regex);
	}

	@Override
	public boolean accept(String buffer) {
		return regex.matcher(buffer).matches();
	}
	
	public abstract T getValue(String value);
	
	@Override
	public IElement<T> from(String buffer) {
		return new Element<T>(getTokenName(), getValue(buffer));
	}
	
	@Override
	public boolean is(String buffer) {
		try {
			Object value = getValue(buffer);
			return value != null;
		}
		catch (Throwable t) {
			return false;
		}
	}
	
}
