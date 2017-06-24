package fr.evolya.javatoolkit.gui.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class Clipboard implements ClipboardOwner {

	private static Clipboard INSTANCE = null;

	public static Clipboard getInstance() {
		synchronized (Clipboard.class) {
			if (INSTANCE == null) {
				INSTANCE = new Clipboard();
			}
		}
		return INSTANCE;
	}
	
	public static java.awt.datatransfer.Clipboard getSystemClipboard() {
		return Toolkit.getDefaultToolkit().getSystemClipboard();
	}
	
	public static void setContents(String data) {
		
		// On fabrique une selection de texte
		StringSelection ss = new StringSelection(data);
		
		// On modifie le contenu
		getSystemClipboard().setContents(ss, getInstance());
		
	}
	
	public static String getStringContents() {
		
		// On recupère l'objet transféré par le presse-papier
		Transferable t = getSystemClipboard().getContents(null);
		
		// Aucun objet, on ne renvoie rien
		if (t == null) {
			return null;
		}
		
		// L'objet n'est pas convertible en texte, donc on oublie
		if (!t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return null;
		}
		
		// On renvoie le contenu sous forme de texte
		try {
			return (String) t.getTransferData(DataFlavor.stringFlavor);
		}
		
		// Impossible de réaliser l'opération
		catch (UnsupportedFlavorException | IOException e) {
			return null;
		}
		
	}

	@Override
	public void lostOwnership(java.awt.datatransfer.Clipboard cp, Transferable obj) {
		// Contenu modifié pas un autre processus
	}
	
}
