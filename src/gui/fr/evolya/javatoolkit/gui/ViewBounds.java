package fr.evolya.javatoolkit.gui;

public class ViewBounds {

	private double _x;
	private double _y;
	private double _w;
	private double _h;

	/**
	 * 
	 *  !!! ATTENTION !!!
	 *  
	 *  Ne jamais faire de constructeur avec un Rectangle ou tout autre objet
	 *  lié à un framework (swing, android, etc...)
	 *  
	 *  Cette classe doit absolument rester indépendante.
	 * 
	 * 
	 */
	public ViewBounds() {
	}
	
	public ViewBounds(double x, double y, double w, double h) {
		_x = x;
		_y = y;
		_w = w;
		_h = h;
	}

	public double getX() {
		return _x;
	}

	public void setX(double _x) {
		this._x = _x;
	}

	public double getY() {
		return _y;
	}

	public void setY(double _y) {
		this._y = _y;
	}

	public double getWidth() {
		return _w;
	}

	public void setWidth(double _w) {
		this._w = _w;
	}

	public double getHeight() {
		return _h;
	}

	public void setHeight(double _h) {
		this._h = _h;
	}
	
	public String toString() {
		return "[" + _x + "," + _y + " " + _w + "," + _h + "]";
	}
	
}
