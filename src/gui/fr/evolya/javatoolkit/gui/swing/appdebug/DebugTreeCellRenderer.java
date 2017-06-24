package fr.evolya.javatoolkit.gui.swing.appdebug;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import fr.evolya.javatoolkit.app.AppActivity;
import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.appstandard.states.PausedState;
import fr.evolya.javatoolkit.appstandard.states.StartedState;
import fr.evolya.javatoolkit.events.attr.CallbackEventListener;
import fr.evolya.javatoolkit.events.attr.CallbackEventSource;
import fr.evolya.javatoolkit.events.attr.CallbackMultipleEventIListener;
import fr.evolya.javatoolkit.events.attr.CallbackSingleEventIListener;
import fr.evolya.javatoolkit.events.attr.CallbackSingleEventMethod;
import fr.evolya.javatoolkit.events.attr.CallbackSingleRunnable;
import fr.evolya.javatoolkit.events.attr.EventCallback;
import fr.evolya.javatoolkit.events.fi.Listener;

/**
 * Le renderer des items de la TreeView.
 */
public class DebugTreeCellRenderer implements TreeCellRenderer {

	/**
	 * Chemin vers le répertoire qui contient les images.
	 */
	protected static final String _path = "/fr/evolya/javatoolkit/gui/swing/appdebug/res/";
	
	/**
	 * Pour mémoriser les maps.
	 */
	protected static Map<String, Image> _images = new HashMap<String, Image>();
	
	/**
	 * Charger une icône
	 * @param filename
	 */
    public static ImageIcon getIcon(String filename) {
    	return new ImageIcon(getImage(filename));
	}
    
    /**
	 * Charger une icône
	 * @param filename
	 */
    public static Image getImage(String filename) {
    	
    	// Chemin vers le fichier image
    	filename = _path + filename + ".png";

    	// On conserve les images déjà chargées
    	if (_images.containsKey(filename)) {
    		return _images.get(filename);
    	}
    	
    	// Chargement de l'icône
    	try {
    		
    		// Chemin vers l'image
    		URL url = App.class.getResource(filename);
    		
    		// Chargement de l'image 
        	Image image = Toolkit.getDefaultToolkit().getImage(url);
	    	
        	// Mémorisation
        	_images.put(filename, image);
	    	
	    	return image;
	    	
    	}
    	catch (Throwable t) {
    		System.err.println("Unable to load: " + filename);
    		return null;
    	}
	}
    
    /**
     * Supprimer cet objet.
     */
    public void dispose() {
    	_images.clear();
    	_images = null;
    }

    /**
     * Méthode appelée pour la création du composant représentant un noeud du tree.
     */
	public Component getTreeCellRendererComponent(JTree tree, Object node, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
    	// On recupère l'objet à l'intérieur du noeud
    	Object userobject = ((DefaultMutableTreeNode) node).getUserObject();
        
    	// On construit un label
        final JLabel label = new JLabel();
        
        // Mise en forme
        if (selected) {
	        label.setForeground(UIManager.getDefaults().getColor("Tree.selectionForeground"));
	        label.setBackground(UIManager.getDefaults().getColor("Tree.selectionBackground"));
        }

        // C'est le noeud qui donne le libellé du label
        label.setText(node.toString());
        
        if (node.toString().contains(getClass().getPackage().getName())) {
        	label.setForeground(new Color(40, 40, 40));
        }
        
        // Pour les répertoires
        if (userobject == null) {
        	
        }
        else if (userobject.equals("Plugins") || userobject.equals("Components")) {
        	label.setIcon(getIcon("plugin"));
        }
        else if (userobject.equals("Events")) {
        	label.setIcon(getIcon("events"));
        }

        // Pour les vues
        else if (userobject instanceof AppViewController) {
        	AppViewController<?,?,?,?> ctrl = (AppViewController<?,?,?,?>) userobject;
        	if (ctrl.getView() == null) {
        		label.setIcon(getIcon("exception"));
        	}
        	else if (ctrl.getView().isVisible()) {
        		label.setIcon(getIcon("visible"));
        	}
        	else {
        		label.setIcon(getIcon("hidden"));
        	}
        }
        
        // Pour les applications
        else if (userobject instanceof App) {
        	final App app = (App) userobject;
        	if (app.getState() instanceof PausedState) {
        		label.setIcon(getIcon("pause"));
        	}
        	else if (app.getState() instanceof StartedState) {
        		label.setIcon(getIcon("play"));
        	}
        	else {
        		label.setIcon(getIcon("stop"));
        	}
        }
        
        // Pour activités
        else if (userobject instanceof AppActivity) {
        	final AppActivity activity = (AppActivity) userobject;
        	if (activity.isStarted()) {
        		label.setIcon(getIcon("play"));
        	}
        	else {
        		label.setIcon(getIcon("stop"));
        	}
        }
        
        // Pour les listeners
        else if (userobject instanceof EventCallback) {
        	
        	if (userobject instanceof CallbackEventListener) {
        		label.setIcon(getIcon("enum_tsk"));
        	}
        	else if (userobject instanceof CallbackEventSource) {
        		label.setIcon(getIcon("synch_co"));
        	}
        	else if (userobject instanceof CallbackMultipleEventIListener) {
        		label.setIcon(getIcon("interface_tsk"));
        	}
        	else if (userobject instanceof CallbackSingleEventIListener) {
        		label.setIcon(getIcon("interface_tsk"));
        	}
        	else if (userobject instanceof CallbackSingleEventMethod) {
        		label.setIcon(getIcon("interface_tsk"));
        	}
        	else if (userobject instanceof CallbackSingleRunnable) {
        		label.setIcon(getIcon("interface_tsk"));
        	}
        }
        
        // Pour les listeners
        else if (userobject instanceof Listener) {
        	label.setIcon(getIcon("interface_tsk"));
        }

        // On renvoie le label
        return label;
        
    }
}
