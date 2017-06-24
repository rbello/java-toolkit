package fr.evolya.javatoolkit.code.editable;

public interface IEditableObject {

	/**
	 * Renvoie la liste des propriétés editables sur l'objet.
	 */
	public EditableProperty<?>[] getEditableProperties();
	
}
