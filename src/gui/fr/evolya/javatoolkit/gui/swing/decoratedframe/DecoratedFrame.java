package fr.evolya.javatoolkit.gui.swing.decoratedframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.event.ApplicationStarting;
import fr.evolya.javatoolkit.app.event.ApplicationWakeup;
import fr.evolya.javatoolkit.app.event.WindowCloseIntent;
import fr.evolya.javatoolkit.events.fi.BindOnEvent;
import fr.evolya.javatoolkit.events.fi.ModelCreated;
import fr.evolya.javatoolkit.events.fi.ModelEvent.ModelItemAdded;
import fr.evolya.javatoolkit.events.fi.Observable;
import fr.evolya.javatoolkit.events.fi.ObservableList;
import fr.evolya.javatoolkit.gui.swing.ComponentDragger;
import fr.evolya.javatoolkit.gui.swing.ComponentResizer;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;

/**
 * Une JFrame avec un système de décoration un peu spécial, style Photoshop CS5.
 * On désactive la décoration de l'OS pour afficher une JMenuBar qui sert à afficher
 * le titre de l'application, les menus et les boutons de icone/minimize/close.
 * 
 * Par défaut, la fenêtre a les coins arrondis
 */
public class DecoratedFrame extends JFrame {

	/**
	 * Serial UID.
	 */
	protected static final long serialVersionUID = 1L;

	/**
	 * Le contentPane de la fenêtre.
	 */
	protected JPanel _contentPane;
	
	/**
	 * Longueur de l'arc des bords de la fenêtre.
	 */
	protected int _roundedCornersArc = 5;
	
	/**
	 * Le composant qui sert à afficher le nom de la fenêtre,
	 * l'icône de l'application, et qui peut servir de menu principal.
	 */
	protected JMenu _appName = null;

	/**
	 * Le mécanisme qui permet de redimensionner la fenêtre.
	 */
	protected ComponentResizer _resizer;
	
	/**
	 * Le mécanisme qui permet de déplacer la fenêtre.
	 */
	protected ComponentDragger _dragger;

	/**
	 * Indique si la redimensionnement de la fenêtre est possible.
	 */
	protected boolean _resizable = true;

	/**
	 * Mémorise les dimensions de la fenêtre avant sa maximisation, histoire
	 * de pouvoir revenir à l'état initial (restore).
	 */
	protected Rectangle _normalBounds;

	/**
	 * Mémorise l'état maximisé, histoire de gêrer la maximisation.
	 */
	protected boolean _isMaximized;

	/**
	 * Le bouton pour icônifier la fenêtre.
	 */
	protected JButton _iconizeButton;

	/**
	 * Le bouton pour maximiser/restaurer la fenêtre.
	 */
	protected JButton _maximizeRestoreButton;

	/**
	 * Le bouton pour fermer la fenêtre.
	 */
	protected JButton _closeButton;

	/**
	 * L'action du bouton d'icônisation.
	 */
	private ActionListener _iconizeAction;

	/**
	 * L'action du bouton du maximise/restore.
	 */
	private ActionListener _maximizeRestoreAction;

	/**
	 * L'action du bouton close.
	 */
	private ActionListener _closeAction;

	/**
	 * Le modèle du menu.
	 */
	private MenuViewModel modelMenu = null;
	
	/**
	 * Constructeur.
	 */
	public DecoratedFrame() {
		
		// On configure l'état par défaut pour la fermeture.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// On n'utilise pas le décorateur du système, on va gêrer la décoration
		// directement dans cette classe.
		setUndecorated(true);

		// Bornes initiales de la fenêtre.
		_normalBounds = new Rectangle(100, 100, 450, 300);
		setBounds(_normalBounds);
		
		// On fabrique la barre de menu d'application.
		JMenuBar menuBar = new JMenuBar() {
			private static final long serialVersionUID = 1L;
			public JMenu add(JMenu menu) {
				// On ajoute le menu dans la barre. Si le composant de titre
				// existe déjà, on place le menu en seconde position. Sinon,
				// on l'ajoute en première.
				return (JMenu) add(menu, _appName != null ? 1 : 0);
			}
		};
		super.setJMenuBar(menuBar);
		
		// On ajoute un séparateur, pour forcer l'affichage des boutons à droite
		// de la fenêtre.
		menuBar.add(Box.createHorizontalGlue());
		
		// On fabrique les boutons de la fenêtre.
		createFrameButtons(menuBar);
		
		// On ajoute une bordure autour de la fenêtre
		getRootPane().setBorder(new LineBorder(menuBar.getBackground(), 3));
		
		// On fabrique le contentpane, et on le configure.
		_contentPane = new JPanel();
		_contentPane.setLayout(new BorderLayout());
		_contentPane.setBorder(new LineBorder(menuBar.getBackground(), 2));
		setContentPane(_contentPane);
		
		// On fait en sorte que la fenêtre soit redimensionnable.
		_resizer = new ComponentResizer();
		_resizer.setSnapSize(new Dimension(3, 3));
		_resizer.registerComponent(this);
		
		// On fait en sorte que la fenêtre soit déplacable.
		_dragger = new ComponentDragger();
		_dragger.registerComponent(this, menuBar);
		
		// On enregistre un listener pour toggler la maximisation quand
		// l'user double-clique sur la barre de titre de l'application.
		menuBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && _resizable) {
					_maximizeRestoreAction.actionPerformed(new ActionEvent(
							e.getSource(),
							e.getID(),
							"switch maximize state"
					));
				}
			}
		});
		
		// On dessine un arrondi sur les angles, et on fait en sorte qu'il se
		// repaint à chaque changement de dimensions.
		repaintRoundedCorners();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				repaintRoundedCorners();
			}
		});
		
	}

	/**
	 * Méthode interne pour créer les boutons d'application (iconifier,
	 * maximiser, restorer et fermer).
	 * Ils sont fait pour s'afficher dans la barre de menu donnée.
	 */
	private void createFrameButtons(JMenuBar menuBar) {
		
		// On fabrique les comportements par défauts
		
		_iconizeAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setState(Frame.ICONIFIED);
			}
		};
		
		_maximizeRestoreAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!_isMaximized) {
					setState(Frame.MAXIMIZED_BOTH);
					_maximizeRestoreButton.setIcon(getImageIcon("/fr/evolya/javatoolkit/gui/swing/decoratedframe/restore.png"));
				}
				else {
					setState(Frame.NORMAL);
					_maximizeRestoreButton.setIcon(getImageIcon("/fr/evolya/javatoolkit/gui/swing/decoratedframe/maximize.png"));
				}
			}
		};
		
		_closeAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Dispatch event before
				DecoratedFrame.this.dispatchEvent(new WindowEvent(DecoratedFrame.this, WindowEvent.WINDOW_CLOSING));
				// Then exit
				if (getDefaultCloseOperation() == EXIT_ON_CLOSE)
					System.exit(1);
				else if (getDefaultCloseOperation() == DISPOSE_ON_CLOSE)
					dispose();
				else if (getDefaultCloseOperation() == HIDE_ON_CLOSE)
					setVisible(false);
			}
		};
		
		
		// La taille par défaut des boutons (sauf le bouton close un peu plus large).
		Dimension d = new Dimension(40, 18);
		
		_iconizeButton = new JButton("");
		_iconizeButton.setIcon(getImageIcon("/fr/evolya/javatoolkit/gui/swing/decoratedframe/minimize.png"));
		_iconizeButton.setMinimumSize(d);
		_iconizeButton.setPreferredSize(d);
		_iconizeButton.setMaximumSize(d);
		_iconizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_iconizeAction.actionPerformed(e);
			}
		});
		menuBar.add(_iconizeButton);
		
		_maximizeRestoreButton = new JButton("");
		_maximizeRestoreButton.setIcon(getImageIcon("/fr/evolya/javatoolkit/gui/swing/decoratedframe/maximize.png"));
		_maximizeRestoreButton.setMinimumSize(d);
		_maximizeRestoreButton.setPreferredSize(d);
		_maximizeRestoreButton.setMaximumSize(d);
		_maximizeRestoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_maximizeRestoreAction.actionPerformed(e);
			}
		});
		menuBar.add(_maximizeRestoreButton);
		
		// Le bouton close est un peu plus large.
		d = new Dimension(50, (int) d.getHeight());
		
		_closeButton = new JButton("");
		_closeButton.setIcon(getImageIcon("/fr/evolya/javatoolkit/gui/swing/decoratedframe/close.png"));
		_closeButton.setMinimumSize(d);
		_closeButton.setPreferredSize(d);
		_closeButton.setMaximumSize(d);
		_closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_closeAction.actionPerformed(e);
			}
		});
		menuBar.add(_closeButton);
	}

	/**
	 * Permet de modifier la valeur de courbure des bords de la fenêtre.
	 * Cette fonctionnalité permet de donner un léger arrondi aux coins.
	 *  
	 * @param pixels La valeur de la forme de courbure, en pixels.
	 */
	public void setRoundedCornersArc(int pixels) {
		_roundedCornersArc = Math.max(pixels, 0);
	}
	
	/**
	 * Méthode interne mais publique pour redessiner les bords arrondis de la fenêtre.
	 * Utiliser setRoundedCornersArc(int) pour modifier la valeur de l'arrondi.
	 */
	public void repaintRoundedCorners() {
		Rectangle bounds = getBounds();
		setShape(new RoundRectangle2D.Double(0, 0, bounds.getWidth(), bounds.getHeight(), _roundedCornersArc, _roundedCornersArc));
	}

	/**
	 * Modifie l'icône de la fenêtre.
	 * @param image
	 */
	public void setIconImage(Image image) {
		
		// On souhaite retirer l'icône.
		if (image == null) {
			
			// On fabrique une image vide transparente.
			Image empty = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
			
			// On l'applique en temps qu'icône d'application dans la barre des tâches.
			super.setIconImage(empty);
			
			// Et on l'applique au composant de titre s'il existe.
			if (_appName != null) {
				_appName.setIcon(new ImageIcon(empty));
			}
			
		}

		// On souhaite placer une icône.
		else {
			
			// On l'applique en temps qu'icône d'application dans la barre des t�ches.
			super.setIconImage(image);
			
			// On fabrique le composant de titre.
			createTitleComponent();
			
			// Et on l'applique au composant de titre.
			_appName.setIcon(new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
			
		}
		
	}
	
	/**
	 * Méthode interne pour créer le composant permettant d'afficher le
	 * nom de l'application, de mettre aussi l'icône, et qui peut
	 * aussi servir de menu.
	 */
	protected void createTitleComponent() {
		
		// Si le composant n'existe pas
		if (_appName == null) {
			
			// On fabrique le composant.
			_appName = new JMenu("");
			
			// On l'affiche en gras.
			_appName.setFont(new Font(_appName.getFont().getName(), Font.BOLD, _appName.getFont().getSize()));
			
			// Et on l'ajoute en première position dans la barre de menu.
			getJMenuBar().add(_appName, 0);
			
		}
		
	}
	
	/**
	 * Renvoie le composant permettant d'afficher le nom de l'application,
	 * de mettre aussi l'icône, et qui peut aussi servir de menu.
	 */
	public JMenu getTitleComponent() {
		return _appName;
	}

	/**
	 * Modifier le nom de l'application. Par défaut, la couleur du label
	 * sera le blanc.
	 */
	public void setApplicationName(String name) {
		setApplicationName(name, Color.WHITE);
	}
	
	/**
	 * Modifier le nom de l'application.
	 */
	public synchronized void setApplicationName(String name, Color fgColor) {
		
		// On cherche à retirer le nom de l'application.
		if (name == null) {

			// C'est déjà fait...
			if (_appName == null) {
				return;
			}
			
			// On retire le menu de la barre de menu.
			getJMenuBar().remove(_appName);
			
			// Et on supprime le composant.
			_appName = null;
			
		}
		
		// On modifie le (vrai) titre de l'application, celui qui sera
		// visible dans la barre d'application.
		super.setTitle(name);
		
		// On fabrique le composant.
		createTitleComponent();
		
		// On lui passe le nom.
		_appName.setText(name);
		
		// Et modifie sa couleur.
		_appName.setForeground(fgColor);
		
	}
	
	/**
	 * Permet d'activer ou de désactiver la possibilité de redimensionner
	 * la fenêtre.
	 */
	@Override
	public void setResizable(boolean resizable) {
		
		// On active.
		if (resizable && !_resizable) {
			_resizable = true;
			_maximizeRestoreButton.setVisible(true);
			_resizer.registerComponent(this);
		}
		
		// On d�sactive.
		else if (!resizable && _resizable){
			_resizable = false;
			_maximizeRestoreButton.setVisible(false);
			_resizer.deregisterComponent(this);
		}
		
		super.setResizable(resizable);
		
	}
	
	/**
	 * Renvoie le status actuel concernant la possibilité de redimensionner
	 * la fenêtre.
	 */
	@Override
	public boolean isResizable() {
		return _resizable;
	}

	/**
	 * Modifie l'état de la fenêtre.
	 */
	@Override
	public synchronized void setState(int state) {
		
		// On va avoir besoin de ces dimensions
		Rectangle bounds = getBounds();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// En fonction de l'état
		switch (state) {
		
		// Restore
		case Frame.NORMAL :
			// Uniquement si on était maximisé
			if (_isMaximized) {
				setBounds(_normalBounds);
				_isMaximized = false;
			}
			break;
			
		// Maximisation horizontale
		case Frame.MAXIMIZED_HORIZ :
			setBounds(0, (int) bounds.getY(), screenSize.width, (int) bounds.getHeight());
			break;
		
		// Maximisation verticale
		case Frame.MAXIMIZED_VERT :
			setBounds((int) bounds.getX(), 0, (int) bounds.getWidth(), screenSize.height);
			break;
		
		// Maximisation totale
		case Frame.MAXIMIZED_BOTH :
			if (!_isMaximized) {
				_normalBounds = getBounds();
				_isMaximized = true;
			}
			setBounds(0, 0, screenSize.width, screenSize.height);
			break;
		
		// Pour tous les autres �tats, on laisse le comportent par d�faut
		// d'une JFrame.
		default :
			super.setState(state);
		
		}

	}
	
	@Deprecated
	@Override
	public void setJMenuBar(JMenuBar menubar) {
		throw new UnsupportedOperationException("You can't change the menu bar. Use getJMenuBar() instead.");
	}
	
	/**
	 * Renvoie le bouton pour icônifier la fenêtre.
	 */
	public JButton getIconizeButton() {
		return _iconizeButton;
	}
	
	/**
	 * Modifier l'action par défaut du bouton pour icônifier la fenêtre.
	 */
	public void setIconizeButtonAction(ActionListener listener) {
		_iconizeAction = listener;
	}

	/**
	 * Renvoie le bouton pour maximiser ou restaurer la fenêtre.
	 */
	public JButton getMaximizeRestoreButton() {
		return _maximizeRestoreButton;
	}
	
	/**
	 * Modifier l'action par défaut du bouton pour maximiser ou restaurer la fenêtre.
	 */
	public void setMaximizeRestoreButtonAction(ActionListener listener) {
		_maximizeRestoreAction = listener;
	}

	/**
	 * Renvoie le bouton pour fermer la fenêtre.
	 */
	public JButton getCloseButton() {
		return _closeButton;
	}

	/**
	 * Modifier l'action par défaut du bouton close.
	 */
	public void setCloseButtonAction(ActionListener listener) {
		_closeAction = listener;
	}
	
	/**
	 * Permet de charger une image se trouvant dans le package de cette classe.
	 */
	protected static Image getImage(String filename) {
		return Toolkit.getDefaultToolkit().getImage(
				DecoratedFrame.class.getResource(filename));
	}
	
	/**
	 * Permet de charger une image se trouvant dans le package de cette classe,
	 * et d'en faire une icône.
	 */
	protected static ImageIcon getImageIcon(String filename) {
		return new ImageIcon(getImage(filename));
	}

	/**
	 * Executer une tâche quand la fenêtre est fermée
	 */
	public void onClose(final Runnable task) {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				task.run();
			}
		});
	}
	
	/**
	 * Construit les differents modèles qui correspondent à cette vue.
	 */
	@BindOnEvent(ApplicationStarting.class)
	public void buildModels(App app) {
		modelMenu = new MenuViewModel(app);
		app.add(modelMenu);
		modelMenu.on(ModelItemAdded.class)
			.executeOnGui((model, item, index) -> getJMenuBar().add((JMenu) item));
		app.notify(ModelCreated.class, modelMenu, app);
	}
	
	/**
	 * Envoie un intent de fermeture de fenêtre.
	 */
	@BindOnEvent(ApplicationStarting.class)
	public void bindShutdown(App app) {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				app.notify(WindowCloseIntent.class, app, DecoratedFrame.this, evt);
			}
		});
	}
	
	/**
	 * Renvoie le modèle du menu.
	 */
	public MenuViewModel getMenuModel() {
		return modelMenu;
	}
	
	/**
	 * Amener la fenêtre en cours devant les autres fenêtres ouvertes
	 * par l'utilisateur.
	 */
	@BindOnEvent(ApplicationWakeup.class)
	public void bringToFront() {
		setVisible(true);
		int state = getExtendedState();
		state &= ~JFrame.ICONIFIED;
		setExtendedState(state);
		setAlwaysOnTop(true);
		toFront();
		requestFocus();
		setAlwaysOnTop(false);
	}

	/**
	 * Classe utilitaire correspondant au modèle du menu.
	 */
	public static class MenuViewModel extends ObservableList<JMenuItem> {
		public MenuViewModel(Observable dispatcher) {
			super(dispatcher);
		}
	}
	
	/**
	 * Secoue la fenêtre.
	 */
	public void bounce() {
		SwingHelper.makeMeBounce(this);
	}
	
}
