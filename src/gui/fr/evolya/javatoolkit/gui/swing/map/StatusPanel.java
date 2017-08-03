package fr.evolya.javatoolkit.gui.swing.map;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class StatusPanel extends JPanel {

	private static final long serialVersionUID = -6855468458877911636L;

	private int _borderWidth;
	private State _state;
	
	public static enum State {
		WARNING(Color.YELLOW), ALERT(Color.RED);
		public final Color color;
		private State(Color color) {
			this.color = color;
		}
	}
	
	/**
	 * Create the panel.
	 */
	public StatusPanel() {
		setBackground(Color.BLACK);
		setForeground(Color.WHITE);
		setBorderWidth(10);
		setState(State.ALERT);
	}
	
	public int getBorderWidth() {
		return _borderWidth;
	}
	
	public void setBorderWidth(int value) {
		this._borderWidth = value;
	}

	public State getState() {
		return _state;
	}

	public void setState(State state) {
		this._state = state;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(_state.color);
		g.fillRect(0, 0, _borderWidth, getHeight());
		g.fillRect(getWidth() - _borderWidth, 0, _borderWidth, getHeight());
		g.fillRect(0, 0, getWidth(), _borderWidth);
		g.fillRect(0, getHeight() - _borderWidth, getWidth(), _borderWidth);
	}

}
