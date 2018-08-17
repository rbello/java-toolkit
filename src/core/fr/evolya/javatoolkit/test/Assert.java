package fr.evolya.javatoolkit.test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.cli.AsciiTable;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;
import fr.evolya.javatoolkit.code.utils.StringUtils;
import fr.evolya.javatoolkit.code.utils.Utils;

public class Assert {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface TestClass {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface TestMethod {
		int value() default 0;
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface ExpectedException {
		Class<? extends Throwable> value() default Exception.class;
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface BeforeTests { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface AfterTests { }
	
	private static Map<Class<?>, Tests> contexts;
	private static int assertions;
	
	static {
		contexts = new HashMap<Class<?>, Tests>();
		assertions = 0;
	}
	
	public static void runTests() {
		runTests(false);
	}

	public static void runTests(boolean printExceptionsStackTraces) {
		try {
			Class<?> type = Class.forName(Utils.last(Thread.currentThread().getStackTrace()).getClassName());
			runTests(type, printExceptionsStackTraces);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to gather launch class", e);
		}
	}
	
	public static void runTests(Class<?> type) {
		runTests(type, true);
	}
	
	public static void runTests(Class<?> type, boolean printExceptionsStackTraces) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		System.err.println(" Execute tests: " + type.getSimpleName());
		System.err.println(" Started:       " + sdf.format(new Date()));
		System.err.println(" ───────────────────────────────────────────────\n");
		
		AsciiTable table = new AsciiTable("Test", 40, "Duration", 12, "Results", 90);
		
		StringBuilder summary = new StringBuilder();
		summary.append(table.header());
		
		FuturInstance<Object> instance = new FuturInstance<Object>();
		FuturInstance<Throwable> error = new FuturInstance<Throwable>();
		// Create instance
		try {
			instance.setInstance(type.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Tests suite = new Tests(instance.getInstance());
		contexts.put(type, suite);
		
		executeMethods(BeforeTests.class, type, instance.getInstance(), error);
		if (!error.isFutur()) throw new RuntimeException(error.getInstance());
		
		Arrays.stream(type.getMethods())
			.filter(method -> method.isAnnotationPresent(TestMethod.class))
			.forEach((method) -> {
				try {
					int order = method.getAnnotation(TestMethod.class).value();
					//System.err.println("Add " + method.getName());
					suite.add(method, order);
				} catch (Exception e) {
					error.setInstance(e);
				}
			});
		if (!error.isFutur()) throw new RuntimeException(error.getInstance());
		
		int tests = 0, success = 0, failures = 0;
		
		for (Method m : suite.tests()) {
			
			assertions = 0;
			
			// Table separator
			if (tests > 0) summary.append(table.nl() + StringUtils.NL_CHAR);
			
			// Start recording elapsed time
			long time = System.currentTimeMillis();
			
			try {
				tests++;
				// Execute test method
				m.setAccessible(true);
				m.invoke(instance.getInstance());
				
				// Expected exception
				if (m.isAnnotationPresent(ExpectedException.class)) {
					assertions++;
					Class<?> ex = m.getAnnotation(ExpectedException.class).value();
					throw new AssertException("ExceptionIsExpected", null, ">>No exception thrown", ex);
				}
				
				// Log success
				summary.append(table.print(m.getName(), Utils.shortDuration(System.currentTimeMillis() - time), "SUCCESS - " + assertions + " assert(s)") + StringUtils.NL_CHAR);
				success++;
				continue;
			}
		
			catch (Throwable t) {
				
				// Stop current timer
				time = System.currentTimeMillis() - time;
				
				// Find root exception
				Throwable top = t;
				while (t.getCause() != null) t = t.getCause();
				
				// Check if the exception was expected
				if (m.isAnnotationPresent(ExpectedException.class)) {
					if (t instanceof AssertException) {
						// Avoid handling this kind of exception like the others
					}
					else {
						Class<?> ex = m.getAnnotation(ExpectedException.class).value();
						if (ex == t.getClass()) {
							// It's a success
							assertions++;
							summary.append(table.print(m.getName(), Utils.shortDuration(time), "SUCCESS - " + assertions + " assert(s)") + StringUtils.NL_CHAR);
							success++;
							continue;
						}
						else {
							t = new AssertException("ExceptionIsExpected", "Exception raised wasn't that expected", t.getClass(), ex, t);
						}
					}
				}
				
				// Print exception stack trace
				System.err.println("UNEXPECTED EXCEPTION INTO TESTCASE " + type.getSimpleName() + "::" + m.getName() + 
						"() --> " + top.getClass().getSimpleName() + " " + top.getMessage());
				if (printExceptionsStackTraces) {
					top.printStackTrace();
				}

				// Count as a failure
				failures++;
				
				// Construct table summary
				StringBuilder sb = new StringBuilder("FAILURE");
				if (t instanceof AssertException) {
					AssertException ex = (AssertException) t;
					sb.append(" - Test ");
					sb.append(ex.getTestCase());
					sb.append(" has failed\nGiven:    ");
					sb.append(ex.getGivenValue2());
					sb.append("\nExpected: ");
					sb.append(ex.getExpectedValue2());
					if (ex.hasMessage()) {
						sb.append("\nMessage:  ");
						sb.append(ex.getMessage());
					}
					if (ex.hasCause()) {
						sb.append("\nCause:    ");
						sb.append(ex.getCauseMessage());
					}
					sb.append("\nSource:   (");
					sb.append(ex.getFileName() + ":" + ex.getFileLine() + ")");
				}
				else {
					sb.append(" - Exception ");
					sb.append(t.getClass().getSimpleName());
					sb.append("\nMessage:  ");
					sb.append(t.getMessage());
					StackTraceElement st = Utils.first(t.getStackTrace());
					sb.append("\nSource:   (");
					sb.append(st.getFileName() + ":" + st.getLineNumber() + ")");
				}
				summary.append(table.print(m.getName(), Utils.shortDuration(time), sb.toString()) + StringUtils.NL_CHAR);
			}
		}
		
		summary.append(table.footer());
		
		System.err.println("\n ───────────────────────────────────────────────\n SUMMARY :");
		System.err.println(summary.toString());
		System.err.println(String.format(" Executed %s test(s), %s failure(s), %s passed without failure", tests, failures, success));
		if (failures < 1) {
			System.err.println(" -- ALL TESTS ARE SUCCESSFUL ! --");
		}
		else {
			System.err.println(" -- " + failures + " TESTS FAILURE --\n");
		}
		
		
		
		executeMethods(AfterTests.class, type, instance.getInstance(), error);
		if (!error.isFutur()) throw new RuntimeException(error.getInstance());
	}
	
	private static void executeMethods(Class<? extends Annotation> annotation,
			Class<?> clazz, Object instance, FuturInstance<Throwable> error) {
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
		private Map<Integer, Method> tests = new TreeMap<>();
		public Tests(Object testObject) {
		}
		public void add(Method method, int order) {
			if (order == 0) order = counter++;
			//System.err.println("Add test " + method.getName() + " at order "+ order);
			while (tests.containsKey(order)) order++;
			tests.put(order, method);
		}
		public List<Method> tests() {
			return new LinkedList<Method>(tests.values());
		}
	}

	public static void notNull(Object obj) {
		notNull(obj, null);
	}
	
	public static void notNull(Object obj, String msg) {
		assertions++;
		if (obj == null) throw new AssertException("NotNull", msg, obj, ">>NOT NULL");
	}
	
	public static void isNull(Object obj) {
		isNull(obj, null);
	}
	
	public static void isNull(Object obj, String msg) {
		assertions++;
		if (obj != null) throw new AssertException("IsNull", msg, obj, null);
	}

	public static void equals(int a, int b) {
		equals(a, b, null);
	}
	
	public static void equals(int a, int b, String msg) {
		assertions++;
		if (a != b) throw new AssertException("Equals(int,int)", msg, a, b);
	}
	
	public static void isFalse(boolean obj) {
		isFalse(obj, null);
	}

	public static void isFalse(boolean obj, String msg) {
		assertions++;
		if (obj != false) throw new AssertException("isFalse(bool)", msg, obj, false);
	}
	
	public static void isTrue(boolean obj) {
		isTrue(obj, null);
	}

	public static void isTrue(boolean obj, String msg) {
		assertions++;
		if (obj != true) throw new AssertException("isTrue(bool)", msg, obj, true);
	}
	
	public static void isInstanceOf(Object given, Class<?> expected) {
		isInstanceOf(given, expected, null);
	}
	
	public static void isInstanceOf(Object given, Class<?> expected, String msg) {
		assertions++;
		if (given == null) throw new AssertException("isInstanceOf", msg, ">>given is NULL", ">>NOT NULL");
		if (expected == null) throw new AssertException("isInstanceOf", msg, ">>expected is NULL", ">>NOT NULL");
		if (!ReflectionUtils.isInstanceOf(expected, given.getClass())) 
			throw new AssertException("isInstanceOf", msg, given.getClass(), expected);
	}
	
	public static <T> void contains(List<T> haystack, T needle) {
		contains(haystack, needle, null);
	}
	
	public static <T> void contains(List<T> haystack, T needle, String msg) {
		assertions++;
		if (haystack == null) throw new AssertException("contains(List)", msg, ">>haystack is NULL", ">>NOT NULL");
		if (needle == null) throw new AssertException("contains(List)", msg, ">>needle is NULL", ">>NOT NULL");
		if (!haystack.contains(needle))
			throw new AssertException("contains(List)", msg, ">>list with " + haystack.size() + " element(s)", needle);
	}
	
	static class AssertException extends RuntimeException {
		
		private static final long serialVersionUID = 4391920521724727011L;
		
		private String testCase;
		private String msg;
		private Object given;
		private Object expected;
		private int line;
		private String fileName;
		private String causeMessage = null;
		
		public AssertException(String testCase, String msg, Object given, Object expected) {
			this(testCase, msg, given, expected, null);
		}
		
		public AssertException(String testCase, String msg, Object given, Object expected, Throwable cause) {
			super(cause);
			this.testCase = testCase;
			this.msg = msg;
			this.given = given;
			this.expected = expected;
			if (cause != null) {
				this.causeMessage = cause.getMessage();
			}
			for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
				if (st.getClassName().startsWith("fr.evolya.javatoolkit.test.")) continue;
				if (st.getClassName().startsWith("java.")) continue;
				if (st.getClassName().startsWith("javax.")) continue;
				if (st.getClassName().startsWith("sun.")) continue;
				this.line = st.getLineNumber();
				this.fileName = st.getFileName();
				break;
			}
		}
		
		public String getCauseMessage() {
			return causeMessage;
		}

		public boolean hasCause() {
			return (causeMessage != null);
		}
		
		public boolean hasMessage() {
			return msg != null;
		}

		public String getFileName() {
			return fileName;
		}

		public int getFileLine() {
			return line;
		}

		public String getTestCase() {
			return testCase;
		}
		
		@Override
		public String getMessage() {
			return msg;
		}
		
		public Object getGivenValue() {
			return given;
		}
		
		public Object getExpectedValue() {
			return expected;
		}
		
		public String getGivenValue2() {
			return printf(given);
		}
		
		public String getExpectedValue2() {
			return printf(expected);
		}
		
		
		private static String printf(Object obj) {
			// Null value
			if (obj == null) return "NULL";
			// Literal string
			if (obj instanceof String && ((String)obj).startsWith(">>")) return ((String)obj).substring(2);
			// Other objects
			return obj.getClass().getSimpleName() + " => " + obj.toString();
		}
		
		
	}

}
