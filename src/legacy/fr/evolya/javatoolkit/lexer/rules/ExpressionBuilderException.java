package fr.evolya.javatoolkit.lexer.rules;

public abstract class ExpressionBuilderException extends RuntimeException {

	public ExpressionBuilderException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = 1L;

	public static class UnexpectedEndOfInput extends ExpressionBuilderException {
		public UnexpectedEndOfInput(String msg) {
			super(msg);
		}
	}
	
}
