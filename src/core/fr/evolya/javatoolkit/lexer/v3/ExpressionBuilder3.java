package fr.evolya.javatoolkit.lexer.v3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import fr.evolya.javatoolkit.code.Debug;

public class ExpressionBuilder3 {

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
		
		Expression ex = new Expression(this, parent);
		
		List<IExpressionRule> rules = getRules(); // copy
		StringBuffer buffer = new StringBuffer();
		
		nextChar:
        for (int i = 0, l = input.length(); i < l; i++) {
            char c = input.charAt(i);
            
            System.out.println("CHAR " + c);
            
            buffer.append(c);
            
            rules.removeIf(rule -> !rule.accept(buffer));
            
            if (rules.isEmpty()) {
            	// TODO
            	System.out.println("EMPTY RULE SET");
            	continue nextChar;
            }
            
            
            
        }
		
		return null;
	}

}
