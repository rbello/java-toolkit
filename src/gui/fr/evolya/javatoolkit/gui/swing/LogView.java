package fr.evolya.javatoolkit.gui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import fr.evolya.javatoolkit.app.event.BeforeApplicationStarted;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.GuiTask;
import fr.evolya.javatoolkit.events.fi.BindOnEvent;

public class LogView extends JFrame {

	private static final long serialVersionUID = 7009291909760965894L;

	private JPanel contentPane;
	private JTextField textField;
	private JTable table;

	private DefaultTableModel tableModel;

	@GuiTask
	public LogView() {

		setTitle("Log");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(350, 400, 800, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();

		textField = new JTextField();
		textField.setColumns(10);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup().addContainerGap()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 416,
										Short.MAX_VALUE)
								.addComponent(textField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 416,
										Short.MAX_VALUE))
						.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE).addContainerGap()));

		table = new JTable();
		tableModel = new DefaultTableModel(new Object[][] {},
				new String[] { "Datetime", "Level", "Source", "Message" }) {
			private static final long serialVersionUID = -1;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setModel(tableModel);
		table.getColumnModel().getColumn(1).setCellRenderer(new StatusColumnCellRenderer());

		// SORT DATA WHEN HEADERS CLICKED
		table.setAutoCreateRowSorter(true);

		// AUTO RESIZE
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(90);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		scrollPane.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Component c = e.getComponent();
				int viewWidth = c.getWidth();
				TableColumn tc = table.getColumnModel().getColumn(3);
				int tableWidth = table.getWidth() - tc.getWidth();
				int diffWidth = viewWidth - tableWidth - 20;
				if (diffWidth > 0) {
					tc.setPreferredWidth(diffWidth);
				}
			}
		});

		// FILTER
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		table.setRowSorter(sorter);
		RowFilter filter = new RowFilter() {
			public boolean include(Entry entry) {
				String text = textField.getText().toLowerCase();
				return entry.getStringValue(0).toLowerCase().contains(text)
						|| entry.getStringValue(1).toLowerCase().contains(text)
						|| entry.getStringValue(2).toLowerCase().contains(text)
						|| entry.getStringValue(3).toLowerCase().contains(text);
			}
		};
		textField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				if (textField.getText().trim().length() == 0) {
					sorter.setRowFilter(null);
				} else {
					sorter.setRowFilter(filter);
				}
			}

			public void insertUpdate(DocumentEvent e) {
				String text = textField.getText();
				if (textField.getText().trim().length() == 0) {
					sorter.setRowFilter(null);
				} else {
					sorter.setRowFilter(filter);
				}
			}

			public void changedUpdate(DocumentEvent e) {
			}
		});

		// DISPLAY MESSAGE DETAILS WHEN ROWS DOUBLE-CLICKED
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() != 2)
					return;
				String data = "" + table.getModel().getValueAt(table.getSelectedRow(), 3);
				JOptionPane.showMessageDialog(null, data, "Detail", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		scrollPane.setViewportView(table);
		contentPane.setLayout(gl_contentPane);
	}

	@BindOnEvent(BeforeApplicationStarted.class)
	public void init() {
		try {
			ConsoleOutputStream cos = new ConsoleOutputStream();
			PrintStream ps = new PrintStream(cos, true);
			Logs.setOutputStream(ps, Logs.ALL);
			setVisible(true);
			new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}

					ps.flush();
				}

			}).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	class ConsoleOutputStream extends ByteArrayOutputStream {

		private final String EOL = System.getProperty("line.separator");

		//
		// @Override
		// public synchronized void write(byte[] b, int off, int len) {
		// // TODO Auto-generated method stub
		// System.out.println("---");
		// super.write(b, off, len);
		// }
		//
		// @Override
		// public synchronized void write(int b) {
		// // TODO Auto-generated method stub
		// System.out.println("--");
		// super.write(b);
		// }
		//
		// @Override
		// public void write(byte[] b) throws IOException {
		// System.out.println("-");
		// super.write(b);
		// }
		//
		//
		// /*
		// * Override this method to intercept the output text. Each line of text
		// * output will actually involve invoking this method twice:
		// *
		// * a) for the actual text message
		// * b) for the newLine string
		// *
		// * The message will be treated differently depending on whether the line
		// * will be appended or inserted into the Document
		// */
		public void flush() {
			// System.out.println("flush");
			String message = toString();

			if (message.length() == 0)
				return;

			handle(message);

			reset();
		}

		private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");

		/*
		 * We don't want to have blank lines in the Document. The first line added will
		 * simply be the message. For additional lines it will be:
		 *
		 * newLine + message
		 */
		private void handle(String message) {
			for (String line : message.split(EOL)) {
				String[] tokens = line.matches("^[0-9\\:\\.]{12} \\|.*\\|.*\\|.*$") ? line.split("\\|", 4)
						: new String[] { sdf.format(new Date()), "", "", line };
				SwingUtilities.invokeLater(() -> {
					tableModel.addRow(tokens);
				});
			}
		}

	}

	public class StatusColumnCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {

			value = ("" + value).trim();
			
			// Cells are by default rendered as a JLabel.
			JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			l.setHorizontalAlignment(CENTER);
			if ("INFO".equals(value))
				l.setBackground(Color.BLUE);
			else if ("DEBUG".equals(value))
				l.setBackground(Color.GRAY);
			else if ("DEBUG_FINE".equals(value))
				l.setBackground(Color.GRAY);
			else if ("WARNING".equals(value))
				l.setBackground(Color.ORANGE);
			else if ("ERROR".equals(value))
				l.setBackground(Color.RED);
			
			return l;

		}

	}
}
