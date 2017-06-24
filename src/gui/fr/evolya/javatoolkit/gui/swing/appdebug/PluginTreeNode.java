package fr.evolya.javatoolkit.gui.swing.appdebug;

import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.evolya.javatoolkit.appstandard.AppPlugin;
import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.events.attr.EventSource;

public class PluginTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;
	private String _pluginID;
	
	public PluginTreeNode(String pluginID, AppPlugin plugin) {
		super(plugin);
		_pluginID = pluginID;
		
		// On recherche les sources d'event
		List<EventSource<?>> events = new ArrayList<EventSource<?>>();
		for (Method m : plugin.getClass().getMethods()) {
			if (m.getReturnType() == null) continue;
			if (m.getReturnType().getSimpleName().equals("EventSource")) {
				EventSource<?> src = null;
				try {
					src = (EventSource<?>) m.invoke(plugin, new Object[] { });
				} catch (Exception e) { }
				events.add(src);
			}
		}
		if (events.size() > 0) {
			final DefaultMutableTreeNode eventsNode = new DefaultMutableTreeNode("Events");
			for (EventSource<?> e : events) {
				if (e != null)
					eventsNode.add(new EventSourceTreeNode(e));
			}
			add(eventsNode);
		}
		
		if (plugin instanceof AppViewController) {
			Object x = ((AppViewController)plugin).getView();
			if (x == null) return;
			for (Object o : ((AppViewController)plugin).getView().getComponents()) {
				if (o instanceof Container) {
					add(new ContainerTreeNode(((Container)o)));
				}
			}
		}
		
	}
	
	public String toString() {
		AppPlugin plugin = (AppPlugin) getUserObject();
		if (plugin instanceof AppService) {
			return "Service " + _pluginID + " (" + plugin.getClass().getSimpleName() + ")";
		}
		if (plugin instanceof AppViewController) {
			return "View " + _pluginID + " (" + plugin.getClass().getSimpleName() + ")";
		}
		return "Plugin " + _pluginID + " (" + plugin.getClass().getSimpleName() + ")";
	}

}
