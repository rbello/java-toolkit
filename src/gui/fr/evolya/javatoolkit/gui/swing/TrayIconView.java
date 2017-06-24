package fr.evolya.javatoolkit.gui.swing;

import java.awt.AWTEvent;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.appstandard.events.ViewListener;
import fr.evolya.javatoolkit.events.alpha.EventDispatcher;
import fr.evolya.javatoolkit.events.alpha.IEventDispatcher;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;

public class TrayIconView implements AppView<TrayIcon, AWTEvent, Void> {

	protected TrayIcon  _icon;
	protected PopupMenu _menu;
	
	protected boolean _visible = false;
	
	protected TrayIconView _view;
	
	protected IEventDispatcher<String> _events = new EventDispatcher<String>();
	private AppViewController<?, TrayIcon, AWTEvent, Void> _ctrl;
	
	public TrayIconView(Image icon) {
		_menu = new PopupMenu();
		_icon = new TrayIcon(icon);
		_icon.setPopupMenu(_menu);
	}

	public PopupMenu getMenu() {
		return _menu;
	}
	
	@Override
	public void dispose() {
		setVisible(false);
		_menu = null;
		_icon = null;
	}
	
	/**
	 * Indique si le syst�me de tray icons est support� sur l'OS.
	 */
	public boolean isTrayIconSupported() {
		return SystemTray.isSupported();
	}

	//@Override
	public synchronized boolean isVisible() {
		return _visible;
	}

	//@Override
	public synchronized void setVisible(boolean visible) {
		// Show
		if (visible) {
			if (_visible) return;
			try {
				SystemTray.getSystemTray().add(_icon);
				_visible = true;
			}
			catch (Throwable ex) { }
		}
		// Hide
		else {
			if (!_visible) return;
			try {
				SystemTray.getSystemTray().remove(_icon);
				_visible = false;
			}
			catch (Throwable ex) { }
		}
	}

	@Override
	public EventSource<? extends ViewListener<AWTEvent>> getEventsView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ViewBounds getViewBounds() {
		return null;
	}

	@Override
	public void setViewBounds(ViewBounds viewBounds) {
	}

	@Override
	public Void[] getComponents() {
		return null;
	}

	@Override
	public AppViewController<?, TrayIcon, AWTEvent, Void> getViewController() {
		return _ctrl;
	}

	@Override
	public void setViewController(AppViewController<?, TrayIcon, AWTEvent, Void> ctrl) {
		_ctrl = ctrl;
	}
	
}
