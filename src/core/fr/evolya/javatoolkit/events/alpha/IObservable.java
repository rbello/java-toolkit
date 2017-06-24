package fr.evolya.javatoolkit.events.alpha;

/**
 * Interface des objets qui mettent à disposition un système de
 * transmission d'event, sans l'implémenter directement.
 * 
 * Cette interface fonctionne avec le IEventDispatcher et la IListener.
 * Ce système est STABLE et peut être utilisé.
 * 
 * <E> Le type d'event
 */
public interface IObservable<E> {
	
	public final int MIN_PRIORITY		= 0;
	public final int LOW_PRIORITY		= 20;
	public final int DEFAULT_PRIORITY	= 100;
	public final int HIGH_PRIORITY		= 400;
	public final int MAX_PRIORITY		= 500;

	/**
	 * Renvoie l'objet qui permet de s'abonner aux events.
	 * @return Le dispatcher d'events.
	 */
	public IEventDispatcher<E> events();
	
}