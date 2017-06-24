package fr.evolya.javatoolkit.gui.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class ComponentDragger extends MouseAdapter {

	/**
	 * Les composants qui sont associés au Dragger.
	 * En clé les handles (c-à-d le composant qu'il faut saisir pour déplacer) ; et en valeur
	 * les targets (les composants à déplacer).
	 */
	private Map<Component, Component> _registered = new HashMap<Component, Component>();
	
	private int _x;
	private int _y;
	
	/**
	 * @param target Le composant qui doit être déplacé.
	 * @param handle Le composant qui sert à déplacer, là où l'utilisateur
	 * 	va pouvoir "saisir" l'objet à déplacer.
	 */
	public void registerComponent(Component target, Component handle) {
		
		// Déjà fait
		if (_registered.containsKey(handle)) {
			return;
		}
		
		// On ajoute le composant dans la liste des managés
		_registered.put(handle, target);
		
		// On s'inscrit comme listener des actions de souris
		handle.addMouseListener(this);
		handle.addMouseMotionListener(this);
		
	}
	
	/**
	 * Désassocie un composant. Il peut s'agir de la cible ou bien du handle.
	 */
	public void unregisterComponent(Component component) {
		Map<Component, Component> copy = new HashMap<Component, Component>(_registered);
		for (Component handle : copy.keySet()) {
			if (handle == component || _registered.get(handle) == component) {
				handle.removeMouseListener(this);
				handle.removeMouseMotionListener(this);
				_registered.remove(handle);
			}
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		e.getComponent().setCursor(new Cursor(Cursor.MOVE_CURSOR));
		_x = e.getXOnScreen();
        _y = e.getYOnScreen();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		e.getComponent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {

		// On recherche la cible, c-à-d le composant à déplacer.
		Component target = _registered.get(e.getComponent());

		// On recupère les positions actuelles du curseur
        int positionx = e.getXOnScreen();
        int positiony = e.getYOnScreen();
        
        // Ajustements sur l'axe X
        if (positionx > _x) {
            target.setLocation(target.getX() + (positionx - _x), target.getY());
        }
        else if (positionx < _x) {
            target.setLocation(target.getX() - (_x - positionx), target.getY());
        }
        
        // Ajustements sur l'axe Y
        if (positiony > _y) {
        	target.setLocation(target.getX(), target.getY() + (positiony - _y));
        }
        else if (positiony < _y){
        	target.setLocation(target.getX(), target.getY() - (_y - positiony));
        }
        
        // On mémorise les nouvelles positions
        _x = positionx;
        _y = positiony;
        
	}

}
