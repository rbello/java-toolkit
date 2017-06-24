/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.exceptions;

@SuppressWarnings("serial")
public class AllreadyRegistredException extends RuntimeException {

	public AllreadyRegistredException() {
		super();
	}

	public AllreadyRegistredException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AllreadyRegistredException(String arg0) {
		super(arg0);
	}

	public AllreadyRegistredException(Throwable arg0) {
		super(arg0);
	}

}
