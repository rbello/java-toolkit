package fr.evolya.javatoolkit.code.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permet d'indiquer les méthodes/classes qui servent pour le debug.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Debug {
	
	/**
	 * Description de ce qui est débuggé.
	 */
	String value() default "";

}
