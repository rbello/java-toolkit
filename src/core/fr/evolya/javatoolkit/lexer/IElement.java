package fr.evolya.javatoolkit.lexer;

public interface IElement<T> {
	
	public String getTokenName();
	public T getTokenValue();
	
	public static class Element<T> implements IElement<T> {
		
		private String tokenName;
		private T tokenValue;

		public Element(String tokenName, T tokenValue) {
			this.tokenName = tokenName;
			this.tokenValue = tokenValue;
		}
		
		public String getTokenName() {
			return tokenName;
		}

		public T getTokenValue() {
			return tokenValue;
		}
		
		@Override
		public String toString() {
			return tokenName;
		}

	}
	
}
