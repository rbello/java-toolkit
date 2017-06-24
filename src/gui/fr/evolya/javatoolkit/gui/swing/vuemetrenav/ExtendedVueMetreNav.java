package fr.evolya.javatoolkit.gui.swing.vuemetrenav;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import fr.evolya.javatoolkit.gui.swing.IColorizable;
import fr.evolya.javatoolkit.gui.swing.IMultiColorizable;

/**
 *
 * @author  rbello
 * 
 */
public class ExtendedVueMetreNav extends javax.swing.JPanel implements IMultiColorizable, IColorizable {

	private static final long serialVersionUID = 739850973851040240L;
	
	private Map<String, IColorizable> colorizableElements = null;
    private CanvasVueMetreNav g2 = null;
    
    public ExtendedVueMetreNav() {
        initComponents();
        initGraphic();
        initColors();
    }

    public void setValue(int i) {
        g2.setValue(i);
    }

    private void initComponents() {

        setLayout(null);
    }

    private void initColors() {
        colorizableElements = new HashMap<String, IColorizable>();
        colorizableElements.put("panel-background", this);
    }

    public Map<String, IColorizable> getColorizableElements() {
        return colorizableElements;
    }

    public boolean colorize(String componentName, Color color) {
        java.util.Iterator<String> keySet = colorizableElements.keySet().iterator();
        while (keySet.hasNext()) {
            String key = keySet.next();
            if (key.equalsIgnoreCase(componentName)) {
                colorizableElements.get(key).colorize(color);
                return true;
            }
        }
        return false;
    }

    public void colorize(Color color) {
        setBackground(color);
    }

    private void initGraphic() {
        g2 = new CanvasVueMetreNav();
        g2.setBounds(0, 0, 200, 100);
        add(g2);
    }

}
