package fr.evolya.javatoolkit.code.editable;

public class EditableProperty<T> {

	/**
	 * Le nom de la property.
	 */
	private String _name;
	
	/**
	 * Indique si le champ est éditable.
	 */
	private boolean _isEditable;

	/**
	 * Le type de valeur du champ.
	 */
	private Class<T> _type;

	/**
	 * La valeur actuelle du champ.
	 */
	private T _value;

	/**
	 * Indique si on peut mettre NULL.
	 */
	private boolean _isNullable;

	/**
	 * Constructeur.
	 */
	public EditableProperty(String name, Class<T> type, boolean isEditable) {
		_name = name;
		_type = type;
		_isEditable = isEditable;
		_isNullable = true;
		_value = null;
	}
	
	/**
	 * Constructeur.
	 */
	public EditableProperty(String name, Class<T> type, boolean isEditable, T defaultValue) {
		_name = name;
		_type = type;
		_isEditable = isEditable;
		_isNullable = true;
		_value = defaultValue;
	}
	
	public String getName() {
		return _name;
	}

	public Class<T> getType() {
		return _type;
	}

	public T getValue() {
		return _value;
	}
	
	public T value() {
		return _value;
	}
	
	public EditableProperty<T> setValue(T value) {
		_value = value;
		return this;
	}
	
	public EditableProperty<T> value(T value) {
		_value = value;
		return this;
	}

	public boolean isEditable() {
		return _isEditable;
	}
	
	/**
	 * Modifier le status d'éditabilité de la property.
	 */
	public EditableProperty<T> setPropertyEditable(boolean editable) {
		_isEditable = editable;
		return this;
	}

	public boolean isNullable() {
		return _isNullable;
	}

	public boolean isNull() {
		return _value == null;
	}

}
