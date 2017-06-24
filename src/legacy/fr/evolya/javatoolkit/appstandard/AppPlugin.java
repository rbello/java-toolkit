package fr.evolya.javatoolkit.appstandard;

import fr.evolya.javatoolkit.appstandard.events.PluginListener;
import fr.evolya.javatoolkit.events.attr.EventSource;

/**
 * Interface pour les plugins d'applications.
 *
 * Un plugin est un terme générique pour désigner un composant d'une
 * application. Il peut s'agir d'une IHM, d'un service, d'un webservice, etc..
 * 
 * L'application se charge ensuite d'initialiser les plugins (en appelant la
 * méthode setApplication) et c'est ensuite aux plugins de se connecter aux
 * events qui les interessent.
 */
@Deprecated
public interface AppPlugin {

	/**
	 * Initialiser le plugin.
	 * 
	 * @param app L'application parente
	 */
	public void setApplication(App app);
	
	/**
	 * Obtenir l'application associée.
	 * Normalement cette méthode renvoie toujours une application si
	 * la méthode setApplication() a été lancée avant.
	 * Mais comme elle est librement implémentable, elle peut éventuellement
	 * renvoyer NULL. 
	 */
	public App getApplication();
	
	/**
	 * Supprimer le plugin, et tous ses composants.
	 */
	public void dispose();
	
	/**
	 * Bus pour les events des plugins.
	 */
	public EventSource<? extends PluginListener> getEventsPlugin();

}
