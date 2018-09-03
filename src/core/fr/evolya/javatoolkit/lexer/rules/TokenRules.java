package fr.evolya.javatoolkit.lexer.rules;

import fr.evolya.javatoolkit.lexer.rules.Element.Token;
import fr.evolya.javatoolkit.lexer.rules.Element.Whitespace;
import fr.evolya.javatoolkit.lexer.rules.Rule.TokenRule;

public abstract class TokenRules implements TokenRule {
	
	private char character;
	
	public TokenRules(char character) {
		this.character = character;
	}
	
	@Override
	public boolean matches(char character) {
		return this.character == character;
	}
	
	@Override
	public void handle(Expression ex, StringBuffer buffer, char character, int column) {
		// TODO Code dupliqué
		if (buffer.length() > 0) {
//			Element el = null;
//			el = ex.handle(buffer);
//			if (el == null) {
//				el = new Litteral(buffer.toString());
//			}
//			ex.add(el);
			ex.getBuilder().handleFreeToken(buffer, ex);
		}
		ex.add(new Token(character));
	}
	
	@Override
	public String toString() {
		return String.format("Will separate tokens with '%s'", this.character);
	}
	
	public static class CharSeparator extends TokenRules {
		public CharSeparator(char character) {
			super(character);
		}
	}
	
	public static class WhitespaceSeparator extends TokenRules {
		public WhitespaceSeparator() {
			super('\0');
		}
		@Override
		public boolean matches(char character) {
			return Character.isWhitespace(character);
		}
		@Override
		public void handle(Expression ex, StringBuffer buffer, char character, int column) {
			// TODO Code dupliqué
			if (buffer.length() > 0) {
//				Element el = null;
//				el = ex.handle(buffer);
//				if (el == null) {
//					el = new Litteral(buffer.toString());
//				}
//				ex.add(el);
				ex.getBuilder().handleFreeToken(buffer, ex);
			}
			ex.add(new Whitespace(character));
		}
	}
	
	public abstract static class Operator extends TokenRules {
		public Operator(char character) {
			super(character);
		}
	}
	
	public static class MinusOperator extends Operator {
		public MinusOperator() {
			super('-');
		}
	}
	
	public static class PlusOperator extends Operator {
		public PlusOperator() {
			super('+');
		}
	}
	
	public static class DivideOperator extends Operator {
		public DivideOperator() {
			super('/');
		}
	}
	
	public static class MultiplyOperator extends Operator {
		public MultiplyOperator() {
			super('*');
		}
	}
	
	public static class ModuloOperator extends Operator {
		public ModuloOperator() {
			super('%');
		}
	}
	
	public static class EqualityOperator extends Operator {
		public EqualityOperator() {
			super('=');
		}
	}
	
}


