package fr.evolya.javatoolkit.code.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permet d'indiquer explicitement qu'une méthode renvoie une référence,
 * dans le cas où deux méthodes existent et l'une d'elles renvoie une
 * copie.
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ByReference {

}
