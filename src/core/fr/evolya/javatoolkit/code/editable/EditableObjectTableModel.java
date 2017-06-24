package fr.evolya.javatoolkit.code.editable;

import javax.swing.table.AbstractTableModel;


public class EditableObjectTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private EditableProperty<?>[] _properties;

	public EditableObjectTableModel(IEditableObject object) {
		_properties = object.getEditableProperties();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return _properties.length + getRowCount(_properties);
	}
	
	protected static int getRowCount(EditableProperty<?>[] properties) {
		int count = 0;
		for (EditableProperty<?> p : properties) {
			if (IEditableObject.class.isAssignableFrom(p.getType())) {
				// TODO Trouver un moyen de sonder l'objet en statique, pour récupérer
				// la liste des champs éditables.
			}
		}
		return count;
	}
	
	protected static boolean isRawType(Object o) {
		if (o instanceof String) return true;
		if (o instanceof Boolean) return true;
		if (o instanceof Double) return true;
		if (o instanceof Float) return true;
		if (o instanceof Integer) return true;
		return false;
	}
	
	protected static boolean isRawType(Class<?> c) {
		if (c == String.class) return true;
		if (c == Boolean.class) return true;
		if (c == Double.class) return true;
		if (c == Float.class) return true;
		if (c == Integer.class) return true;
		return false;
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Name";
		}
		return "Value";
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column == 0) {
			return _properties[row].getName();
		}
		if (isRawType(_properties[row].getType())) {
			return _properties[row].getValue() + "";
		}
		return _properties[row].getValue();
	}
	
	@Override
	public void setValueAt(Object value, int row, int column) {

	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (row == 0) {
			return false;
		}
		return _properties[row].isEditable()
			&& isRawType(_properties[row].getType());
	}
}
