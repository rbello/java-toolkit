package fr.evolya.javatoolkit.gui.swing.console;

import fr.evolya.javatoolkit.events.attr.EventListener;
import fr.evolya.javatoolkit.gui.swing.console.IConsoleView.ConsoleInputHandler;

public interface ConsoleViewListener extends EventListener {
	
	/**
	 * Avant le traitement de la commande.
	 * Si la valeur renvoyée est FALSE, la commande ne sera pas prise en compte.
	 */
	public boolean beforeCommandInput(String cmd, IConsoleView view, boolean isUserIntent);
	
	/**
	 * Event pour demander le traitement d'une commande.
	 * 
	 * @param cmd La commande à executer
	 * @param view La vue qui a levé l'event
	 * @param isUserIntent Indique s'il s'agit d'une demande de l'utilisateur via l'IHM
	 * @param wasHandled Indique si la commande a déjà été traitée par un handler
	 */
	public void onCommandInput(String cmd, IConsoleView view, boolean isUserIntent, boolean wasHandled);
	
	/**
	 * Utilisation de la touche TAB
	 */
	public void onAutoCompleteIntent(IConsoleView view, Object event, boolean isUserIntent);
	
	public void onInterruptRequest(IConsoleView view, boolean isUserIntent);
	
	public boolean beforeInputHandlerStop(IConsoleView view, ConsoleInputHandler inputHandler, boolean isUserIntent);
	
	public void afterInputHandlerStop(IConsoleView view, ConsoleInputHandler inputHandler, boolean isUserIntent);
	
	public void onKeyCloseIntent(IConsoleView view, Object event, boolean isUserIntent);

}
