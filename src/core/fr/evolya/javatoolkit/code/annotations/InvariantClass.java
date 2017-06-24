package fr.evolya.javatoolkit.code.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permet d'indiquer que la classe est invariante, c'est à dire que ses
 * attributs ne peuvent plus être modifiés après la construction.
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InvariantClass {

}
