package fr.evolya.javatoolkit.gui.swing.appdebug;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.evolya.javatoolkit.appstandard.AppPlugin;
import fr.evolya.javatoolkit.appstandard.bridge.ILocalApplication;
import fr.evolya.javatoolkit.code.Instance;
import fr.evolya.javatoolkit.events.attr.EventSource;

/**
 * Un noeud de TreeView spécialisé dans la représentation d'une application.
 */
public class AppTreeNode extends DefaultMutableTreeNode {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructeur
	 */
	public AppTreeNode(ILocalApplication app) {
		
		// On enregistre l'application comme UserObject
		super(app);

		// Spécialisation pour les applications V1
		if (app instanceof fr.evolya.javatoolkit.appstandard.App) {
			
			// On recupère la liste des plugins de l'application
			final Map<String, AppPlugin> plugins = ((fr.evolya.javatoolkit.appstandard.App) app).getPlugins();
			
			// On fabrique un noeud pour les plugins de l'application
			final DefaultMutableTreeNode pluginsNode = new DefaultMutableTreeNode("Plugins");
			
			// On parcours les IDs des plugins
			for (String pluginID : plugins.keySet()) {
				
				// Et on fabrique les noeuds enfants
				pluginsNode.add(new PluginTreeNode(pluginID, plugins.get(pluginID)));
				
			}
			
			// Si on a des sous-noeuds, on rajoute au noeud principal
			if (pluginsNode.getChildCount() > 0) {
				add(pluginsNode);
			}
		
		}
		
		// Spécialisation pour les applications V1
		if (app instanceof fr.evolya.javatoolkit.app.App) {
			
			fr.evolya.javatoolkit.app.App app2 = (fr.evolya.javatoolkit.app.App) app;
			// On fabrique un noeud pour les plugins de l'application
			final DefaultMutableTreeNode pluginsNode = new DefaultMutableTreeNode("Components");
			// On parcours les composants
			for (Map.Entry<Class<?>, Instance> component : app2.getComponents().entrySet()) {
				// Et on fabrique les noeuds enfants
				pluginsNode.add(new ComponentTreeNode(app2, component.getValue()));
			}
			// Si on a des sous-noeuds, on rajoute au noeud principal
			if (pluginsNode.getChildCount() > 0) {
				add(pluginsNode);
			}
		}
		
		// On recherche les sources d'event
		List<EventSource<?>> events = new ArrayList<EventSource<?>>();
		for (Method m : app.getClass().getMethods()) {
			if (m.getReturnType() == null) continue;
			if (m.getReturnType().getSimpleName().equals("EventSource")) {
				EventSource<?> src = null;
				try {
					src = (EventSource<?>) m.invoke(app, new Object[] { });
				} catch (Exception e) { }
				events.add(src);
			}
		}
		if (events.size() > 0) {
			final DefaultMutableTreeNode eventsNode = new DefaultMutableTreeNode("Events");
			for (EventSource<?> e : events) {
				eventsNode.add(new EventSourceTreeNode(e));
			}
			add(eventsNode);
		}
		

	}

	/**
	 * Affichage sous forme de string.
	 */
	public String toString() {
		final ILocalApplication app = (ILocalApplication) getUserObject();	
		return "Application " + app.getApplicationName();
	}

}
