package fr.evolya.javatoolkit.appstandard;

import fr.evolya.javatoolkit.app.config.AppConfiguration;

@Deprecated
public interface Configurable {

	/**
	 * Renvoie la configuration actuelle de l'objet.
	 */
	public AppConfiguration getConfig();

	/**
	 * Indiquer le chemin vers le fichier qui sera utilisé pour lire
	 * ou sauvegarder la configuration.
	 */
	public void setConfigStorage(String filename);

}
