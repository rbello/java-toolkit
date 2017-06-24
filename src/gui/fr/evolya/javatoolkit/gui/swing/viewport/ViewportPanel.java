package fr.evolya.javatoolkit.gui.swing.viewport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;
import fr.evolya.javatoolkit.gui.swing.JPanelView;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;
import fr.evolya.javatoolkit.gui.swing.viewport.layers.Layer;
import fr.evolya.javatoolkit.gui.swing.viewport.layers.LayerPanelListener;

public class ViewportPanel extends JPanelView {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Events de la vue.
	 */
	private EventSource<LayerPanelListener> _eventsView = new EventSource<LayerPanelListener>(this) {
		public Class<LayerPanelListener> getListenerClass() {
			return LayerPanelListener.class;
		}
	};
	
	/**
	 * Le ScrollPane qui contient le LayeredPane.
	 */
	private JScrollPane _scrollPane;
	
	/**
	 * Le LayeredPane qui sert à afficher les calques.
	 */
	private JLayeredPane _layeredPane;
	
	/**
	 * Indique si l'on se trouve en mode debug. Le mode debug permet d'afficher
	 * des informations directement sur le repaint, comme la PreferedBounds. 
	 */
	public Color border = null;

	/**
	 * Le manager du viewport.
	 */
	private ViewportManager _viewportMgr;
	
	/**
	 * Constructeur du panel.
	 */
	public ViewportPanel() {
		
		// On change le layout par défaut
		setLayout(new BorderLayout(0, 0));
		
		// On fabrique le LayeredPane.
		if (border != null) {
			_layeredPane = new JLayeredPane() {
				private static final long serialVersionUID = 1L;
				public void paint(Graphics g) {
					super.paint(g);
					
					Rectangle preferedBounds = _viewportMgr.getPreferredBounds(false);
					
					g.setColor(border);
					g.drawLine(
							(int)preferedBounds.getX(),
							(int)preferedBounds.getY(),
							(int)(preferedBounds.getX() + preferedBounds.getWidth()),
							(int)preferedBounds.getY()
					);
					g.drawLine(
							(int)(preferedBounds.getX() + preferedBounds.getWidth()),
							(int)preferedBounds.getY(),
							(int)(preferedBounds.getX() + preferedBounds.getWidth()),
							(int)(preferedBounds.getY() + preferedBounds.getHeight())
					);
					g.drawLine(
							(int)(preferedBounds.getX() + preferedBounds.getWidth()),
							(int)(preferedBounds.getY() + preferedBounds.getHeight()),
							(int)preferedBounds.getX(),
							(int)(preferedBounds.getY() + preferedBounds.getHeight())
					);
					g.drawLine(
							(int)preferedBounds.getX(),
							(int)(preferedBounds.getY() + preferedBounds.getHeight()),
							(int)preferedBounds.getX(),
							(int)preferedBounds.getY()
					);
				}
			};
		}
		else {
			_layeredPane = new JLayeredPane();
		}
		
		// On fabrique le ScrollPane
		_scrollPane = new JScrollPane(_layeredPane);
		_scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		_scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		// On fabrique le manager de viewport de ce panel
		_viewportMgr = new ViewportManager(this);

		// On assemble la vue
		add(_scrollPane);
		
	}
	
	/**
	 * Renvoie l'objet de viewport de ce composant.
	 */
	public JViewport getViewport() {
		return _scrollPane.getViewport();
	}

	@Override
	public void dispose() {
		removeAll();
		_scrollPane = null;
		_layeredPane = null;
		if (_viewportMgr != null) {
			_viewportMgr.dispose();
			_viewportMgr = null;
		}
	}

	@Override
	public EventSource<LayerPanelListener> getEventsView() {
		return _eventsView;
	}

	@Override
	public ViewBounds getViewBounds() {
		return SwingHelper.bounds(getBounds());
	}

	@Override
	public void setViewBounds(ViewBounds bounds) {
		setBounds(SwingHelper.bounds(bounds));
	}
	
	/**
	 * Ajouter un calque dans le ViewportPanel, au z-index par d�faut.
	 */
	public void addLayer(Layer layer) {
		addLayer(layer, JLayeredPane.PALETTE_LAYER);
	}

	/**
	 * Ajouter un calque dans le ViewportPanel, au z-index donné.
	 */
	public void addLayer(Layer layer, int zindex) {
		
		// On redimensionne en fonction du zoom
		//layer.setBounds(_viewportMgr.getResizedBoundsOf(layer));
		
		// On ajoute le calque dans le LayeredPane
		_layeredPane.add(layer, new Integer(zindex), 0);

	}
	
	/**
	 * Surcharge pour provoquer le nettoyage du LayeredPane à la place
	 * de ce panel.
	 */
	@Override
	public void removeAll() {
		removeAllLayers();
	}
	
	/**
	 * Renvoie le manager de viewport.
	 */
	public ViewportManager getViewportManager() {
		return _viewportMgr;
	}
	
	/**
	 * Modifie le manager de viewport.
	 */
	public void setViewportManager(ViewportManager manager) {
		
		// Méfiance...
		if (manager == null) {
			throw new NullPointerException("Given viewport manager is null");
		}
		
		// On retire l'ancien si besoin
		if (_viewportMgr != null) {
			_viewportMgr.dispose();
		}
		
		// On ajoute le nouveau
		_viewportMgr = manager;
		
	}

	/**
	 * Retirer tous les calques
	 */
	public void removeAllLayers() {
		_layeredPane.removeAll();
	}

	public void setZoomFactor(double factor) {
		_viewportMgr.setZoomFactor(factor);
	}

	public void setZoomAuto() {
		_viewportMgr.setZoomAuto();
	}

	public JLayeredPane getLayeredPane() {
		return _layeredPane;
	}
	
	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	public void removeLayer(Layer layer) {
		_layeredPane.remove(layer);
	}

	public double getZoomAuto() {
		return _viewportMgr.getZoomAuto();
	}
	
}
