package fr.evolya.javatoolkit.code.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A mettre sur une méthode abstraite, pour indiquer que le type de retour
 * peut ou doit être spécialisé.
 * En effet, il arrive que les méthodes abstraites aient des prototypes avec
 * des des types génériques. Or dans les classes concrètes, il est possible
 * de re-spécialiser ces types.
 * Cette annotation désigne donc que ce comportement est recommandé.
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Specializable {

}
