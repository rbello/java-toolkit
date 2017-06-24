/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.exceptions;


@SuppressWarnings("serial")
public class AllreadyStartedException extends StateChangeException {

	public AllreadyStartedException() {
		super();
	}

	public AllreadyStartedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AllreadyStartedException(String message) {
		super(message);
	}

	public AllreadyStartedException(Throwable cause) {
		super(cause);
	}

}
