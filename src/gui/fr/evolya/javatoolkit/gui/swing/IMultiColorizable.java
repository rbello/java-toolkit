package fr.evolya.javatoolkit.gui.swing;

import java.awt.Color;

/**
 *
 * @author rbello
 *
 */
public interface IMultiColorizable {

    public java.util.Map<String, IColorizable> getColorizableElements();
    
    public boolean colorize(String componentName, Color color);
    
}
