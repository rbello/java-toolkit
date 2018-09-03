package fr.evolya.javatoolkit.lexer.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.evolya.javatoolkit.lexer.rules.Element.Litteral;
import fr.evolya.javatoolkit.lexer.rules.Rule.ExpressionRule;
import fr.evolya.javatoolkit.lexer.rules.Rule.ParserRule;
import fr.evolya.javatoolkit.lexer.rules.Rule.TokenRule;

public class ExpressionBuilder2 {

	
	private List<Rule> rules = new ArrayList<>();

	public void addRule(Rule rule) {
		this.rules .add(rule);
	}
	
	public List<Rule> getRules() {
		return new ArrayList<>(this.rules);
	}
	
	public Expression parse(String str) {
		return parse(str, null);
	}

	protected Expression parse(String str, Expression parent) {
		
		Expression ex = new Expression(this, parent);
		
		ExpressionRule buildingExpression = null;
		List<Rule> rules = getRules(); // copy
		StringBuffer buffer = new StringBuffer();
		
		nextChar:
        for (int i = 0, l = str.length(); i < l; i++) {
            char c = str.charAt(i);
            
            if (buildingExpression != null) {
            	// End of expression
            	if (buildingExpression.matches(c, buffer)) {
            		buildingExpression.finish();
//            		System.out.println(">>> PUSH " + buffer.toString() + " --> INTO " + buildingExpression.getClass().getSimpleName());
            		ex.add(buildingExpression.build(buffer, ex));
            		buildingExpression = null;
            		buffer.setLength(0);
        			continue nextChar;
            	}
            	else {
            		buffer.append(c);
            		continue nextChar;
            	}
            }
            
            for (Rule rule : rules) {
            	
        		// Sub-expressions
        		if (rule instanceof ExpressionRule) {
        			if (((ExpressionRule)rule).matches(c, buffer)) {
            			buildingExpression = (ExpressionRule)rule;
            			buildingExpression.begin();
            			if (buffer.length() > 0) {
            				handleFreeToken(buffer, ex);
            			}
            			continue nextChar;
        			}
        		}
        		// Separator
        		else if (rule instanceof TokenRule) {
        			if (((TokenRule)rule).matches(c)) {
            			((TokenRule)rule).handle(ex, buffer, c, i);
            			continue nextChar;
        			}
        		}
        		else {
        			//System.out.println("Unknown rule : " + rule.getClass());
        		}
            	
            }
            
            buffer.append(c);
            
        }
		
		if (buildingExpression != null) {
			// TODO Exception unexpected end
			throw new RuntimeException("Unexpected end of expression");
		}
		
		// TODO Code dupliqué
		if (buffer.length() > 0) {
			handleFreeToken(buffer, ex);
			
		}
		
		return ex;
	}
	
	protected void handleFreeToken(StringBuffer buffer, Expression parent) {
		Element el = null;
		el = parent.handle(buffer);
		if (el == null) {
			el = new Litteral(buffer.toString());
		}
		parent.add(el);
		buffer.setLength(0); // clear
	}

	public Element tryToParse(StringBuffer buffer) {
		String str = buffer.toString();
		Optional<ParserRule> parser = getRules()
			.stream()
			.filter(ParserRule.class::isInstance)
			.map(ParserRule.class::cast)
			.filter(rule -> rule.accept(str))
			.findFirst();
		return parser.isPresent() ? parser.get().parse(str) : null;
	}
	
}
