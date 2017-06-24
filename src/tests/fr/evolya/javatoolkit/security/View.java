package fr.evolya.javatoolkit.security;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import fr.evolya.javatoolkit.security.otp.HOTP;

public class View extends JFrame implements Runnable {

	private static final long serialVersionUID = -1000153217325301637L;
	private JPanel contentPane;
	private JTextField keyTextField;
	private JLabel labelTime;
	private JLabel labelPin;
	private HOTP ga;
	
	private static final SimpleDateFormat stdDateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					View frame = new View();
					frame.setVisible(true);
					new Thread(frame).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public View() {
		this.ga = new HOTP("LJHL5P65A5QCJ7GB");
		setTitle("Google Authenticator in Java");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 149);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel labelKey = new JLabel("Key :");
		labelKey.setFont(new Font("Tahoma", Font.PLAIN, 17));
		
		labelTime = new JLabel("Time :");
		labelTime.setFont(new Font("Tahoma", Font.PLAIN, 17));
		
		keyTextField = new JTextField();
		keyTextField.setColumns(10);
		keyTextField.setText(ga.getKey());
		keyTextField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				ga.setKey(keyTextField.getText());
			}
		});
		
		labelPin = new JLabel("-");
		labelPin.setHorizontalAlignment(SwingConstants.CENTER);
		labelPin.setFont(new Font("Tahoma", Font.PLAIN, 17));
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(labelPin, GroupLayout.PREFERRED_SIZE, 404, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(labelKey, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(keyTextField, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
						.addComponent(labelTime, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(labelKey, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(keyTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(labelTime, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labelPin, GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
					.addContainerGap())
		);
		contentPane.setLayout(gl_contentPane);
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						labelTime.setText("Time: " + stdDateF.format(new Date()));
						labelPin.setText("" + ga.now());
					}
					catch (Exception e) {
						System.out.println("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
					}
				}
			});
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				return;
			}
		}
	}
	
}
