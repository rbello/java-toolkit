package fr.evolya.javatoolkit.core.app.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fr.evolya.javatoolkit.code.annotations.GuiTask;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Font;

public class View extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JLabel label;

	@GuiTask
	public View() {
		
		if (!SwingUtilities.isEventDispatchThread()) 
			throw new IllegalStateException("GUI must be created in EDT");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 138);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		label = new JLabel("New label");
		label.setFont(new Font("Tahoma", Font.PLAIN, 30));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		contentPane.add(label, BorderLayout.CENTER);
	}
	
	public JLabel getLabel() {
		return label;
	}

}
