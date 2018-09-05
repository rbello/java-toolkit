package fr.evolya.javatoolkit.lexer.v3;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.lexer.v3.IElement.Element;
import fr.evolya.javatoolkit.lexer.v3.IExpressionRule.CustomComparatorRule;
import fr.evolya.javatoolkit.lexer.v3.IExpressionRule.PatternComparatorRule;
import fr.evolya.javatoolkit.lexer.v3.IExpressionRule.StringComparatorRule;

public class ExpressionBuilder3 {

	public static final Logger LOGGER = Logs.getLogger("ExpressionBuilder");
	
	private List<IExpressionRule> rules = new LinkedList<>();
	
	public ExpressionBuilder3() {
	}

	public List<IExpressionRule> getRules() {
		return new LinkedList<>(this.rules);
	}

	public ExpressionBuilder3 add(IExpressionRule rule) {
		this.rules.add(rule);
		return this;
	}

	public ExpressionBuilder3 addOperator(String token, String name) {
		// TODO Check arguments, name too
		return add(new StringComparatorRule(token, "T_OPERATOR_" + name.toUpperCase()));
	}

	public ExpressionBuilder3 addKeywords(String... tokens) {
		for (String token : tokens) {
			// TODO Check arguments, name too
			add(new StringComparatorRule(token, "T_KEY_" + token.toUpperCase()));
		}
		return this;
	}

	public <T> ExpressionBuilder3 addPattern(String regex, String name, Function<String, T> mapper) {
		// TODO Check arguments, name too
		return add(new PatternComparatorRule<T>(regex, "T_" + name.toUpperCase()) {
			public T getValue(String value) {
				return mapper.apply(value);
			}
		});
	}

	public ExpressionBuilder3 addEncapsedSequence(String startToken, String endToken, String name) {
		return addEncapsedSequence(startToken, endToken, name, null);
	}

	public ExpressionBuilder3 addEncapsedSequence(String startToken, String endToken, String name, String escapeToken) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addEncapsedExpression(String string, String string2) {
		// TODO Auto-generated method stub
		
	}

	public void addToken(String token, String name) {
		// TODO Auto-generated method stub
		
	}
	
	public void addToken(String tokenName, Predicate<String> predicate) {
		// TODO Check arguments, name too
		add(new CustomComparatorRule("T_" + tokenName.toUpperCase(), predicate));
	}

	public Expression parse(String input) {
		return parse(input, null);
	}
	
	public Expression parse(String input, Expression parent) {
		// TODO Auto-generated method stub
		
		if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
			LOGGER.log(Logs.DEBUG_FINE, "BEGIN WITH => " + input);
		}
		
		Expression ex = new Expression(input, parent);
		
		List<IExpressionRule> rules = getRules(); // copy
		final int max = rules.size();
		final StringBuffer buffer = new StringBuffer();
		
		nextChar:
        for (int i = 0, l = input.length() - 1; i <= l; i++) {
            char c = input.charAt(i);
            
            // Log
            if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
            	StringBuilder log = new StringBuilder();
            	log.append(i);
            	log.append(". CHAR [ ");
            	log.append(c);
            	log.append(" ]");
    			if (rules.size() == max)
    				log.append(" NEW");
    			else {
    				log.append(" - Candidates: ");
    				log.append(rules);
    			}
    			LOGGER.log(Logs.DEBUG_FINE, log.toString());
    		}
            
            // On se retrouve avec une liste de règles applicables vide. Cela signifie que nous ne sommes plus
            // au premier caractère, et que le caractère -1 ne correspondait plus avec les règles retenues.
            if (rules.size() == 0) {
            	
            	// On fait donc une copie de toutes les règles initiales, et on regarde celles qui pourraient
            	// correspondre au nouveau caractère actuel.
            	List<IExpressionRule> newCandidates = getRules();
            	newCandidates.removeIf(rule -> !rule.accept("" + c));
                
            	// Si on a des règles qui correspondent, cela signifie que la portion précédente (contenue dans buffer)
            	// ne correspondait à aucune règle, c'est donc une expression littérale.
                if (newCandidates.size() > 0) {
                	LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
                	// On ajoute donc un élément de type T_LITTERAL
                	ex.add(new Element<String>("T_LITTERAL", buffer.toString()));
                	buffer.setLength(0);
                	buffer.append(c);
                	// On conserve les règles applicables au caractère courant et on passe au suivant 
                	rules = newCandidates;
                }
                
                // Sinon, tant qu'on ne trouve pas une règle applicable, on continue à stoquer les caractères qui arrivent
                // dans le buffer et on continue.
                else {
                	LOGGER.log(Logs.DEBUG_FINE, "	-> CONTINUE");
                	buffer.append(c);
                }
                
                if (i == l) {
                	handleLastToken(ex, rules, buffer, c);
                	break;
                }
                
                continue nextChar;
            	
            }
            
            // On se retrouve ici si il y a des règles applicables pour le contenu du buffer. On fait donc une copie
            // de ces règles.
            List<IExpressionRule> oldCandidates = new LinkedList<>(rules);
            
            // On retire toutes les règles qui ne correspondent pas au contenu du buffer plus le nouveau caractère.
            rules.removeIf(rule -> !rule.accept(buffer.toString() + c));
            
            // On se retrouve donc soit avec une liste de règles vide, ou bien on arrive à la fin de l'input : on va donc
            // chercher à voir si des règles existaient avant et peuvent être appliquées au contenu du buffer.
            if (rules.isEmpty() || i == l) {
            	
            	// Avant que l'on rencontre le caractère actuel, certaintes règles étaient applicables spécifiquement
            	// au contenu du buffer (ce n'était également pas toutes les règles qui avaient été initialisées)
            	if (oldCandidates.size() > 0 && oldCandidates.size() < max) {
            		
            		// Si il avait plusieurs règles applicables (par exemple le caractère '-' peut correspondre à l'opérateur
            		// T_OPERATOR_MINUS, ou bien à l'opérateur d'accès aux objets ('->') ou bien encore au signe moins d'un
            		// nombre négatif). On tente donc de réduire le nombre de règles à celles qui correspondent EXACTEMENT au
            		// contenu du buffer. Par exemple le caractère '-' correspond exactemnt à la règle de l'opérateur moins.
            		// Normalement il ne devrait y en avoir qu'une.
            		if (oldCandidates.size() > 1) {
            			LOGGER.log(Logs.DEBUG_FINE, "	-> REDUCE Exactly " + buffer.toString());
            			oldCandidates.removeIf(rule -> !rule.is(buffer.toString()));
            		}
        			
            		// Nous sommes donc dans le cas normal où seule une règle ne correspond parfaitemnt au contenu du buffer.
            		// On va donc demander à la règle de créer un IElement à rajouter dans l'expression.
            		if (oldCandidates.size() == 1) {
            			IElement<?> el = oldCandidates.get(0).from(buffer.toString());
            			ex.add(el);
            			LOGGER.log(Logs.DEBUG_FINE, "	-> ADD " + el.getTokenName());
            			buffer.setLength(0);
            			// On va repasser sur le caractère en cours, en réinitialisant les règles.
            			i--;
            			rules = getRules();
            		}
            		
            		// Il n'y a plus aucune règle : cela signife que plusieurs règles étaient valides mais aucune ne
            		// correspond exactement au contenu du buffer. Par exemple, si le buffer contient 'p' les règles
            		// des tokens 'public' et 'private' vont correspondre. Mais si on rencontre ensuite le caractère
            		// 'a' plus aucune règle ne correspond exactement. On a donc une expression littérale à créer.
            		else if (oldCandidates.size() == 0) {
            			throw new RuntimeException("Create litteral => " + buffer); // TODO 
            		}
            		
            		// Plusieurs règles sont encore en concurrence, mais valident EXACTEMENT le contenu du buffer. Il s'agit
            		// ici d'une erreur de configuration du builder, car deux règles ne sont pas sensées valider exactemnt
            		// le même contenu et pour autant être des règles différentes. On va donc lever une exception.
            		else {
            			throw new RuntimeException("Unexpected " + c); // TODO 
            		}
            		
            		 if (i == l) {
                     	System.out.println("Can't continue");
                     }
            		
            		// Dans tous les cas on continue au caractère suivant.
            		continue nextChar;
            	}
            }

            // On tombe sur un espace blanc, qui va jouer le rôle de séparateur. On va donc stocker dans la structure
            // un nouvel élément T_LITTERAL si le buffer contenait du contenu; et ajouter égalemement un élément
            // de type T_WHITESPACE.
//            if (Character.isWhitespace(c)) {
//            	if (buffer.length() > 0) {
//            		ex.add(new Element<String>("T_LITTERAL", buffer.toString()));
//            		LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
//            	}
//            	ex.add(new Element<Character>("T_WHITESPACE", c));
//            	LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_WHITESPACE [" + buffer.toString() + "]");
//            	buffer.setLength(0);
//            	rules = getRules();
//            	continue nextChar;
//            }
            
            buffer.append(c);
            
            // On traite ici le cas du contenu à la fin de l'input. Si le buffer n'est pas vide on va traiter son
            // contenu. Pour cela, on va rechercher dans les règles applicables celle qui correspond exactement
            // au contenu du buffer. Si on en trouve une seule, alors c'est la bonne. Zero cela signifie que ce
            // dernier token est de type T_LITTERAL. Sinon (plusieurs règles) on se retrouve dans une erreur.
            if (i == l && buffer.length() > 0) {
            	handleLastToken(ex, rules, buffer, c);
            	break;
            }
            
        }
		
		return ex;
	}

	private void handleLastToken(Expression ex, List<IExpressionRule> rules, final StringBuffer buffer, char c) {
		rules.removeIf(rule -> !rule.is(buffer.toString()));
		if (rules.size() == 1) {
			IElement<?> el = rules.get(0).from(buffer.toString());
			ex.add(el);
			LOGGER.log(Logs.DEBUG_FINE, "	-> ADD " + el.getTokenName());
		}
		else if (rules.size() == 0) {
			ex.add(new Element<String>("T_LITTERAL", buffer.toString()));
			LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
		}
		// Plusieurs règles sont encore en concurrence, mais valident EXACTEMENT le contenu du buffer. Il s'agit
		// ici d'une erreur de configuration du builder, car deux règles ne sont pas sensées valider exactemnt
		// le même contenu et pour autant être des règles différentes. On va donc lever une exception.
		else {
			throw new RuntimeException("Unexpected " + c); // TODO 
		}
	}
	
	public static ExpressionBuilder3 createNew() {
		return new ExpressionBuilder3();
	}
	
	public static ExpressionBuilder3 createDefault() {
		ExpressionBuilder3 builder = new ExpressionBuilder3();
		
		builder.addOperator("+", "plus");
		builder.addOperator("-", "minus");
		builder.addOperator("/", "div");
		builder.addOperator("*", "mul");
		
		builder.addOperator(".", "object");
		builder.addOperator("->", "pointer");
		builder.addOperator("::", "nekudotayim");
		
		builder.addOperator("+=", "assign_plus");
		builder.addOperator("-=", "assign_minus");
		builder.addOperator("/=", "assign_div");
		builder.addOperator("*=", "assign_mul");
		
		builder.addOperator("=", "equal");
		
		builder.addKeywords("public", "protected", "private");
		builder.addToken("$", "selector");
		
		builder.<Number>addPattern("[+-]?((\\d+\\.?\\d*)|(\\.\\d+))", "number", (value) -> {
			if (value.contains(".")) return Double.parseDouble(value);
			return Integer.parseInt(value);
		});
		
		builder.addToken("whitespace", (value) -> {
			if (value.length() > 1) return false;
			return Character.isWhitespace(value.charAt(0));
		});
		
		builder.addEncapsedSequence("/*", "*/", "comment");
		builder.addEncapsedSequence("\"", "\"", "string", "\\");
		builder.addEncapsedExpression("(", ")");
		
		return builder;
	}

}
