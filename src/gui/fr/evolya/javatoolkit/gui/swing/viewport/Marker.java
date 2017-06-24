package fr.evolya.javatoolkit.gui.swing.viewport;

import java.awt.event.MouseEvent;

import fr.evolya.javatoolkit.math.vecmath.Point2d;

public abstract class Marker {

	private Cartesian _position;
	private Point2d _point;

	private Marker(Cartesian position) {
		_position = position;
	}
	
	public Cartesian getPosition() {
		return _position;
	}
	
	public Marker setPosition(Cartesian position) {
		_position = position;
		_point = null;
		return this;
	}
	
	public Point2d getDisplayPosition() {
		return _point;
	}
	
	public Marker setDisplayPosition(Point2d point) {
		_point = point;
		return this;
	}
	
	public boolean equals(Marker m) {
		if (m == null) return false;
		return m.getPosition().equals(_position);
	}
	
	public abstract void onClick(MouseEvent e);
	
	public static class PointMarker extends Marker {
		
		public PointMarker(Cartesian position) {
			super(position);
		}

		@Override
		public void onClick(MouseEvent e) {
			System.out.println("Clic sur un marker!");
		}
		
	}
	
	public static class BezierMarker extends Marker {
		
		public BezierMarker(Cartesian position) {
			super(position);
		}

		@Override
		public void onClick(MouseEvent e) {
			
		}
		
	}
	
}
