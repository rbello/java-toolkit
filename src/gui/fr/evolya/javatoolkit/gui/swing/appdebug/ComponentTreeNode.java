package fr.evolya.javatoolkit.gui.swing.appdebug;

import java.awt.Container;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.appstandard.AppPlugin;
import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.code.Instance;
import fr.evolya.javatoolkit.events.fi.Listener;

public class ComponentTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;
	private String _pluginID;
	
	public ComponentTreeNode(App app, Instance component) {
		super(component);
		_pluginID = component.getInstanceClass().getSimpleName();

		for (Listener<?> listener : app.getListeners(component.getInstanceClass())) {
			add(new DefaultMutableTreeNode(listener));
		}
		
		if (component.getInstance() instanceof AppViewController) {
			Object view = ((AppViewController<?, ?, ?, ?>)component.getInstance()).getView();
			if (view == null) return;
			for (Object o : ((AppViewController<?, ?, ?, ?>)component.getInstance()).getView().getComponents()) {
				if (o instanceof Container) {
					add(new ContainerTreeNode(((Container)o)));
				}
			}
		}
		
	}
	
	public String toString() {
		if (getUserObject() instanceof AppService) {
			AppPlugin plugin = (AppPlugin) getUserObject();
			return "Service " + _pluginID + " (" + plugin.getClass().getSimpleName() + ")";
		}
		if (getUserObject() instanceof AppViewController) {
			@SuppressWarnings("rawtypes")
			AppViewController plugin = (AppViewController) getUserObject();
			return "View " + _pluginID + " (" + plugin.getClass().getSimpleName() + ")";
		}
		if (getUserObject() instanceof Instance) {
			Object plugin = ((Instance) getUserObject()).getInstance();
			return "Component " + plugin.getClass().getSimpleName();
		}
		return "Object " + getUserObject().getClass().getSimpleName();
	}

}
