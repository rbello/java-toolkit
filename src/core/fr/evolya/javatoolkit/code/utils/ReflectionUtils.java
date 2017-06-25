package fr.evolya.javatoolkit.code.utils;

import java.lang.reflect.Method;

public final class ReflectionUtils {
	
	private ReflectionUtils() {
	}

	public static Method findMethod(Method method, Object target) {
		// TODO Doc
		// TODO Comparer aussi les arguments
		for (Method m : target.getClass().getMethods()) {
			if (m.getName().equals(method.getName())) return m;
		}
		throw new UnsupportedOperationException("Unable to bind method");
	}
	
}
