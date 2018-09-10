package fr.evolya.javatoolkit.lexer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.ByCopy;
import fr.evolya.javatoolkit.lexer.IElement.Element;
import fr.evolya.javatoolkit.lexer.IExpressionRule.CustomComparatorRule;
import fr.evolya.javatoolkit.lexer.IExpressionRule.EncapsedComparatorRule;
import fr.evolya.javatoolkit.lexer.IExpressionRule.PatternComparatorRule;
import fr.evolya.javatoolkit.lexer.IExpressionRule.StringComparatorRule;

public class ExpressionBuilder {

	/**
	 * Logger de cette classe.
	 */
	public static final Logger LOGGER = Logs.getLogger("ExpressionBuilder");
	
	/**
	 * Liste des règles applicables pour ce builder.
	 */
	private List<IExpressionRule> rules = new LinkedList<>();
	
	/**
	 * Constructeur par défaut.
	 */
	public ExpressionBuilder() {
	}

	/**
	 * Renvoie la liste des règles applicables pour ce builder.
	 */
	@ByCopy
	public List<IExpressionRule> getRules() {
		return new LinkedList<>(this.rules);
	}

	/**
	 * Parse une séquence de charactère et tente d'en extraire une expression, à partir des règles
	 * données en configuration. 
	 * 
	 * @param input La chaîne de charactère 
	 * @throws BuilderException En cas d'erreur dans l'expression
	 * @return Une Expression, contenant des IElement
	 */
	public Expression parse(final String input) {

		// Log
		if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
			LOGGER.log(Logs.DEBUG_FINE, "BEGIN WITH => " + input);
		}

		// L'expression construite en sortie
		final Expression ex = new Expression(input);

		// Cette liste contiendra les règles applicables 
		List<IExpressionRule> rules = getRules(); // copy

		// Nombre max de règles : sert à déterminer si pour un token donné les règles ont été affinées
		// ou bien si toutes les règles sont applicables (dans ce cas il n'y a pas eu d'affinement)
		final int max = rules.size();

		// Le buffer qui sert à stocker les charactères pendant l'évalution
		final StringBuffer buffer = new StringBuffer();

		// Cette variable est affectée quand on a rencontré une séquence imbriquée, c-à-d. typiquement
		// les string ou les parenthèses. 
		EncapsedComparatorRule sub = null;

		// On parcours chaque charactère de l'input
		nextChar:
        for (int i = 0, l = input.length() - 1; i <= l; i++) {
            char c = input.charAt(i);

            // Gestion des séquence imbriquées
            if (sub != null) {

            	// On ajoute le charactère au buffer
            	buffer.append(c);

            	// On détecte la fin de la séquence
            	if (sub.finished(buffer.toString())) {
            		String contents = buffer.substring(0, buffer.length() - sub.getEndToken().length());
            		IElement<?> el = sub.from(contents);
            		ex.add(el);
            		sub = null;
            		buffer.setLength(0);
            		if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
            			LOGGER.log(Logs.DEBUG_FINE, "	-> ADD NESTED " + el.getTokenName() + " = " + contents);
            		}
            	}

            	// On détecte une fin de séquence prématurée
            	else if (i == l) {
            		throw new BuilderException("Unexpected end of expression", i+1, sub, input);
            	}

            	continue nextChar;
            }

            // Log
            if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
            	StringBuilder log = new StringBuilder();
            	log.append(i);
            	log.append(". CHAR [ ");
            	log.append(c);
            	log.append(" ]");
    			if (rules.size() == max)
    				log.append(" -> NEW");
    			else {
    				log.append(" -> Candidates: ");
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
                	// Log
                	if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
                		LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
                	}
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
                	if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
                		LOGGER.log(Logs.DEBUG_FINE, "	-> CONTINUE");
                	}
                	buffer.append(c);
                }

                // Arrivée en bout de chaîne input
                if (i == l) {
                	handleLastToken(ex, rules, buffer, c, i, input);
                	break;
                }

                continue nextChar;

            }

            // On se retrouve ici si il y a des règles applicables pour le contenu du buffer. On fait donc une copie
            // de ces règles.
            List<IExpressionRule> oldCandidates = new LinkedList<>(rules);

            // On retire toutes les règles qui ne correspondent pas au contenu du buffer plus le nouveau caractère.
            rules.removeIf(rule -> !rule.accept(buffer.toString() + c));

            // Log
            if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
            	LOGGER.log(Logs.DEBUG_FINE, "   Rules matching '" + buffer + c + "' = " + rules);
            }
            
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
            			if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
            				LOGGER.log(Logs.DEBUG_FINE, "	-> REDUCE Exactly " + buffer.toString());
            			}
            			oldCandidates.removeIf(rule -> !rule.is(buffer.toString()));
            		}
        			
            		// Nous sommes donc dans le cas normal où seule une règle ne correspond parfaitemnt au contenu du buffer.
            		// On va donc demander à la règle de créer un IElement à rajouter dans l'expression.
            		if (oldCandidates.size() == 1) {
            			// Cas spécifique : création d'une sous-expression
            			if (oldCandidates.get(0) instanceof EncapsedComparatorRule) {
            				sub = (EncapsedComparatorRule) oldCandidates.get(0);
            				if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
            					LOGGER.log(Logs.DEBUG_FINE, "	-> BEGIN SUB EXPRESSION ");
            				}
            			}
            			// Cas normal : création d'un élément à partir de la règle
            			else {
	            			IElement<?> el = oldCandidates.get(0).from(buffer.toString());
	            			ex.add(el);
	            			if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
	            				LOGGER.log(Logs.DEBUG_FINE, "	-> ADD " + el.getTokenName());
	            			}
            			}
            			// On va repasser sur le caractère en cours, en réinitialisant les règles et le buffer.
            			buffer.setLength(0);
            			i--;
            			rules = getRules();
            		}
            		
            		// Il n'y a plus aucune règle : cela signife que plusieurs règles étaient valides mais aucune ne
            		// correspond exactement au contenu du buffer. Par exemple, si le buffer contient 'p' les règles
            		// des tokens 'public' et 'private' vont correspondre. Mais si on rencontre ensuite le caractère
            		// 'a' plus aucune règle ne correspond exactement. On poursuit donc.
            		else if (oldCandidates.size() == 0) {
            			buffer.append(c);
            			continue nextChar;
            		}
            		
            		// Plusieurs règles sont encore en concurrence, mais valident EXACTEMENT le contenu du buffer. Il s'agit
            		// ici d'une erreur de configuration du builder, car deux règles ne sont pas sensées valider exactemnt
            		// le même contenu et pour autant être des règles différentes. On va donc lever une exception.
            		else {
            			throw new BuilderException("ExpressionBuilder configuration error: two rules are in concurrency", i, oldCandidates, input);
            		}
            		
            		 if (i == l) {
                     	System.out.println("Can't continue"); // TODO
                     }
            		
            		// Dans tous les cas on continue au caractère suivant.
            		continue nextChar;
            	}
            }

            buffer.append(c);
            
            // On traite ici le cas du contenu à la fin de l'input. Si le buffer n'est pas vide on va traiter son
            // contenu. Pour cela, on va rechercher dans les règles applicables celle qui correspond exactement
            // au contenu du buffer. Si on en trouve une seule, alors c'est la bonne. Zero cela signifie que ce
            // dernier token est de type T_LITTERAL. Sinon (plusieurs règles) on se retrouve dans une erreur.
            if (i == l && buffer.length() > 0) {
            	handleLastToken(ex, rules, buffer, c, i, input);
            	break;
            }
            
        }
		
		return ex;
	}

	protected void handleLastToken(Expression ex, List<IExpressionRule> rules, final StringBuffer buffer, char c, int i, String input) {
		rules.removeIf(rule -> !rule.is(buffer.toString()));
		if (rules.size() == 1) {
			IElement<?> el = rules.get(0).from(buffer.toString());
			ex.add(el);
			if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
				LOGGER.log(Logs.DEBUG_FINE, "	-> ADD " + el.getTokenName());
			}
		}
		else if (rules.size() == 0) {
			ex.add(new Element<String>("T_LITTERAL", buffer.toString()));
			if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
				LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
			}
		}
		// Plusieurs règles sont encore en concurrence, mais valident EXACTEMENT le contenu du buffer. Il s'agit
		// ici d'une erreur de configuration du builder, car deux règles ne sont pas sensées valider exactemnt
		// le même contenu et pour autant être des règles différentes. On va donc lever une exception.
		else {
			throw new BuilderException("ExpressionBuilder configuration error: two rules are in concurrency", i, rules, input);
		}
	}
	
	/**
	 * Ajouter une règle applicable pour la construction d'expressions par ce builder.
	 * 
	 * @return Le builder actuel (method chaining)
	 */
	public ExpressionBuilder add(IExpressionRule rule) {
		if (rule == null) throw new NullPointerException("Given IExpressionRule is null");
		this.rules.add(rule);
		return this;
	}

	/**
	 * Ajouter une règle d'identification des opérateurs dans la chaîne d'entrée fournie.
	 * Les tokens identifiés par cette règle auront le type 'T_OPERATOR_<TOKEN_NAME>'.
	 * 
	 * @param token Le token à identifier dans la chaîne d'entrée.
	 * @param tokenName Le nom du token
	 * @return Le builder actuel (method chaining)
	 */
	public ExpressionBuilder addOperator(String token, String tokenName) {
		checkTokenName(tokenName);
		return add(new StringComparatorRule("T_OPERATOR_" + tokenName.toUpperCase(), token));
	}

	/**
	 * Ajouter une règle d'identification de mots clés spécifiques. Par exemple, cela permet
	 * d'identifier des mots clés spécifiques comme 'public', 'class', 'abstract', etc.
	 * Les tokens identifiés par cette règle auront le type 'T_KEY_<TOKEN_NAME>'.
	 * 
	 * @param tokens La liste des mots clés à identifier
	 * @return Le builder actuel (method chaining)
	 */
	public ExpressionBuilder addKeywords(String... tokens) {
		for (String token : tokens) {
			add(new StringComparatorRule("T_KEY_" + tokenNameFromValue(token).toUpperCase(), token));
		}
		return this;
	}

	/**
	 * Ajouter une règle d'identification basée sur la correspondance avec une expression regulière.
	 * Les tokens identifiés par cette règle auront le type 'T_<TOKEN_NAME>'.
	 * 
	 * @param regex L'expression regulière à vérifier
	 * @param tokenName Le nom du token
	 * @param mapper La fonction permettant de faire correspondre la chaîne de caractère correspondant
	 *     à l'expression regulière avec un type réel. Par exemple, convertir "1.23" en float.
	 * @return Le builder actuel (method chaining)
	 * @see java.util.regex.Pattern
	 */
	public <T> ExpressionBuilder addPattern(String regex, String tokenName, Function<String, T> mapper) {
		checkTokenName(tokenName);
		return add(new PatternComparatorRule<T>(regex, "T_" + tokenName.toUpperCase()) {
			public T getValue(String value) {
				return mapper.apply(value);
			}
		});
	}

	/**
	 * Ajouter une règle d'identification permettant d'identifier des séquences de caractères entourées par
	 * un token de début et un token de fin. Par exemple, une chaîne de caractère entre guillemets.
	 * Les tokens identifiés par cette règle auront le type 'S_<TOKEN_NAME>'.
	 * 
	 * @param startToken Le token identifiant le début de la séquence
	 * @param endToken Le token identifant la fin de la séquence
	 * @param tokenName Le nom du token
	 * @return Le builder actuel (method chaining)
	 */
	public ExpressionBuilder addEncapsedSequence(String startToken, String endToken, String tokenName) {
		return addEncapsedSequence(startToken, endToken, null, tokenName);
	}

	/**
	 * Ajouter une règle d'identification permettant d'identifier des séquences de caractères entourées par
	 * un token de début et un token de fin. Par exemple, une chaîne de caractère entre guillemets.
	 * Cette méthode apporte également la possibilié de préciser une séquence d'échappement pour invalider
	 * la séquence de fin. Par exemple l'anti-slash pour échapper les guillemets.
	 * Les tokens identifiés par cette règle auront le type 'S_<TOKEN_NAME>'.
	 * 
	 * @param startToken Le token identifiant le début de la séquence
	 * @param endToken Le token identifant la fin de la séquence
	 * @param escapeToken Le token d'échappement
	 * @param tokenName Le nom du token
	 * @return Le builder actuel (method chaining)
	 */
	public ExpressionBuilder addEncapsedSequence(String startToken, String endToken, String escapeToken, String tokenName) {
		checkTokenName(tokenName);
		return add(new EncapsedComparatorRule("S_" + tokenName.toUpperCase(), startToken, endToken, escapeToken));
	}

	/**
	 *  Ajouter une règle d'identification permettant de détecter des sous-expressions. Par exemple, typiquement les
	 *  parenthèses permettent de définir des sous-composants de l'expression, qui sont eux même des expressions.
	 *  Il est très fréquent également que les sous-expressions soient interprétées en premier avant l'interprétation
	 *  de l'expression globale. 
	 *  Les tokens identifiés par cette règle auront le type 'S_EXPRESSION'.
	 *  
	 * @param startToken Le token identifiant le début de l'expression
	 * @param endToken Le token identifant la fin de l'expression
	 * @return Le builder actuel (method chaining)
	 * @see https://en.wikipedia.org/wiki/Order_of_operations
	 */
	public ExpressionBuilder addEncapsedExpression(String startToken, String endToken) {
		return add(new EncapsedComparatorRule("S_EXPRESSION", startToken, endToken) {
			public IElement<?> from(String buffer) {
				return parse(buffer);
			}
		});
	}

	/**
	 * Ajouter une règle d'identification permettant de détecter des tokens simples (string).
	 * Cette méthode ressemble à addKeywords() à l'exception que le nom du token peut être directemnt fourni.
	 * Les tokens identifiés par cette règle auront le type 'T_<TOKEN_NAME>'.
	 * 
	 * @param token Le token à identifier dans la chaîne d'entrée.
	 * @param tokenName Le nom du token
	 * @return Le builder actuel (method chaining)
	 */
	public ExpressionBuilder addToken(String token, String tokenName) {
		checkTokenName(tokenName);
		return add(new StringComparatorRule("T_" + tokenName.toUpperCase(), token));
	}
	
	/**
	 * Ajouter une règle d'identification permettant de détecter des tokens librement, à l'aide
	 * d'une fonction passée en paramètre. Par exemple, pour la détection des espaces au sein
	 * de la chaîne d'entrée, on peut se base sur la méthode Character.isWhitespace() qui identifie
	 * tous les caractères considérés comme des espaces (espace, retour chariot, tabulation, etc.)
	 * Ainsi, cette méthode donne la liberté de détecter des tokens à partir de n'importe quel algo.
	 * La fonction predicate reçoit en entrée le contenu du buffer de lecture, et doit renvoyer en
	 * sortie un boolean pour indiquer si un token a été détecté.
	 * 
	 * <code>
	 * builder.addToken("whitespace", (value) -> {
	 *      if (value.length() > 1) return false;
	 *      return Character.isWhitespace(value.charAt(0));
	 * });
	 * </code>
	 * 
	 * @param tokenName Le nom du token
	 * @param predicate La function qui identifie le token
	 * @return Le builder actuel (method chaining)
	 */
	public ExpressionBuilder addToken(String tokenName, Predicate<String> predicate) {
		checkTokenName(tokenName);
		return add(new CustomComparatorRule("T_" + tokenName.toUpperCase(), predicate));
	}
	
	/**
	 * Vérifie que le nom du token est valide, c.à.d. qu'il respecte l'expression régulière
	 * suivante : ^[a-zA-Z]\\w*[a-zA-Z]$
	 * 
	 * @param tokenName Le nom de token à vérifier
	 * @throws IllegalArgumentException Si le nom de token n'est pas valide
	 */
	protected void checkTokenName(String tokenName) {
		if (!tokenName.matches("^[a-zA-Z]\\w*[a-zA-Z]$")) {
			throw new IllegalArgumentException("Invalid token name: " + tokenName);
		}
	}
	
	/**
	 * Fabrique un nom de token à partir de sa valeur.
	 * 
	 * @param token La valeur du token
	 * @return Le nom du token
	 * @throws IllegalArgumentException Si le nom de token n'est pas valide
	 */
	protected String tokenNameFromValue(String token) {
		String name = token.trim().replace(" ", "_");
		checkTokenName(name);
		return name;
	}
	
	/**
	 * Fabrique un builder sans configuration initiale.
	 * 
	 * @return Un builder sans aucune configuration
	 */
	public static ExpressionBuilder createNew() {
		return new ExpressionBuilder();
	}
	
	/**
	 * Fabrique un builder avec une configuration par défaut. Le builder en question contient
	 * des règles courante en programmation logicielle : gestion des opérateurs, strings, nombres,
	 * boolean, mots clé private/protected/public, commentaires ou encore sous-expression contenues
	 * entre des parenthèses.
	 * 
	 * @return Un builder pré-configuré
	 */
	public static ExpressionBuilder createDefault() {
		ExpressionBuilder builder = new ExpressionBuilder();
		
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
		builder.addEncapsedSequence("\"", "\"", "\\", "string");
		builder.addEncapsedExpression("(", ")");
		
		return builder;
	}

}
