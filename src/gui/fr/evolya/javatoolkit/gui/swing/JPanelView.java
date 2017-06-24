package fr.evolya.javatoolkit.gui.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.appstandard.events.ContainerViewListener;
import fr.evolya.javatoolkit.appstandard.events.ViewListener;
import fr.evolya.javatoolkit.events.attr.EventListener;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;

/**
 * Classe permettant d'intégrer simplement un JPanel dans une application Inca.
 * Cette class fait l'adaptation entre les deux, et bind automatiquement
 * les events principaux.
 */
public class JPanelView extends JPanel implements AppView<JPanel, AWTEvent, Component> {

	public static final long serialVersionUID = 1L;
	
	private EventSource<ContainerViewListener<AWTEvent>> _eventsView = new EventSource<ContainerViewListener<AWTEvent>>(this) {
		@Override
		public Class<? extends EventListener> getListenerClass() {
			return ContainerViewListener.class;
		}
	};

	private AppViewController<?, JPanel, AWTEvent, Component> _controller;
	
	@Override
	public AppViewController<?, JPanel, AWTEvent, Component> getViewController() {
		return _controller;
	}
	
	@Override
	public void setViewController(AppViewController<?, JPanel, AWTEvent, Component> ctrl) {
		_controller = ctrl;
	}

	public JPanelView() {
		super();
		init();
	}
	
	public JPanelView(LayoutManager layout) {
		super(layout);
		init();
	}
	
	public JPanelView(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		init();
	}
	
	public JPanelView(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		init();
	}
	
	private void init() {
		
		// Component
		
		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
				_eventsView.trigger("onViewComponentHidden", JPanelView.this, e);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				_eventsView.trigger("onViewComponentMoved", JPanelView.this, e);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				_eventsView.trigger("onViewComponentResized", JPanelView.this, e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				_eventsView.trigger("onViewComponentShown", JPanelView.this, e);
			}
			
		});
		
		// Container
		
		this.addContainerListener(new ContainerListener() {

			@Override
			public void componentAdded(ContainerEvent e) {
				_eventsView.trigger("onViewComponentAdded", JPanelView.this, e);
			}

			@Override
			public void componentRemoved(ContainerEvent e) {
				_eventsView.trigger("onViewComponentRemoved", JPanelView.this, e);
			}
			
		});
		
		// Focus
		
		this.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				_eventsView.trigger("onViewFocusGained", JPanelView.this, e);
			}

			@Override
			public void focusLost(FocusEvent e) {
				_eventsView.trigger("onViewFocusLost", JPanelView.this, e);
			}
			
		});
		
	}

	@Override
	public ViewBounds getViewBounds() {
		Rectangle r = getBounds();
		return new ViewBounds(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	
	@Override
	public void setViewBounds(ViewBounds bounds) {
		if (bounds == null) {
			throw new NullPointerException("bounds is null");
		}
		setBounds(new Rectangle(
			(int)bounds.getX(),
			(int)bounds.getY(),
			(int)bounds.getWidth(),
			(int)bounds.getHeight())
		);
	}
	
	@Override
	public EventSource<? extends ViewListener<AWTEvent>> getEventsView() {
		return _eventsView;
	}

	@Override
	public void dispose() {
		
		// Comme on est bindé aux events de du JPanel, on delete
		// le systeme d'event interne dans la queue Swing
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_eventsView.dispose();
				_eventsView = null;
			}
		});
	}

}
