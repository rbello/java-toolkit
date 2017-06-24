package fr.evolya.javatoolkit.gui.swing.appdebug;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.evolya.javatoolkit.events.attr.EventCallback;
import fr.evolya.javatoolkit.events.attr.EventSource;

public class EventSourceTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	public EventSourceTreeNode(EventSource<?> e) {
		super(e);
		
		for (EventCallback<?> callback : e.getListeners()) {
			if (callback.toString().contains(getClass().getPackage().getName())) {
	        	continue;
	        }
			add(new DefaultMutableTreeNode(callback));
		}
	}
	
	public EventSource<?> getEventSource() {
		return (EventSource<?>) getUserObject();
	}

}
