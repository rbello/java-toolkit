package fr.evolya.javatoolkit.events.alpha;

/**
 * Interface pour les objets qui implémentent le pattern listener.
 * Avec gestion de la priorité, et de l'interruption de propagation.
 * 
 * Cette classe est STABLE.
 * 
 * <E> Le type d'event
 */
public interface IEventDispatcher<E> extends IObservable<E> {

	/**
	 * Connecter un listener sur un event en particulier, avec une priorité par défaut.
	 * La méthode notifyEvent() du listener sera appelée lors des notifications.
	 */
	public void bind(E event, IListener<E> listener);
	
	/**
	 * Connecter un listener sur un event en particulier, avec une priorité.
	 * La méthode notifyEvent() du listener sera appelée lors des notifications.
	 */
	public void bind(E event, IListener<E> listener, Integer priority);

	/**
	 * Connecter un listener sur un event en particulier, en spécifiant le nom
	 * d'une méthode à appeler sur le listener, avec une priorité par défaut.
	 */
	public void bind(E event, IListener<E> listener, String methodName);
	
	/**
	 * Connecter un listener sur un event en particulier, en spécifiant le nom
	 * d'une méthode à appeler sur le listener, et une priorité.
	 */
	public void bind(E event, IListener<E> listener, String methodName, Integer priority);
	
	/**
	 * Déconnecter tous les listeners sur un event.
	 */
	public void unbind(E event);

	/**
	 * Déconnecter un listener sur tous les events.
	 */
	public void unbind(IListener<E> listener);
	
	/**
	 * Déconnecter un listener sur un event en particulier.
	 */
	public boolean unbind(E event, IListener<E> listener);
	
	/**
	 * Notifier tous les listeners.
	 */
	public boolean trigger(E event, Object... args);
	
	/**
	 * Notifier uniquement un listener en particulier.
	 */
	public boolean trigger(E event, IListener<E> listenerFilter, Object... args);

	/**
	 * Rediriger des events vers ce dispatcher.
	 */
	public boolean trigger(E event, IEventDispatcher<E> source, Object... args);
	
	/**
	 * Rediriger des events vers un autre IObservable
	 */
	public void redirect(E event, IObservable<E> target);
	
	/**
	 * Rediriger des events vers un autre IObservable en indiquant une priorité
	 * au listener qui sera créé pour la redirection.
	 */
	public void redirect(E event, IObservable<E> target, Integer priority);
	
	/**
	 * Rediriger tous les events de ce dispatcher sur un autre IObservable.
	 */
	public void redirect(IObservable<E> target);
	
	/**
	 * Supprimer cet objet et libérer la mémoire.
	 */
	public void dispose();
	
}
