package fr.evolya.javatoolkit.gui.swing.map.buildings;

import java.awt.Rectangle;

import fr.evolya.javatoolkit.gui.swing.map.IMapComponent;

public class RectangleRoom implements IMapComponent {

	private Rectangle size;

	public RectangleRoom(Rectangle size) {
		this.size = size;
	}
	
	public RectangleRoom(int x, int y, int width, int height) {
		this.size = new Rectangle(x, y, width, height);
	}

}
