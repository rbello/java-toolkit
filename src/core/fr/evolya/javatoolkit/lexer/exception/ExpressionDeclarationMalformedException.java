package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class ExpressionDeclarationMalformedException extends ParserException {

	public ExpressionDeclarationMalformedException(int line, int column) {
        super(line, column);
    }

    @Override
    public String getName() {
        return "expression declaration malformed";
    }

}
