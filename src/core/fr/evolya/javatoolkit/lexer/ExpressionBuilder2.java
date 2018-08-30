package fr.evolya.javatoolkit.lexer;

import java.util.ArrayList;
import java.util.List;

public class ExpressionBuilder2 {

	public static interface Rule {
		
	}

	public abstract static class SingleCharExpression implements Rule {
		public SingleCharExpression(char character) {
		}
	}
	
	public abstract static class OpenCloseExpression implements Rule {
		public OpenCloseExpression(char open, char close) {
		}
		public OpenCloseExpression(char open, char close, char escape) {
		}
	}
	
	public static class SquareBracket extends OpenCloseExpression {
		public SquareBracket() {
			super('[', ']');
		}
	}

	public static class CurlyBrace extends OpenCloseExpression {
		public CurlyBrace() {
			super('{', '}');
		}
	}

	public static class Parenthesis extends OpenCloseExpression {
		public Parenthesis() {
			super('"', '"', '\\');
		}
	}
	
	public static class CharToken extends SingleCharExpression {
		public CharToken(char character) {
			super(character);
		}
	}
	
	public static class Expression {
		
	}

	private List<Rule> rules = new ArrayList<>();

	public void addRule(Rule rule) {
		this.rules .add(rule);
	}
	
	public List<Rule> getRules() {
		return new ArrayList<>(this.rules);
	}

	public Expression parse(String string) {
		return new Expression();
	}
	
}
