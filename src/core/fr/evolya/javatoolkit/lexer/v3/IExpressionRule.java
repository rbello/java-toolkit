package fr.evolya.javatoolkit.lexer.v3;

import java.util.function.Predicate;

import fr.evolya.javatoolkit.lexer.v3.IElement.Element;

public interface IExpressionRule {

	public String getTokenName();
	public boolean accept(String buffer);
	public IElement<?> from(String buffer);
	public boolean is(String buffer);

	public static abstract class AbstractExpressionRule implements IExpressionRule {

		private String tokenName;

		public AbstractExpressionRule(String tokenName) {
			this.tokenName = tokenName;
		}

		@Override
		public String getTokenName() {
			return tokenName;
		}
		
		@Override
		public String toString() {
			return this.tokenName;
		}
		
	}
	
	public static class StringComparatorRule extends AbstractExpressionRule {

		private String tokenValue;

		public StringComparatorRule(String tokenName, String tokenValue) {
			super(tokenName);
			this.tokenValue = tokenValue;
		}

		@Override
		public boolean accept(String buffer) {
			if (buffer.length() > tokenValue.length()) return false;
			if (tokenValue.startsWith(buffer)) return true;
			return false;
		}
		
		@Override
		public IElement<String> from(String buffer) {
			return new Element<String>(getTokenName(), buffer);
		}
		
		@Override
		public boolean is(String buffer) {
			return this.tokenValue.equals(buffer);
		}
		
	}

	public static abstract class PatternComparatorRule<T> extends AbstractExpressionRule {

		private java.util.regex.Pattern regex;
		
		public PatternComparatorRule(String regex, String name) {
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

	public static class CustomComparatorRule extends AbstractExpressionRule {

		private Predicate<String> predicate;

		public CustomComparatorRule(String tokenName, Predicate<String> predicate) {
			super(tokenName);
			this.predicate = predicate;
		}

		@Override
		public boolean accept(String buffer) {
			return is(buffer);
		}

		@Override
		public IElement<?> from(String buffer) {
			return new Element<String>(getTokenName(), buffer);
		}

		@Override
		public boolean is(String buffer) {
			return predicate.test(buffer);
		}
		
	}

	
}
