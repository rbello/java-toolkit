package fr.evolya.javatoolkit.lexer;

import fr.evolya.javatoolkit.lexer.v3.ExpressionBuilder3;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class TestV3 {
	
	public static void main(String[] args) {
		Assert.runTests(true);
	}
	

	@TestMethod
	public void TestEmptyExpression() {
		// Create builder
		ExpressionBuilder3 eb = ExpressionBuilder3.create();
		eb.addOperator("+", "plus");
		eb.addOperator("-", "minus");
		eb.addOperator("/", "div");
		eb.addOperator("*", "mul");
		eb.addOperator(".", "object");
		eb.addOperator("->", "pointer");
		eb.addOperator("::", "nekudotayim");
		eb.addOperator("+=", "assign_plus");
		eb.addOperator("-=", "assign_minus");
		eb.addKeywords("public", "protected", "private");
		eb.<Number>addPattern("^-?[0-9]{1,12}(?:\\.[0-9]{1,13})?$", "number", (value) -> {
			if (value.contains(".")) return Double.parseDouble(value);
			return Integer.parseInt(value);
		});
		eb.addEncapsedExpression("/*", "*/", "comment");
		eb.addEncapsedExpression("\"", "\"", "string", "\\");
	}

}
