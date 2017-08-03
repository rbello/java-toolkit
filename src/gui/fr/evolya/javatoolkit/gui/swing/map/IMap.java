package fr.evolya.javatoolkit.gui.swing.map;

import java.awt.Graphics;
import java.awt.Rectangle;

public interface IMap {

	void paint(Graphics graphic, MapPanel panel);

	void setViewport(Rectangle viewport);

}
