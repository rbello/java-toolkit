package fr.evolya.javatoolkit.cli;

import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.ExpectedException;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class AsciiTableTest {

	public static void main(String[] args) {
		Assert.runTests();
	}
	
	@TestMethod
	@ExpectedException(IllegalArgumentException.class)
	public void ConstructorFailureNoArguments() {
		new AsciiTable();
	}
	
	@TestMethod
	@ExpectedException(IllegalArgumentException.class)
	public void ConstructorFailureBadArgumentsNumber() {
		new AsciiTable("toto");
	}
	
	@TestMethod
	@ExpectedException(ClassCastException.class)
	public void ConstructorFailureBadArgumentsType1() {
		new AsciiTable("toto", false);
	}
	
	@TestMethod
	@ExpectedException(NullPointerException.class)
	public void ConstructorFailureBadArgumentsValue1() {
		new AsciiTable(null, 0);
	}
	
	@TestMethod
	@ExpectedException(IllegalArgumentException.class)
	public void ConstructorFailureBadArgumentsValue2() {
		new AsciiTable("", 0);
	}
	
	@TestMethod
	@ExpectedException(IllegalArgumentException.class)
	public void ConstructorFailureBadArgumentsValue3() {
		new AsciiTable("toto", -1);
	}
	
	@TestMethod
	@ExpectedException(IllegalArgumentException.class)
	public void ConstructorFailureBadArgumentsValue4() {
		new AsciiTable("toto", 1);
	}

}
