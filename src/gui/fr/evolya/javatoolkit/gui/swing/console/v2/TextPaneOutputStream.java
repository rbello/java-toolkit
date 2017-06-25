package fr.evolya.javatoolkit.gui.swing.console.v2;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;

import fr.evolya.javatoolkit.cli.v2.TextShell.Observable;
import fr.evolya.javatoolkit.cli.v2.TextShell.RichTextOutputStream;
import fr.evolya.javatoolkit.code.MutableMap;
import fr.evolya.javatoolkit.code.MutableMap.Setter;
import fr.evolya.javatoolkit.code.utils.StringUtils;

public class TextPaneOutputStream implements RichTextOutputStream<SimpleAttributeSet> {

	protected DefaultStyledDocument document;
	protected JTextPane textPane;
	protected StyledEditorKit editorKit;
	protected SimpleAttributeSet styleStd;
	protected SimpleAttributeSet styleError;
	protected String prompText;
	protected Style baseStyle;
	protected SimpleAttributeSet stylePrompt;
	
	/**
	 * Constructeur.
	 * 
	 * @param document
	 * @param textPane
	 */
	public TextPaneOutputStream(DefaultStyledDocument document, final JTextPane textPane, MutableMap config) {
		
		this.document = document;
		this.textPane = textPane;
		this.editorKit = (StyledEditorKit)textPane.getEditorKit();
		this.prompText = "$ ";
		
		// Base du style du texte
		StyleContext context = new StyleContext();
		baseStyle = context.addStyle("defaultStyle", null);
		StyleConstants.setFontFamily(baseStyle, "Courier New");
		document.setLogicalStyle(document.getLength(), baseStyle);
		
		// Set de styles par défaut du texte
		styleStd = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleStd, "Courier New");
        
        // Set de styles pour les erreurs
 		styleError = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleError, "Courier New");
        
        // Set de styles pour les erreurs
		stylePrompt = new SimpleAttributeSet();
        StyleConstants.setFontFamily(stylePrompt, "Courier New");
        
        // On ajoute les items de configuration
 		config.set("Styles.ForegroundColor.Standard", Color.WHITE, new Setter<Color>() {
 			public void set(Color value) {
 				StyleConstants.setForeground(baseStyle, value);
 				StyleConstants.setForeground(styleStd, value);
 			}
 		});
 		config.set("Styles.ForegroundColor.Error", Color.RED, new Setter<Color>() {
			public void set(Color value) {
				StyleConstants.setForeground(styleError, value);
			}
		});
        config.set("Styles.ForegroundColor.Prompt", Color.GREEN, new Setter<Color>() {
			public void set(Color value) {
				StyleConstants.setForeground(stylePrompt, value);
			}
		});
        config.set("Styles.BackgroundColor", Color.DARK_GRAY, new Setter<Color>() {
			public void set(Color value) {
				textPane.setBackground(value);
			}
		});
        config.set("Styles.ForegroundColor.Caret", Color.DARK_GRAY, new Setter<Color>() {
			public void set(Color value) {
				textPane.setCaretColor(value);
			}
		});
        
     // TODO
//		BasicCaret caret = new BasicTextUI.BasicCaret();
//		textArea.setCaret(caret);
		
	}

	@Override
	public RichTextOutputStream<SimpleAttributeSet> print(String str) {
		return print(str, false);
	}

	@Override
	public RichTextOutputStream<SimpleAttributeSet> println(String str) {
		return print(str + StringUtils.NL_CHAR, false);
	}

	@Override
	public RichTextOutputStream<SimpleAttributeSet> write(String str, SimpleAttributeSet style) {
		try {
			document.insertString(document.getLength(), str, style);
			editorKit.getInputAttributes().removeAttributes(style);
		} catch (BadLocationException ex) {
			throw new RuntimeException(ex);
		}
		return this;
	}

	@Override
	public RichTextOutputStream<SimpleAttributeSet> print(String str, boolean error) {
		return write(str, error ? styleError : styleStd);
	}

	@Override
	public RichTextOutputStream<SimpleAttributeSet> println(String str, boolean error) {
		return print(str + StringUtils.NL_CHAR, error);
	}

	@Override
	public RichTextOutputStream<SimpleAttributeSet> prompt() {
		// On affiche le prompt
		write(prompText, stylePrompt);
		// Et on positionne le curseur à la fin
		textPane.setCaretPosition(document.getLength());
		return this;
	}

	public String getPromptText() {
		return prompText;
	}

	public void setPromptText(String text) {
		this.prompText = text;
	}

	@Override
	public void setEventTarget(Observable target) {
		// TODO Auto-generated method stub
		
	}
	
}
