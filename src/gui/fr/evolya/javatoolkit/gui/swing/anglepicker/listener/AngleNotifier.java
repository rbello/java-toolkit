package fr.evolya.javatoolkit.gui.swing.anglepicker.listener;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author matthieu.lhotellerie
 */
public class AngleNotifier {
    /** */
    Set<AngleListener> listeners = new HashSet<AngleListener>();

    /**
     * 
     * @param l
     */
    public void addAngleListener(AngleListener listener) {
        if (listener == null) {
            return;
        }

        this.listeners.add(listener);
    }

    /**
     * 
     * @param l
     */
    public void removeAngleListener(AngleListener listener) {
        if (listener == null) {
            return;
        }

        this.listeners.remove(listener);
    }

    /**
     * 
     * @return
     */
    public AngleListener[] getAngleListeners() {
        return this.listeners.toArray(new AngleListener[0]);
    }

    /**
     * 
     * @param event
     */
    public void fireAngleChanged(AngleEvent event) {
        double oldValue = event.getPreviousAngleRad();
        double newValue = event.getCurrentAngleRad();

        if (oldValue == newValue)
            return;

        for (AngleListener listener : this.listeners) {
            listener.angleChanged(event);
        }
    }

    /**
     * 
     * @param event
     */
    public void fireAngleChanging(AngleEvent event) {
        double oldValue = event.getPreviousAngleRad();
        double newValue = event.getCurrentAngleRad();

        if (oldValue == newValue)
            return;

        for (AngleListener listener : this.listeners) {
            listener.angleChanging(event);
        }
    }
}
