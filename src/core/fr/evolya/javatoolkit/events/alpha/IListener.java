package fr.evolya.javatoolkit.events.alpha;

/**
 * Interface pour les listeners.
 * 
 * Cette interface est utilis�e par la classe IObservable.
 * 
 * <E> Le type d'event
 */
public interface IListener<E> {

	/**
	 * Appel� lors de la notification d'un �v�nement.
	 * 
	 * Le retour boolean permet de stopper la propagation de l'event
	 * si la valeur renvoy�e est FALSE. Lors de l'invocation du trigger()
	 * il est possible de r�cup�rer ce boolean, et d'interrompre aussi
	 * une partie de l'execution. C'est un comportement facultatif.
	 * 
	 * Pour ne pas perturber le syst�me, renvoyer TRUE sauf si vous avez
	 * une bonne raison de vous interompre l'envoie de l'event.  
	 * 
	 * @param event L'identifiant ou l'objet de l'event
	 * @param args Les arguments.
	 * @return TRUE pour continuer la propagation de l'event.
	 */
	public boolean notifyEvent(E event, Object... args);

}
