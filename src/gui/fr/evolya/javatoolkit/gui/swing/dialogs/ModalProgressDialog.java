package fr.evolya.javatoolkit.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;

public class ModalProgressDialog extends JDialog {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 4029167815473197228L;
	
	private final JPanel contentPanel = new JPanel();
	private JLabel message;
	private JProgressBar progressBar;
	
	/**
	 * Constructor modal.
	 */
	public ModalProgressDialog(JFrame jframe) {
		super(jframe, "", Dialog.ModalityType.DOCUMENT_MODAL);
		createComponents();
	}

	/**
	 * Constructor.
	 */
	public ModalProgressDialog() {
		createComponents();
	}
	
	/**
	 * Création des composants
	 */
	private void createComponents() {
		setResizable(false);
		setUndecorated(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 394, 68);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new LineBorder(new Color(60, 60, 60)));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			message = new JLabel("Please wait...");
			message.setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		progressBar = new JProgressBar();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
						.addComponent(message, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(message)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(69, Short.MAX_VALUE))
		);
		contentPanel.setLayout(gl_contentPanel);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public JLabel getMessage() {
		return message;
	}

	public static ModalProgressDialog dialog(final JFrame frame) {
		// On a besoin d'un objet pour contenir une référence
		// TODO C'est un peu idiot d'utiliser un objet aussi complexe
		// que ça mais ça marche là et j'avais rien d'autre
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
		try {
			// Synchronisé sur le thread de dispatch UI
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// Construction du dialog
					ModalProgressDialog dialog = new ModalProgressDialog(/* frame TODO Modal = bloquant...  */);
					dialog.getProgressBar().setIndeterminate(true);
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
					// Enregistrement pour le retour
					node.setUserObject(dialog);
				}
			});
			// On fait le retour
			return (ModalProgressDialog) node.getUserObject();
		} catch (Exception ex) {
			System.err.println(ex.getClass().getSimpleName() +" in ModalProgressDialog.dialog()");
			ex.printStackTrace();
		}
		return null;
	}
	
}
