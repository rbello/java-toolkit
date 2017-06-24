package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class IllegalEscapeCharacterException extends ParserException {

    public IllegalEscapeCharacterException(int line, int column) {
        super(line, column);
    }

    @Override
    public String getName() {
        return "illegal escape character";
    }

}
