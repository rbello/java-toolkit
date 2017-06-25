package fr.evolya.javatoolkit.gui.swing.console.v2;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultStyledDocument;

import fr.evolya.javatoolkit.cli.v2.AdvancedShell;
import fr.evolya.javatoolkit.cli.v2.BasicShell.Command;
import fr.evolya.javatoolkit.cli.v2.TextShell;
import fr.evolya.javatoolkit.code.MutableMap;
import fr.evolya.javatoolkit.code.annotations.DesignPattern;
import fr.evolya.javatoolkit.code.annotations.Pattern;
import fr.evolya.javatoolkit.code.utils.Utils;

@DesignPattern(type = Pattern.Adapter)
public class JConsolePanel extends JPanel implements TextShell<TextPaneInputStream, TextPaneOutputStream> {

	private static final long serialVersionUID = 3968180425666243587L;
	
	
	protected 	JTextPane 					textArea;
	private 	DefaultStyledDocument 		document;
	
	private 	Map<String, TextShell.KeyCatcher> catchers;


	private AdvancedShell<TextPaneInputStream, TextPaneOutputStream> shell;

	public JConsolePanel() {
		
		shell = new AdvancedShell<TextPaneInputStream, TextPaneOutputStream>();
		
		initGUI();
		
		shell.setInputStream(new TextPaneInputStream(shell.getConfig()));
		shell.setOutputStream(new TextPaneOutputStream(document, textArea, shell.getConfig()));
		
		catchers = new HashMap<String, TextShell.KeyCatcher>();
		
		initKeyBehavior();
		
	}
	
	private void initKeyBehavior() {
		
		textArea.addKeyListener(new KeyAdapter() {
			//public void keyPressed(KeyEvent evt) {
			//public void keyTyped(KeyEvent evt) {
			public void keyReleased(KeyEvent evt) {
				
				System.out.println((int)evt.getKeyChar() + "-" + evt.getModifiers() + " = " + evt.getKeyChar());

				// Saisie impossible.
//				if (inputState == State.INPUT_LOCKED) {
//					evt.consume();
//					return;
//				}
				
				boolean preventDefault = false;
				
				// On regarde si on a un catcher
				TextShell.KeyCatcher catcher = catchers.get((int)evt.getKeyChar() + "-" + evt.getModifiers());
				
				if (catcher != null) {
					preventDefault = ! catcher.handle(JConsolePanel.this, evt);
				}
				
				if (!preventDefault) {
					
					// Caractère non affichable
					if (!Utils.isPrintableChar(evt.getKeyChar())) {
						return;
					}

					// On indique qu'on a lu un caractère
					if (getInputStream().write(evt.getKeyChar())) {
						// Le caractère est lu, on arrête là
						return;
					}
					
					// Append char to input buffer
					getInputStream().getBuffer().append(evt.getKeyChar());
				}
				
			}
		});
		
		// Enter
		catchKey(10, 0, new KeyCatcher() {
			public boolean handle(TextShell shell, KeyEvent evt) {
				/* TODO if (getInputStream().hasReader()) {
					return true;
				}*/
				// On empêche le retour à la ligne provoqué par l'appui sur la touche entrée 
				evt.consume();
				
				String input = getInputStream().getBuffer().toString();
				
				getInputStream().resetBuffer();
				
				// TODO
				if (getInputStream().write(input)) {
					return true;
				}
				
				// Append char to input buffer
				return false;
			}
		});
		
		// Return
		catchKey(8, 0, new KeyCatcher() {
			public boolean handle(TextShell shell, KeyEvent evt) {
				StringBuffer sb = getInputStream().getBuffer();
				if (sb.length() < 1) {
					evt.consume();
					return false;
				}
				sb.setLength(sb.length() - 1);
				return false;
			}
		});
		
	}

	protected void initGUI() {
		
		// Initialize text area
		document = new DefaultStyledDocument();
		textArea = new JTextPane(document);
		JScrollPane jsp = new JScrollPane(textArea);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// Inflate in layout
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(jsp, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(jsp, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
		);
		setLayout(groupLayout);
		
	}
	
	public MutableMap getConfig() {
		return shell.getConfig();
	}

	public void catchKey(int key, int modifier, KeyCatcher catcher) {
		catchers.put(key + "-" + modifier, catcher);
	}

	@Override
	public TextPaneInputStream getInputStream() {
		return shell.getInputStream();
	}

	@Override
	public TextPaneOutputStream getOutputStream() {
		return shell.getOutputStream();
	}

	public ErrorOutputStream getErrorStream() {
		return shell.getErrorStream();
	}
	
	@Override
	public void start() {
		shell.start();
	}

	public JConsolePanel addCommand(String command, Command handler) {
		shell.addCommand(command, handler);
		return this;
	}

	@Override
	public void addListener(TextShell.Listener listener) {
		shell.addListener(listener);
	}

}
