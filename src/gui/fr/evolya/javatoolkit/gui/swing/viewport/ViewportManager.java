package fr.evolya.javatoolkit.gui.swing.viewport;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

import fr.evolya.javatoolkit.gui.swing.viewport.layers.Layer;

/**
 * Manager de viewport, gérant principalement le Zoom et le viewport.
 */
public class ViewportManager implements ContainerListener {
	
	/**
	 * Facteur de zoom.
	 * Compris entre ]0;1+maxZoomInRatio]
	 */
	protected double _zoomFactor = 1;

	/**
	 * Le content panel.
	 */
	protected ViewportPanel _viewportPanel;

	/**
	 * Le layered pane.
	 */
	protected JLayeredPane _layeredPane;

	/**
	 * Le scroll pane.
	 */
	protected JScrollPane _scrollPane;
	
	/**
	 * Les bornes d'affichage du LayeredPane.
	 */
	protected Rectangle _preferedBounds = new Rectangle(0, 0, 0, 0);

	/**
	 * Constructeur.
	 */
	public ViewportManager(ViewportPanel viewport) {

		// On conserve les références
		_viewportPanel = viewport;
		_layeredPane = viewport.getLayeredPane();
		_scrollPane = viewport.getScrollPane();
		
		// Si on ajoute ou retire des composants du ViewportPanel, on provoque
		// le recalcule des bounds des calques.
		_layeredPane.addContainerListener(this);
		
	}
	
	/**
	 * Permet de redimensionner automatiquement les calques pour qu'ils occupent
	 * le maximum de place dans le viewport sans en dépasser.
	 */
	public void setZoomAuto() {
		
		// On calcule le facteur de zoom
		double factor = getZoomAuto();
		
		// Si il y a au moins un calque d'au moins 1 pixel
		if (factor != -1) {
			setZoomFactor(factor);
		}

	}
	
	public double getZoomAuto() {
		
		// On recupère les dimensions initiales du LayeredPane
		Rectangle bounds = getPreferredBounds(true);
		
		// S'il n'y a aucun calque pris en compte, on ne fait rien
		if (bounds.isEmpty()) {
			return -1;
		}

		// On calcule le facteur de zoom
		// TODO On n'utilise ici que le ratio largeur/largeur, on ne prends pas en
		// compte la limite en hauteur par rapport à scrollPane.getHeight() 
		return (_scrollPane.getWidth() - _scrollPane.getVerticalScrollBar().getWidth() - 1) / bounds.getWidth();
		
	}
	
	public void setZoomFactor(double factor) {
		setZoomFactor(factor, true);
	}
	
	/**
	 * Modifie le facteur de zoom de ce ViewportPanel.
	 * 
	 * La valeur doit être comprise entre -1 et 1.
	 */
	public void setZoomFactor(double factor, boolean refresh) {
		
		// Méfiance...
		if (factor <= 0) {
			throw new IllegalArgumentException();
		}
		
		// Inutile
		if (factor == _zoomFactor) {
			return;
		}

		// On conserve une copie de l'ancienne valeur
		double copy = _zoomFactor;
		
		// On sauvegarde le facteur de zoom
		_zoomFactor = factor;
		
		if (refresh) {
		
			// Et on provoque un repaint
			//System.out.println("Refresh for set zoom factor (" + factor + ")");
			refreshAll();
			
			// On trigger un event
			_viewportPanel.getEventsView().trigger("onZoomFactorChanged", _zoomFactor, copy);
			
		}
		
	}
	
	public double getZoomFactor() {
		return _zoomFactor;
	}
	
	/**
	 * Méthode pour redimensionner automatiquement tous les calques
	 * et le LayeredPane.
	 */
	public void refreshAll() {
		
		// Les calques en premier
		refreshLayers();
		
		// Puis le container
		refreshLayeredPane();
		
	}
	
	/**
	 * Met à jour les bounds des calques en fonction du facteur de zoom.
	 */
	public void refreshLayers() {

		// On parcours les composants qui sont dans le LayeredPane.
		for (Component component : _layeredPane.getComponents()) {
			
			// On ne va s'occuper que des composants qui sont bien des calques
			if (!(component instanceof Layer)) {
				continue;
			}
			
			// On recupère le vrai type
			Layer layer = (Layer) component;
			
			// On redimensionne les bounds du calque
			layer.setBounds(getResizedBoundsOf(layer));
			
		}
	}
	
	/**
	 * Met à jour les bounds du LayeredPane. Cette méthode doit être appelée
	 * après déplacement des calques.
	 */
	public void refreshLayeredPane() {
		
		// On recupère les dimensions totales de tous les calques maintenant
		// qu'ils ont été redimensionnés.
		_preferedBounds = getPreferredBounds(false);
		
		// On redimensionne le LayeredPane pour tout afficher.
		_layeredPane.setLocation(_preferedBounds.getLocation());
		_layeredPane.setPreferredSize(_preferedBounds.getSize());
		_layeredPane.setSize(_preferedBounds.getSize());
		
	}
	
	/**
	 * Renvoie un rectangle couvrant tout l'espace occupé par les calques.
	 * 
	 * Cette m�thode fabrique un rectangle vide, parcours les calques,
	 * ne conserve que ceux qui implémentent le type Layer, et ajoute
	 * les bounds des calques au rectangle. 
	 * 
	 * @param original Si original vaut TRUE, alors les bornes initiales des calques seront
	 * prises en compte, ce qui signifie que le zoomFactor ne sera pas pris
	 * en compte. Par contre, si original vaut FALSE, alors la mèthode utilise
	 * la getBounds() sur les calques.
	 */
	public Rectangle getPreferredBounds(boolean original) {
		
		// On prépare un rectangle qui sera la somme de toutes
		// les bornes des calques
		Rectangle rect = new Rectangle();
		
		// On parcours les calques
		for (Component component : _layeredPane.getComponents()) {
			
			// On ne conserve que les calques valides
			if (!(component instanceof Layer)) continue;
			
			// On recupère le bon type
			Layer layer = (Layer) component;
			
			// Et on étend le rectangle, à partir des dimensions originales
			// ou des dimensions actuelles.
			rect.add(original ? layer.getOriginalBounds() : layer.getBounds());
			
		}
		
		// On renvoie le rectangle
		return rect;
		
	}
	
	public Rectangle getResizedBoundsOf(Layer layer) {
		
		// On recupére la dimension initiale du calque, avant que celle-ci
		// ne soit modifiée par le zoomFactor.
		Rectangle bounds = layer.getOriginalBounds();
		
		// On calcule les nouvelles dimensions
		double x = bounds.getX() * _zoomFactor;
		double y = bounds.getY() * _zoomFactor;
		double width = bounds.getWidth() * _zoomFactor;
		double height = bounds.getHeight() * _zoomFactor;
		
		// On renvoie un nouveau rectangle
		return new Rectangle((int)x, (int)y, (int)width, (int)height);
		
	}
	
	@Override
	public void componentAdded(ContainerEvent e) {
		//System.out.println("Refresh for added (" + e.getComponent() + ")");
		refreshAll();
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		//System.out.println("Refresh for removed (" + e.getComponent() + ")");
		refreshAll();
	}
	
	public synchronized void dispose() {
		if (_viewportPanel != null) {
			_layeredPane.removeContainerListener(this);
			_layeredPane = null;
			_preferedBounds = null;
			_scrollPane = null;
			_viewportPanel = null;
		}
	}
    
	
	///
	///////   Outils image
	///
	
	/**
	 * Crop une image.
	 */
	public static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
		return src.getSubimage(rect.x, rect.y, rect.width, rect.height);
	}
	
	/**
	 * Redimensionne une image.
	 * 
	 * Note: est-il possible de caster le retour en BufferedImage ?
	 */
    public static Image resizeImage(BufferedImage src, int width, int height) {
    	return src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
	
	/**
	 * Effectuer une copie d'image.
	 */
    public static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	/**
	 * Effectuer une copie d'image en redimensionnant.
	 */
	public static BufferedImage deepCopy(Image img, int w, int h) {
		BufferedImage copyOfImage =  new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics g = copyOfImage.createGraphics();
		g.drawImage(img, 0, 0, null);
		return copyOfImage;
	}
	
}