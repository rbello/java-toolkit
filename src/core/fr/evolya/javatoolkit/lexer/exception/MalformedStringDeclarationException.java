package fr.evolya.javatoolkit.lexer.exception;

public class MalformedStringDeclarationException extends ParserException {

	private static final long serialVersionUID = -884527045370660193L;

	public MalformedStringDeclarationException(int line, int column) {
        super("String declaration malformed", line, column);
    }

}
