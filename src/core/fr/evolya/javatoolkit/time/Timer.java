package fr.evolya.javatoolkit.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.evolya.javatoolkit.events.basic.Event;
import fr.evolya.javatoolkit.events.basic.Listener;

/**
 * Classe abstraite pour la création de timers qui se lancent à fréquence régulière.
 */
public abstract class Timer {

	/**
	 * Le délais entre chaque tick du timer, en millisecondes.
	 */
	protected long _frequency;
	
	/**
	 * La liste des listeners de ce timer.
	 */
	protected List<Listener<TimerEvent>> _listeners = new ArrayList<Listener<TimerEvent>>();

	/**
	 * Renvoie la fréquence de refresh du timer.
	 * 
	 * @return Le délais entre chaque tick du timer, en millisecondes.
	 */
	public long getFrequency() {
		return _frequency;
	}
	
	/**
	 * Modifie la fréquence de refresh du timer.
	 * 
	 * @param frequency Le délais entre chaque tick du timer, en millisecondes.
	 */
	public void setFrequency(long frequency) {
		_frequency = frequency;
	}
	
	/**
	 * Active le timer.
	 */
	public abstract boolean start();
	
	/**
	 * Stoppe le timer.
	 */
	public abstract boolean stop();
	
	/**
	 * Indique si le timer est actif.
	 * 
	 * @return Renvoie TRUE si le timer est actuellement actif.
	 */
	public abstract boolean isAlive();
	
	/**
	 * Inscrit un listener aux ticks de cet event.
	 * 
	 * @param listener Le listener à ajouter.
	 */
	public void onElapsed(Listener<TimerEvent> listener) {
		
		// Vérification d'argument
		if (listener == null) {
			throw new NullPointerException();
		}
		
		// Remove de la liste
		synchronized (_listeners) {
			_listeners.add(listener);
		}
		
	}
	
	/**
	 * Retire l'inscription d'un listener sur ce Timer.
	 * 
	 * @param listener Le listener à retirer.
	 */
	public void offElapsed(Listener<TimerEvent> listener) {

		// Vérification d'argument 
		if (listener == null) {
			throw new NullPointerException();
		}
		
		// Remove de la liste
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
		
	}
	
	/**
	 * Déclenche un tick du timer.
	 */
	protected void raiseElapsed() {

		// Référence datée du tick
		TimerEvent event = new TimerEvent(this, new Date());
		
		// Copie de la liste des listeners
		List<Listener<TimerEvent>> listeners;
		synchronized (_listeners) {
			listeners = new ArrayList<Listener<TimerEvent>>(_listeners);
		}
		
		// Notification des listeners, non-protégée des exceptions
		for (Listener<TimerEvent> listener : listeners) {
			listener.notifyEvent(event);
		}
		
	}
	
	/**
	 * Classe d'event pour les Timer.
	 */
	public static class TimerEvent extends Event {

		private Timer _timer;
		private Date _date;

		public TimerEvent(Timer timer, Date date) {
			_timer = timer;
			_date = date;
		}
		
		public Timer getTime() {
			return _timer;
		}
		
		public Date getDate() {
			return _date;
		}
		
	}
	
}
