package fr.evolya.javatoolkit.lexer.rules;

import fr.evolya.javatoolkit.lexer.rules.ExpressionRules.QuotationMarks;
import fr.evolya.javatoolkit.lexer.rules.ExpressionRules.RoundBrackets;
import fr.evolya.javatoolkit.lexer.rules.ParserRules.NumericParser;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.DivideOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.EqualityOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.MinusOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.MultiplyOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.PlusOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.WhitespaceSeparator;

public class CodeExpressionBuilder extends ExpressionBuilder2 {
	
	public CodeExpressionBuilder() {
		addRule(new WhitespaceSeparator());
		addRule(new PlusOperator());
		addRule(new MinusOperator());
		addRule(new MultiplyOperator());
		addRule(new DivideOperator());
		addRule(new EqualityOperator());
		addRule(new QuotationMarks());
		addRule(new NumericParser());
		addRule(new RoundBrackets(true));
	}

}
