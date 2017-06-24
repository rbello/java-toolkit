package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class ReachedEndOfLineWhileParsingException extends ParserException {

    @Override
    public String getName() {
        return "reached end of line while parsing";
    }

}
