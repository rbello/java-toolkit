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
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;

/**
 * Classe permettant d'intégrer simplement un JFrame dans une application Inca.
 * Cette class fait l'adaptation entre les deux, et bind automatiquement
 * les events principaux.
 */
public class JFrameView extends JFrame implements AppView<JFrame, AWTEvent, Component> {

	public static final long serialVersionUID = 1L;
	
	private EventSource<WindowViewListener<AWTEvent>> _eventsView = 
			new EventSource<WindowViewListener<AWTEvent>>(WindowViewListener.class, this);

	private AppViewController<?, JFrame, AWTEvent, Component> _controller;
	
	@Override
	public AppViewController<?, JFrame, AWTEvent, Component> getViewController() {
		return _controller;
	}
	
	@Override
	public void setViewController(
			AppViewController<?, JFrame, AWTEvent, Component> ctrl) {
		_controller = ctrl;
	}
	
	public JFrameView() {

		final JFrameView thiz = this;
		
		// Comportement par défaut
		setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
		
		// Component
		
		this.addComponentListener(new ComponentListener() {

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
		
		this.addContainerListener(new ContainerListener() {

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
		
		this.addFocusListener(new FocusListener() {

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
		
		this.addWindowListener(new WindowListener() {

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
//				System.out.println("trigger onViewCloseIntent on " + _eventsView + " " + _eventsView.hashCode());
//				for (EventCallback<WindowViewListener<AWTEvent>> a : _eventsView.getListeners()) {
//					System.out.println(" " + a);
//				}
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
		
		this.addWindowStateListener(new WindowStateListener() {
			
			@Override
			public void windowStateChanged(WindowEvent e) {
				_eventsView.trigger("onViewStateChanged", thiz, e);
			}
			
		});
		
	}

	/**
	 * Le bus des events des vues.
	 */
	public EventSource<? extends ViewListener<AWTEvent>> getEventsView() {
		return _eventsView;
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
	public void dispose() {
		super.dispose();
		
		// Comme on est bindé aux events de la JFrame, on delete
		// le systeme d'event interne dans la queue Swing
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (_eventsView != null) {
					_eventsView.dispose();
					_eventsView = null;
				}
			}
		});
	}

}
