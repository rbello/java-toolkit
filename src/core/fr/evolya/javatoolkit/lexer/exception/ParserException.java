package fr.evolya.javatoolkit.lexer.exception;


@SuppressWarnings("serial")
public class ParserException extends Exception {

    private int line = 0;
    private int column = 0;
    private String name = "unknown error";
    
    public ParserException() {
        super();
    }

    public ParserException(String msg) {
        super(msg);
    }

    public ParserException(Throwable t) {
        super(t);
    }

    public ParserException(int line, int column) {
        this.line = line;
        this.column = column;
    }
    
    public ParserException(String msg, int line, int column) {
        super(msg);
        this.line = line;
        this.column = column;
    }

    public ParserException(ParserException ex, int line, int column) {
        this(ex.getMessage(), line, column);
        name = ex.getName();
    }
    
    public String getName() { return name; }
    
    public int getLine() { return line; }
    
    public int getColumn() { return column; }

}
