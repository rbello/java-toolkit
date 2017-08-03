package fr.evolya.javatoolkit.gui.swing.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

public class MapPanel extends JPanel {

	private static final long serialVersionUID = -5755407202154464006L;

	private IMap map;

	/**
	 * Create the panel.
	 */
	public MapPanel() {
		setBackground(Color.BLACK);
		setForeground(Color.WHITE);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (map != null) map.paint(g, this);
	}

	public void setMap(IMap map) {
		this.map = map;
		this.map.setViewport(new Rectangle(0, 0, getWidth(), getHeight()));
	}
	
	public IMap getMap() {
		return this.map;
	}

}
