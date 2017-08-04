package fr.evolya.javatoolkit.gui.swing.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class StatusPanel extends JPanel {

	private static final long serialVersionUID = -6855468458877911636L;

	private State state;

	private int borderWidth = 6;
	private int cartoucheWidth = 50;
	private int cartoucheHeight = 15;

	private JLabel cartoucheLabelLeft;
	private JLabel cartoucheLabelRight;
	private JLabel cartoucheValueLeft;
	private JLabel cartoucheValueRight;
	private JLabel cartoucheLabelCenterBig;
	private JLabel cartoucheLabelCenterSmall;
	
	public static enum State {
		NORMAL(Color.GREEN), WARNING(Color.YELLOW), ALERT(Color.RED);
		public final Color color;
		private State(Color color) {
			this.color = color;
		}
	}
	
	/**
	 * Create the panel.
	 */
	public StatusPanel() {
		setLayout(null);
		setBackground(Color.BLACK);
		setForeground(Color.WHITE);
		setState(State.ALERT);
		
		cartoucheLabelLeft = new JLabel();
		cartoucheLabelLeft.setHorizontalAlignment(SwingConstants.CENTER);
		cartoucheLabelLeft.setFont(new Font("Arial", Font.BOLD, 12));
		add(cartoucheLabelLeft);
		cartoucheValueLeft = new JLabel();
		cartoucheValueLeft.setFont(new Font("Arial", Font.BOLD, 25));
		cartoucheValueLeft.setHorizontalAlignment(SwingConstants.CENTER);
		add(cartoucheValueLeft);
		
		cartoucheLabelRight = new JLabel();
		cartoucheLabelRight.setHorizontalAlignment(SwingConstants.CENTER);
		cartoucheLabelRight.setFont(new Font("Arial", Font.BOLD, 12));
		add(cartoucheLabelRight);
		cartoucheValueRight = new JLabel();
		cartoucheValueRight.setHorizontalAlignment(SwingConstants.CENTER);
		cartoucheValueRight.setFont(new Font("Arial", Font.BOLD, 25));
		add(cartoucheValueRight);
		
		cartoucheLabelCenterBig = new JLabel();
		cartoucheLabelCenterBig.setHorizontalAlignment(SwingConstants.CENTER);
		cartoucheLabelCenterBig.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 35));
		add(cartoucheLabelCenterBig);
		cartoucheLabelCenterSmall = new JLabel();
		cartoucheLabelCenterSmall.setHorizontalAlignment(SwingConstants.CENTER);
		cartoucheLabelCenterSmall.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
		add(cartoucheLabelCenterSmall);
		
		cartoucheLabelLeft.setText("LEFT");
		cartoucheValueLeft.setText("0");
		cartoucheLabelRight.setText("RIGHT");
		cartoucheValueRight.setText("0");
		cartoucheLabelCenterBig.setText("BIG");
		cartoucheLabelCenterSmall.setText("SMALL");
		
		replace();
	}
	
	protected void replace() {
		
		cartoucheLabelLeft.setBounds(borderWidth, borderWidth, cartoucheWidth, cartoucheHeight);
		cartoucheValueLeft.setBounds(borderWidth, borderWidth + borderWidth / 2 + cartoucheHeight, cartoucheWidth, getHeight() - borderWidth * 2 - borderWidth / 2 - cartoucheHeight);
		
		cartoucheLabelRight.setBounds(getWidth() - cartoucheWidth - borderWidth, borderWidth, cartoucheWidth, cartoucheHeight);
		cartoucheValueRight.setBounds(getWidth() - cartoucheWidth - borderWidth, borderWidth + borderWidth / 2 + cartoucheHeight, cartoucheWidth, getHeight() - borderWidth * 2 - borderWidth / 2 - cartoucheHeight);
		
		cartoucheLabelCenterBig.setBounds(borderWidth * 2 + cartoucheWidth, borderWidth, getWidth() - 4 * borderWidth - 2 * cartoucheWidth, getHeight() - 2 * borderWidth - borderWidth / 2 - cartoucheHeight);
		cartoucheLabelCenterSmall.setBounds(borderWidth * 2 + cartoucheWidth, getHeight() - borderWidth - cartoucheHeight, getWidth() - 4 * borderWidth - 2 * cartoucheWidth, cartoucheHeight);
	}
	
	public int getBorderWidth() {
		return borderWidth;
	}
	
	public void setBorderWidth(int value) {
		this.borderWidth = value;
	}
	
	public int getCartoucheWidth() {
		return cartoucheWidth;
	}

	public void setCartoucheWidth(int cartoucheWidth) {
		this.cartoucheWidth = cartoucheWidth;
	}

	public int getCartoucheHeight() {
		return cartoucheHeight;
	}

	public void setCartoucheHeight(int cartoucheHeight) {
		this.cartoucheHeight = cartoucheHeight;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		replace();
		g.setColor(state.color);
		// Border
		g.fillRect(0, 0, borderWidth, getHeight());
		g.fillRect(getWidth() - borderWidth, 0, borderWidth, getHeight());
		g.fillRect(0, 0, getWidth(), borderWidth);
		g.fillRect(0, getHeight() - borderWidth, getWidth(), borderWidth);
		// Left
		g.fillRect(borderWidth, borderWidth + cartoucheHeight, cartoucheWidth, borderWidth / 2);
		g.fillRect(borderWidth + cartoucheWidth, borderWidth, borderWidth, getWidth() - borderWidth);
		// Right
		g.fillRect(getWidth() - borderWidth - cartoucheWidth, borderWidth + cartoucheHeight, cartoucheWidth, borderWidth / 2);
		g.fillRect(getWidth() - borderWidth*2 - cartoucheWidth, borderWidth, borderWidth, getHeight() - borderWidth *2);
		// Separator
		g.fillRect(borderWidth * 2 + cartoucheWidth, getHeight() - borderWidth - cartoucheHeight - borderWidth / 2, getWidth() - 2 * cartoucheWidth - borderWidth * 4, borderWidth / 2);
	}
	
	public JLabel getCartoucheLabelLeft() {
		return cartoucheLabelLeft;
	}

	public JLabel getCartoucheLabelRight() {
		return cartoucheLabelRight;
	}

	public JLabel getCartoucheValueLeft() {
		return cartoucheValueLeft;
	}

	public JLabel getCartoucheValueRight() {
		return cartoucheValueRight;
	}

	public JLabel getCartoucheLabelCenterBig() {
		return cartoucheLabelCenterBig;
	}

	public JLabel getCartoucheLabelCenterSmall() {
		return cartoucheLabelCenterSmall;
	}

	public void setCartoucheInfo(String text) {
		cartoucheLabelLeft.setText(text);
		cartoucheLabelRight.setText(text);
	}

	public void setCartoucheLevel(int value) {
		cartoucheValueLeft.setText("" + value);
		cartoucheValueRight.setText("" + value);
	}

	public void setMainText(String text) {
		cartoucheLabelCenterBig.setText(text);
	}

	public void setInfoText(String text) {
		cartoucheLabelCenterSmall.setText(text);
	}

}
