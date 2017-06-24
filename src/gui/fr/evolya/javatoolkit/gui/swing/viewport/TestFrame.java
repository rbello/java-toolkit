package fr.evolya.javatoolkit.gui.swing.viewport;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.evolya.javatoolkit.gui.swing.SwingHelper;
import fr.evolya.javatoolkit.gui.swing.viewport.layers.Layer;

public class TestFrame extends JFrame {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -8256530950408416175L;
	
	/**
	 * Le panel de contenu.
	 */
	private JPanel contentPane;
	
	/**
	 * Le panel viewport.
	 */
	private ViewportPanel viewport;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		SwingHelper.initLookAndFeel();
		SwingHelper.adjustGlobalFontSize(13);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestFrame frame = new TestFrame();
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
	public TestFrame() {
		setTitle("Test du LayerPanel");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Tools", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		viewport = new ViewportPanel();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 193, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(viewport, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(viewport, GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
				.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		JButton btnNewButton = new JButton("Add GREEN layer");
		btnNewButton.setToolTipText("Un petit calque vert (-10,-10,20,20)");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Layer layer = new Layer();
				layer.setOriginalBounds(-10,-10,20,20);
				layer.setBackground(Color.GREEN);
				viewport.addLayer(layer, 5);
			}
		});
		
		JButton btnNewButton_1 = new JButton("Add BLUE layer");
		btnNewButton_1.setToolTipText("Un gros layer au centre (50,50,250,150)");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Layer layer = new Layer();
				layer.setOriginalBounds(50,50,250,150);
				layer.setBackground(Color.BLUE);
				viewport.addLayer(layer, 5);
			}
		});
		
		JButton btnClean = new JButton("RAZ");
		btnClean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewport.removeAllLayers();
			}
		});
		
		JButton btnNewButton_2 = new JButton("Add YELLOW layer");
		btnNewButton_1.setToolTipText("Un tres long layer en largeur (20,100,400,50)");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Layer layer = new Layer();
				layer.setOriginalBounds(20,100,400,50);
				layer.setBackground(Color.YELLOW);
				viewport.addLayer(layer, 5);
			}
		});
		
		final JSlider slider = new JSlider();
		slider.setMinimum(1);
		slider.setMaximum(100);
		slider.setValue(50);
		
		JButton btnReset = new JButton("100%");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setValue(50);
			}
		});
		
		JButton btnNewButton_3 = new JButton("Fit");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setValue((int)(Math.min(2, viewport.getZoomAuto()) / 2d * 100d));
				
			}
		});
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(btnNewButton_1, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
						.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
						.addComponent(btnNewButton_2, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(btnNewButton_3, 0, 0, Short.MAX_VALUE)
								.addComponent(btnClean, GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
							.addGap(10)
							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnReset, GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
								.addComponent(slider, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnNewButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_2)
					.addPreferredGap(ComponentPlacement.RELATED, 348, Short.MAX_VALUE)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnReset)
						.addComponent(btnNewButton_3))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(slider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnClean))
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		contentPane.setLayout(gl_contentPane);
		
		// Clic sur le slider de zoom
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				viewport.setZoomFactor(slider.getValue() / 50d);
			}
		});
		
	}
}
