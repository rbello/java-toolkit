package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.lexer.ExpressionBuilder2.CharToken;
import fr.evolya.javatoolkit.lexer.ExpressionBuilder2.Parenthesis;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class TestV2 {
	
	public static void main(String[] args) {
		Assert.runTests();
	}

	@TestMethod
	public void test1() throws Throwable {
		
		ExpressionBuilder2 ex = new ExpressionBuilder2();

		ex.addRule(new CharToken(' '));
		ex.addRule(new Parenthesis());
		
		ex.getRules();
		
		ex.parse("bonjour les amis");
		
	}
	
}
