package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class WrongOperatorSyntaxException extends ParserException {

    public WrongOperatorSyntaxException(int row, int col) {
        super("wrong operator syntax", row, col);
    }

}