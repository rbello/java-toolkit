package fr.evolya.javatoolkit.lexer.exception;


public class ParserException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private int line = 0;
    private int column = 0;

    public ParserException(String msg) {
        super(msg);
    }

    public ParserException(Throwable t) {
        super(t); // Cause
    }

    public ParserException(int line, int column) {
        this.line = line;
        this.column = column;
    }
    
    public ParserException(String msg, int line, int column) {
        super(String.format("%s, at line %s column %s", msg, line, column));
        this.line = line;
        this.column = column;
    }

    public ParserException(ParserException ex, int line, int column) {
        super(String.format("%s, at line %s column %s", ex.getMessage(), line, column), ex);
        this.line = line;
        this.column = column;
    }
    
    public int getLine() { return line; }
    
    public int getColumn() { return column; }
    
}
