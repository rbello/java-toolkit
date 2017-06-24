/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.exceptions;

public class TooManyObserversException extends RuntimeException {

	public TooManyObserversException() {
		super();
	}

	public TooManyObserversException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyObserversException(String message) {
		super(message);
	}

	public TooManyObserversException(Throwable cause) {
		super(cause);
	}
	
	private static final long serialVersionUID = 1L;

}
