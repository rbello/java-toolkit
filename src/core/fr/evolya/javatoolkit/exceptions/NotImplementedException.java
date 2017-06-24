package fr.evolya.javatoolkit.exceptions;

public class NotImplementedException extends UnsupportedOperationException {

	private static final long serialVersionUID = -8976331732269186484L;

	public NotImplementedException() {
		super("Not implemented yet!");
	}

	public NotImplementedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotImplementedException(String message) {
		super(message);
	}

	public NotImplementedException(Throwable cause) {
		super(cause);
	}

}
