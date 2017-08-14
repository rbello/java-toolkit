package fr.evolya.javatoolkit.code.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ReflectionUtils {
	
	private ReflectionUtils() {
	}

	public static Method getMethodMatching(Object targetObject, Method method) {
		// TODO Doc
		// TODO Comparer aussi les arguments
		for (Method m : targetObject.getClass().getMethods()) {
			if (m.getName().equals(method.getName())) return m;
		}
		// TODO Ameliorer le log
		// Class X has no method X
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

    /**
     * Get the actual type arguments a child class has used to extend a generic
     * base class.
     *
     * @param baseClass the base class
     * @param childClass the child class
     * @return a list of the raw classes for the actual type arguments.
     * @see http://www.artima.com/weblogs/viewpost.jsp?thread=208860
     */
    public static <T> List<Class<?>> getTypeArguments(
            Class<T> baseClass, Class<? extends T> childClass)
    {
        Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
        Type type = childClass;
        // start walking up the inheritance hierarchy until we hit baseClass
        while (!baseClass.equals(getClass(type))) {
            if (type instanceof Class) {
                // there is no useful information for us in raw types, so just keep going.
                type = ((Class<?>) type).getGenericSuperclass();
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();

                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                }

                if (!rawType.equals(baseClass)) {
                    type = rawType.getGenericSuperclass();
                }
            }
        }

        // finally, for each actual type argument provided to baseClass, determine (if possible)
        // the raw class for that type argument.
        Type[] actualTypeArguments;
        if (type instanceof Class) {
            actualTypeArguments = ((Class<?>) type).getTypeParameters();
        } else {
            actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        // resolve types by chasing down type variables.
        for (Type baseType : actualTypeArguments) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClass(baseType));
        }
        return typeArgumentsAsClasses;
    }
    
    /**
     * Get the underlying class for a type, or null if the type is a variable
     * type.
     *
     * @param type the type
     * @return the underlying class
     * @see http://www.artima.com/weblogs/viewpost.jsp?thread=208860
     */
    public static Class<?> getClass(Type type)
    {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

	public static boolean isInstanceOf(Class<?> parentClass, Class<?> childClass) {
		while (childClass != null) {
			// Class comparison
			if (parentClass == childClass) return true;
			// Interface comparison
			if (Arrays.asList(childClass.getInterfaces()).contains(parentClass))
				return true;
			childClass = childClass.getSuperclass();
		}
		return false;
	}

	public static Object invokeMethod(Object target, Method method, Object[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (method.getParameters().length < args.length) {
			Object[] copy = args;
			args = new Object[method.getParameters().length];
			for (int i = 0; i < args.length; ++i) {
				args[i] = copy[i];
			}
		}
		method.setAccessible(true);
		return method.invoke(target, args);
	}
	
	public static String toString(Method method) {
		return toString(null, method);
	}

	public static String toString(Object target, Method method) {
		return toString(target, method, true);
	}
	
	public static String toString(Object target, Method method, boolean fullClassName) {
		Class<?> type = (target == null) ? method.getDeclaringClass() : target.getClass();
		StringBuilder sb = new StringBuilder();
		sb.append(fullClassName ? type.toString() : type.getSimpleName());
		sb.append("::");
		sb.append(method.getName());
		sb.append("(");
		sb.append(Arrays.stream(method.getParameterTypes()).map(Class::getName)
				.collect(Collectors.joining(", ")));
		sb.append(")");
		return sb.toString();
	}

	public static void forEachMethodsHaving(Class<?> type, Class<? extends Annotation> annotation,
			Consumer<? super Method> consumer) {
		Arrays.stream(type.getMethods())
			// Uniquement celles avec l'annotation d'injection
			.filter(method -> method.isAnnotationPresent(annotation))
			// Fetch
			.forEach(consumer);
	}

	public static Method getMethodMatching(Class<?> type, String methodName) {
		for (Method m : type.getMethods()) {
			if (m.getName().equals(methodName)) return m;
		}
		return null;
	}

	public static Field getFieldMatching(Class<?> type, String fieldName)
			throws NoSuchFieldException {
		while (type != null) {
			try {
				return type.getDeclaredField(fieldName);
			}
			catch (NoSuchFieldException ex) { }
			type = type.getSuperclass();
		}
		throw new NoSuchFieldException(fieldName);
	}

	public static Method getMethodMatchingIgnoreCase(Class<?> type, String methodName) {
		methodName = methodName.toUpperCase();
		for (Method m : type.getMethods()) {
			if (m.getName().toUpperCase().equals(methodName)) return m;
		}
		return null;
	}

}
