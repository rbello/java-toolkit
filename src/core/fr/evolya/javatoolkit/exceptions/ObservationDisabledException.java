/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.exceptions;

public class ObservationDisabledException extends RuntimeException {

	public ObservationDisabledException() {
		super();
	}

	public ObservationDisabledException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObservationDisabledException(String message) {
		super(message);
	}

	public ObservationDisabledException(Throwable cause) {
		super(cause);
	}
	
	private static final long serialVersionUID = 1L;

}
