package fr.evolya.javatoolkit.gui.swing.viewport.layers;

import java.awt.Rectangle;

import javax.swing.JComponent;

import fr.evolya.javatoolkit.code.editable.EditableProperty;
import fr.evolya.javatoolkit.code.editable.IEditableObject;

public class Layer extends JComponent implements IEditableObject {
	
	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * La propriété : nom du calque.
	 */
	protected EditableProperty<String> _propertyLayerName =
		new EditableProperty<String>("LayerName", String.class, true);

	/**
	 * Mémorise la sélection.
	 */
	protected boolean _selected = false;

	/**
	 * La taille originale du calque.
	 */
	private Rectangle _prefered = new Rectangle();

	/**
	 * Modifie la taille originale du calque.
	 */
	public void setOriginalBounds(double x, double y, double w, double h) {
		setOriginalBounds(new Rectangle((int)x, (int)y, (int)w, (int)h));
	}
	
	/**
	 * Modifie la taille originale du calque.
	 */
	public void setOriginalBounds(Rectangle bounds) {
		_prefered = bounds;
		setBounds(bounds);
	}
	
	public Rectangle getOriginalBounds() {
		return _prefered;
	}

	public EditableProperty<String> getPropertyLayerName() {
		return _propertyLayerName;
	}
	
	@Override
	public EditableProperty<?>[] getEditableProperties() {
		return new EditableProperty<?>[] {
				_propertyLayerName
		};
	}

	public void setSelected(boolean selected) {
		_selected = selected;
	}
	
	public double getResizedRatio() {
		return getBounds().getWidth() / getOriginalBounds().getWidth();
	}
	
}
