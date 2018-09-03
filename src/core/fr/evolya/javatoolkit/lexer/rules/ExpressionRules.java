package fr.evolya.javatoolkit.lexer.rules;

import fr.evolya.javatoolkit.lexer.rules.Element.Litteral;
import fr.evolya.javatoolkit.lexer.rules.Element.Varchar;
import fr.evolya.javatoolkit.lexer.rules.Rule.ExpressionRule;

public abstract class ExpressionRules implements ExpressionRule {
	
	protected char open;
	protected char close;
	protected char escape;
	protected boolean isExpression;

	protected char comparator;
	
	public ExpressionRules(char open, char close) {
		this(open, close, '\0');
	}
	public ExpressionRules(char open, char close, boolean isExpression) {
		this(open, close, '\0', isExpression);
	}
	public ExpressionRules(char open, char close, char escape) {
		this(open, close, escape, false);
	}
	public ExpressionRules(char open, char close, char escape, boolean isExpression) {
		this.open = open;
		this.close = close;
		this.escape = escape;
		this.isExpression = isExpression;
		// comparator
		this.comparator = open;
	}
	
	@Override
	public void begin() {
		this.comparator = close;
	}
	
	@Override
	public void finish() {
		this.comparator = open;
	}
	
	@Override
	public Element build(StringBuffer buffer, Expression parent) {
		if (isExpression) {
			return parent.getBuilder().parse(buffer.toString(), parent);
		}
		else {
			return new Litteral(buffer.toString()); // TODO
		}
	}
	
	@Override
	public boolean matches(char character, StringBuffer buffer) {
		// Escape char
		if (this.escape != '\0' && this.comparator == this.close) {
			char[] last = new char[1];
			int length = buffer.length();
			if (length > 0) {
				buffer.getChars(length-1, length, last, 0);
				if (last[0] == escape) {
					return false;
				}
			}
		}
		return character == this.comparator;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Will create ");
		sb.append(isExpression ? "expression" : "token");
		sb.append(" beginning with '");
		sb.append(this.open);
		sb.append("' and closing with '");
		sb.append(this.close);
		sb.append("'");
		if (this.escape != '\0') {
			sb.append(", espaced by '");
			sb.append(this.escape);
			sb.append("'");			
		}
		return sb.toString();
	}
	
	public static class SquareBrackets extends ExpressionRules {
		public SquareBrackets() {
			super('[', ']');
		}
	}

	public static class RoundBrackets extends ExpressionRules {
		public RoundBrackets() {
			this(false);
		}
		public RoundBrackets(boolean isExpression) {
			super('(', ')', true);
		}
	}
	
	public static class CurlyBraces extends ExpressionRules {
		public CurlyBraces() {
			super('{', '}');
		}
	}

	public static class QuotationMarks extends ExpressionRules {
		public QuotationMarks() {
			super('"', '"', '\\', false);
		}
		@Override
		public Element build(StringBuffer buffer, Expression parent) {
			return new Varchar(buffer.toString());
		}
	}
	
}
