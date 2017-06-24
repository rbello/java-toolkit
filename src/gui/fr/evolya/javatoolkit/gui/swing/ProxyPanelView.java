package fr.evolya.javatoolkit.gui.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;

import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.appstandard.events.ViewListener;
import fr.evolya.javatoolkit.appstandard.events.WindowViewListener;
import fr.evolya.javatoolkit.events.attr.EventListener;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;

public class ProxyPanelView<P extends JPanel> implements AppView<JPanel, AWTEvent, Component> {

	private P _view;
	
	private EventSource<WindowViewListener<AWTEvent>> _eventsView = new EventSource<WindowViewListener<AWTEvent>>(this) {
		@Override
		public Class<? extends EventListener> getListenerClass() {
			return WindowViewListener.class;
		}
	};

	private AppViewController<?, JPanel, AWTEvent, Component> _ctrl;

	/**
	 * TODO Extraire les méthodes de addListener dans une méthode bind(),
	 * et faire l'unbind() qui va bien
	 */
	public ProxyPanelView(P frame) {
		_view = frame;

		final ProxyPanelView<P> thiz = this;
		
		// Component
		
		frame.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
				_eventsView.trigger("onViewComponentHidden", thiz, e);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				_eventsView.trigger("onViewComponentMoved", thiz, e);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				_eventsView.trigger("onViewComponentResized", thiz, e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				_eventsView.trigger("onViewComponentShown", thiz, e);
			}
			
		});
		
		// Container
		
		frame.addContainerListener(new ContainerListener() {

			@Override
			public void componentAdded(ContainerEvent e) {
				_eventsView.trigger("onViewComponentAdded", thiz, e);
			}

			@Override
			public void componentRemoved(ContainerEvent e) {
				_eventsView.trigger("onViewComponentRemoved", thiz, e);
			}
			
		});
		
		// Focus
		
		frame.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				_eventsView.trigger("onViewFocusGained", thiz, e);
			}

			@Override
			public void focusLost(FocusEvent e) {
				_eventsView.trigger("onViewFocusLost", thiz, e);
			}
			
		});
		
	}

	@Override
	public void dispose() {
		
		if (_view == null) {
			return;
		}
		
		_eventsView.dispose();
		_eventsView = null;
		_view = null;
		
	}

	@Override
	public EventSource<? extends ViewListener<AWTEvent>> getEventsView() {
		return _eventsView;
	}

	@Override
	public ViewBounds getViewBounds() {
		final Rectangle r = _view.getBounds();
		return new ViewBounds(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	
	@Override
	public void setViewBounds(ViewBounds bounds) {
		if (bounds == null) {
			throw new NullPointerException("bounds is null");
		}
		_view.setBounds(new Rectangle(
			(int)bounds.getX(),
			(int)bounds.getY(),
			(int)bounds.getWidth(),
			(int)bounds.getHeight())
		);
	}

	@Override
	public boolean isVisible() {
		return _view.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		_view.setVisible(visible);
	}
	
	public P getPanelView() {
		return _view;
	}
	
	@Override
	public String toString() {
		return _view + "";
	}

	@Override
	public Component[] getComponents() {
		return _view.getComponents();
	}

	@Override
	public AppViewController<?, JPanel, AWTEvent, Component> getViewController() {
		return _ctrl;
	}

	@Override
	public void setViewController(AppViewController<?, JPanel, AWTEvent, Component> ctrl) {
		_ctrl = ctrl;
	}
	
	
	
}