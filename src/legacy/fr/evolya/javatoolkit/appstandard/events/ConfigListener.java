package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.app.config.Configurable;
import fr.evolya.javatoolkit.events.attr.EventListener;

public interface ConfigListener extends EventListener {

	/**
	 * Quand un fichier .properties a été lu et chargé.
	 */
	public void onConfigurationRestored(AppConfiguration config, Configurable target, String filename, boolean success);
	
	/**
	 * Quand la configuration est disponible en lecture comme en écriture,
	 * même si aucun fichier n'a été chargé.
	 */
	public void onConfigurationReady(AppConfiguration config, Configurable target);
	
	/**
	 * Avant l'enregistrement de la configuration dans un fichier.
	 */
	public boolean beforeConfigurationSaved(AppConfiguration config, Configurable target, String filename);
	
	/**
	 * Aprés l'enregistrement dans le fichier.
	 */
	public void afterConfigurationSaved(AppConfiguration config, Configurable target, String filename);

}
