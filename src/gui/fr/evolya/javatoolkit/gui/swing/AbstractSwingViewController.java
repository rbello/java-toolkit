package fr.evolya.javatoolkit.gui.swing;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.appstandard.events.ViewControllerListenerAdapter;

public abstract class AbstractSwingViewController<V extends JFrame>
	extends AppViewController<ProxyFrameView<V>, V, AWTEvent, Component> {

	public AbstractSwingViewController() {
		super();
	}

	public AbstractSwingViewController(App app, ProxyFrameView<V> view) {
		super(app, view);
	}

	public AbstractSwingViewController(App app) {
		super(app);
	}
	
	/**
	 * A la demande de fermeture, pouvant venir de plusieurs vues
	 */
//	@Override
//	public synchronized void onViewCloseIntent(AppView<?, ?, ?> view, Object event, Boolean isUserIntent) {
//
//		// Si on est en cours de construction, c'est une erreur
//		if (_working != null && _working == true) {
//			throw new SecurityException("can't close during building");
//		}
//		
//		// Si on est déjà en train de le faire, on ne fait rien
//		if (_working != null && _working == false) {
//			return;
//		}
//		
//    	// On ne garde que les events qui viennent de la vue du controlleur.
//		// Pour les proxy, on compare aussi à la vue interne.
//		if (_view instanceof ProxyFrameView) {
//			if (view != ((ProxyFrameView<?>) _view).getFrameView()
//					&& view != _view) {
//				return;
//			}
//		}
//		// Pour les vues normales
//		else {
//	    	if (view != _view) {
//	    		return;
//	    	}
//		}
//
//    	// Event before
//    	if (!getEventsViewCtrl().trigger("beforeViewCloseIntent", this, isUserIntent)) {
//    		return;
//    	}
//    
//    	// Destruction interne
//    	onViewClosed();
//    	
//		// On masque la vue
//    	_view.setVisible(false);
//		
//		// On détruit la vue
//    	_view.dispose();
//    	_view = null;
//    	
//		// Event after
//		getEventsViewCtrl().trigger("afterViewCloseIntent", this, isUserIntent);
//    	
//	}

	/**
	 * Demande la création de la vue, en asynchrone.
	 * 
	 * @return TRUE Si la vue est en cours de création, ou FALSE
	 * si elle est déjé créée
	 */
//	public synchronized boolean buildView(final Boolean setVisible) {
//		
//		// La vue existe déjé
//		if (_view != null) {
//			return false;
//		}
//		
//		// Un travail de méme nature est en cours
//		if (_working != null && _working == true) {
//			return true;
//		}
//		
//		// Un travail de nature différente est en cours
//		if (_working != null && _working == false) {
//			throw new SecurityException("Can't build during dispose");
//		}
//
//		// Création de l'IHM dans le pool GUI
//		SwingUtilities.invokeLater(new Runnable() {
//			
//			public void run() {
//        		
//            	// Construction de la vue
//            	final AppView<W, AWTEvent, Component> view = constructView();
//
//            	// Association
//        		setView(view, setVisible);
//            	
//            }
//            
//        });
//		
//		return true;
//		
//	}
	
	public boolean enableHideToSystemTray(final TrayIcon trayIcon) {
		
		if (_view == null || !SystemTray.isSupported()){
			return false;
		}
		
		final V frame = _view.getFrameView();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		final SystemTray tray = SystemTray.getSystemTray();
		
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.setExtendedState(JFrame.NORMAL);
				frame.setVisible(true);
				tray.remove(trayIcon);
			}
		});
        
		
		getEventsViewCtrl().bind(new ViewControllerListenerAdapter() {
			@Override
			public boolean beforeViewCloseIntent(AppViewController ctrl, boolean isUserIntent) {

				if (!isUserIntent) return true;
				
				// On place l'icône
				try {
					tray.add(trayIcon);
				} catch (AWTException e) {
					// En cas d'erreur on laisse la fermeture se faire
					return true;
				}
				
				frame.setVisible(false);
				frame.setExtendedState(JFrame.ICONIFIED);
				
				// On arrête la fermeture
				return false;
			}
		});
        

        return true;
	}
	
	
	
}
