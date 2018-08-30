package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.lexer.Structure.Expression;
import fr.evolya.javatoolkit.lexer.Structure.Symbol;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class TestV1 {
	
	public static void main(String[] args) {
		Assert.runTests();
	}

	@TestMethod
	public void test1() throws Throwable {
		Expression ex = ExpressionBuilder.build("test");
		Assert.equals(ex.size(), 1);
		Assert.isInstanceOf(ex.getLastElement(), Symbol.class);
	}
	
	@TestMethod
	public void test2() throws Throwable {
		Expression ex = ExpressionBuilder.build("test = 3");
		Assert.equals(ex.size(), 3);
		Assert.isInstanceOf(ex.getLastElement(), Structure.Float.class);
	}
	
	@TestMethod
	public void test3() throws Throwable {
		Expression ex = ExpressionBuilder.build("test=3");
		Assert.equals(ex.size(), 3);
		Assert.isInstanceOf(ex.getLastElement(), Structure.Float.class);
	}
	
	@TestMethod
	public void test4() throws Throwable {
		Expression ex = ExpressionBuilder.build("a b c d");
		Assert.equals(ex.size(), 4);
		Assert.isInstanceOf(ex.getLastElement(), Symbol.class);
	}
	
	@TestMethod
	public void test5() throws Throwable {
		Expression ex = ExpressionBuilder.build("a=(1+2)");
		System.out.println(ex.toXml(" "));
	}
	
}
