package fr.evolya.javatoolkit.gui.swing.vuemetrenav;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.HashMap;
import java.util.Map;

import fr.evolya.javatoolkit.gui.swing.IColorizable;
import fr.evolya.javatoolkit.gui.swing.IMultiColorizable;

/**
 *
 * @author rbello
 *
 */
public class CanvasVueMetreNav extends Canvas implements IMultiColorizable {

	private static final long serialVersionUID = -3884993844416456928L;

	// Valeur
	protected int value = 0; // sur 100
    
    // Couleurs
    protected Map<String, IColorizable> colorizableElements = null;
    protected Color arcColor = Color.white;
    protected Color bgColor = Color.black;
    protected Color valuesColor = Color.red;
    protected Color pointerColor = Color.red;
    
    // Méthode DrawValueText
    protected FontRenderContext frc;
    protected Font f;
    
    public CanvasVueMetreNav() {
        initColors();
    }

    public void setValue(int i) {
        if (i < 0 || i > 100) return;
        if (value == i) return;
        value = i;
        repaint();
    }
    
    private void initColors() {
        colorizableElements = new HashMap<String, IColorizable>();
        colorizableElements.put("vuemetre-background", new IColorizable() {
            public void colorize(Color color) {
                bgColor = color;
            }
        });
        colorizableElements.put("vuemetre-arc", new IColorizable() {
            public void colorize(Color color) {
                arcColor = color;
            }
        });
        colorizableElements.put("vuemetre-valuestext", new IColorizable() {
            public void colorize(Color color) {
                valuesColor = color;
            }
        });
        colorizableElements.put("vuemetre-pointer", new IColorizable() {
            public void colorize(Color color) {
            	pointerColor = color;
            }
        });
    }
    
    public Map<String, IColorizable> getColorizableElements() {
        return colorizableElements;
    }

    public boolean colorize(String componentName, Color color) {
        java.util.Iterator<String> keySet = colorizableElements.keySet().iterator();
        while (keySet.hasNext()) {
            String key = keySet.next();
            if (key.equalsIgnoreCase(componentName)) {
                colorizableElements.get(key).colorize(color);
                return true;
            }
        }
        return false;
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        paintBackground(g2);
        paintArc(g2);
    }

    private void paintArc(Graphics2D g2) {
        g2.setColor(arcColor);
        int r = 40; // rayon
        for (int i = 0; i <= 50; i++) {
            int x1 = (int) (r * Math.sin(i));
            int y1 = (int) (r * Math.cos(i));
            g2.drawRect(x1, y1, x1, y1);
        }
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(bgColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    protected void drawValueText(Graphics2D g2, String s, int x, int y) {
        if (frc == null) frc = g2.getFontRenderContext();
        if (f == null) f = new Font("Helvetica", Font.PLAIN, 8);
        TextLayout tl = new TextLayout(s, f, frc);
        g2.setColor(valuesColor);
        tl.draw(g2, x, y);
    }
}
