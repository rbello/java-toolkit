package fr.evolya.javatoolkit.lexer.rules;

public interface Element {
	
	public static class Litteral implements Element {
		private String string;
		public Litteral(String string) {
			if (string == null || string.isEmpty())
				throw new IllegalArgumentException("Empty litteral");
			this.string = string;
		}
		@Override
		public String toString() {
			return String.format("[%s]", this.string);
		}
	}
	
	public static class Token implements Element {
		private char charactere;
		public Token(char charactere) {
			this.charactere = charactere;
		}
		@Override
		public String toString() {
			return String.format("[%s]", this.charactere);
		}
	}
	
	public static class Whitespace extends Token {
		public Whitespace(char charactere) {
			super(charactere);
		}
	}
	
	public abstract static class Number implements Element {
	}
	
	public static class IntegerNumber extends Number {
		private int value;
		public IntegerNumber(int value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return String.format("[%s]", this.value);
		}
	}
	
	public static class FloatNumber extends Number {
		private double value;
		public FloatNumber(double value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return String.format("[%s]", this.value);
		}
	}
	
	public static class Varchar implements Element {
		private String string;
		public Varchar(String string) {
			if (string == null || string.isEmpty())
				throw new IllegalArgumentException("Empty Varchar");
			this.string = string;
		}
		@Override
		public String toString() {
			return String.format("[\"%s\"]", this.string);
		}
	}

}
