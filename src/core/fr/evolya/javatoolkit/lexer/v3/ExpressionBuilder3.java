package fr.evolya.javatoolkit.lexer.v3;

import java.util.function.Function;

public class ExpressionBuilder3 {

	public ExpressionBuilder3() {
	}
	
	public static ExpressionBuilder3 create() {
		return new ExpressionBuilder3();
	}

	public ExpressionBuilder3 add(Object operator) {
		// TODO
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

	public ExpressionBuilder3 addEncapsedExpression(String startToken, String endToken, String name) {
		return addEncapsedExpression(startToken, endToken, name);
	}

	public ExpressionBuilder3 addEncapsedExpression(String startToken, String endToken, String name, String escapeToken) {
		// TODO Auto-generated method stub
		return null;
	}

}
