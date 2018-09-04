package fr.evolya.javatoolkit.lexer.v3;

import fr.evolya.javatoolkit.lexer.v3.IExpressionRule.AbstractStringComparatorRule;

public class Operator extends AbstractStringComparatorRule {

	public Operator(String token, String name) {
		super(name, token);
	}

}
