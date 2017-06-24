package fr.evolya.javatoolkit.gui.swing.anglepicker.listener;

import java.util.EventListener;


/**
 * 
 * @author matthieu.lhotellerie
 * 
 */
public interface AngleListener extends EventListener {

    /**
     * Event send for each time the angle value is modified.
     * 
     * @param e
     */
    public void angleChanging(AngleEvent e);

    /**
     * Event send when the angle is changed by the user, <i>i.e.</i> when mouse
     * is released.
     * 
     * @param e
     */
    public void angleChanged(AngleEvent e);
}
