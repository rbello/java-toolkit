
package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.lexer.Structure.Symbol;

public interface Context {

    // les variables enregistrées
    Symbol getStaticSymbolByName(String name);
    
}
