package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class CannotFindSymbolException extends ParserException {
	
	public CannotFindSymbolException(String symbol) {
        super("symbol "+symbol);
    }

    @Override
    public String getName() {
        return "cannot find symbol";
    }
    
}
