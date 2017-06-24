package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.lexer.exception.EmptyExpressionException;
import fr.evolya.javatoolkit.lexer.exception.ExpressionDeclarationMalformedException;
import fr.evolya.javatoolkit.lexer.exception.IllegalEscapeCharacterException;
import fr.evolya.javatoolkit.lexer.exception.MalformedStringDeclarationException;
import fr.evolya.javatoolkit.lexer.exception.ParserException;
import fr.evolya.javatoolkit.lexer.exception.ReachedEndOfLineWhileParsingException;
import fr.evolya.javatoolkit.lexer.exception.WrongOperatorSyntaxException;

/**
 * Cet objet fabrique une expression à partir d'une string.
 * 
 * Cette expression n'est pas du tout dépendante du langage, il s'agit
 * de la décomposition de la chaîne en elements reconnaissables.
 */
public final class ExpressionBuilder {

    private static final char CHAR_ESCAPE       = '\\';
    private static final char CHAR_STRING       = '"';
    private static final char CHAR_EXP_START    = '(';
    private static final char CHAR_EXP_END      = ')';
    private static final char CHAR_SPACE        = ' ';
    private static final char CHAR_PLUS         = '+';
    private static final char CHAR_MINUS        = '-';
    private static final char CHAR_DIVISION     = '/';
    private static final char CHAR_MULTIPLY     = '*';
    private static final char CHAR_MODULO       = '%';
    
    
    private ExpressionBuilder() {
    }

    public static final Structure.Expression build(String txt) throws Throwable {
        
        if (txt.trim().isEmpty()) throw new EmptyExpressionException();
        
        final Structure.Expression e = new Structure.Expression();
        
        int context = 0;
        
        StringBuilder buffer1 = new StringBuilder();
        int buffer2 = 0;
        
        // Parsing
        
        for (int i = 0, y = txt.length(); i < y; i++) {
            
            char c = txt.charAt(i);
            
            switch (context) {
                
                    // EVERYTHING
                    // buffer1 stoque tous les characteres qui ne sont pas ( " et spc
                case 0 :
                    
                    // On vient de tomber sur un espace.
                    if (c == CHAR_SPACE) {
                        // Si le buffer n'est pas vide, on va construire un element
                        // On passe par la méthode build qui va se charger de reconnaitre
                        // le mot pour en faire un element approprié
                        if (buffer1.length() != 0) {
                            e.addStructure(ElementBuilder.build(buffer1.toString()));
                            buffer1 = null;
                            buffer1 = new StringBuilder();
                        }
                    }
                    
                    else if (c == CHAR_PLUS || c == CHAR_MINUS || c == CHAR_DIVISION
                            || c == CHAR_MULTIPLY || c == CHAR_MODULO) {
                        
                        // On commence par ajouter l'element declaré dans buffer1
                        // si il y a lieu
                        if (buffer1.length() != 0) {
                            e.addStructure(ElementBuilder.build(buffer1.toString()));
                            buffer1 = null;
                            buffer1 = new StringBuilder();
                        }
                        
                        // @TODO Buggé
                        
                        // 
                        if (c != CHAR_MINUS) {
                            // On recupere le dernier element enregistré, et on regarde
                            // s'il s'agit d'un autre operateur.
                            Structure s = e.getLastElement();
                            if (s != null && s instanceof Structure.Operator) {
                                throw new WrongOperatorSyntaxException(0, i+1);
                            }
                        }

                        // On ajoute l'element de structure correspondant au signe
                        if (c == CHAR_PLUS) e.addStructure(Structure.Plus.INSTANCE);
                        else if (c == CHAR_MINUS) e.addStructure(Structure.Minus.INSTANCE);
                        else if (c == CHAR_MULTIPLY) e.addStructure(Structure.Multiply.INSTANCE);
                        else if (c == CHAR_DIVISION) e.addStructure(Structure.Division.INSTANCE);
                        else if (c == CHAR_MODULO) e.addStructure(Structure.Modulo.INSTANCE);
                        
                    }
                    
                    else if (c == CHAR_EXP_END) {
                        throw new ExpressionDeclarationMalformedException(0, i+1);
                    }
                    
                    // On vient de tomber sur une parenthèse ouvrante.
                    // Il s'agit d'un début de déclaration d'expression
                    else if (c == CHAR_EXP_START) {
                        // Si le buffer1 n'est pas vide, c'est qu'il y a un problème
                        if (buffer1.length() != 0) {
                            throw new ExpressionDeclarationMalformedException(0, i+1);
                        }
                        context = 2;
                    }
                    // On vient de tomber sur des guillemets.
                    // Il s'agit d'un début de déclaration de string
                    else if (c == CHAR_STRING) {
                        if (buffer1.length() != 0) {
                            // Si le buffer1 n'est pas vide, c'est qu'il y a un problème
                            throw new MalformedStringDeclarationException(0, i+1);
                        }
                        // On se place en mode STRING
                        context = 1;
                    }
                    
                    else {
                        buffer1.append(c);
                    }
                    
                    break;
                    
                    // STRINGS (in current expression)
                    // C'est buffer1 qui contient le contenu de la chaîne
                case 1 :
                    
                    // On est dans une string, et on vient de tomber sur le
                    // charactère d'échapement
                    if (c == CHAR_ESCAPE) {
                        
                        // Si c'est le dernier charactère, alors il y a une erreur
                        if (i+1 >= y) throw new ReachedEndOfLineWhileParsingException();
                        
                        final char next = txt.charAt(i+1);
                        
                        // Echapement du guillemet
                        if (next == CHAR_STRING) {
                            buffer1.append(CHAR_STRING);
                            i++;
                        }
                        
                        // Echapement du charactere d'echapement
                        else if (next == CHAR_ESCAPE) {
                            buffer1.append(CHAR_ESCAPE);
                            i++;
                        }
                        
                        // Echapement du retour à la ligne
                        else if (next == 'n') {
                            buffer1.append('\n');
                            i++;
                        }
                        
                        else throw new IllegalEscapeCharacterException(0, i+1);
                        
                    }
                    
                    // On vient de tomber sur une deuxième accolade, et elle n'a
                    // pas été échapée. Il s'agit de la fin de la string
                    else if (c == CHAR_STRING) {
                        // On fabrique une Structure, et on l'ajoute à l'expression e
                        e.addStructure(ElementBuilder.buildString(buffer1.toString()));
                        // On supprime le buffer et on en fabrique une autre
                        buffer1 = null;
                        buffer1 = new StringBuilder();
                        // On replace dans le contexte zero (à la recherche de tout)
                        context = 0;
                    }
                    
                    // Sinon, on ajoute le charactère dans le buffer
                    else {
                        buffer1.append(c);
                    }
                    
                    break;
                    
                    // EXPRESSION (out of string)
                    // buffer1 contient le contenu de l'expression
                    // buffer2 s'incremente à chaque nouvelle parenthese
                    //  et permet de controler que toutes les expressions
                    //  sont bien fermées
                case 2 :
                    
                    // Si on tombe sur une nouvelle parenthese, on incremente
                    // buffer2 pour bien recompter les parentheses fermantes,
                    // et on ajoute le charactère dans buffer1 qui sera
                    // réevalué en temps qu'expression
                    if (c == CHAR_EXP_START) {
                        buffer1.append(c);
                        buffer2++;
                    }
                    // Fin de l'expression. 
                    else if (c == CHAR_EXP_END) {
                        // Si buffer2 est à zero, on prends le contenu de buffer1
                        // et on tente la construction d'une structure avec
                        if (buffer2 == 0) {
                            try {
                                e.addStructure(ExpressionBuilder.build(buffer1.toString()));
                            } catch (ParserException ex) {
                                throw new ParserException(ex, 0, i+1);
                            } catch (Throwable t) {
                                throw t;
                            }
                            // On réinitialise le buffer1
                            buffer1 = null;
                            buffer1 = new StringBuilder();
                            // On replace le contexte à zero (à la recherche de tout)
                            context = 0;
                        }
                        // Sinon, on vient simplement de fermer une des expressions ouvertes
                        // On décrémente buffer2
                        else {
                            buffer1.append(c);
                            buffer2--;
                        }
                    }
                    // On vient de tomber sur un guillemet.
                    // Il s'agit d'un debut de string dans une expression interne
                    // On passe en contexte 3
                    else if (c == CHAR_STRING) {
                        buffer1.append(c);
                        context = 3;
                    }
                    // Sinon on ajoute le charactère au buffer
                    else {
                        buffer1.append(c);
                    }
                    
                    break;
                    
                    // STRING (in inner expression)
                    // Se produit lorsqu'une string est déclarée dans une
                    // déclaration d'expression (des guillemets dans des parentheses).
                    // On se contente de detecter la fin de la string en faisant
                    // attention au charactère d'échapement.
                    // Cette string sera évaluée comme un element string lors
                    // de l'evaluation de l'expression.
                case 3 :
                    
                    if (c == CHAR_STRING) {
                        
                        if (txt.charAt(i-1) != CHAR_ESCAPE) {
                            buffer1.append(c);
                            context = 2;
                        }
                        
                        else {
                            buffer1.append(c);
                        }
                        
                    }
                    
                    else {
                        buffer1.append(c);
                    }
                    
                    break;
                    
                default :
                    throw new UnsupportedOperationException("not supposed to be thrown");
            }
            
        }
        
        // Si il reste quelque chose dans le buffer, on fabrique un element avec
        if (buffer1.length() != 0) e.addStructure(ElementBuilder.build(buffer1.toString()));
        buffer1 = null;
        
        // On verifie que le contexte soit bien à zero, sinon c'est qu'on était entrain de faire qqch
        if (context != 0) throw new ReachedEndOfLineWhileParsingException();
        
        // On verifie qu'on ne termine par sur un operateur
        Structure s = e.getLastElement();
        if (s != null && s instanceof Structure.Operator) {
            throw new ReachedEndOfLineWhileParsingException();
        }
        
        return e;
        
    }

}
