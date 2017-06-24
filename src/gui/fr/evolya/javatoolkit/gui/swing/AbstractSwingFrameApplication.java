package fr.evolya.javatoolkit.gui.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import fr.evolya.javatoolkit.appstandard.AbstractSingleFrameApplication;
import fr.evolya.javatoolkit.code.Util;
import fr.evolya.javatoolkit.exceptions.AllreadyStartedException;

/**
 * Classe helper pour la création d'applications Swing basées sur une JFrame principale.
 *
 * @param <F> La classe de la JFrame
 * @param <C> La classe du controlleur de vue
 */
public abstract class AbstractSwingFrameApplication

	<F extends JFrame, C extends AbstractSwingViewController<F>>

	extends AbstractSingleFrameApplication<C, ProxyFrameView<F>, F, AWTEvent, Component> {

	public AbstractSwingFrameApplication(String appName, String appVersion, boolean isMainApplication) {
		super(appName, appVersion, isMainApplication);
	}
	
	public boolean enableSingletonBehavior() throws AllreadyStartedException {
		try {
			return super.enableSingletonBehavior();
		}
		catch (final AllreadyStartedException ex) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				});
			} catch (Exception e) {
				
			}
			throw ex;
		}
	}

}
