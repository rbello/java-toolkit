package fr.evolya.javatoolkit.math;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.evolya.javatoolkit.math.vecmath.Point2d;

public class ExtrapolatorTest extends JFrame {

	private static final long serialVersionUID = 1L;

	private static enum InterpolaType {
		LINEAR, QUADRATIVE, COSINE, CUBIC, HERMITE
	};
	
	private JPanel contentPane;
		
	private double _interpolastep = 1;
	
	private InterpolaType _interpolatype = InterpolaType.LINEAR;

	private JPanel _panel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExtrapolatorTest frame = new ExtrapolatorTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ExtrapolatorTest() {
		setTitle("Extrapolator Test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 533, 428);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		final JComboBox<InterpolaType> comboBox = new JComboBox<InterpolaType>();
		comboBox.setModel(new DefaultComboBoxModel<InterpolaType>(InterpolaType.values()));
		
		final JSlider slider = new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(1000);
		slider.setValue(1000);
		
		_panel = new JPanel() {

			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				
				Dimension size = getSize();

				// Liste des points à afficher (exprimé en %, repère en bas à droite)
				Point2d[] points = {
						new Point2d(25, 14),
						new Point2d(19, 21),
						new Point2d(16, 28),
						new Point2d(9, 50),
						new Point2d(9, 65),
						new Point2d(9, 74),
						new Point2d(10, 80),
						new Point2d(16, 89),
						new Point2d(26, 91),
						new Point2d(36, 91),
						new Point2d(44, 89),
						new Point2d(51, 80),
						new Point2d(52, 66),
						new Point2d(50, 58),
						new Point2d(53, 49),
						new Point2d(61, 42),
						new Point2d(83, 44),
						new Point2d(90, 38),
						new Point2d(92, 26),
						new Point2d(89, 17),
						new Point2d(83, 11),
						new Point2d(71, 11),
						new Point2d(59, 11),
						new Point2d(49, 16),
						new Point2d(42, 18),
						new Point2d(31, 11)
				};
				
				// Fond
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, size.width, size.height);
				
				// Affichage des points
				g.setColor(Color.GRAY);
				Point2d[] realPoints = new Point2d[points.length];
				for (int i = 0; i < points.length; i++) {
					realPoints[i] = new Point2d(points[i].x / 100d * size.width, (100d - points[i].y) / 100d * size.height);
					if (i > 0) {
						g.drawLine(
								(int)realPoints[i-1].x,
								(int)realPoints[i-1].y,
								(int)realPoints[i].x,
								(int)realPoints[i].y
						);
					}
				}
				for (int i = 0; i < points.length; i++) {
					g.setColor(Color.WHITE);
					g.fillOval((int)realPoints[i].x - 2, (int)realPoints[i].y - 2, 4, 4);
					g.setColor(Color.GRAY);
					g.drawOval((int)realPoints[i].x - 2, (int)realPoints[i].y - 2, 4, 4);
					g.drawString("P"+i, (int)realPoints[i].x + 5, (int)realPoints[i].y - 5);
				}
				
				// Interpolation
				g.setColor(Color.RED);
				for (int i = 0; i < points.length; i++) {
					for (double j = _interpolastep; j > 0; j -= 0.01) {
						Point2d p = null;
						if (_interpolatype == InterpolaType.QUADRATIVE) {
							p = getQuadrativeInterpolation(j, realPoints, i);
						}
						else if (_interpolatype == InterpolaType.COSINE) {
							p = getCosineInterpolation(j, realPoints, i);
						}
						else if (_interpolatype == InterpolaType.LINEAR) {
							p = getLinearInterpolation(j, realPoints, i);
						}
						else if (_interpolatype == InterpolaType.CUBIC) {
							p = getCubicInterpolation(j, realPoints, i);
						}
						else if (_interpolatype == InterpolaType.HERMITE) {
							p = getHermiteInterpolation(j, realPoints, i);
						}
						if (p != null) {
							g.fillOval((int)p.x - 2, (int)p.y - 2, 4, 4);
						}
					}
				}
			}

		};
		
		_panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				double x = (double)e.getX() / (double)_panel.getWidth();
				double y = ((double)_panel.getHeight() - (double)e.getY()) / (double)_panel.getHeight();
				System.out.println("new Point2d(" + Math.round(x * 100) + ", " + Math.round(y * 100) + ")");
			}
		});

		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_interpolatype = InterpolaType.valueOf(comboBox.getSelectedItem().toString());
				_panel.repaint();
			}
		});
		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				_interpolastep = ((double)slider.getValue() / 1000d);
				_panel.repaint();
			}
		});
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(slider, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
					.addContainerGap())
				.addComponent(_panel, GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
					.addComponent(_panel, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(slider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		);
		contentPane.setLayout(gl_contentPane);
	}
	
	private Point2d getQuadrativeInterpolation(double t, Point2d[] points, int i) {
		if (i < 0 || i + 2 >= points.length) return null;
		double oneMinusTS = (1-t) * (1-t);
		double TS = t*t;
		Point2d result = new Point2d();
	    result.x = oneMinusTS*points[i].x + 2*(1-t) * t * points[i+1].x + TS * points[i+2].x;
	    result.y = oneMinusTS*points[i].y + 2*(1-t) * t * points[i+1].y + TS * points[i+2].y;
	    return result;
	}
	
	private Point2d getCosineInterpolation(double t, Point2d[] points, int i) {
		if (i < 0 || i + 1 >= points.length) return null;
		Point2d result = new Point2d();
		result.x = points[i].x + (points[i+1].x - points[i].x) * t;
		result.y = Interpolation.CosineInterpolate(points[i].y, points[i+1].y, t);
		return result;
	}
	
	private Point2d getLinearInterpolation(double t, Point2d[] points, int i) {
		if (i < 0 || i + 1 >= points.length) return null;
		Point2d result = new Point2d();
		result.x = points[i].x + (points[i+1].x - points[i].x) * t;
		result.y = Interpolation.LinearInterpolate(points[i].y, points[i+1].y, t);
		return result;
	}
	
	private Point2d getCubicInterpolation(double t, Point2d[] points, int i) {
		if (i < 0 || i + 3 >= points.length) return null;
		Point2d result = new Point2d();
		result.x = points[i].x + (points[i+1].x - points[i].x) * t;
		result.y = Interpolation.CubicInterpolate(points[i].y, points[i+1].y, points[i+2].y, points[i+3].y, t);
		return result;
	}
	
	private Point2d getHermiteInterpolation(double t, Point2d[] points, int i) {
		if (i < 0 || i + 3 >= points.length) return null;
		Point2d result = new Point2d();
		result.x = points[i].x + (points[i+1].x - points[i].x) * t;
		result.y = Interpolation.HermiteInterpolate(points[i].y, points[i+1].y, points[i+2].y, points[i+3].y, t, -1, 0);
		return result;
	}
	

}
