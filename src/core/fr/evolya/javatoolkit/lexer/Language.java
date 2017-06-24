package fr.evolya.javatoolkit.lexer;

public interface Language {
    
    boolean isFloat(String txt);
    
    boolean isInteger(String txt);
    
    boolean isCommand(String txt);

}