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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.appstandard.events.ViewListener;
import fr.evolya.javatoolkit.appstandard.events.WindowViewListener;
import fr.evolya.javatoolkit.events.attr.EventListener;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;

public class ProxyFrameView<F extends JFrame> implements AppView<F, AWTEvent, Component> {

	private F _view;
	
	private EventSource<WindowViewListener<AWTEvent>> _eventsView;

	private AppViewController<?, F, AWTEvent, Component> _ctrl;
	
	/**
	 * TODO Extraire les méthodes de addListener dans une méthode bind(),
	 * et faire l'unbind() qui va bien
	 */
	public ProxyFrameView(F frame) {
		
		// On conserve la référence vers la fenêtre 
		_view = frame;
		
		// On override le comportement par defaut pour la fermeture
		frame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		
		// Si la frame est déjà une vue, on va copier la référence vers
		// sa source d'event.
		if (frame instanceof AppView) {
			_eventsView = ((AppView) frame).getEventsView();
			// On ne bind pas les events
			return;
		}
		
		// Si non, on fabrique une source d'event
		else {
			_eventsView = new EventSource<WindowViewListener<AWTEvent>>(this) {
				@Override
				public Class<? extends EventListener> getListenerClass() {
					return WindowViewListener.class;
				}
			};
		}

		// On va avoir besoin de ça
		final ProxyFrameView<F> thiz = this;
		
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
		
		// WindowListener
		
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent e) {
				_eventsView.trigger("onViewActivated", thiz, e);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				_eventsView.trigger("onViewClosed", thiz, e);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				//_eventsView.trigger("onViewClosing", thiz, e);
				// TODO Rendre le close annulable
				_eventsView.trigger("onViewCloseIntent", thiz, e, true);
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				_eventsView.trigger("onViewDeactivated", thiz, e);
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				_eventsView.trigger("onViewDeiconified", thiz, e);
			}

			@Override
			public void windowIconified(WindowEvent e) {
				_eventsView.trigger("onViewIconified", thiz, e);
			}

			@Override
			public void windowOpened(WindowEvent e) {
				_eventsView.trigger("onViewOpened", thiz, e);
			}
			
		});
		
		// WindowStateListener
		
		frame.addWindowStateListener(new WindowStateListener() {
			
			@Override
			public void windowStateChanged(WindowEvent e) {
				_eventsView.trigger("onViewStateChanged", thiz, e);
			}
			
		});
		
	}

	@Override
	public void dispose() {
		
		if (_view == null) {
			return;
		}
		
		_view.dispose();
		
		// Comme on est bindé aux events de la JFrame, on delete
		// le systeme d'event interne dans la queue Swing
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				//_eventsView.setEnabled(false);
				_eventsView.dispose();
				//_eventsView = null;
				_view = null;
			}
		});
		
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
	
	public F getFrameView() {
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
	public AppViewController<?, F, AWTEvent, Component> getViewController() {
		return _ctrl;
	}

	@Override
	public void setViewController(AppViewController<?, F, AWTEvent, Component> ctrl) {
		_ctrl = ctrl;
	}
	
}