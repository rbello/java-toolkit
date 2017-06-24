package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class MalformedStringDeclarationException extends ParserException {

    public MalformedStringDeclarationException(int line, int column) {
        super(line, column);
    }

    @Override
    public String getName() {
        return "string declaration malformed";
    }

}
