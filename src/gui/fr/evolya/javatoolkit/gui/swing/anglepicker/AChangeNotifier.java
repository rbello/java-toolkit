package fr.evolya.javatoolkit.gui.swing.anglepicker;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * Abstract class to (un)register listener on a change event. Each changement
 * should be followed by a call of <code>fireStateChanged()</code> method.
 * 
 * @author matthieu.lhotellerie
 */
public class AChangeNotifier {
    /** Store listener to fire when a change occure */
    protected EventListenerList listenerList = new EventListenerList();

    /** The event */
    ChangeEvent event = null;

    /**
     * 
     * @param listener
     */
    public void addChangeListener(ChangeListener listener) {
        this.listenerList.add(ChangeListener.class, listener);
    }

    /**
     * 
     * @param listener
     */
    public void removeChangeListener(ChangeListener listener) {
        this.listenerList.remove(ChangeListener.class, listener);
    }

    /**
     * This method shoud be called when a state has changed, and it will inform
     * any registered listener.
     */
    protected void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (this.event == null)
                    this.event = new ChangeEvent(this);

                ((ChangeListener) listeners[i + 1]).stateChanged(this.event);
            }
        }
    }

    /**
     * 
     * @return
     */
    public ChangeListener[] getChangeListeners() {
        return this.listenerList.getListeners(ChangeListener.class);
    }
}
