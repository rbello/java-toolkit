package fr.evolya.javatoolkit.app.cdi;

import java.util.NoSuchElementException;

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
		Assert.runTests(true);
	}
	
	@BeforeTests
	public void init() {
		cdi = new DependencyInjectionContext(job -> job.run());
		Logs.setGlobalLevel(Logs.ALL);
	}
	
	@TestMethod(1)
	public void BeforeAll() {
		Assert.equals(cdi.getComponents().size(), 0);
	}
	
	@TestMethod(2)
	public void BasicChecks() {
		Assert.isFalse(cdi.isComponentRegistered(null));
		Assert.isFalse(cdi.isComponentRegistered(Assert.class));
	}
	
	@TestMethod(3)
	@ExpectedException(NoSuchElementException.class)
	public void BadComponentConstruction() {
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
	public void ButRegistredNevertheless() {
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
		cdi.register(H.class, new D());
		// D should be registred as H
		Assert.equals(cdi.getComponents().size(), 3);
		Assert.isFalse(cdi.isComponentRegistered(D.class));
		Assert.isTrue(cdi.isComponentRegistered(H.class));
		// And already created
		Assert.isFalse(cdi.getComponent(H.class).isFutur());
		D d = (D) cdi.getInstance(H.class);
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
	//@ExpectedException(IllegalArgumentException.class)
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
	@ExpectedException(NoSuchElementException.class)
	public void InjectOnTheFlyWithFailure() {
		A a = new A();
		Assert.isNull(a.d);
		cdi.inject(a, "d", D.class);
	}
	
	@TestMethod(15)
	public void InjectOnTheFly() {
		A a = new A();
		Assert.isNull(a.d);
		cdi.inject(a, "d", H.class);
		Assert.notNull(a.d);
	}

	@TestMethod(16)
	public void CrossDependency() {
		cdi.register(I.class);
		cdi.register(J.class);
		// Not supposed to lock or throw any exception
		cdi.build();
	}

	@TestMethod(17)
	public void LateInjection() {
		// Let's change the default behavior
		cdi.setMissingComponentInjectionBehavior(DependencyInjectionContext.BEHAVIOR_WAIT_FOR_IT);
		// And check if only one injection wasn't executed
		Assert.equals(cdi.getUnexecutedInjections().size(), 1);
		Assert.contains(cdi.getUnexecutedInjections(), "(A) B::a");
		// Now register the A component
		cdi.register(A.class, new A());
		// Check injections are well dispatched
		Assert.notNull(cdi.getInstance(A.class).d);
		Assert.notNull(cdi.getInstance(B.class).a);
	}

	@TestMethod(18)
	@ExpectedException(StackOverflowError.class)
	public void CircularDependency() {
		cdi.register(K.class);
		cdi.build();
	}
	
	public static class A {
		@Inject(H.class) public D d;
	}
	
	public static class B {
		@Inject private A a;
	}
	
	public static class C {
		@Inject public B b;
	}
	
	public static class D extends H {
		@Inject public C c;
	}
	
	public static class E {
		@Inject(H.class) protected D d;
	}
	
	public static class F {
		// B is no a subtype of H
		@Inject(H.class) protected B b;
	}
	
	public static class G {
		@Inject protected B b;
	}
	
	public static class H {
		@Inject private B b;
	}

	public static class I {
		@Inject private J j;
	}
	
	public static class J {
		@Inject private I i;
	}
	
	public static class K {
		@Inject public K k;
	}
}
