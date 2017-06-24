/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.exceptions;


@SuppressWarnings("serial")
public class AllreadyStoppedException extends StateChangeException {

	public AllreadyStoppedException() {
		super();
	}

	public AllreadyStoppedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AllreadyStoppedException(String message) {
		super(message);
	}

	public AllreadyStoppedException(Throwable cause) {
		super(cause);
	}

}
