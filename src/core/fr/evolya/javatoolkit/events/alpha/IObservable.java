package fr.evolya.javatoolkit.events.alpha;

/**
 * Interface des objets qui mettent � disposition un syst�me de
 * transmission d'event, sans l'impl�menter directement.
 * 
 * Cette interface fonctionne avec le IEventDispatcher et la IListener.
 * Ce syst�me est STABLE et peut �tre utilis�.
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