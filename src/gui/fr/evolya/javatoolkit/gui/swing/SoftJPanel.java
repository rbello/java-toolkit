package fr.evolya.javatoolkit.gui.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SoftJPanel extends JPanel {
	
    private static final JButton lafDeterminer = new JButton();
    
    private static final long serialVersionUID = 1L;
    private boolean rectangularLAF;
    private float alpha = 1f;

    public SoftJPanel() {
		super();
	}

	public SoftJPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public SoftJPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public SoftJPanel(LayoutManager layout) {
		super(layout);
	}

	public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        if (rectangularLAF && isBackgroundSet()) {
            Color c = getBackground();
            g2.setColor(c);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g2);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        lafDeterminer.updateUI();
        rectangularLAF = lafDeterminer.isOpaque();
    }
    
}