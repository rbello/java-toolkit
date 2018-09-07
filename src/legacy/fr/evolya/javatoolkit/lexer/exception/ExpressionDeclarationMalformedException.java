package fr.evolya.javatoolkit.lexer.exception;

public class ExpressionDeclarationMalformedException extends ParserException {

	private static final long serialVersionUID = 2050637521283722968L;

	public ExpressionDeclarationMalformedException(String msg, int line, int column) {
        super(msg, line, column);
    }
	
	public ExpressionDeclarationMalformedException(int line, int column) {
        super("Expression declaration malformed", line, column);
    }

}
