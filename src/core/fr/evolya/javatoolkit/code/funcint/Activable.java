/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.code.funcint;

public interface Activable {
	
	void activate() throws Exception;
	
	void passivate() throws Exception;
	
	boolean isActivated();

}
