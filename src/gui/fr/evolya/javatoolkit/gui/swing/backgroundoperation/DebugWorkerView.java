package fr.evolya.javatoolkit.gui.swing.backgroundoperation;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import fr.evolya.javatoolkit.gui.swing.JFrameView;

public class DebugWorkerView extends JFrameView {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -2968389177286465735L;

	private JPanel contentPane;
	
	private JLabel lblState;

	private JButton btnStart;

	private JButton btnStop;
	private JTable table;

	private DefaultTableModel tableModel;

	private JComboBox<String> comboBox;

	private JLabel lblJobs;

	/**
	 * Create the frame.
	 */
	public DebugWorkerView() {
		
		setTitle("WorkerFrame");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 610, 395);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblNewLabel = new JLabel("Etat du worker :");
		
		lblState = new JLabel("-");
		
		lblJobs = new JLabel("-");
		
		JProgressBar progressBar = new JProgressBar();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		btnStart = new JButton("Start");
		btnStop = new JButton("Stop");
		
		comboBox = new JComboBox<String>();
		
		JLabel lblAjouter = new JLabel("Ajouter :");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblState)
					.addPreferredGap(ComponentPlacement.RELATED, 405, Short.MAX_VALUE)
					.addComponent(lblJobs))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(10)
					.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnStart)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnStop)
					.addPreferredGap(ComponentPlacement.RELATED, 355, Short.MAX_VALUE)
					.addComponent(lblAjouter)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel)
						.addComponent(lblState)
						.addComponent(lblJobs))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnStart)
						.addComponent(btnStop)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblAjouter)))
		);
		
		table = new JTable();
		tableModel = new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"JobName", "Progress", "Remaining"
			}
		);
		table.setModel(tableModel);
		scrollPane.setViewportView(table);
		contentPane.setLayout(gl_contentPane);
	}

	public JLabel getLblJobs() {
		return lblJobs;
	}

	public JComboBox<String> getComboBox() {
		return comboBox;
	}

	public JPanel getContentPane() {
		return contentPane;
	}

	public JLabel getLblState() {
		return lblState;
	}

	public JButton getBtnStart() {
		return btnStart;
	}

	public JButton getBtnStop() {
		return btnStop;
	}

	public JTable getTable() {
		return table;
	}

	public DefaultTableModel getTableModel() {
		return tableModel;
	}

}
