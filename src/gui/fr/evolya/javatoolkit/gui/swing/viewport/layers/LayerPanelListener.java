package fr.evolya.javatoolkit.gui.swing.viewport.layers;

import java.awt.AWTEvent;

import fr.evolya.javatoolkit.appstandard.events.ViewListener;
import fr.evolya.javatoolkit.gui.swing.viewport.Cartesian;
import fr.evolya.javatoolkit.gui.swing.viewport.Cartographic;

public interface LayerPanelListener extends ViewListener<AWTEvent> {

	/**
	 * Event levé quand le facteur de zoom change.
	 * 
	 * Le facteur est un nombre compris entre [0;+∞[
	 * 
	 * @param newValue
	 * @param oldValue
	 */
	public void onZoomFactorChanged(double newValue, double oldValue);
	
	/**
	 * Lors du déplacement du curseur sur le calque de cartographie. 
	 * 
	 * @param layer
	 * @param ca
	 * @param cg
	 */
	public void onImageryLayerMouseMotion(Imagery2DLayer layer, Cartesian ca, Cartographic cg);
	
}
