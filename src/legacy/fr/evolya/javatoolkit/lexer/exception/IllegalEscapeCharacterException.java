package fr.evolya.javatoolkit.lexer.exception;

public class IllegalEscapeCharacterException extends ParserException {

	private static final long serialVersionUID = -6582222936656116439L;

	public IllegalEscapeCharacterException(int line, int column) {
        super("illegal escape character", line, column);
    }

}
