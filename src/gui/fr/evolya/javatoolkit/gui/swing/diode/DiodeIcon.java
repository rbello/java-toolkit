package fr.evolya.javatoolkit.gui.swing.diode;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author rbello
 *
 */
public class DiodeIcon extends JLabel {
    
	private static final long serialVersionUID = -6853501546781719777L;
	
	public static Icon yes = null;
    public static Icon no  = null;
    public static Icon off = null;
    
    boolean status = false;

    public DiodeIcon() {
        if (yes == null) {
            yes = new ImageIcon(getClass().getResource("/fr/evolya/javatoolkit/gui/swing/diode/yes.gif"));
            no  = new ImageIcon(getClass().getResource("/fr/evolya/javatoolkit/gui/swing/diode/no.gif"));
            off = new ImageIcon(getClass().getResource("/fr/evolya/javatoolkit/gui/swing/diode/off.gif"));
        }
        setIcon(off);
        setText("");
        setBounds(0, 0, 17, 14);
    }
    
    public DiodeIcon(State state) {
    	this();
    	state.setDiode(this);
    }
    
    public void setDiode(boolean set) {
        if (status != set) {
            status = set;
            setIcon(set ? yes : no);
        }
    }
    
    public void setEnabled(boolean set) {
        if (set) setIcon(status ? yes : no);
        else setIcon(off);
    }

	public void switchDiode() {
		setDiode(!status);
	}

	public void switchDiodeRed() {
		if (status) {
			setIcon(off);
			status = false;
		}
		else {
			setIcon(no);
			status = true;
		}
	}
	
	public void switchDiodeGreen() {
		if (status) {
			setIcon(off);
			status = false;
		}
		else {
			setIcon(yes);
			status = true;
		}
	}
	
	public static class State {
		private DiodeIcon icon;
		protected void setDiode(DiodeIcon icon) {
			this.icon = icon;
		}
		public DiodeIcon setDiode(boolean set) {
			icon.setDiode(set);
			return icon;
		}
		public DiodeIcon setEnabled(boolean set) {
			icon.setEnabled(set);
			return icon;
		}
	}

}
