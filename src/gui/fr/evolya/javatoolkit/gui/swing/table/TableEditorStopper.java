package fr.evolya.javatoolkit.gui.swing.table;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Permet de regler le bug "Cell editing does not complete when JTable loses focus"
 * 
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4503845
 * 
 */
public class TableEditorStopper extends FocusAdapter implements
		PropertyChangeListener {
	
	private Component focused;
	private JTable table;

	public TableEditorStopper(JTable table) {
		this.table = table;
		table.addPropertyChangeListener("tableCellEditor", this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (focused != null) {
			focused.removeFocusListener(this);
		}
		focused = table.getEditorComponent();
		if (focused != null) {
			focused.addFocusListener(this);
		}
	}

	public void focusLost(FocusEvent e) {
		if (focused != null) {
			focused.removeFocusListener(this);
			focused = e.getOppositeComponent();
			if (table == focused || table.isAncestorOf(focused)) {
				focused.addFocusListener(this);
			}
			else {
				focused = null;
				TableCellEditor editor = table.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
			}
		}
	}

}
