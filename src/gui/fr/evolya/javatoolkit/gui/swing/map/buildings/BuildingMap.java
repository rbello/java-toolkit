package fr.evolya.javatoolkit.gui.swing.map.buildings;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import fr.evolya.javatoolkit.gui.swing.map.IMap;
import fr.evolya.javatoolkit.gui.swing.map.IMapComponent;
import fr.evolya.javatoolkit.gui.swing.map.MapPanel;

public class BuildingMap implements IMap {

	private List<IMapComponent> components;
	private int width;
	private int height;
	private Rectangle viewport;
	
	public BuildingMap(int width, int height) {
		this.width = width;
		this.height = height;
		this.components = new ArrayList<>();
	}

	public void add(IMapComponent component) {
		this.components.add(component);
	}
	
	public List<IMapComponent> getComponents() {
		return new ArrayList<>(this.components);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void paint(Graphics graphic, MapPanel panel) {
		
		graphic.setColor(panel.getForeground());
		
		double ratio = viewport.getWidth() / (double)panel.getWidth();
		System.out.println("Ratio = " + ratio);
		int step = 20;
		int w = width * 20; // meters to pixels
		
		
		for (int x = 0; x <= width; x++) {
			graphic.drawLine(x * step, 0, x * step, panel.getHeight());
		}
		for (int y = 0; y <= height; y++) {
			graphic.drawLine(0, y * step, panel.getWidth(), y * step);
		}
	}

	@Override
	public void setViewport(Rectangle viewport) {
		this.viewport = viewport;
	}

}
