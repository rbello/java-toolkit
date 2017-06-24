package fr.evolya.javatoolkit.code.funcint;

public interface Callback<T, E> {

	public void onSuccess(T result);

	public void onFailure(E error);
	
	public static class Adapter<A, B> implements Callback<A, B> {

		public void onSuccess(A result) { }

		public void onFailure(B error) { }
		
	}

}
