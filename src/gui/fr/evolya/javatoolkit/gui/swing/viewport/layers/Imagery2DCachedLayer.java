package fr.evolya.javatoolkit.gui.swing.viewport.layers;

import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

public class Imagery2DCachedLayer extends Imagery2DLayer {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Surcharge pour afficher l'image.
	 */
	@Override
    public void paintComponent(Graphics g) {
    	if (_cacheImg != null) {
    		g.drawImage(_cacheImg, 0, 0, null);
    	}
    }
	
	/**
	 * Surcharge pour empecher de lancer dans l'EDT.
	 */
	@Override
	public void setImage(File file) throws IOException {
		
		// Cette opération ne doit pas être faite dans l'EDT
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IllegalAccessError("You can't run Imagery2DCachedLayer.setImage() in UI dispatch thread");
		}
		
		super.setImage(file);
		
	}

	/**
	 * Permet de demander à l'image de rafraichir son affichage.
	 * Cette opération ne doit pas être faite dans l'EDT.
	 */
	public void refresh() {
	
		// Cette opération ne doit pas être faite dans l'EDT
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IllegalAccessError("You can't run Imagery2DCachedLayer.refresh() in UI dispatch thread");
		}

		// On ne fait rien si aucune image n'est chargée
		if (_img == null) {
			return;
		}
		
		// On met à jour l'image du cache
		_cacheImg = getResizedImage();
		
	}
	
}
