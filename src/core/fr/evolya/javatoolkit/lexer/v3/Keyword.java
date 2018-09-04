package fr.evolya.javatoolkit.lexer.v3;

import fr.evolya.javatoolkit.lexer.v3.IExpressionRule.AbstractStringComparatorRule;

public class Keyword extends AbstractStringComparatorRule {

	public Keyword(String token, String name) {
		super(name, token);
	}

}
