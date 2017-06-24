package fr.evolya.javatoolkit.code.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permet d'indiquer qu'une méthode est buggée.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Bug {

	/**
	 * La description du bug.
	 */
	String value() default "";
	
}
