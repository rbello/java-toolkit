package fr.evolya.javatoolkit.lexer.exception;

public class CannotFindSymbolException extends ParserException {
	
	private static final long serialVersionUID = -7692710922520092454L;

	public CannotFindSymbolException(String symbol) {
        super("cannot find symbol "+symbol);
    }
    
}
