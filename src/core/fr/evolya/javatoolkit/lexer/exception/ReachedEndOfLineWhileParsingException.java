package fr.evolya.javatoolkit.lexer.exception;

public class ReachedEndOfLineWhileParsingException extends ParserException {

	private static final long serialVersionUID = -5167262926505957465L;

	public ReachedEndOfLineWhileParsingException() {
		super("reached end of line while parsing");
	}
	
}
