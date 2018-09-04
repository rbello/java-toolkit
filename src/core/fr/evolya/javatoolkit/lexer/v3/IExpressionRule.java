package fr.evolya.javatoolkit.lexer.v3;

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
	
	public static abstract class AbstractStringComparatorRule extends AbstractExpressionRule {

		private String tokenValue;

		public AbstractStringComparatorRule(String tokenName, String tokenValue) {
			super(tokenName);
			this.tokenValue = tokenValue;
		}

		@Override
		public boolean accept(String buffer) {
//			System.out.println("Compare " + tokenValue + " WITH " + buffer);
			
			if (buffer.length() > tokenValue.length()) return false;
			
			if (tokenValue.startsWith(buffer)) return true;
			
			return false; // TODO
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

	

	
}
