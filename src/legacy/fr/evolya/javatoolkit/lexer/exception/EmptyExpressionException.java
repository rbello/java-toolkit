package fr.evolya.javatoolkit.lexer.exception;

public class EmptyExpressionException extends ExpressionDeclarationMalformedException {
    
	private static final long serialVersionUID = 6753660182803366799L;

	public EmptyExpressionException() {
        super("empty expression", 0, 0);
    }
    
}
