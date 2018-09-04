package fr.evolya.javatoolkit.lexer.v3;

public interface IExpressionRule {

	public String getTokenName();
	public boolean accept(StringBuffer buffer);

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
		public boolean accept(StringBuffer buffer) {
//			System.out.println("Compare " + tokenValue + " WITH " + buffer);
			
			if (buffer.length() > tokenValue.length()) return false;
			
			if (tokenValue.startsWith(buffer.toString())) return true;
			
			return false; // TODO
		}
		
	}
	
}
