package fr.evolya.javatoolkit.gui.swing.viewport;

import java.awt.Cursor;



public class MarkerSelection {

	public static Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	public static Cursor hoverCursor = new Cursor(Cursor.HAND_CURSOR);
	private Marker _marker;
	private Cursor _cursor;
	
	public MarkerSelection(Marker marker) {
		this(marker, defaultCursor);
	}
	
	public MarkerSelection(Marker marker, int cursor) {
		this(marker, new Cursor(cursor));
	}
	
	public MarkerSelection(Marker marker, Cursor cursor) {
		_marker = marker;
		_cursor = cursor;
	}

	public Marker getMarker() {
		return _marker;
	}
	
	public Cursor getCursor() {
		return _cursor;
	}

}
