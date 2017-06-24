package fr.evolya.javatoolkit.cli;

import java.io.IOException;

public interface CLISessionStream {
	
	/**
	 * Les styles en texte riche.
	 */
	public enum ConsoleTextStyle {
		BOLD, ITALIC, UNDERLINE
	}
	
	/**
	 * Les différents types d'input.
	 */
	public static enum ConsoleInputMode {
		NORMAL,
		PASSWORD
	}

	/**
	 * Ecrire vers la sortie standard.
	 */
	public void outputWrite(String str) throws IOException;
	
	/**
	 * Ecrire une ligne vers la sortie standard.
	 */
	public void outputWriteLine(String str) throws IOException;
	
	/**
	 * Nettoyer la console, retirer tout le contenu affiché.
	 */
	public void outputClean() throws IOException;
	
	/**
	 * Retirer un nombre de lignes, en partant du bas, c'est à dire
	 * des plus récentes.
	 */
	public void outputCleanLines(int howMany) throws IOException;
	
	/**
	 * Renvoie TRUE si la console de sortie supporte les styles de texte.
	 */
	public boolean isRichTextSupported();
	
	public void setStyle(ConsoleTextStyle... style);
	
	public void clearStyle(ConsoleTextStyle... style);
	
	public void clearStyles();
	
	public String applyStyles(String text, ConsoleTextStyle... styles);
	
	/**
	 * Renvoie TRUE si le changement a été effectué, ou FALSE
	 * si le mode était déjà sélectionné ou s'il n'est pas 
	 * possible de passer dans cet état.
	 */
	public boolean setInputMode(ConsoleInputMode mode);
	
}
