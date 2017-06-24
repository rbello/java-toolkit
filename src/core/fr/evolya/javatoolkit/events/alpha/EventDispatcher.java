package fr.evolya.javatoolkit.events.alpha;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.Logs;

/**
 * Impl�mentation de la l'interface IObservable.
 * 
 * <E> Le type d'event
 */
public class EventDispatcher<E> implements IEventDispatcher<E> {
	
	/**
	 * Logger
	 */
	protected static final Logger LOGGER = Logs.getLogger("EventDispatcher");

	/**
	 * La liste des listeners.
	 * 
	 * Map<String event, Map<Integer priority, Triggerable listenerCallback>>
	 */
	protected Map<Object, SortedMap<Integer, Triggerable<E>>> _eventsListeners = new HashMap<Object, SortedMap<Integer, Triggerable<E>>>();
	
	/**
	 * L'objet observable qui est associ� � ce dispatcher (pour les logs)
	 */
	protected IObservable<E> _sender = null;

	/**
	 * Permet de sp�cifier un autre objet observable pour lui rediriger tous
	 * les events qui sont propag�s sur ce dispatcher.
	 */
	protected IObservable<E> _redirect = null;

	/**
	 * Permet de configurer si les exceptions doivent stopper la propagation
	 * des events, ou bien simplement �tre logg�es.
	 */
	public boolean _exceptionRaiseStopPropagation = false;
	
	/**
	 * Constructeur simple
	 */
	public EventDispatcher() {
	}
	
	/**
	 * Constructeur, avec l
	 * 
	 * @param sender L'objet qui emet les �vents r�ellement
	 */
	public EventDispatcher(IObservable<E> sender) {
		_sender = sender;
	}
	
	/**
	 * Renvoie l'objet observable qui envoie les events
	 */
	public IObservable<E> getTarget() {
		return _sender;
	}
	
	@Override
	public void bind(E event, IListener<E> listener) {
		bind(event, listener, IObservable.DEFAULT_PRIORITY);
	}

	@Override
	public void bind(E event, IListener<E> listener, Integer priority) {
		// Check arguments
		if (event == null || listener == null || priority == null) {
			throw new NullPointerException();
		}
		// Synchronized
		synchronized (_eventsListeners) {
			// Pointer to the map 
			SortedMap<Integer, Triggerable<E>> map;
			// Create the map
			if (!_eventsListeners.containsKey(event)) {
				map = new TreeMap<Integer, Triggerable<E>>();
				_eventsListeners.put(event, map);
			}
			// Restore the map
			else {
				map = _eventsListeners.get(event);
			}
			// Set right priority
			while (map.containsKey(priority)) {
				priority++;
			}
			// Add observer
			map.put(priority, new InterfaceTriggerable<E>(listener));
			// Debug
			/*if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
				LOGGER.log(IncaLogger.DEBUG, "BIND " + this.toString()
						+ ".bind('" + event + "', '" + listener + "', "
						+ priority + ")");
			}*/
		}
	}
	
	public void bind(E event, IListener<E> listener, String methodName) {
		bind(event, listener, methodName, IObservable.DEFAULT_PRIORITY);
	}
	
	@Override
	public void bind(E event, IListener<E> listener, String methodName, Integer priority) {
		// Check arguments
		if (event == null || listener == null || methodName == null || priority == null) {
			throw new NullPointerException();
		}
		// Synchronized
		synchronized (_eventsListeners) {
			// Pointer to the map 
			SortedMap<Integer, Triggerable<E>> map;
			// Create the map
			if (!_eventsListeners.containsKey(event)) {
				map = new TreeMap<Integer, Triggerable<E>>();
				_eventsListeners.put(event, map);
			}
			// Restore the map
			else {
				map = _eventsListeners.get(event);
			}
			// Set right priority
			while (map.containsKey(priority)) {
				priority++;
			}
			// Add observer
			map.put(priority, new MethodTriggerable<E>(listener, methodName));
			// Debug
			/*if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
				LOGGER.log(IncaLogger.DEBUG, "BIND "
						+ (_sender == null ? "?" : _sender.getClass().getSimpleName())
						+ ".bind('" + event + "', '" + listener.getClass().getSimpleName() + "', "
						+ priority + ")");
			}*/
		}
		
	}
	
	@Override
	public boolean trigger(E event, Object... args) {
		return trigger(event, this, null, args);
	}
	
	@Override
	public boolean trigger(E event, IEventDispatcher<E> source, Object... args) {
		return trigger(event, source, null, args);
	}
	
	@Override
	public boolean trigger(E event, IListener<E> listenerFilter, Object... args) {
		return trigger(event, this, listenerFilter, args);
	}
	
	protected boolean trigger(E event, IEventDispatcher<E> source, IListener<E> listenerFilter, Object... args) {
		
		// V�rification des arguments
		if (event == null) {
			throw new NullPointerException();
		}
		
		// Debug
		if (LOGGER.isLoggable(Logs.EVENT)) {
			Level lvl = Logs.EVENT;
			StringBuilder sb = new StringBuilder();
			sb.append("TRIGGER [");
			sb.append(event);
			sb.append("]");
			sb.append(_sender == null ? "" : " SENDER=" + _sender.getClass().getSimpleName());
			if (source != null && source != this) {
				lvl = Logs.EVENT_REDIRECT;
				sb.append(" ORIGINAL_SENDER=" + source);
			}
			sb.append(" ARGS=(");
			if (args != null) {
				int c = 0;
				for (Object o : args) {
					if (c++ > 0) {
						sb.append(", ");
					}
					if (o == null) {
						sb.append("null");
					}
					else {
						sb.append(o.getClass().getSimpleName());
					}
				}
			}
			sb.append(")");
			LOGGER.log(lvl, sb.toString());
		}
		
		// On copie la liste des listeners en synchro
		SortedMap<Integer, Triggerable<E>> map = null;
		synchronized (_eventsListeners) {
			// Aucun listener
			if (!_eventsListeners.containsKey(event)) {
				// Redirection
				if (_redirect != null) {
					return _redirect.events().trigger(event, this, args);
				}
				return true;
			}
			// Liste des listeners
			map = _eventsListeners.get(event);
		}
		
		// Inversion de l'ordre, pour respecter les priorit�s : plus
		// elle est haute, plus on est pr�venu t�t
		SortedMap<Integer, Triggerable<E>> m = new TreeMap<Integer, Triggerable<E>>(Collections.reverseOrder());
		m.putAll(map);
		map = m;
		
		// Indique si la propagation est interrompue par un listener
		// (en renvoyant false)
		boolean interrupted = false;
		
		// Fetch listeners list
		for (Integer index : map.keySet()) {
			
			// Current listener
			Triggerable<E> listener = map.get(index);
			
			// Si on ne filtre qu'un listener en particulier
			if (listenerFilter != null) {
				if (listener.getListener() != listenerFilter) {
					continue;
				}
			}
			
			// Log
			if (LOGGER.isLoggable(Logs.EVENT)) {
				LOGGER.log(Logs.EVENT, "NOTIFY [" + event
						+ "] TARGET=" + listener + "()  PRIORITY=" + index);
			}
			
			// Notify du listener
			try {
				
				if (!listener.trigger(event, args)) {
					
					// On stoppe la propagation
					interrupted = true;
					break;
					
				}
				
			}
			catch (Throwable ex) {
				
				// Debug
				if (LOGGER.isLoggable(Logs.ERROR)) {
					LOGGER.log(Logs.ERROR, "Exception on callback trigger "
							+ this.getClass().getCanonicalName() + ".trigger('"
							+ event + "', '" + listener + "') : "
							+ ex.getClass().getCanonicalName() + " - " + ex.getMessage());
					ex.printStackTrace();
				}
				
				// Si on stoppe la propagation
				if (_exceptionRaiseStopPropagation) {
					interrupted = true;
					break;
				}
				
			}
		}
		
		// Redirection des events
		if (!interrupted && _redirect != null) {
			if (!_redirect.events().trigger(event, this, args)) {
				interrupted = true;
			}
		}
		
		return !interrupted;
	}

	@Override
	public void unbind(E event) {
		if (event == null) {
			throw new NullPointerException();
		}
		synchronized (_eventsListeners) {
			_eventsListeners.remove(event);
		}
	}

	@Override
	public void unbind(IListener<E> listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		synchronized (_eventsListeners) {
			// Fetch listeners list according to events names
			for (Map<Integer, Triggerable<E>> m : _eventsListeners.values()) {
				if (m.containsValue(listener)) {
					// Fetch listeners
					for (Integer i : m.keySet()) {
						if (m.get(i).getListener() == listener) {
							m.remove(i);
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean unbind(E event, IListener<E> listener) {
		if (event == null || listener == null) {
			throw new NullPointerException();
		}
		Map<Integer, Triggerable<E>> map;
		synchronized (_eventsListeners) {
			if (!_eventsListeners.containsKey(event)) {
				return false;
			}
			map = _eventsListeners.get(event);
		}
		// Fetch listeners
		for (Integer i : map.keySet()) {
			if (map.get(i).getListener() == listener) {
				map.remove(i);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IEventDispatcher<E> events() {
		return this;
	}

	@Override
	public synchronized void dispose() {
		_eventsListeners.clear();
		_eventsListeners = null;
		_sender = null;
		_redirect = null;
	}

	/**
	 * Cette interface est utilis�e pour notifier les listeners de
	 * l'arriv�e d'un event.
	 * 
	 * @param <E> Le type d'event
	 */
	protected static interface Triggerable<E> {
		
		/**
		 * Notifier le listener.
		 */
		public boolean trigger(E event, Object... args) throws Exception;
		
		/**
		 * Renvoie le listener.
		 */
		public IListener<E> getListener();
	}
	
	/**
	 * Cet objet d�signe un listener classique, c-�-d qui impl�mente IListener.
	 * 
	 * La m�thode notifyEvent() est appel�e sur ce listener � la notification.
	 *
	 * @param <E> Le type d'event
	 */
	protected static class InterfaceTriggerable<E> implements Triggerable<E> {
		
		/**
		 * Le listener associ�
		 */
		private IListener<E> listener;
		
		/**
		 * Constructeur
		 */
		public InterfaceTriggerable(IListener<E> listener) {
			this.listener = listener;
		}
		
		/**
		 * Envoi de la notification
		 */
		@Override
		public boolean trigger(E event, Object... args) throws Exception {
			return this.listener.notifyEvent(event, args);
		}
		
		/**
		 * Renvoie le listener associ�
		 */
		@Override
		public IListener<E> getListener() {
			return listener;
		}
		
		/**
		 * Affiche cet objet sous forme de string
		 */
		@Override
		public String toString() {
			return listener.getClass().getSimpleName() + ".onNotify";
		}
	}
	
	/**
	 * Cet objet d�signe un listener simple, quand un nom de m�thode
	 * est donn� et qu'on va le chercher en utilisant la r�flexion.
	 * 
	 * @param <E> Le type d'event
	 */
	protected static class MethodTriggerable<E> implements Triggerable<E> {
		
		/**
		 * Le listener cible
		 */
		private IListener<E> listener;
		
		/**
		 * Le nom de la m�thode � appeler
		 */
		private String methodName;
		
		/**
		 * Constructeur
		 */
		public MethodTriggerable(IListener<E> listener, String methodName) {
			this.listener = listener;
			this.methodName = methodName;
		}
		
		@Override
		public boolean trigger(E event, Object... args) throws Exception {
			
			// Technique 1
			//
			// On recherche une m�thode qui a exactement la bonne
			// signature par rapport aux arguments donn�s
			java.lang.reflect.Method m = null;
			
			// Nombre de m�thode ayant ce nom trouv�es
			int count = 0;
			
			// On parcours les m�thodes de la classe du listener
			for (java.lang.reflect.Method n : listener.getClass().getMethods()) {
				
				// V�rification du nom de la m�thode
				if (!n.getName().equals(methodName)) continue;
				
				// On a trouv� une m�thode avec le m�me nom
				count++;
				
				// Les classes des parametres de la m�thode
				Class<?>[] params = n.getParameterTypes();
				
				// V�rification du nombre d'arguments
				if (params.length > args.length) continue;
				
				// Pour tester la validit� des arguments par rapport au nombre de param�tres
				boolean valid = true;
				
				// On parcours les param�tres
				for (int i = 0, l = params.length; i < l; i++) {
					
					// Si on n'a pas d'argument pour ce param�re, on est dans une
					// situation ambigue, mais on va donner raison
					if (args[i] == null) continue;
					
					// L'argument ne correspond pas � la classe du param�tre
					if (!params[i].isInstance(args[i])) {

						if (LOGGER.isLoggable(Logs.DEBUG)) {
							StringBuilder sb = new StringBuilder();
							sb.append("Mismatch at argument ");
							sb.append(i);
							sb.append("\nListener: ");
							sb.append(listener.getClass().getSimpleName());
							sb.append('.');
							sb.append(methodName);
							sb.append('(');
							for (int j = 0, s = params.length; j < s; j++) {
								if (j > 0) sb.append(", ");
								sb.append(params[j] == null ? "NULL" : params[j].getClass().getName());
							}
							sb.append(")\nEvent: ");
							sb.append(event);
							sb.append('(');
							for (int j = 0, s = args.length; j < s; j++) {
								if (j > 0) sb.append(", ");
								sb.append(args[j] == null ? "NULL" : args[j].getClass().getSimpleName());
							}
							sb.append(')');
							StackTraceElement[] stack = Thread.currentThread().getStackTrace();
							String p = getClass().getPackage().getName();
							for (StackTraceElement e : stack) {
								if (e.getClassName().startsWith("java.lang.Thread"))
									continue;
								if (e.getClassName().startsWith(p)) {
									continue;
								}
								sb.append("\nFired by: ");
								sb.append(e);
								break;
							}
							LOGGER.log(Logs.DEBUG, sb.toString());
						}
						
						valid = false;
						break;
					}
					
				}
				
				// On a trouv�
				if (valid) {
					m = n;
					break;
				}

			}
			
			// Technique 2
			//
			// On recherche une m�thode ayant le nom donn� et qui a une String et
			// un Object[] en argument : comme notifyEvent() et IListener.
			//
			// NOTE : c'est couteux et c'est pas utile normalement car on utilise
			// plutot la InterfaceTriggerable.
			//
			/*if (m == null) {
				try {
					
					// Recherche de la m�thode par rapport � sa signature
					m = listener.getClass().getMethod(methodName, new Class<?>[] {
						String.class,
						Object[].class
					});
					
					// Copie des arguments, en ajoutant le nom de l'event en premier
					args = new Object[] { event, args };
					
				} catch (Throwable ex) { }
			}*/
			
			// Aucune callback trouv�
			if (m == null) {
				if (LOGGER.isLoggable(Logs.ERROR)) {
					StringBuilder sb = new StringBuilder();
					sb.append("Method ");
					sb.append(methodName);
					sb.append("(");
					for (int i = 0, l = args.length; i < l; i++) {
						if (i > 0) sb.append(", ");
						sb.append(args[i] == null ? "NULL" : args[i].getClass().getSimpleName());
					}
					sb.append(") not found on ");
					sb.append(listener.toString());
					sb.append(" (found ");
					sb.append(count);
					sb.append(" method(s) with this name)");
					LOGGER.log(Logs.ERROR, sb.toString());
				}
				// On laisse continuer la propagation.
				return true;
			}
			
			// On adapte le nombre d'argument � la m�thode
			int length = m.getParameterTypes().length;
			if (length != args.length) {
				Object[] copy = new Object[length];
				for (int i = 0; i < length; i++) {
					copy[i] = args[i];
				}
				args = copy;
				copy = null;
			}
			
			// Pour permetre l'invocation d'une m�thode dans une classe anonyme
			m.setAccessible(true);
			
			// Si la m�thode renvoie une boolean, on s'en sert pour le retour
			if (m.getReturnType().toString().equals("boolean") || m.getReturnType() == Boolean.class) {
				return (Boolean) m.invoke(listener, args);
			}
			// Sinon on fait juste l'invocation, et on renvera TRUE � la fin
			else {
				m.invoke(listener, args);
				return true;
			}
		
		}
		
		@Override
		public IListener<E> getListener() {
			return listener;
		}
		
		@Override
		public String toString() {
			return listener.getClass().getSimpleName() + "." + methodName;
		}
		
	}

	@Override
	public void redirect(E event, IObservable<E> target) {
		redirect(event, target, IObservable.DEFAULT_PRIORITY);
	}

	@Override
	public void redirect(E event, final IObservable<E> target, Integer priority) {
		
		// On bind un listener qui va rediriger les events
		bind(
			event,
			new IListener<E>() {
				@Override
				public boolean notifyEvent(E event, Object... args) {

					// On redirige l'event sur la cible
					return target.events().trigger(event, args);
					
				}
			},
			priority
		);
		
	}

	@Override
	public void redirect(IObservable<E> target) {
		
		// Log
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			if (target != null) {
				LOGGER.log(Logs.DEBUG, "REDIRECT " + this + " -> "+ target.events());
			}
			else {
				LOGGER.log(Logs.DEBUG, "UNREDIRECT " + this);
			}
		}
		
		// On enregistre la cible de la redirection
		_redirect = target;
		
	}
	
	public String toString() {
		return "EventDispatcher["
			+ (_sender == null ? "?" : _sender.getClass().getSimpleName()) + "]";
	}
	
}