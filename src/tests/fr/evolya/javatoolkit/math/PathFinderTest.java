package fr.evolya.javatoolkit.math;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import fr.evolya.javatoolkit.math.vecmath.Point2d;

public class PathFinderTest extends JFrame {

	private JPanel contentPane;
	
	private double discretisation = 10; // en cm
	
	// Liste des points à afficher (exprimé en %, repère en bas à droite)
	private Point2d[] exterior = {
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
	
	private Point2d[] interior = {
			new Point2d(27, 21),
			new Point2d(22, 27),
			new Point2d(18, 36),
			new Point2d(15, 45),
			new Point2d(14, 51),
			new Point2d(13, 58),
			new Point2d(15, 70),
			new Point2d(17, 80),
			new Point2d(24, 85),
			new Point2d(35, 84),
			new Point2d(43, 80),
			new Point2d(47, 73),
			new Point2d(45, 64),
			new Point2d(45, 55),
			new Point2d(48, 46),
			new Point2d(52, 41),
			new Point2d(57, 38),
			new Point2d(65, 36),
			new Point2d(75, 36),
			new Point2d(82, 35),
			new Point2d(86, 28),
			new Point2d(82, 21),
			new Point2d(72, 20),
			new Point2d(66, 21),
			new Point2d(59, 22),
			new Point2d(52, 23),
			new Point2d(45, 28),
			new Point2d(40, 28),
			new Point2d(36, 24),
			new Point2d(31, 21)
	};

	private JPanel drawing;

	private JTextPane textPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PathFinderTest frame = new PathFinderTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	static public double sqr(double a) {
        return a*a;
	}
	static public double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(sqr(y2 - y1) + sqr(x2 - x1));
    }
	static public double distance(Point2d a, Point2d b) {
        return distance(a.x, a.y, b.x, b.y);
    }
	
	/**
	 * Create the frame.
	 */
	public PathFinderTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 816, 616);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JButton btnGenererLaRsolution = new JButton("Generer la résolution");
		btnGenererLaRsolution.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Dimension size = drawing.getSize();
				
				log("Dimension de la zone : " + size.width + " x " + size.height + " pixels");
				log("On dit que 1 pixel = 1 mètre.");
				log("");
				log("Analyse du bord extérieur...");
				Point2d p1 = null;
				for (int i = 0; i < exterior.length; i++) {
					/*if (i == 0)
						
						Point2d realp1 = new Point2d(
								p1.x / 100d * size.width,
								(100d - p1.y) / 100d * size.height
						);
						Point2d realp0 = new Point2d(
								p0.x / 100d * size.width,
								(100d - p0.y) / 100d * size.height
						);
						
						
						log("Distance entre P" + (i-1) + " et P" + i + " = " + distance(realp1, realp0));
					}
					i++;
					p1 = p0;*/
				}
				
			}
		});
		
		drawing = new JPanel(){
			@Override
			public void paint(Graphics g) {
				Dimension size = getSize();
				
				// Fond
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, size.width, size.height);
				
				// Affichage des points exterieurs
				g.setColor(Color.GREEN);
				Point2d[] realPoints = new Point2d[exterior.length];
				for (int i = 0; i < exterior.length; i++) {
					realPoints[i] = new Point2d(
							exterior[i].x / 100d * size.width,
							(100d - exterior[i].y) / 100d * size.height
					);
					if (i > 0) {
						g.drawLine(
								(int)realPoints[i-1].x,
								(int)realPoints[i-1].y,
								(int)realPoints[i].x,
								(int)realPoints[i].y
						);
					}
					if (i+1 == exterior.length) {
						g.drawLine(
								(int)realPoints[0].x,
								(int)realPoints[0].y,
								(int)realPoints[i].x,
								(int)realPoints[i].y
						);
					}
				}
				for (int i = 0; i < exterior.length; i++) {
					g.setColor(Color.WHITE);
					g.fillOval((int)realPoints[i].x - 2, (int)realPoints[i].y - 2, 4, 4);
					g.setColor(Color.GRAY);
					g.drawOval((int)realPoints[i].x - 2, (int)realPoints[i].y - 2, 4, 4);
					g.drawString("Ex"+i, (int)realPoints[i].x + 5, (int)realPoints[i].y - 5);
				}
				
				// Affichage des points exterieurs
				g.setColor(Color.BLUE);
				realPoints = new Point2d[interior.length];
				for (int i = 0; i < interior.length; i++) {
					realPoints[i] = new Point2d(
							interior[i].x / 100d * size.width,
							(100d - interior[i].y) / 100d * size.height
					);
					if (i > 0) {
						g.drawLine(
								(int)realPoints[i-1].x,
								(int)realPoints[i-1].y,
								(int)realPoints[i].x,
								(int)realPoints[i].y
						);
					}
					if (i+1 == interior.length) {
						g.drawLine(
								(int)realPoints[0].x,
								(int)realPoints[0].y,
								(int)realPoints[i].x,
								(int)realPoints[i].y
						);
					}
				}
				for (int i = 0; i < interior.length; i++) {
					g.setColor(Color.WHITE);
					g.fillOval((int)realPoints[i].x - 2, (int)realPoints[i].y - 2, 4, 4);
					g.setColor(Color.GRAY);
					g.drawOval((int)realPoints[i].x - 2, (int)realPoints[i].y - 2, 4, 4);
					g.drawString("In"+i, (int)realPoints[i].x + 5, (int)realPoints[i].y - 5);
				}
				
			}
		};
		drawing.setBorder(new LineBorder(Color.LIGHT_GRAY));
		
		drawing.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				double x = (double)e.getX() / (double)drawing.getWidth();
				double y = ((double)drawing.getHeight() - (double)e.getY()) / (double)drawing.getHeight();
				System.out.println("new Point2d(" + Math.round(x * 100) + ", " + Math.round(y * 100) + ")");
			}
		});
		
		JPanel panel_1 = new JPanel();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(btnGenererLaRsolution)
					.addContainerGap(635, Short.MAX_VALUE))
				.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
				.addComponent(drawing, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(btnGenererLaRsolution)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(drawing, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE))
		);
		panel_1.setLayout(new CardLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, "name_1084356258781785");
		
		textPane = new JTextPane();
		textPane.setFont(new Font("Courier New", Font.PLAIN, 13));
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		contentPane.setLayout(gl_contentPane);
	}
	
	public void log(String str) {
		textPane.setText(textPane.getText() + "\n" + str);
	}

}
