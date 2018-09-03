package fr.evolya.javatoolkit.lexer.rules;

public abstract interface Rule {
	
	public static interface TokenRule extends Rule {
		public boolean matches(char character);
		public void handle(Expression ex, StringBuffer buffer, char character, int column);
	}
	
	public static interface ExpressionRule extends Rule {
		public boolean matches(char character, StringBuffer buffer);
		public void begin();
		public void finish();
		public Element build(StringBuffer buffer, Expression parent);
	}
	
	public static interface ParserRule extends Rule {
		boolean accept(String str);
		Element parse(String str);
	}
	
}
