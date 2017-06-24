package fr.evolya.javatoolkit.gui.swing.appdebug;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import fr.evolya.javatoolkit.code.annotations.Debug;
import fr.evolya.javatoolkit.gui.swing.JFrameView;

/**
 * Une vue qui permet de debugger les applications, en montrant tout ce
 * qui se trouve à l'intérieur comme composants standards.
 */
@Debug
public class DebugTreeView extends JFrameView {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -3012246345990850391L;
	
	/**
	 * Le content pane.
	 */
	protected JPanel contentPane;
	
	/**
	 * La treeview.
	 */
	protected JTree tree;
	
	/**
	 * La barre de menu.
	 */
	private JMenuBar menuBar;
	
	/**
	 * Un label de status.
	 */
	private JLabel statusBar;

	/**
	 * Create the frame.
	 */
	public DebugTreeView() {
		setTitle("DebugTree View");
		setBounds(100, 100, 373, 490);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		
		statusBar = new JLabel(" ");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(statusBar, GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(10)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
					.addGap(10))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(statusBar))
		);
		
		tree = new JTree();

		scrollPane.setViewportView(tree);
		contentPane.setLayout(gl_contentPane);
	}
	
	public JTree getTree() {
		return tree;
	}
	
	public JMenuBar getFrameMenuBar() {
		return menuBar;
	}
	
	public JLabel getStatusBar() {
		return statusBar;
	}

}
