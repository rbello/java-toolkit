package fr.evolya.javatoolkit.gui.swing.viewport;

import fr.evolya.javatoolkit.code.editable.EditableProperty;

public class Cartesian {

	public final EditableProperty<Double> X =
		new EditableProperty<Double>("X", Double.class, true, 0d);
	
	public final EditableProperty<Double> Y =
		new EditableProperty<Double>("Y", Double.class, true, 0d);
	
	public final EditableProperty<Double> Z =
		new EditableProperty<Double>("Z", Double.class, true, 0d);
	
	public Cartesian() {
		this(0, 0, 0);
	}
	
	public Cartesian(Cartesian c) {
		this(c.X.getValue(), c.Y.getValue(), c.Z.getValue());
	}
	
	public Cartesian(double x, double y, double z) {
		X.setValue(x);
		Y.setValue(y);
		Z.setValue(z);
	}

	@Override
	public String toString() {
		return "[X="
			+ X.getValue() + " Y=" + Y.getValue()
			+ " Z=" + Z.getValue() + "]";
	}

	public static Cartesian empty() {
		return new Cartesian(0, 0, 0);
	}
	
	public boolean equals(Cartesian c) {
		if (c == null) return false;
		if (X.getValue() != c.X.getValue()) return false;
		if (Y.getValue() != c.Y.getValue()) return false;
		if (Z.getValue() != c.Z.getValue()) return false;
		return true;		
	}
	
}
