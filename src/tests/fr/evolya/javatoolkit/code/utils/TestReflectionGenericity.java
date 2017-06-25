package fr.evolya.javatoolkit.code.utils;

public abstract class TestReflectionGenericity<T> {

	public Class<T> returnedClass()
    {
        return (Class<T>) ReflectionUtils.getTypeArguments(TestReflectionGenericity.class, getClass()).get(0);
    }
	
	public static class Toto extends TestReflectionGenericity<String> {
		
	}
	
	public static void main(String[] args) {
		Toto toto = new Toto();
		Class<?> type = toto.returnedClass();
		System.out.println(type);
		System.out.println(type == String.class);
	}
	
}
