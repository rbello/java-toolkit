package fr.evolya.javatoolkit.gui.swing;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.gui.ViewBounds;
import fr.evolya.javatoolkit.gui.animation.AnimationConfig;
import fr.evolya.javatoolkit.gui.animation.Timeline;
import fr.evolya.javatoolkit.gui.animation.Timeline.TimelineState;
import fr.evolya.javatoolkit.gui.animation.callback.TimelineCallbackAdapter;
import fr.evolya.javatoolkit.gui.animation.interpolator.CorePropertyInterpolators;
import fr.evolya.javatoolkit.gui.swing.animation.AWTPropertyInterpolators;

public class SwingHelper {
	
	/**
	 * Le logger de cette classe.
	 */
	public static final Logger LOGGER = Logs.getLogger("GUI");
	
	/**
	 * Indique si l'initialisation du système d'animation a déjà été fait.
	 */
	private static boolean INIT_ANIMATION = false;

	/**
	 * Initialiser le look&feel par défaut pour les applications faite avec le toolkit.
	 * Les looks and feel disponibles sont :
			SubstanceAutumnLookAndFeel 
			SubstanceBusinessBlackSteelLookAndFeel 
			SubstanceBusinessBlueSteelLookAndFeel 
			SubstanceBusinessLookAndFeel 
			SubstanceChallengerDeepLookAndFeel 
			SubstanceCremeCoffeeLookAndFeel 
			SubstanceCremeLookAndFeel 
			SubstanceDustCoffeeLookAndFeel 
			SubstanceDustLookAndFeel 
			SubstanceEmeraldDuskLookAndFeel 
			SubstanceGeminiLookAndFeel 
			SubstanceGraphiteAquaLookAndFeel 
			SubstanceGraphiteGlassLookAndFeel 
			SubstanceGraphiteLookAndFeel 
			SubstanceMagellanLookAndFeel 
			SubstanceMistAquaLookAndFeel 
			SubstanceMistSilverLookAndFeel 
			SubstanceModerateLookAndFeel 
			SubstanceNebulaBrickWallLookAndFeel 
			SubstanceNebulaLookAndFeel 
			SubstanceOfficeBlue2007LookAndFeel 
			SubstanceOfficeSilver2007LookAndFeel
			SubstanceRavenLookAndFeel 
			SubstanceSaharaLookAndFeel 
			SubstanceTwilightLookAndFeel 
	 */
    public static void initLookAndFeel() {
    	
    	// On détecte la version V6 de la librairie substance
    	String className = "org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel";
    	try {
    		Class.forName(className);
    	}
    	catch (ClassNotFoundException ex1) {
        	// On détecte la version V5 de la librairie substance
        	className = "org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel";
        	try {
        		Class.forName(className);
        	}
        	catch (ClassNotFoundException ex2) {
        		// On a rien trouvé, le changement du look&feel ne pourra avoir lieu
        		if (LOGGER.isLoggable(Logs.WARNING)) {
        			LOGGER.log(Logs.WARNING, "Unable to detect Substance look and feel. The library may not be loaded.");
        		}
        		// Fin du processus
        		return;
        	}
    	}
    	
    	// On fait une copie de la classe du LAF 
    	final String lafClass = className;
    	
    	EventQueue.invokeLater(new Runnable() {
			public void run() {
				// On tente le chargement de laf
		    	try {
		    		UIManager.setLookAndFeel(lafClass);
		    		// Le chargement a réussi, on log
		    		if (LOGGER.isLoggable(Logs.INFO)) {
		    			LOGGER.log(Logs.INFO, "Look and feel loaded: " + lafClass);
		    		}
		    	}
		    	catch (Throwable ex) {
		    		// Le chargement a échoué, on log
		    		if (LOGGER.isLoggable(Logs.ERROR)) {
		    			LOGGER.log(Logs.ERROR, "Unable to load look and feel: " + lafClass, ex);
		    		}
		    	}
			}
		});
    	
    }
    
    public static void adjustGlobalFontSize(final int size) {
    	
    	// On force l'execution de cette opération dans le thread de l'UI
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        adjustGlobalFontSize(size);
                    }
                });
                return;
            } catch (Exception e) {}
        }
        
        // On parcours les styles de l'UI manager
        Enumeration<?> keySet = UIManager.getDefaults().keys();
        while (keySet.hasMoreElements()) {
            Object key = keySet.nextElement();
            Object value = UIManager.get(key);
            // On modifie la taille de toutes les polices
            if (value instanceof Font) {
                Font f = (Font)value;         
                FontUIResource resf = new FontUIResource(f.getName(), f.getStyle(), size);
                UIManager.put(key, resf);
            }
        }
        
    }

    /**
     * Configuration des animations par défaut pour SWING
     */
    public static void initSwingAnimations() {
    	if (INIT_ANIMATION) {
    		return;
    	}
    	INIT_ANIMATION = true;
    	AnimationConfig.getInstance().addPropertyInterpolatorSource(new CorePropertyInterpolators());
    	AnimationConfig.getInstance().addPropertyInterpolatorSource(new AWTPropertyInterpolators());
    }

	public static void groupAnimation(Object target, String propertyToInterpolate, long durationMs, Object[] values) {
		groupAnimation(target, propertyToInterpolate, durationMs, values, 0);
	}
	
	private static void groupAnimation(final Object target, final String propertyToInterpolate, final long durationMs, final Object[] values, final int index) {
		if (index >= values.length - 1) {
			return;
		}
		Timeline timeline = new Timeline(target);
		timeline.addPropertyToInterpolate(propertyToInterpolate, values[index], values[index+1]);
		timeline.setDuration(25);
		timeline.addCallback(new TimelineCallbackAdapter() {
			@Override
			public void onTimelineStateChanged(TimelineState old, TimelineState neo, float fraction, float position) {
				if (neo != TimelineState.IDLE) return;
				groupAnimation(target, propertyToInterpolate, durationMs, values, index+1);
			}
		});
		timeline.play();
	}
    
	public static void expandAll(JTree tree) {
	    TreeNode root = (TreeNode) tree.getModel().getRoot();
	    // Traverse tree from root
	    expandAll(tree, new TreePath(root), true);
	}
	
	public static void collapseAll(JTree tree) {
	    TreeNode root = (TreeNode) tree.getModel().getRoot();
	    // Traverse tree from root
	    expandAll(tree, new TreePath(root), false);
	}
	 
	private static void expandAll(JTree tree, TreePath parent, boolean expand) {
	    // Traverse children
	    TreeNode node = (TreeNode) parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
	        for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
	            TreeNode n = (TreeNode) e.nextElement();
	            TreePath path = parent.pathByAddingChild(n);
	            expandAll(tree, path, expand);
	        }
	    }
	 
	    // Expansion or collapse must be done bottom-up
	    if (expand) {
	        tree.expandPath(parent);
	    } else {
	        tree.collapsePath(parent);
	    }
	}
	
	@SuppressWarnings("rawtypes")
	public static ProxyFrameView toView(JFrame frame) {
		return new ProxyFrameView<JFrame>(frame);
	}

	@SuppressWarnings("rawtypes")
	public static ProxyPanelView toView(JPanel panel) {
		return new ProxyPanelView<JPanel>(panel);
	}

	public static ViewBounds bounds(Rectangle bounds) {
		return new ViewBounds(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}
	
	public static Rectangle bounds(ViewBounds bounds) {
		return new Rectangle((int)bounds.getX(), (int)bounds.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
	}

	public static boolean isInsideEDT() {
		return SwingUtilities.isEventDispatchThread();
	}

	public static void makeMeBounce(Component component) {
		
		// On fait les étapes de l'animation
		Rectangle[] bounds = new Rectangle[7]; 
		bounds[0] = component.getBounds();
		int x = bounds[0].x;
		int y = bounds[0].y;
		int w = bounds[0].width;
		int h = bounds[0].height;
		bounds[1] = new Rectangle(x + 6, y + 5, w, h);
		bounds[2] = new Rectangle(x + 2, y - 1, w, h);
		bounds[3] = new Rectangle(x - 1, y - 6, w, h);
		bounds[4] = new Rectangle(x - 3, y + 4, w, h);
		bounds[5] = new Rectangle(x - 5, y + 1, w, h);
		bounds[6] = bounds[0];
		
		// On lance une animation de groupe
		SwingHelper.groupAnimation(component, "bounds", 3, bounds);
	}
	
	private static final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	
	public static final String dispatchWindowClosingActionMapKey = 
		    "com.spodding.tackline.dispatch:WINDOW_CLOSING"; 
	
	public static void installEscapeCloseOperation(final JDialog dialog) { 
	    Action dispatchClosing = new AbstractAction() { 
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) { 
	            dialog.dispatchEvent(new WindowEvent( 
	                dialog, WindowEvent.WINDOW_CLOSING 
	            )); 
	        } 
	    }; 
	    JRootPane root = dialog.getRootPane(); 
	    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( 
	        escapeStroke, dispatchWindowClosingActionMapKey 
	    ); 
	    root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing); 
	}

	public static Component searchInTopHierarchy(Component c, Class<?> type) {
		if (c == null) {
			return null;
		}
		if (c.getParent() == null) {
			return null;
		}
		if (type.isInstance(c.getParent())) {
			return c.getParent();
		}
		return searchInTopHierarchy(c.getParent(), type);
	}

	public static Component getTopParent(Component component) {
		Component parent = getTopParentZ(component);
		return parent != component ? parent : null;
	}
	
	private static Component getTopParentZ(Component component) {
		if (component.getParent() == null) {
			return component;
		}
		return getTopParentZ(component.getParent());
	}
	
}
