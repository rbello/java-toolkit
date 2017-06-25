package fr.evolya.javatoolkit.test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;

public class Assert {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface TestMethod {
		int value() default 0;
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface BeforeTests { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface AfterTests { }
	
	private static Map<Class<?>, Tests> contexts;
	
	static {
		contexts = new HashMap<Class<?>, Tests>();
	}

	public static void runTests(Class<?> clazz) {
		System.err.println("Run tests on " + clazz.getSimpleName());
		
		FuturInstance<Object> instance = new FuturInstance<Object>();
		FuturInstance<Exception> error = new FuturInstance<Exception>();
		// Create instance
		try {
			instance.setInstance(clazz.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Tests suite = new Tests(instance.getInstance());
		contexts.put(clazz, suite);
		
		executeMethods(BeforeTests.class, clazz, instance.getInstance(), error);
		if (!error.isFutur()) throw new RuntimeException(error.getInstance());
		
		Arrays.stream(clazz.getMethods())
			.filter(method -> method.isAnnotationPresent(TestMethod.class))
			.forEach((method) -> {
				try {
					int order = method.getAnnotation(TestMethod.class).value();
					suite.add(method, order);
				} catch (Exception e) {
					error.setInstance(e);
				}
			});
		if (!error.isFutur()) throw new RuntimeException(error.getInstance());
		
		for (Method m : suite.tests()) {
			try {
				System.err.println("Execute test " + instance.getClass().getSimpleName()
						+ "::" + m.getName());
				m.invoke(instance.getInstance());
			} catch (Exception e) {
				error.setInstance(e);
				if (!error.isFutur()) throw new RuntimeException(error.getInstance());
			}
		}
		System.err.println("ALL TESTS ARE SUCCESSFUL !");
		
		executeMethods(AfterTests.class, clazz, instance.getInstance(), error);
		if (!error.isFutur()) throw new RuntimeException(error.getInstance());
	}
	
	private static void executeMethods(Class<? extends Annotation> annotation,
			Class<?> clazz, Object instance, FuturInstance<Exception> error) {
		Arrays.stream(clazz.getMethods())
			.filter(method -> method.isAnnotationPresent(annotation))
			.forEach((method) -> {
				try {
					method.invoke(instance);
				} catch (Exception e) {
					error.setInstance(e);
				}
			});
	}
	
	private static class Tests {
		private int counter = 0;
		private Map<Integer, Method> tests = new HashMap<>();
		public Tests(Object testObject) {
		}
		public void add(Method method, int order) {
			if (order == 0)
				order = counter++;
			tests.put(order, method);
		}
		public List<Method> tests() {
			return new ArrayList<Method>(tests.values());
		}
	}
	
	private static void log(String test, String msg, Object given, Object expected) {
		System.err.println("[FAILURE] " + test + " : " + msg);
		System.out.println("          Given:    " + given);
		System.out.println("          Expected: " + expected);
	}

	public static void notNull(Object obj, String msg) {
		if (obj == null) log("NotNull", msg, obj, null);
	}
	
	public static void notNull(Object obj) {
		if (obj == null) log("NotNull", "", obj, null);
	}

	public static void equals(int a, int b, String msg) {
		if (a != b) log("Equals(int,int)", msg, a, b);
	}
	
	public static void equals(int a, int b) {
		if (a != b) log("Equals(int,int)", "", a, b);
	}

}
