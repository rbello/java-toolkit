/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.exceptions;

/**
 * Cette exception intervient lorsqu'un probleme d'etat est provoqué.
 * Par exemple, si vous essayez de démarrer un moteur qui est déja lancé.
 */
public class StateChangeException extends RuntimeException {
	
	public StateChangeException() {
		super();
	}

	public StateChangeException(String message, Throwable cause) {
		super(message, cause);
	}

	public StateChangeException(String message) {
		super(message);
	}

	public StateChangeException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}
