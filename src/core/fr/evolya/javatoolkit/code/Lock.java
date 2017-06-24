/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.code;

public final class Lock {
	
	private Object id;
	
	public Lock(Object id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return id+"";
	}
	
}
