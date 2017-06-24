/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.exceptions;


@SuppressWarnings("serial")
public class AllreadyPausedException extends StateChangeException {

	public AllreadyPausedException() {
		super();
	}

	public AllreadyPausedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AllreadyPausedException(String message) {
		super(message);
	}

	public AllreadyPausedException(Throwable cause) {
		super(cause);
	}

}
