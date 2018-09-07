package fr.evolya.javatoolkit.lexer;

import java.util.List;

import fr.evolya.javatoolkit.lexer.IExpressionRule.EncapsedComparatorRule;

public class BuilderException extends RuntimeException {

	private static final long serialVersionUID = -6182699910003040666L;

	public BuilderException(String msg, int charNumber, IExpressionRule rule, String rawInput) {
		super(String.format("%s, at char %s\nRaw input: '%s'\nRule: %s", msg, charNumber, rawInput, rule));
	}
	
	public BuilderException(String msg, int charNumber, EncapsedComparatorRule rule, String rawInput) {
		super(String.format("%s, at char %s\nRaw input: '%s'\nRule: %s\nExpected: %s", msg, charNumber, rawInput, rule, rule.getEndToken()));
	}

	public BuilderException(String msg, int charNumber, List<IExpressionRule> rules, String rawInput) {
		super(String.format("%s, at char %s\nRaw input: '%s'\nRules: %s", msg, charNumber, rawInput, rules));
	}

}
