package fr.evolya.javatoolkit.gui.swing.appdebug;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.evolya.javatoolkit.events.attr.EventSource;

public class ContainerTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	private Container _component;

	public ContainerTreeNode(Container component) {
		_component = component;
		
		// On recherche les sources d'event
		List<EventSource<?>> events = new ArrayList<EventSource<?>>();
		for (Method m : component.getClass().getMethods()) {
			if (m.getReturnType() == null) continue;
			if (m.getReturnType().getSimpleName().equals("EventSource")) {
				EventSource<?> src = null;
				try {
					src = (EventSource<?>) m.invoke(component, new Object[] { });
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
		
		for (Component c : component.getComponents()) {
			if (c instanceof Container) {
				add(new ContainerTreeNode(((Container)c)));
			}
		}
		
	}

	@Override
	public String toString() {
		return "Container " + _component.getClass().getSimpleName()
				+ " (" + _component.getName() + ")";
	}
}
