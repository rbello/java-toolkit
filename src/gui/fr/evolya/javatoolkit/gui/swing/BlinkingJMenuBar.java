package fr.evolya.javatoolkit.gui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import fr.evolya.javatoolkit.code.annotations.Bug;
import fr.evolya.javatoolkit.gui.animation.Timeline;

@Bug("Ne fonctionne pas")
public class BlinkingJMenuBar extends JMenuBar implements ContainerListener, ActionListener {

	public BlinkingJMenuBar() {
		
		// On se bind sur les events de type container liés à cette barre de menu
		addContainerListener(this);
		
	}
	
	@Override
	public void componentAdded(ContainerEvent e) {
		
		// Si c'est un sous-menu
		if (e.getChild() instanceof JMenu) {
			
			// On recupère le menu
			JMenu menu = (JMenu) e.getChild();
			
			System.out.println("Added menu: " + menu.getText() + " (" + menu.getMenuComponentCount() + ")");
			
			// On s'inscrit aux events container. TODO NE FONCTIONNE PAS
			menu.addContainerListener(this);
			
			// On regarde si le menu contient déjà des enfants
			for (Component child : menu.getMenuComponents()) {
				
				// On propage un faux event sur l'enfant pour bind les menuitems
				// de manière récursive.
				componentAdded(new ContainerEvent(
					menu,
					e.getID(),
					child
				));
				
			}
			
		}
		
		// Si c'est un item
		else if (e.getChild() instanceof JMenuItem) {
			
			// On recupère l'item
			JMenuItem item = (JMenuItem) e.getChild();
			
			System.out.println("Added item: " + item.getText());
			
			// On recupère tous les action listeners de cet item
			ActionListener[] listeners = item.getActionListeners();
			
			// On les retire tous
			for (ActionListener l : listeners)
				item.removeActionListener(l);
						
			// On se bind en premier sur l'action
			item.addActionListener(this);
			
			// Puis on remet les anciens
			for (ActionListener l : listeners)
				item.addActionListener(l);
						
		}
		
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		
		// Si c'est un sous-menu, on se désinscrit des events container.
		if (e.getChild() instanceof JMenu) {
			
			// On recupère le menu
			JMenu menu = (JMenu) e.getChild();
			
			System.out.println("Removed menu: " + menu.getText() + " (" + menu.getMenuComponentCount() + ")");
			
			// On se désinscrit aux events container. TODO NE FONCTIONNE PAS
			menu.addContainerListener(this);
			
			// On regarde si le menu contient encore des enfants
			for (Component child : menu.getMenuComponents()) {
				
				// On propage un faux event sur l'enfant pour unbind les menuitems
				// de manière récursive.
				componentRemoved(new ContainerEvent(
					menu,
					e.getID(),
					child
				));
				
			}
			
		}
		
		// Si c'est un item
		else if (e.getChild() instanceof JMenuItem) {
			
			// On recupère l'item
			JMenuItem item = (JMenuItem) e.getChild();
			
			System.out.println("Removed item: " + item.getText());
			
			// On se bind sur les events de type souris
			item.removeActionListener(this);
			
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		System.out.println("Action on: " + e.getSource());
		
		// Méfiance...
		if (!(e.getSource() instanceof JMenuItem)) return;
		
		// On recupère l'item
		JMenuItem item = (JMenuItem) e.getSource();
		
		Timeline tl = new Timeline(item);
		tl.setDuration(1000);
		tl.addPropertyToInterpolate("background", item.getBackground(), Color.red);
		tl.play();
		
	}
	
}
