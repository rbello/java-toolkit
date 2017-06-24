package fr.evolya.javatoolkit.net.http;


public interface HttpCallback {
	
	public void onSuccess(HttpRequest response);

	public void onFailure(int returnCode, String message, HttpRequest request, Exception ex);

}
