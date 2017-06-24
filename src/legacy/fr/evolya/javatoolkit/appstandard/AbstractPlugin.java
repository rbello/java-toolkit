package fr.evolya.javatoolkit.appstandard;

import fr.evolya.javatoolkit.app.AppActivity;
import fr.evolya.javatoolkit.appstandard.events.PluginListener;
import fr.evolya.javatoolkit.code.annotations.ToOverride;
import fr.evolya.javatoolkit.events.attr.EventSource;

/**
 * Classe adapter pour la création de nouveaux plugins d'applications.
 * 
 * Cette classe gere la création du dispatcher d'event, et la mmorisation
 * de l'application assocée au plugin.
 */
@Deprecated
public abstract class AbstractPlugin implements AppPlugin {
	
	/**
	 * L'application
	 */
	private App _invoker;
	
	/**
	 * Events des plugins
	 */
	private EventSource<PluginListener> _eventsPlugin = new EventSource<PluginListener>(this) {
		@Override
		public Class<PluginListener> getListenerClass() {
			return PluginListener.class;
		}
	};
	
	/**
	 * Supprimer cet objet
	 */
	@Override
	public synchronized void dispose() {
		_invoker = null;
		_eventsPlugin.dispose();
	}
	
	/**
	 * Connexion du plugin avec l'application
	 */
	@Override
	public final void setApplication(App app) {
		_invoker = app;
		connected(app);
		_eventsPlugin.trigger("onPluginConnected", this, app);
	}

	/**
	 * Pour notifier aux classes enfantes que le la connexion avec l'application
	 * a eu lieu. C'est le moment de se binder aux events.
	 */
	@ToOverride
	protected abstract void connected(App app);

	/**
	 * Récupèrer l'application associée à ce plugin
	 */
	@Override
	public final App getApplication() {
		return _invoker;
	}
	
	@SuppressWarnings("unchecked")
	protected final <A extends App> A getApplication(Class<A> classe) {
		return (A) getApplication();
	}
	
	public final AppActivity getInvoker() {
		return _invoker;
	}
	
	@Override
	public EventSource<PluginListener> getEventsPlugin() {
		return _eventsPlugin;
	}

}
