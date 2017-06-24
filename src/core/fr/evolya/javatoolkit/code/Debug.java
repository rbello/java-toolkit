package fr.evolya.javatoolkit.code;

import java.util.Map;

public final class Debug {

	public static final void print(Map<?, ?> map) {
		System.out.println(map.getClass().getSimpleName() + " (" + map.size() + ")");
		for (Object key : map.keySet()) {
			System.out.print(key.toString() + " => ");
			Object value = map.get(key);
			if (value == null) {
				System.out.println("null");
				continue;
			}
			if (value.getClass().isArray()) {
				System.out.print("Array[");
				print((Object[]) value, true);
				System.out.println("]");
				continue;
			}
			System.out.println(value.toString());
		}
	}

	public static final void print(Object[] value) {
		print(value, false);
	}

	public static final void print(Object[] array, boolean inline) {
		if (!inline)
			System.out.println(array.getClass().getSimpleName() + " (" + array.length + ")");
		for (int i = 0; i < array.length; i++) {
			if (inline && i > 0) System.out.print(", ");
			Object value = array[i];
			System.out.print(value == null ? "null" : value.toString());
			if (!inline) System.out.println();
		}
	}

}
