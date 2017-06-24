package fr.evolya.javatoolkit.gui.swing.appdebug;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.AppActivity;
import fr.evolya.javatoolkit.app.event.ApplicationReady;
import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.appstandard.events.ServiceListener;
import fr.evolya.javatoolkit.appstandard.events.ViewControllerListenerAdapter;
import fr.evolya.javatoolkit.events.attr.EventCallback;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.events.attr.SourceListener;
import fr.evolya.javatoolkit.events.fi.BindOnEvent;
import fr.evolya.javatoolkit.exceptions.StateChangeException;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;

/**
 * Le controleur par défaut de la vue DebugTreeView.
 */
public class DebugTreeViewController2
	extends AppViewController<DebugTreeView, JFrame, AWTEvent, Component>
	implements Runnable {
	
	/**
	 * L'application observée.
	 */
	private App _app;
	
	private JMenu _menuFile;
	private JMenu _menuActivity;
	private JMenuItem _menuItemStart;
	private JMenuItem _menuItemStop;
	private JMenuItem _menuItemExit;
	
	private DefaultMutableTreeNode _selectedNode;

	private JMenu _menuDisplay;

	private JMenuItem _menuItemRefresh;

	private JMenuItem _menuItemExpand;

	private JMenuItem _menuItemCollapse;

	private JMenu _menuEvents;

	private JMenuItem _menuItemDescribe;

	/**
	 * Constructeur
	 */
	public DebugTreeViewController2() {
	}
	
	/**
	 * Mise en conformité avec l'API d'app v2
	 */
	@BindOnEvent(ApplicationReady.class)
	public void onApplicationReady(App app) {
		_app = app;
		this.buildView(true);
	}
	
	@Override
	public DebugTreeView constructView() {
		
		// On construit la vue
		DebugTreeView view = new DebugTreeView();
		view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// On modifie le titre de la fenêtre
		view.setTitle(_app.getApplicationName() + " - DebugTreeView");
		
		// Et l'icon
		view.setIconImage(DebugTreeCellRenderer.getImage("bean"));
		
		// On configure la tree view
		view.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		view.getTree().setCellRenderer(new DebugTreeCellRenderer());
		
		// On construit un model
		AppTreeNode root = new AppTreeNode(_app);
		DefaultTreeModel model = new DefaultTreeModel(root);
		
		// On associe le model à la vue
		view.getTree().setModel(model);
		
		// On construit le menu
		createMenuComponents(view);
		
		// On bind les events
		bindTreeEvents(view.getTree()); // sur le JTree component
		bindNodeEvents(view, root, true); // sur les objets représentés par les noeuds
		
		// Et on retourne la vue construite
		return view;
		
	}
	
	private void bindTreeEvents(final JTree tree) {
	
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					if (_selectedNode == null) return;
					if (_selectedNode.getChildCount() != 0) return;
					Object userobject = _selectedNode.getUserObject();
					if (userobject != null && userobject instanceof EventSource) {
						describeEventSource((EventSource<?>) userobject);
					}
				}
			}
		});
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {	

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				
				// Aucune selection
				if (tree.isSelectionEmpty()) {
					_selectedNode = null;
					_menuActivity.setEnabled(false);
					_menuItemStart.setEnabled(false);
					_menuItemStop.setEnabled(false);
					return;
				}
				
				// On recupère le noeud et son user object
				TreePath path = tree.getSelectionPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object userObject = selectedNode.getUserObject();
				
				// Tests de types
				boolean isActivity = (userObject instanceof AppActivity);
				boolean isEvents = (userObject instanceof EventSource);
				
				// Activation ou non des éléments de menu Activity
				_menuActivity.setEnabled(isActivity);
				_menuItemStart.setEnabled(isActivity);
				_menuItemStop.setEnabled(isActivity);
				
				// Activation ou non des éléments du menu Events
				_menuEvents.setEnabled(isEvents);
				_menuItemDescribe.setEnabled(isEvents);
				
				// On mémorise la sélection
				_selectedNode = selectedNode;
				
			}
		});
		
	}

	private void createMenuComponents(final DebugTreeView view) {
		
		_menuFile = new JMenu("File");
		_menuItemExit = new JMenuItem("Exit");
		_menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		_menuItemExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				view.getEventsView().trigger("onViewCloseIntent", view, event, true);
			}
		});
		_menuFile.add(_menuItemExit);
		
		_menuDisplay = new JMenu("Display");
		_menuItemRefresh = new JMenuItem("Refresh");
		_menuItemRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		_menuItemRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				view.getTree().repaint();
			}
		});
		_menuDisplay.add(_menuItemRefresh);
		_menuItemExpand = new JMenuItem("Expand all");
		_menuItemExpand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		_menuItemExpand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SwingHelper.expandAll(view.getTree());
			}
		});
		_menuDisplay.add(_menuItemExpand);
		_menuItemCollapse = new JMenuItem("Collapse all");
		_menuItemCollapse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		_menuItemCollapse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SwingHelper.collapseAll(view.getTree());
			}
		});
		_menuDisplay.add(_menuItemCollapse);
		
		_menuActivity = new JMenu("Activity");
		_menuActivity.setEnabled(false);
		_menuItemStart = new JMenuItem("Start");
		_menuItemStart.setEnabled(false);
		_menuItemStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (_selectedNode == null || !(_selectedNode.getUserObject() instanceof AppActivity)) {
					return;
				}
				AppActivity activity = (AppActivity) _selectedNode.getUserObject();
				if (activity.isStarted()) {
					view.getStatusBar().setText("Activity " + activity + " is already started.");
					return;
				}
				view.getStatusBar().setText("Starting activity " + activity + "...");
				try {
					activity.start();
				} catch (StateChangeException ex) {
					view.getStatusBar().setText("Unable to start " + activity + " : " + ex.getClass().getSimpleName());
				}
				catch (SecurityException ex) {
					view.getStatusBar().setText("Unable to start " + activity + " : " + ex.getClass().getSimpleName());
				}
				view.getStatusBar().setText("Activity started: " + activity);
			}
		});
		_menuActivity.add(_menuItemStart);
		_menuItemStop = new JMenuItem("Stop");
		_menuItemStop.setEnabled(false);
		_menuItemStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (_selectedNode == null || !(_selectedNode.getUserObject() instanceof AppActivity)) {
					return;
				}
				AppActivity activity = (AppActivity) _selectedNode.getUserObject();
				if (!activity.isStarted()) {
					view.getStatusBar().setText("Activity " + activity + " is already stopped.");
					return;
				}
				view.getStatusBar().setText("Stopping activity " + activity + "...");
				try {
					activity.stop();
				} catch (StateChangeException ex) {
					view.getStatusBar().setText("Unable to stop " + activity + " : " + ex.getClass().getSimpleName());
				}
				catch (SecurityException ex) {
					view.getStatusBar().setText("Unable to stop " + activity + " : " + ex.getClass().getSimpleName());
				}
				view.getStatusBar().setText("Activity stopped: " + activity);
			}
		});
		_menuActivity.add(_menuItemStop);
		
		_menuEvents = new JMenu("Events");
		_menuEvents.setEnabled(false);
		_menuItemDescribe = new JMenuItem("Describe");
		_menuItemDescribe.setEnabled(false);
		_menuItemDescribe.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (_selectedNode == null || !(_selectedNode.getUserObject() instanceof EventSource)) {
					return;
				}
				describeEventSource((EventSource<?>) _selectedNode.getUserObject());
			}

		});
		_menuEvents.add(_menuItemDescribe);
		
		// Construction des sous-menus de 1er niveau
		view.getFrameMenuBar().add(_menuFile);
		view.getFrameMenuBar().add(_menuDisplay);
		view.getFrameMenuBar().add(_menuActivity);
		view.getFrameMenuBar().add(_menuEvents);
		
	}
	
	private void describeEventSource(EventSource<?> source) {
		// On fabrique le message de description de la source
		StringBuilder sb = new StringBuilder();
		sb.append("EventSource<");
		sb.append(source.getListenerClass().getSimpleName());
		sb.append("> on " + source.getSender().getClass().getSimpleName());
		for (Method m : source.getListenerClass().getMethods()) {
			sb.append("\n      ");
			sb.append(m.getName());
			sb.append("(");
			int i = 0;
			for (Class<?> p : m.getParameterTypes()) {
				if (i++ > 0) sb.append(", ");
				sb.append(p.getSimpleName());
			}
			sb.append(")");
		}
		// On affiche un popup
		JOptionPane.showMessageDialog(null, sb.toString());
	}

	/**
	 * Etablir 
	 */
	@SuppressWarnings("unchecked")
	protected void bindNodeEvents(final DebugTreeView view, final DefaultMutableTreeNode node, boolean recursive) {
		
		// Récursion
		Enumeration<DefaultMutableTreeNode> sub = node.children();
		while (sub.hasMoreElements()) {
			bindNodeEvents(view, sub.nextElement(), recursive);
		}
		
		// Pour les noeuds qui représentent des applications
		if (node instanceof AppTreeNode) {
		
			// L'application
			final App app = (App) node.getUserObject();
			
			// TODO
			// On se bind aux events de changements d'états
//			app.getEventsApp().bind("onApplicationStateChanged", new IListener<String>() {
//				@Override
//				public boolean notifyEvent(String event, Object... args) {
//					
//					// On recupére l'état actuel de l'application
//					ApplicationState state = (ApplicationState) args[1];
//					
//					// On supprime tous les enfants
//					if (state instanceof StoppedState) {
//						for (int i = 0, l = node.getChildCount(); i < l; i++) {
//							node.remove(i);
//						}
//					}
//					
//					// On ordonne un repaint
//					view.getTree().repaint();
//					
//					return true;
//					
//				}
//			});
		
		}
		
		// Pour les vues
		else if (node instanceof PluginTreeNode && node.getUserObject() instanceof AppViewController) {
			
			// Le controleur de la vue
			final AppViewController<?, ?, ?, ?> ctrl = (AppViewController<?, ?, ?, ?>) node.getUserObject();
			
			ctrl.getEventsViewCtrl().bind(new ViewControllerListenerAdapter() {
				@SuppressWarnings("rawtypes")
				@Override
				public void afterViewCloseIntent(AppViewController ctrl, boolean isUserIntent) {
					// On ordonne un repaint
					view.getTree().repaint();
				}
				@SuppressWarnings("rawtypes")
				@Override
				public void onViewCreated(AppViewController ctrl, AppView v) {
					// On ordonne un repaint
					view.getTree().repaint();
				}
			});
			
		}
		
		else if (node instanceof PluginTreeNode && node.getUserObject() instanceof AppService) {
			((AppService) node.getUserObject()).getEventsService().bind(new ServiceListener() {
				public void onServiceStopped(AppService service) {
					// On ordonne un repaint
					view.getTree().repaint();
				}
				public void onServiceStarted(AppService service) {
					// On ordonne un repaint
					view.getTree().repaint();
				}
			});
			
		}
		
		// Pour les sources d'events
		else if (node instanceof EventSourceTreeNode) {
			
			// La source des events
			final EventSource<?> src = (EventSource<?>) node.getUserObject();
			
			src.getEventsSource().bind(new SourceListener() {

				@Override
				public void onBind(EventCallback<?> listener, EventSource<?> source) {
					if (listener.toString().contains(getClass().getPackage().getName())) {
			        	return;
			        }
					// On rajoute un noeud
					node.add(new DefaultMutableTreeNode(listener));
					// On ordonne un repaint
					view.getTree().repaint();
				}

				@Override
				public void onUnbindAll(EventSource<?> source) {
					// On retire tous les sous-noeuds
					for (int i = 0; i < node.getChildCount(); i++) {
						node.remove(i);
					}
					// On ordonne un repaint
					view.getTree().repaint();
				}

				@Override
				public void onUnbind(EventCallback<?> listener, EventSource<?> source) {
					// On vérifie que la vue existe toujours
					if (getView() == null) {
						return;
					}
					// On cherche les noeuds à retirer
					List<DefaultMutableTreeNode> remove = new ArrayList<DefaultMutableTreeNode>();
					for (int i = 0, l = node.getChildCount(); i < l; i++) {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode) node.getChildAt(i);
						if (n.getUserObject() != listener) {
							continue;
						}
						remove.add(n);
					}
					// On retire les noeuds
					final DefaultTreeModel model = (DefaultTreeModel) getView().getTree().getModel();
					for (DefaultMutableTreeNode n : remove) {
						model.removeNodeFromParent(n);
					}
					// On ordonne un repaint
					view.getTree().repaint();
				}
				
			});
			
		}
		
	}

	@Override
	protected void onViewClosed() {
		_app.remove(this);
	}

	@Override
	protected void onViewCreated() {
	}

	@Override
	public void run() {
		buildView(true);
	}
	
}
