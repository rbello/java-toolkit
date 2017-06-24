package fr.evolya.javatoolkit.gui.swing.console.v2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fr.evolya.javatoolkit.cli.v2.BasicShell;
import fr.evolya.javatoolkit.cli.v2.TextShell;
import fr.evolya.javatoolkit.cli.v2.TextShell.BooleanReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.IntReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.KeyCatcher;
import fr.evolya.javatoolkit.cli.v2.TextShell.LineReader;

public class JConsolePanelTest extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JConsolePanel shell;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// On fabrique et affiche la fenêtre
				final JConsolePanelTest frame = new JConsolePanelTest();
				frame.setVisible(true);
				// On lance le shell
				frame.shell.start();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JConsolePanelTest() {
		setTitle("Test Console");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0,0,0,0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// On fabrique un shell
		shell = new JConsolePanel();
		
		// On modifie le prompt
		shell.getOutputStream().setPromptText("toto@host $ ");
		
		// On change les couleurs
		shell.getConfig()
			.set("Styles.BackgroundColor", Color.BLACK)
			.set("Styles.ForegroundColor.Standard", Color.GREEN)
			.set("Styles.ForegroundColor.Prompt", Color.BLUE)
			.set("Styles.ForegroundColor.Error", Color.YELLOW)
			.set("Styles.ForegroundColor.Caret", Color.RED);
		
		// Ctrl + C
		shell.catchKey(3, 2, new KeyCatcher() {
			public boolean handle(TextShell shell, KeyEvent evt) {
				JConsolePanelTest.this.shell.getErrorStream().println("Interrupted", true);
				JConsolePanelTest.this.shell.getInputStream().resetBuffer();
				JConsolePanelTest.this.shell.getOutputStream().prompt();
				return false;
			}
		});
		
		shell.addCommand("lightmode", new BasicShell.Command() {
			@Override
			public void handle(List<String> args, String plain, String cmd) {
				doLight();
			}
		});
		
		shell.addCommand("test", new BasicShell.Command() {
			@Override
			public void handle(List<String> args, String plain, String cmd) {
				doTest();
			}
		});
		
		contentPane.add(shell);
		
	}

	private void doTest() {

		final TextPaneOutputStream stdOut = shell.getOutputStream();
		final TextPaneInputStream stdIn = shell.getInputStream();
		
		stdOut.print("[Y]es or [N]o ? ");
		stdIn.read(new BooleanReader() {
			public void read(Boolean value) {
				stdOut.println("\nRead: " + value).println("Your name?");
				stdIn.read(new LineReader() {
					public void read(String value) {
						stdOut.println("I understood: " + value).println("Your age?");
						stdIn.read(new IntReader() {
							public void read(Integer value) {
								stdOut.println("You are " + value + " years old").println("Ok it's done");
							}
						});
					}
				});
			}
		});
		
	}

	private void doLight() {
		shell.getConfig().set("Styles.BackgroundColor", Color.WHITE)
						 .set("Styles.ForegroundColor.Standard", Color.DARK_GRAY)
						 .set("Styles.ForegroundColor.Prompt", Color.BLACK)
						 .set("Styles.ForegroundColor.Error", Color.RED)
						 .set("Styles.CaretColor", Color.BLUE);
	}

}
