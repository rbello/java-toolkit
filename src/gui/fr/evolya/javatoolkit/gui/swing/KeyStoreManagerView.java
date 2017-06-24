package fr.evolya.javatoolkit.gui.swing;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.evolya.javatoolkit.net.http.CertificateTools;

public class KeyStoreManagerView extends JFrameView {

	private static final long serialVersionUID = 7214469748735835746L;
	
	private JPanel contentPane;
	private JLabel lblKeystoreFile;
	private JLabel lblKeystoreProvider;
	private JLabel lblKeystoreType;
	private JTree tree;
	private DefaultMutableTreeNode nodeTop;
	private DefaultMutableTreeNode nodeCert;
	private DefaultMutableTreeNode nodeChain;

	/**
	 * Create the frame.
	 */
	public KeyStoreManagerView(File file) {
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("KeyStore Manager");
		setBounds(100, 100, 580, 331);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		lblKeystoreFile = new JLabel("Keystore file:");
		
		lblKeystoreProvider = new JLabel("Keystore provider:");
		
		lblKeystoreType = new JLabel("Keystore type:");
		
		nodeTop = new DefaultMutableTreeNode();
		nodeCert = new DefaultMutableTreeNode("Certificates");
		nodeChain = new DefaultMutableTreeNode("Chains");
		nodeTop.add(nodeCert);
		nodeTop.add(nodeChain);
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(11)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblKeystoreFile, GroupLayout.PREFERRED_SIZE, 457, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(lblKeystoreProvider)
									.addGap(199)
									.addComponent(lblKeystoreType))))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(11)
					.addComponent(lblKeystoreFile)
					.addGap(11)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(lblKeystoreProvider)
						.addComponent(lblKeystoreType))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
					.addContainerGap())
		);
		tree = new JTree(nodeTop);
		scrollPane.setViewportView(tree);
		contentPane.setLayout(gl_contentPane);
		
		update(file);
	}
	
	public void update(File file) {
		
		try {
			
			lblKeystoreFile.setText("Keystore file: " + file.getAbsolutePath());
			
			KeyStore ks = CertificateTools.createKeystoreInstance(file, "changeit");
			
			lblKeystoreProvider.setText("Keystore provider: " + ks.getProvider());
			lblKeystoreType.setText("Keystore type: " + ks.getType());
			
			nodeCert.removeAllChildren();
			nodeChain.removeAllChildren();
			
			Enumeration<String> aliases = ks.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();

				if (ks.isCertificateEntry(alias)) {
					Certificate c = ks.getCertificate(alias);
					nodeCert.add(new DefaultMutableTreeNode(alias + " = " + c));
				}
				
				else if (ks.isKeyEntry(alias)) {
					Certificate[] c = ks.getCertificateChain(alias);
					nodeChain.add(new DefaultMutableTreeNode(alias + " = " + c));
				}

			}
		
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		//X509Certificate[] cert = CertificateTools.getCertificates(file);
		
		
	}

}
