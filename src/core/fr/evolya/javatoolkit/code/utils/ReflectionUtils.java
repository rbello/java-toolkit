package fr.evolya.javatoolkit.code.utils;

import java.lang.reflect.Method;

public final class ReflectionUtils {
	
	private ReflectionUtils() {
	}

	public static Method getMethodMatching(Object targetObject, Method method) {
		// TODO Doc
		// TODO Comparer aussi les arguments
		for (Method m : targetObject.getClass().getMethods()) {
			if (m.getName().equals(method.getName())) return m;
		}
		throw new UnsupportedOperationException("Unable to bind method");
	}
	
	/**
	 * Recursively search through the class hierarchy of <code>targetClass</code> for
	 * the specified method.
	 */
	public static Method getMethodMatching(Class<?> targetClass, String methodName, Class<?>[] methodArgsClasses) {

		Method out = null;
		
		for (Method m : targetClass.getDeclaredMethods()) {
			
			// Le nom de la méthode ne correspond pas
			if (!m.getName().equals(methodName)) {
				continue;
			}
			
			// On recherche les types des arguments de la méthode
			Class<?>[] types = m.getParameterTypes();
			
			// Si le nombre d'argument ne match pas
			if (methodArgsClasses.length != types.length) {
				continue;
			}

			StringBuilder sb = new StringBuilder();

			// On parcours les types
			int i = 0;
			boolean ok = true;
			for (Class<?> type : m.getParameterTypes()) {
				
				// Récupération de l'argument correspondant au paramètre
				Class<?> arg = methodArgsClasses[i++];
				
				sb.append(type.getCanonicalName() + "/" + arg.getCanonicalName() + ",");
				
				// Type générique
				// La plus-value de cette méthode est ici.
				if (type.getCanonicalName().equals("java.lang.Object")) {
					continue;
				}

				// Mauvais type d'argument
				if (!type.getCanonicalName().equals(arg.getCanonicalName())) {
					ok = false;
					break;
				}

			}
			
			// Method found
			if (ok) {
				out = m;
				break;
			}
			
		}
		
		// Recursivity
		if (out == null) {
			targetClass = targetClass.getSuperclass();
			if (targetClass != null) {
				out = getMethodMatching(targetClass, methodName, methodArgsClasses);
			}
		}
		
		return out;
	}
	
	public static String getMethodSignature(String methodName, Class<?>[] argsType) {
		StringBuffer sb = new StringBuffer(methodName).append("(");
		for (int i = 0; i < argsType.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(argsType[i].getName());
		}
		sb.append(")");
		return sb.toString();
	}
	
}
