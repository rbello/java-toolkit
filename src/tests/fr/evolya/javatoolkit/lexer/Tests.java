package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.lexer.v3.Expression;
import fr.evolya.javatoolkit.lexer.v3.ExpressionBuilder3;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.BeforeTests;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class Tests {
	
	public static void main(String[] args) {
		
		boolean verbose = true;
		
		if (verbose) Logs.setGlobalLevel(Logs.ALL);
		Assert.runTests(verbose);
	}
	
private ExpressionBuilder3 builder;
	
	@BeforeTests
	public void init() {
		// Create builder
		builder = ExpressionBuilder3.create();
		
		builder.addOperator("+", "plus");
		builder.addOperator("-", "minus");
		builder.addOperator("/", "div");
		builder.addOperator("*", "mul");
		
		builder.addOperator(".", "object");
		builder.addOperator("->", "pointer");
		builder.addOperator("::", "nekudotayim");
		
		builder.addOperator("+=", "assign_plus");
		builder.addOperator("-=", "assign_minus");
		builder.addOperator("/=", "assign_div");
		builder.addOperator("*=", "assign_mul");
		
		builder.addOperator("=", "equal");
		
		builder.addKeywords("public", "protected", "private");
		builder.addToken("$", "selector");
		
		builder.<Number>addPattern("[+-]?((\\d+\\.?\\d*)|(\\.\\d+))", "number", (value) -> {
			if (value.contains(".")) return Double.parseDouble(value);
			return Integer.parseInt(value);
		});
		
		builder.addEncapsedSequence("/*", "*/", "comment");
		builder.addEncapsedSequence("\"", "\"", "string", "\\");
		builder.addEncapsedExpression("(", ")");
	}
	
//
//	@TestMethod
//	public void TestEmptyExpression() throws Throwable {
//		Expression e = builder.parse("");
//		Assert.equals(e.getCount(), 0);
//		Assert.equals(e.toString(), "()");
//	}
//	
//	@TestMethod
//	public void TestSingleLitteral() throws Throwable {
//		Expression e = builder.parse("a");
//		Assert.equals(e.toString(), "([a])");
//		Assert.equals(e.getCount(), 1);
//	}
//	
//	@TestMethod
//	public void TestSingleNumeric() throws Throwable {
//		Expression e = builder.parse("5");
//		Assert.equals(e.toString(), "([5])");
//		Assert.equals(e.getCount(), 1);
//		Assert.equals(e.getCount("T_NUMBER"), 1);
//	}
//
//	@TestMethod
//	public void TestSimpleExpression() throws Throwable {
//		Expression e = builder.parse("hello world !");
//		Assert.equals(e.toString(), "([hello][ ][world][ ][!])");
//		Assert.equals(e.getCount(), 5);
//		Assert.equals(e.getCount("T_WHITESPACE"), 2);
//		Assert.equals(e.getCount("T_LITTERAL"), 3);
//	}
//	
//	@TestMethod
//	public void TestDoubleWhitespaces() throws Throwable {
//		Expression e = builder.parse(" a   b ");
//		Assert.equals(e.toString(), "([ ][a][ ][ ][ ][b][ ])");
//		Assert.equals(e.getCount(), 7);
//		Assert.equals(e.getCount("T_WHITESPACE"), 5);
//		Assert.equals(e.getCount("T_LITTERAL"), 2);
//	}
//	
//	@TestMethod
//	public void TestNumericExpression() throws Throwable {
//		Expression e = builder.parse("1+2.1 =3");
//		e.removeWhitespaces();
//		Assert.equals(e.toString(), "([1][+][2.1][=][3])");
//		Assert.equals(e.getCount(), 5);
//		Assert.equals(e.getCount("T_NUMBER"), 3);
//		Assert.equals(e.getCount("T_OPERATOR_PLUS"), 1);
//		Assert.equals(e.getCount("T_OPERATOR_EQUAL"), 1);
//	}
//	
	@TestMethod
	public void TestNestedExpression() throws Throwable {
		Expression e = builder.parse("ax=3-(1+2)");
		Assert.equals(e.toString(), "([x][=][3][-]([1][+][2]))");
		Assert.equals(e.getCount(), 5); // without nested expression
		Assert.equals(e.getCount(true), 7); // with it
		Assert.equals(e.getCount("T_LITTERAL"), 1);
		Assert.equals(e.getCount("T_NUMBER"), 3);
		Assert.equals(e.getCount("T_OPERATOR"), 3);
	}

//	@TestMethod
//	public void TestCloseNestedExpression() throws Throwable {
//		Expression e = builder.parse("a(b)");
//		Assert.equals(e.toString(), "([a]([b]))");
//		Assert.equals(e.getCount(true), 2);
//		Assert.equals(e.getCount("T_LITTERAL"), 2);
//	}
//	
//	@TestMethod
//	public void TestStringExpression() throws Throwable {
//		Expression e = builder.parse("String str = \"Hello (world + \\\"1\\\")\";");
//		Assert.equals(e.toString(), "([String][ ][str][ ][=][ ][\"Hello (world + \\\"1\\\")\"][;])");
//		Assert.equals(e.getCount(true), 8);
//		Assert.equals(e.getCount("T_LITTERAL"), 3);
//		Assert.equals(e.getCount("T_ENCAPSED_STRING"), 1);
//	}
	
//	@TestMethod
//	@ExpectedException(UnexpectedEndOfInput.class)
//	public void TestUnexpectedEndOfInput() throws Throwable {
//		builder.parse("(");
//	}
	
//	@TestMethod
//	@ExpectedException(UnexpectedEndOfInput.class)
//	public void xxx() throws Throwable {
//		// Parse expression
//		Expression e = builder.parse(")");
//		System.out.println(e);
//	}
	
}
