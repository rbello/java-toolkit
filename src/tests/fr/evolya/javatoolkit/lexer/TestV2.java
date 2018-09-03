package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.lexer.rules.CodeExpressionBuilder;
import fr.evolya.javatoolkit.lexer.rules.Element;
import fr.evolya.javatoolkit.lexer.rules.Element.Litteral;
import fr.evolya.javatoolkit.lexer.rules.Element.Whitespace;
import fr.evolya.javatoolkit.lexer.rules.Expression;
import fr.evolya.javatoolkit.lexer.rules.ExpressionBuilder2;
import fr.evolya.javatoolkit.lexer.rules.ExpressionRules.QuotationMarks;
import fr.evolya.javatoolkit.lexer.rules.ExpressionRules.RoundBrackets;
import fr.evolya.javatoolkit.lexer.rules.ParserRules.NumericParser;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.DivideOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.EqualityOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.MinusOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.MultiplyOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.PlusOperator;
import fr.evolya.javatoolkit.lexer.rules.TokenRules.WhitespaceSeparator;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class TestV2 {
	
	public static void main(String[] args) {
		Assert.runTests(true);
	}
	
	@TestMethod
	public void TestEmptyExpression() throws Throwable {
		// Create builder
		ExpressionBuilder2 ex = new CodeExpressionBuilder();
		// Parse expression
		Expression e = ex.parse("");
		Assert.equals(e.getCount(), 0);
		Assert.equals(e.toString(), "()");
	}

	@TestMethod
	public void TestSimpleExpression() throws Throwable {
		// Create builder
		ExpressionBuilder2 ex = new ExpressionBuilder2();
		ex.addRule(new WhitespaceSeparator());
		ex.addRule(new QuotationMarks());
		ex.addRule(new RoundBrackets(true));
		// Parse expression
		Expression e = ex.parse("hello world !");
		Assert.equals(e.getCount(), 5);
		Assert.equals(e.getCount(Whitespace.class), 2);
		Assert.equals(e.getCount(Litteral.class), 3);
		Assert.equals(e.toString(), "([hello][ ][world][ ][!])");
	}
	
	@TestMethod
	public void TestDoubleWhitespaces() throws Throwable {
		// Create builder
		ExpressionBuilder2 ex = new ExpressionBuilder2();
		ex.addRule(new WhitespaceSeparator());
		ex.addRule(new QuotationMarks());
		ex.addRule(new RoundBrackets(true));
		// Parse expression
		Expression e = ex.parse(" a   b ");
		Assert.equals(e.getCount(), 7);
		Assert.equals(e.getCount(Whitespace.class), 5);
		Assert.equals(e.getCount(Litteral.class), 2);
		Assert.equals(e.toString(), "([ ][a][ ][ ][ ][b][ ])");
	}
	
	@TestMethod
	public void TestNumericExpression() throws Throwable {
		// Create builder
		ExpressionBuilder2 ex = new ExpressionBuilder2();
		ex.addRule(new WhitespaceSeparator());
		ex.addRule(new PlusOperator());
		ex.addRule(new MinusOperator());
		ex.addRule(new MultiplyOperator());
		ex.addRule(new DivideOperator());
		ex.addRule(new EqualityOperator());
		ex.addRule(new QuotationMarks());
		ex.addRule(new NumericParser());
		ex.addRule(new RoundBrackets(true));
		// Parse expression
		Expression e = ex.parse("1+2.1 =3");
		e.removeWhitespaces();
		Assert.equals(e.getCount(), 5);
		Assert.equals(e.getCount(Element.Number.class), 3);
		Assert.equals(e.getCount(Element.Token.class), 2);
		Assert.equals(e.toString(), "([1][+][2.1][=][3])");
	}
	
	@TestMethod
	public void TestNestedExpression() throws Throwable {
		// Create builder
		ExpressionBuilder2 ex = new CodeExpressionBuilder();
		// Parse expression
		Expression e = ex.parse("x=3-(1+2)");
		Assert.equals(e.getCount(), 5); // without nested expression
		Assert.equals(e.getCount(true), 7); // with it
		Assert.equals(e.getCount(Element.Litteral.class), 1);
		Assert.equals(e.getCount(Element.Number.class), 3);
		Assert.equals(e.getCount(Element.Token.class), 3);
		Assert.equals(e.toString(), "([x][=][3][-]([1][+][2]))");
	}

	@TestMethod
	public void TestCloseNestedExpression() throws Throwable {
		// Create builder
		ExpressionBuilder2 ex = new CodeExpressionBuilder();
		// Parse expression
		Expression e = ex.parse("a(b)");
		Assert.equals(e.getCount(true), 2);
		Assert.equals(e.getCount(Element.Litteral.class), 2);
		Assert.equals(e.toString(), "([a]([b]))");
	}
	
	@TestMethod
	public void TestStringExpression() throws Throwable {
		// Create builder
		ExpressionBuilder2 ex = new CodeExpressionBuilder();
		// Parse expression
		Expression e = ex.parse("String str = \"Hello (world + \\\"1\\\")\";");
		Assert.equals(e.getCount(true), 8);
		Assert.equals(e.getCount(Element.Litteral.class), 3);
		Assert.equals(e.getCount(Element.Varchar.class), 1);
		Assert.equals(e.toString(), "([String][ ][str][ ][=][ ][\"Hello (world + \\\"1\\\")\"][;])");
	}
	
}
