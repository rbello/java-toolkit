package fr.evolya.javatoolkit.lexer.v3;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.log4j.spi.LoggerRepository;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.lexer.v3.IElement.Element;

public class ExpressionBuilder3 {

	public static final Logger LOGGER = Logs.getLogger("ExpressionBuilder");
	
	private List<IExpressionRule> rules = new LinkedList<>();
	
	public ExpressionBuilder3() {
	}
	
	public static ExpressionBuilder3 create() {
		return new ExpressionBuilder3();
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
		return add(new Operator(token, "T_OPERATOR_" + name.toUpperCase()));
	}

	public ExpressionBuilder3 addKeywords(String... tokens) {
		for (String token : tokens) {
			// TODO Check arguments, name too
			add(new Keyword(token, "T_KEY_" + token.toUpperCase()));
		}
		return this;
	}

	public <T> ExpressionBuilder3 addPattern(String regex, String name, Function<String, T> mapper) {
		// TODO Check arguments, name too
		return add(new Pattern<T>(regex, "T_" + name.toUpperCase()) {
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
            
            if (rules.size() == 0) {
            	
            	List<IExpressionRule> newCandidates = getRules();
            	newCandidates.removeIf(rule -> !rule.accept("" + c));
                
                if (newCandidates.size() > 0) {
                	LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
                	ex.add(new Element<String>("T_LITTERAL", buffer.toString()));
                	buffer.setLength(0);
                	buffer.append(c);
                	rules = newCandidates;
                	continue nextChar;
                }
                else {
                	LOGGER.log(Logs.DEBUG_FINE, "	-> CONTINUE");
                	buffer.append(c);
                	continue nextChar;
                }
            	
            }
            
            // Make copy of old rules before reduce
            List<IExpressionRule> oldCandidates = new LinkedList<>(rules);
            
            // Reduce : remove all 
            rules.removeIf(rule -> !rule.accept(buffer.toString() + c));
            
            if (rules.isEmpty() || i == l) {
            	
//            	String str = buffer.toString();
//    			if (i != l) str = str.substring(0, str.length() - 1);
            	
            	if (oldCandidates.size() > 0 && oldCandidates.size() < max) {
            		
            		if (oldCandidates.size() > 1) {
//            			System.err.println("CHAR " + c);
//            			System.err.println("CONFLIT = on a " + candidates.size() + " candidats");
//            			System.err.println(candidates);
            			oldCandidates.removeIf(rule -> !rule.is(buffer.toString()));
//            			System.err.println("Reduce...");
//            			System.err.println(candidates);
            		}
        			
            		// Match
            		if (oldCandidates.size() == 1) {
            			IElement<?> el = oldCandidates.get(0).from(buffer.toString());
            			ex.add(el);
            			LOGGER.log(Logs.DEBUG_FINE, "	-> ADD " + el.getTokenName());
            			buffer.setLength(0);
            			i--;
            			rules = getRules();
            		}
            		else {
            			// n régles concurrentes au début, non validées avec is()
            			throw new RuntimeException("Create litteral => " + buffer); // TODO 
            			
            		}
            		continue nextChar;
            	}
            }
//            else {
//            	System.out.print("Candidates = ");
//            	Debug.print(rules);
//            }
            
            if (Character.isWhitespace(c)) {
            	if (buffer.length() > 0) {
            		ex.add(new Element<String>("T_LITTERAL", buffer.toString()));
            		LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
            	}
            	ex.add(new Element<Character>("T_WHITESPACE", c));
            	LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_WHITESPACE [" + buffer.toString() + "]");
            	buffer.setLength(0);
            	rules = getRules();
            	continue nextChar;
            }
            
            buffer.append(c);
            
            if (i == l && buffer.length() > 0) {
            	// Reduce rules
            	rules.removeIf(rule -> !rule.is(buffer.toString()));
//            	System.out.println("Finish with " + buffer + " and rules " + rules);
            	if (rules.size() == 1) {
            		IElement<?> el = rules.get(0).from(buffer.toString());
        			ex.add(el);
        			LOGGER.log(Logs.DEBUG_FINE, "	-> ADD " + el.getTokenName());
            	}
            	else {
            		ex.add(new Element<String>("T_LITTERAL", buffer.toString()));
            		LOGGER.log(Logs.DEBUG_FINE, "	-> ADD T_LITTERAL " + buffer.toString());
            	}
            	break;
            }
            
        }
		
		return ex;
	}

}
