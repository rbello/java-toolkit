package fr.evolya.javatoolkit.net.http;

public class HttpException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private int code;

	public HttpException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public int getReturnCode() {
		return code;
	}
	
}
