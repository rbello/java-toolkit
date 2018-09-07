package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.lexer.Structure.Expression;

/**
 * Un executor permet d'executer des commandes.
 * L'entrée principale est la méthode execute publique.
 * 
 */
public class Executor {

    public Structure execute(String txt, Language lang, SecurityManager manager) throws Throwable {
        return execute(ExpressionBuilder1.build(txt), lang, null, manager);
    }
    
    public Structure execute(String txt, Language lang, Context context,
            SecurityManager manager) throws Throwable {
        return execute(ExpressionBuilder1.build(txt), lang, context, manager);
    }

    private Structure execute(Expression exp, Language lang, Context context, SecurityManager manager) {
        
        return new Structure.String(exp.toXml(" "));
        
        //return null;
    }

}
