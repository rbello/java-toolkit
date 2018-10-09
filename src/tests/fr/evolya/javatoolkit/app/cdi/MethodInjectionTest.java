package fr.evolya.javatoolkit.app.cdi;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.BeforeTests;
import fr.evolya.javatoolkit.test.Assert.ExpectedException;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class MethodInjectionTest {
	
	private DependencyInjectionContext cdi;

	public static void main(String[] args) {
		boolean debug = true;
		Assert.runTests(debug);
	}
	
	@BeforeTests
	public void init() {
		cdi = new DependencyInjectionContext(job -> job.run());
		cdi.register(X.class);
		Logs.setGlobalLevel(Logs.ALL);
	}
	
	@TestMethod(1)
	public void CallMethodWithoutArguments() {
		Integer r = cdi.call(Instance.futur(MethodInjectionTest.class), "a");
		Assert.equals(r, -6);
	}
	
	@TestMethod(2)
	public void CallMethodWithOneInjectedArgument() {
		X r = cdi.call(Instance.futur(MethodInjectionTest.class), "b");
		Assert.isInstanceOf(r, X.class);
	}
	
	@TestMethod(3)
	@ExpectedException(IllegalArgumentException.class)
	public void CallMethodWithTooManyArguments() {
		X r = cdi.call(Instance.futur(MethodInjectionTest.class), "b", 1);
		Assert.isInstanceOf(r, X.class);
	}
	
	@TestMethod(4)
	@ExpectedException(IllegalArgumentException.class)
	public void CallMethodWithNotEnoughtArguments() {
		cdi.call(Instance.futur(MethodInjectionTest.class), "c", 1);
	}
	
	public int a() {
		return -6;
	}
	
	public X b(X a) {
		return a;
	}
	
	public Boolean c(int a, X b, String c) {
		return true;
	}
	
	public static class X {
		
	}

}
