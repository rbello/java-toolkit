/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.test;

import java.util.List;
import java.util.Set;


public abstract class TestCase {

	private String name = null;
	private int step = 0;
	
	public TestCase() {	
	}

	public TestCase(String testName) {
		setName(testName);
	}
	
	public abstract void run() throws Exception;
	
	public void startTest() throws Exception {
		run();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public static void out(String message) {
		out(message, false);
	}
	
	public static void out(String message, boolean fatal) {
		System.err.println("[TEST FAILURE] "+message);
		if (fatal) throw new TestFailure();
	}
	
	public void assertEquals(String test, boolean a, boolean b) {
		if (a == b) return;
		out(test);
	}
	
	public void assertTrue(String test, boolean a) {
		if (a == true) return;
		out(test);
	}
	
	public void assertFalse(String test, boolean a) {
		if (a == false) return;
		out(test);
	}
	
	public void assertNotNull(String test, Object a) {
		if (a != null) return;
		out(test);
	}
	
	public void assertNull(String test, Object a) {
		if (a == null) return;
		out(test + " (" + a + ")");
	}
	
	public void assertNotNegative(String test, long a) {
		if (a >= 0) return;
		out(test + " (" + a + ")");
	}
	
	public void assertNotNegative(String test, double a) {
		if (a >= 0) return;
		out(test + " (" + a + ")");
	}
	
	public void assertEqual(String test, long a, long b) {
		if (a == b) return;
		out(test + " (" + a + " != " + b + ")");
	}
	
	public void assertEqual(String test, String m, String n) {
		if (m != null && n != null) {
			if (m.equals(n)) return;
		}
		out(test + " (" + m + " != " + n + ")");
	}
	
	public void assertEqual(String test, Object a, Object b) {
		if (a != null && b != null) {
			if (a.equals(b)) return;
		}
		out(test + " (" + a + " != " + b + ")");
	}
	
	public void assertEqual(String test, long a, long b, long delta) {
		if (Math.abs(a - b) < delta) return;
		out(test + " (" + a + " != " + b + "  +/- " + delta + ")");
	}
	
	public void assertEqual(String test, double a, double b) {
		if (a == b) return;
		out(test + " (" + a + " != " + b + ")");
	}
	
	public void assertEqual(String test, double a, double b, double delta) {
		if (Math.abs(a - b) < delta) return;
		out(test + " (" + a + " != " + b + "  +/- " + delta + ")");
	}
	
	public void assertIllegalState(String test) {
		out(test);
	}
	
	public void assertNoThrow(String test, Throwable t) {
		out(test);
		t.printStackTrace();
	}
	
	public void assertNotEmpty(String test, Set<?> s) {
		if (s != null && s.size() > 0) return;
		out(test);
	}
	
	public void assertInstanceOf(String test, Object o, Class<?> clazz) {
		if (o != null && clazz != null) {
			if (clazz.isInstance(o)) return;
		}
		out(test);
	}

	public void assertNotEmpty(String test, List<?> s) {
		if (s != null && s.size() > 0) return;
		out(test+"");
	}
	
	public TestCase sout(Object o) {
		System.out.println(o);
		return this;
	}
	
	public static interface UT<T1, T2> {
		public boolean test(T1 a, T2 b);
	}
	
	public boolean assertNotNull(Object... o) {
		return testSuccess(o, null, new UT<Object[], Void>() {
			public boolean test(Object[] a, Void b) {
				for (Object o : a) {
					if (o == null) return false;
				}
				return true;
			}
		});
	}
	
	public void assertEqual(Object a, Object b) {
		testSuccess(a, b, new UT<Object, Object>() {
			public boolean test(Object a, Object b) {
				if (a == null && b == null) return true;
				if (a.equals(b)) return true;
				return false;
			}
		});
	}
	public void assertNotSameInstance(Object a, Object b) {
		testSuccess(a, b, new UT<Object, Object>() {
			public boolean test(Object a, Object b) {
				return a != b;
			}
		});
	}
	
	public static <T1, T2> boolean testSuccess(T1 a, T2 b, UT<T1, T2> ut) {
		try {
			if (ut.test(a, b)) {
				success(null, a, b);
				return true;
			}
			else {
				failure(null, a, b, null);
			}
		}
		catch (Throwable t) {
			failure(null, a, b, t);
		}
		return false;
	}
	
	private static <T1, T2> void success(String testName, T1 a, T2 b) {
		System.out.println(String.format("[SUCCESS] %s %s  (%s=%s)", testName, getEmplacement(), a, b));
	}
	
	private static <T1, T2> void failure(String testName, T1 a, T2 b, Throwable t) {
		System.err.println(String.format("[FAILURE] %s %s  (%s!=%s)", testName, getEmplacement(), a, b));
	}

	private static String getEmplacement() {
		try {
			throw new Exception();
		}
		catch (Exception ex) {
			StackTraceElement e = ex.getStackTrace()[4];
			return e.getMethodName() + ":" + e.getFileName() + "(" + e.getLineNumber() + ")";
		}
	}

	/**
	 * Cette exception se lève pour stoper l'evaluation du code lorsqu'une
	 * assertion 
	 * 
	 * @author rbello
	 * @see TestCase
	 */
	@SuppressWarnings("serial")
	public static final class TestFailure extends RuntimeException {
	}
	
}
