package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class EmptyExpressionException extends ExpressionDeclarationMalformedException {
    
	public EmptyExpressionException() {
        super(0, 0);
    }
    
    @Override
    public String getName() {
        return "empty expression";
    }

}
