package fr.evolya.javatoolkit.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class ErrorDialog extends JDialog {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Le panel principal de contenu.
	 */
	private final JPanel contentPanel = new JPanel();
	
	/**
	 * La zone pour afficher les stack traces.
	 */
	private JTextPane _errorStacktrace;
	
	private JScrollPane scrollPane2;
	private JButton _okButton;

	private JTextPane _errorLabel;

	public ErrorDialog() {
		
		setResizable(false);
		setModal(true);
		
		setTitle("Error");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ErrorDialog.class.getResource("/fr/evolya/javatoolkit/gui/swing/dialogs/error.png")));
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel lblShowEetails = new JLabel("Show details");
		lblShowEetails.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblShowEetails.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				toggleDetailVisibility();
			}
		});
		lblShowEetails.setIcon(new ImageIcon(ErrorDialog.class.getResource("/fr/evolya/javatoolkit/gui/swing/dialogs/expand.png")));
		
		scrollPane2 = new JScrollPane();
		
		JScrollPane scrollPane1 = new JScrollPane();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
						.addComponent(lblShowEetails)
						.addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblShowEetails)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		_errorLabel = new JTextPane();
		_errorLabel.setEditable(false);
		_errorLabel.setBackground(contentPanel.getBackground());
		scrollPane1.setViewportView(_errorLabel);
		scrollPane1.setBorder(null);
		
		_errorStacktrace = new JTextPane();
		_errorStacktrace.setFont(new Font("Consolas", Font.PLAIN, 11));
		_errorStacktrace.setEditable(false);
		scrollPane2.setViewportView(_errorStacktrace);
		contentPanel.setLayout(gl_contentPanel);
		
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		_okButton = new JButton("OK");
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		_okButton.setActionCommand("OK");
		buttonPane.add(_okButton);
		getRootPane().setDefaultButton(_okButton);
		
		setDetailVisible(false);
		
	}
	
	public ErrorDialog setException(Throwable t, String msg) {
		setTitle(t.getClass().getSimpleName());
		_errorLabel.setText(msg);
		_errorStacktrace.setText(toString(t));
		return this;
	}
	
	public static String toString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	
	public ErrorDialog setDetailVisible(boolean visible) {
		Rectangle r = getBounds();
		if (!visible) {
			scrollPane2.setVisible(false);
			r.setSize(450, 157);
			setResizable(false);
		}
		else {
			scrollPane2.setVisible(true);
			r.setSize((int) r.getWidth(), 300);
			setResizable(true);
		}
		setBounds(r);
		return this;
	}
	
	public ErrorDialog toggleDetailVisibility() {
		return setDetailVisible(getBounds().getHeight() <= 157);
	}

	public static void showDialog(final Throwable ex, final String title, final String message) {
		showDialog(ex, title, message, null);
	}
	
	public static void showDialog(final Throwable ex, final String title, final String message, final Runnable onClose) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ErrorDialog dialog = new ErrorDialog();
				dialog.setException(ex, message);
				dialog.setTitle(title);
				dialog.setVisible(true);
				if (onClose != null) {
					dialog.addWindowListener(new WindowAdapter() {
						public void windowClosed(WindowEvent e) {
							onClose.run();
						}
					});
				}
			}
		});
	}
	
}
