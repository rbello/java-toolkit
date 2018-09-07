package fr.evolya.javatoolkit.lexer;

import java.util.function.Predicate;

import fr.evolya.javatoolkit.lexer.IElement.Element;

public interface IExpressionRule {

	public String getTokenName();
	public boolean accept(String buffer);
	public IElement<?> from(String buffer);
	public boolean is(String buffer);

	public static abstract class AbstractExpressionRule implements IExpressionRule {

		protected String tokenName;

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
	
	/**
	 * Comparaison avec une string à partir du début.
	 */
	public static class StringComparatorRule extends AbstractExpressionRule {

		protected String tokenValue;

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
		public IElement<?> from(String buffer) {
			return new Element<String>(getTokenName(), buffer);
		}
		
		@Override
		public boolean is(String buffer) {
			return this.tokenValue.equals(buffer);
		}
		
	}

	/**
	 * Correspondance avec une expression regulière.
	 *
	 * @param <T>
	 */
	public static abstract class PatternComparatorRule<T> extends AbstractExpressionRule {

		protected java.util.regex.Pattern regex;
		
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

	/**
	 * Correspondance customisable à l'aide d'une fonction Predicate.
	 */
	public static class CustomComparatorRule extends AbstractExpressionRule {

		protected Predicate<String> predicate;

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
	
	/**
	 * Correspondance avec un token de début et un token de fin.
	 */
	public static class EncapsedComparatorRule extends StringComparatorRule {

		protected String endToken;
		private String escapeToken;
		
		public EncapsedComparatorRule(String tokenName, String startToken, String endToken) {
			this(tokenName, startToken, endToken, null);
		}

		public EncapsedComparatorRule(String tokenName, String startToken, String endToken, String escapeToken) {
			super(tokenName, startToken);
			this.endToken = endToken;
			this.escapeToken = escapeToken;
		}

		public boolean finished(String value) {
			if (value.endsWith(endToken)) {
				if (escapeToken != null) {
					int pos = value.length() - endToken.length() - escapeToken.length();
					if (escapeToken.equals(value.substring(pos, pos + escapeToken.length()))) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		public String getStartToken() {
			return tokenValue;
		}
		
		public String getEndToken() {
			return endToken;
		}
		
	}

	
}
