package fr.evolya.javatoolkit.gui.swing.anglepicker;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 * 
 * @author matthieu.lhotellerie
 * 
 * @param <C>
 */
public abstract class AComponentUI<C extends JComponent> extends ComponentUI {
    /** The parent component associated */
    protected C component;

    @SuppressWarnings("unchecked")
    @Override
    public void installUI(JComponent c) {
        this.component = (C) c;

        installDefaults();
        installComponents();
        installListeners();
    }

    @Override
    public void uninstallUI(JComponent c) {
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
    }

    /** Installs default settings for the associated component. */
    abstract public void installDefaults();

    /** Installs listeners for the associated component. */
    abstract public void installListeners();

    /** Installs components for the associated component. */
    abstract public void installComponents();

    /** Uninstalls default settings for the associated component. */
    abstract public void uninstallDefaults();

    /** Uninstalls listeners for the associated component. */
    abstract public void uninstallListeners();

    /** Uninstalls components for the associated component. */
    abstract public void uninstallComponents();
}