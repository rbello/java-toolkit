/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.code.funcint;

/**
 * Cette interface définit le comportement d'un objet capable de filtrer un
 * objet en fonction de ses critères.
 * <p>
 * L'interface ne contient qu'une méthode <code>boolean accept(E)</code> qui
 * retourne true si l'objet passé en paramètre est accepté par le filtre.
 * 
 * @version 1.0  10/10/08
 * @author rbello
 *
 * @param <E> Le type d'objet à filtrer
 */
@FunctionalInterface
public interface Filter<E> {

	boolean accept(E value);
	
}
