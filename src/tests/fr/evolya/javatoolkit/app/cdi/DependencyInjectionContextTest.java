package fr.evolya.javatoolkit.app.cdi;

import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.Inject;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.BeforeTests;
import fr.evolya.javatoolkit.test.Assert.ExpectedException;
import fr.evolya.javatoolkit.test.Assert.TestClass;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

@TestClass
public class DependencyInjectionContextTest {
	
	private DependencyInjectionContext cdi;
	
	public static void main(String[] args) {
		Assert.runTests();
	}
	
	@BeforeTests
	public void init() {
		cdi = new DependencyInjectionContext(job -> job.run());
		Logs.setGlobalLevel(Logs.ALL);
	}
	
	@TestMethod(1)
	public void AvantTout() {
		Assert.equals(cdi.getComponents().size(), 0);
	}
	
	@TestMethod(2)
	public void VerificationsDeBase() {
		Assert.isFalse(cdi.isComponentRegistered(null));
		Assert.isFalse(cdi.isComponentRegistered(Assert.class));
	}
	
	@TestMethod(3)
	@ExpectedException(NullPointerException.class)
	public void MauvaiseConstruction() {
		cdi.register(B.class);
		// B should be registred
		Assert.equals(cdi.getComponents().size(), 1);
		Assert.isTrue(cdi.isComponentRegistered(B.class));
		// But not created yet
		Assert.isTrue(cdi.getComponent(B.class).isFutur());
		// Should raise an exception because A is required to build B
		cdi.build();
	}
	
	@TestMethod(4)
	public void RegistredNevertheless() {
		// Despite B wasn't correctly built, it was registred
		Assert.equals(cdi.getComponents().size(), 1);
		Assert.isTrue(cdi.isComponentRegistered(B.class));
		// And created
		Assert.isFalse(cdi.getComponent(B.class).isFutur());
		// Should no raise any more exceptions : injections are avoided
		cdi.build();
	}
	
	@TestMethod(5)
	public void ButNoMoreRaise() {
		B b = cdi.getInstance(B.class);
		// B is well created
		Assert.notNull(b);
		// But injection has failed
		Assert.isNull(b.a);
	}
	
	@TestMethod(6)
	@ExpectedException(IllegalStateException.class)
	public void DuplicateComponent() {
		// Component B is already registred
		cdi.register(B.class);
	}
	
	@TestMethod(7)
	public void CreateNewComponent() {
		cdi.register(C.class);
		// C should be registred
		Assert.equals(cdi.getComponents().size(), 2);
		Assert.isTrue(cdi.isComponentRegistered(C.class));
		// But not created yet
		Assert.isTrue(cdi.getComponent(C.class).isFutur());
	}
	
	@TestMethod(8)
	public void BuildNewComponent() {
		cdi.build();
		// C is created now
		Assert.isFalse(cdi.getComponent(C.class).isFutur());
		C c = cdi.getInstance(C.class);
		Assert.notNull(c);
		// And the injection is expected to be done
		Assert.notNull(c.b);
		Assert.isInstanceOf(c.b, B.class);
	}
	
	@TestMethod(9)
	public void RealComponentRegistredForParentClass() {
		cdi.register(A.class, new D());
		// D should be registred as A
		Assert.equals(cdi.getComponents().size(), 3);
		Assert.isFalse(cdi.isComponentRegistered(D.class));
		Assert.isTrue(cdi.isComponentRegistered(A.class));
		// And already created
		Assert.isFalse(cdi.getComponent(A.class).isFutur());
		D d = (D) cdi.getInstance(A.class);
		Assert.notNull(d);
		// Injections are done on the fly because D is not futur
		Assert.notNull(d.c);
	}
	
	@TestMethod(11)
	public void FuturComponentCreatedManually() {
		cdi.register(E.class);
		// E should be registred
		Assert.equals(cdi.getComponents().size(), 4);
		Assert.isTrue(cdi.isComponentRegistered(E.class));
		// Component is not created yet
		FuturInstance<E> i = (FuturInstance<E>) cdi.getComponent(E.class);
		Assert.isTrue(i.isFutur());
		// But since i created it manually...
		E e = i.getInstance();
		Assert.isFalse(i.isFutur());
		// Injections are done
		Assert.notNull(e.d);
	}
	
	@TestMethod(12)
	@ExpectedException(IllegalArgumentException.class)
	public void TypeMismatch() {
		// See type F to understand the expected error
		cdi.register(F.class);
		cdi.build();
	}
	
	@TestMethod(13)
	public void AttributeProtectionIsNotAProbleme() {
		// Try to inject into private attribute
		cdi.register(G.class);
		cdi.build();
		// Is expected to work properly
		Assert.notNull(cdi.getInstance(G.class).b);
		Assert.isInstanceOf(cdi.getInstance(G.class).b, B.class);
	}
	
	@TestMethod(14)
	@ExpectedException(IllegalArgumentException.class)
	public void InjectOnTheFlyWithFailure() {
		A a = new A();
		Assert.isNull(a.d);
		cdi.inject(a, "d", D.class);
	}
	
	@TestMethod(15)
	public void InjectOnTheFly() {
		A a = new A();
		Assert.isNull(a.d);
		cdi.inject(a, "d", A.class);
		Assert.notNull(a.d);
	}
	
	public static class A {
		@Inject public D d;
	}
	
	public static class B {
		@Inject private A a;
	}
	
	public static class C {
		@Inject public B b;
	}
	
	public static class D extends A {
		@Inject public C c;
	}
	
	public static class E {
		@Inject(A.class) protected D d;
	}
	
	public static class F {
		// B is no a subtype of A
		@Inject(A.class) protected B b;
	}
	
	public static class G {
		@Inject private B b;
	}
	
}
