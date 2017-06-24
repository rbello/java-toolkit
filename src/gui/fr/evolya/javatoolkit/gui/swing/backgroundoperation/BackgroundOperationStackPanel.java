package fr.evolya.javatoolkit.gui.swing.backgroundoperation;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import fr.evolya.javatoolkit.appstandard.events.ViewListener;
import fr.evolya.javatoolkit.code.IncaLogger;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;
import fr.evolya.javatoolkit.gui.animation.Timeline;
import fr.evolya.javatoolkit.gui.animation.Timeline.TimelineState;
import fr.evolya.javatoolkit.gui.animation.callback.TimelineCallback;
import fr.evolya.javatoolkit.gui.swing.JPanelView;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;
import fr.evolya.javatoolkit.threading.worker.IOperation;

/**
 * Composant permettant de mettre en stack des opérations.
 * Il se présente sous la forme d'un panel affichant des sous-panels pour chaque tâche,
 * avec un label et une progress bar. Les opérations sont automatiquement retirées
 * quand elles se terminent.
 */
public class BackgroundOperationStackPanel extends JPanelView {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * La liste des opérations en cours.
	 */
	private Map<IOperation, JPanel> _operations = new HashMap<IOperation, JPanel>();
	
	/**
	 * La hauteur normale des sous-panels qui composent cette vue.
	 */
	private static int TASK_HEIGHT = 28;
	
	/**
	 * Initialisation statique des animations.
	 */
	static {
		SwingHelper.initSwingAnimations();
	}
	
	/**
	 * Constructeur.
	 */
	public BackgroundOperationStackPanel() {
		super();
		init();
	}
	
	/**
	 * Constructeur.
	 */
	public BackgroundOperationStackPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		init();
	}

	/**
	 * Constructeur.
	 */
	public BackgroundOperationStackPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		init();
	}

	/**
	 * Constructeur.
	 */
	public BackgroundOperationStackPanel(LayoutManager layout) {
		super(layout);
		init();
	}

	/**
	 * Create the panel.
	 */
	public void init() {
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// Lors de la redimension du composant principal, on redimensionne les
		// sous composants sans lancer d'animation.
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				autoresize(false, null);
			}
		});
		
	}

	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), Math.max(1, _operations.size() * TASK_HEIGHT));
	}
	
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, Math.max(1, _operations.size() * TASK_HEIGHT));
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(100, Math.max(1, _operations.size() * TASK_HEIGHT));
	};
	
	/**
	 * Renvoie TRUE si des opérations sont en cours.
	 */
	public boolean hasOperationPending() {
		synchronized (this) {
			return _operations.size() > 0;
		}
	}
	
	/**
	 * Renvoie une liste des opérations en cours.
	 */
	public Set<IOperation> getPendingOperations() {
		return _operations.keySet();
	}

	/**
	 * Ajouter une opération.
	 */
	public void addOperation(final IOperation op, final String name) {
		
		// Si la tâche est déjà terminée, on ne fait rien
		if (op.isCompleted()) {
			return;
		}
		
		// Si la tâche existe déjà dans la liste, on ne fait rien
		if (_operations.containsKey(op)) {
			return;
		}
		
		// On force l'utilisation de cette méthode dans l'EDT
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						addOperation(op, name);
					}
				});
				return;
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		
		// On fabrique un sous-panel
		final Dimension size = new Dimension(BackgroundOperationStackPanel.this.getWidth(), TASK_HEIGHT);
		final JPanel sub = new JPanel() {
			private static final long serialVersionUID = 1L;
			public Dimension getPreferredSize() {
				return size;
			}
			public Dimension getMaximumSize() {
				return size;
			}
			public Dimension getMinimumSize() {
				return size;
			};
		};
		sub.setLayout(null);
		sub.setBounds(0, _operations.size() * TASK_HEIGHT, getWidth(), TASK_HEIGHT);
		
		// Un label
		final JLabel label = new JLabel();
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 10));
		label.setText(name);
		label.setBounds(2, 1, getWidth() - 4, 11);
		sub.add(label);
		
		// Une barre de progression
		final JProgressBar bar = new JProgressBar(0, 100);
		bar.setBounds(1, 15, getWidth() - 2, 13);
		sub.add(bar);

		// L'ajout est synchronisé
		synchronized (this) {

			// On ajoute l'opération dans la liste interne
			_operations.put(op, sub);
			
			// On ajoute le panel dans le contentpane
			add(sub);
		
		}
		
		// On rendimensionne la vue
		autoresize(true, null);
		
		// On fabrique un thread pour suivre l'évolution de l'opération
		new Thread(new Runnable() {
			public void run() {
				
				// On boule jusqu'à la fin de l'opération
				while (true) {
					
					// Quand l'opération est terminée
					if (op.isCompleted()) {
						
						// On place la valeur de la progressbar à 100%
						bar.setIndeterminate(false);
						bar.setValue(100);

						// On retire l'opération de la liste. On ne retire pas le composant
						// du container encore, on attent la fin de l'animation.
						_operations.remove(op);
//						BackgroundOperationView.this.remove(sub);
					
						// On repaint
						autoresize(true, new Runnable() {
							public void run() {
								// On retire le composant
								BackgroundOperationStackPanel.this.remove(sub);
								BackgroundOperationStackPanel.this.repaint();
							}
						});
						
						// On arrête de boucler
						return;
					}
					
					// On recupère l'avancement actuel de l'opération
					float pc = op.getOperationPercent();
					
					// Si la valeur est invalide, on met le mode indeterminé
					if (Float.isNaN(pc) || pc < 0) {
						if (!bar.isIndeterminate()) {
							bar.setIndeterminate(true);
						}
					}
					
					// Si la valeur est valide, on modifie l'avancement
					else {
						
						// On désactive le mode indeterminé
						if (bar.isIndeterminate()) {
							bar.setIndeterminate(false);
						}
						
						// On modifie la valeur
						bar.setValue(Math.min(100, Math.max((int) pc, 0)));
						
					}
					
					// On attend la fin de la tâche, ou un maximum de 1 seconde
					//op.waitForCompletion(1000);
					
				}
			}
		}).start();

	}

	/**
	 * Demande le redimensionnement du composant, qui va aussi repositionner
	 * les sous-composants.
	 */
	protected void autoresize(boolean resize, final Runnable callback) {
		
		// On prépare la hauteur que doit avoir le container
		Dimension a = getSize();
		Dimension b = new Dimension((int)a.getWidth(), _operations.size() * TASK_HEIGHT);

		// On boucle sur les sub-panels pour les repositionner par
		// rapport au composant qui les contient.
		int i = 0;
		for (JPanel sub : _operations.values()) {
			sub.setLocation(0, i++ * TASK_HEIGHT);
		}
				
		// Modification des dimensions
		if (resize && !a.equals(b)) {
			
			// On ne cummule pas les animations
			// NOTE: si on fait ça, il va y avoir des callbacks qui ne vont pas se décvlancher
			// et donc des composants qui ne vont pas être retirés. Trident gêre déjà les animations
			// concurrentes sur les objets.
//			if (_timeline != null && !_timeline.isDone()) {
//				_timeline.abort();
//			}
			
			// Log
			if (Timeline.LOGGER.isLoggable(IncaLogger.DEBUG)) {
				Timeline.LOGGER.log(IncaLogger.DEBUG, "Resize for " + _operations.size() + " operations");
			}
			
			// Création de la timeline
			final Timeline _timeline = new Timeline(this);
			_timeline.addPropertyToInterpolate("size", a, b);
			_timeline.setDuration(250);
			_timeline.addCallback(new TimelineCallback() {
				public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction,
						float timelinePosition) {
					
					// On s'interesse à la fin de l'animation
					if (newState != TimelineState.IDLE) return;
					
					// On passe dans l'EDT
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {

							// Execution de la callback
							if (callback != null) {
								callback.run();
							}
							
							// On recherche le parent le plus en haut de la vue (typiquement c'est la frame)
							Component component = SwingHelper.getTopParent(BackgroundOperationStackPanel.this);
							
							// Si un parent existe, on lui envoie un faux event de redimensionnement, ce qui
							// va notifier tous les layouts pour qu'ils se réajustent.
							if (component != null) {
								component.dispatchEvent(new ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED));
							}
							
						}
					});
					
				}
				public void onTimelinePulse(float durationFraction, float timelinePosition) { }
			});
			_timeline.play();
		}
		
	}

	@Override
	public void dispose() {

		// On retire tous les composants
		removeAll();
		
		// On vide la liste des opérations
		_operations.clear();
		_operations = null;
		
	}

	@Override
	public EventSource<? extends ViewListener<AWTEvent>> getEventsView() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ViewBounds getViewBounds() {
		return SwingHelper.bounds(getBounds());
	}

	@Override
	public void setViewBounds(ViewBounds viewBounds) {
		setBounds(SwingHelper.bounds(viewBounds));
		autoresize(false, null);
	}

}
