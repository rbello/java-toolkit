package fr.evolya.javatoolkit.gui.swing.console;

import fr.evolya.javatoolkit.cli.CLISessionStream.ConsoleInputMode;
import fr.evolya.javatoolkit.code.Flag;
import fr.evolya.javatoolkit.events.attr.EventSource;

public interface IConsoleView {
	
	/**
	 * Les différents effets visuels
	 */
	public static final int EFFECT_NONE = 0;
	public static final int EFFECT_DEFAULT = EFFECT_NONE;
	public static final int EFFECT_RED_INPUT = 1;
	public static final int EFFECT_WAIT_STATUS = 2;
	
	/**
	 * Events de la console
	 */
	public EventSource<ConsoleViewListener> getEventsConsole(); 
	
	/**
     * Executer une commande.
     * 
     * @param cmd
     * @param isUserInput
     * @return TRUE si la commande a bien été prise en compte, ou FALSE si non
     */
	public boolean inputCommand(String cmd, boolean isUserIntent);
	
	/**
	 * Activer ou désactiver la possibilité d'utiliser le champ d'input
	 */
	public void setInputEnabled(boolean set);
	
	/**
	 * Ecrire une string dans l'output
	 * 
	 * @param msg
	 */
	public void outputWrite(String msg);
	
	/**
	 * Ecrire une ligne dans l'output
	 * 
	 * @param msg
	 */
	public void outputWriteLine(final String msg);
	
	/**
	 * Vide l'output
	 */
	public void outputClean();
	
	/**
	 * La taille du buffer qui mémorise les commandes entrées précedement par l'user
	 * @return
	 */
	public int getBufferSize();
	
	/**
	 * Modifier la taille du buffer qui mémorise les commandes entrées précedement par l'user
	 * @return
	 */
	public void setBufferSize(int size);
	
	/**
	 * Cette méthode permet de mettre en place un handler qui receptionnera
	 * la prochaine commande entrée par l'utilisateur.
	 * 
	 * C'est pour donner la possibilité d'interroger l'user, en lui posant
	 * une question, et en traitant sa réponse. Si la r�ponse lui convient,
	 * il est automatiquement retiré après son utilisation.
	 *  
	 * Le Ctrl+C peut eventuellement permettre de faire sauter le handler.
	 * 
	 * @param handler
	 */
	public void setInputHandler(ConsoleInputHandler handler);
	
	/**
	 * Cette mêthode permet de tenter l'interruption du consoleInputHandler
	 * s'il existe. 
	 * 
	 * @param isUserIntent
	 */
	public boolean stopInputHandler(boolean isUserIntent);
	
	/**
	 * Renvoie TRUE si l'autosroll de l'output est activé.
	 */
	public boolean isAutoScrollDown();
	
	/**
	 * Active ou d�sactive l'autoscroll de l'output.
	 */
	public void setAutoScrollDown(boolean enable);
	
	/**
	 * Renvoie les flags de mise en forme.
	 */
	public Flag effects();
	
	/**
	 * Modifier le mode de saisie de l'input
	 */
	public boolean setInputMode(ConsoleInputMode mode);
	
	/**
	 * Renvoyer le texte actuellement dans le champ d'input.
	 * @return
	 */
	public String getInputText();
	
	/**
	 * Modifier le texte dans le champ d'input.
	 * @param value
	 */
	public void setInputText(String value);
	
	/**
	 * Interface des callback des InputHandler.
	 */
	public static interface ConsoleInputCallback {

		/**
		 * 
		 * @param cmd La commande entr�e par l'utilisateur
		 * @param view La vue
		 * @param isUserIntent Indique si c'est l'user qui est à l'initiative de cette réponse
		 * @param question La question posée, ou NULL
		 * @return TRUE si la réponse est correcte et que le handler a terminé son travail,
		 * ou FALSE si la réponse ne va pas et que le ConsoleInputHandler doit rester.
		 */
		boolean handleInputCommand(String cmd, IConsoleView view, boolean isUserIntent, String question);

		/**
		 * Permet de réécrite la commande.
		 * Si la méthode renvoie null, la commande restera inchangée.
		 * Si la méthode renvoie une string, alors la commande sera réécrite.
		 * Si en plus handleInputCommand() renvoie true, alors l'event
		 * sera propagé avec le hasHandler à false, et donc le traitement
		 * "normal" d'envoie de la commande se fera.
		 * @param cmd
		 * @return
		 */
		String rewriteInputCommand(String cmd);
		
	}
	
	/**
	 * Cette classe représente une handler de commande.
	 * Le but est de pouvoir demander quelque chose � l'utilisateur, en récupérant
	 * la réponse.
	 */
	public static class ConsoleInputHandler {
		
		protected ConsoleInputCallback _callback;
		protected String _question;
		protected ConsoleInputMode _mode;
		protected boolean _stoppable;

		/**
		 * Constructeur simple
		 */
		public ConsoleInputHandler(ConsoleInputCallback callback) {
			this(null, ConsoleInputMode.NORMAL, true, callback);
		}
		
		/**
		 * Constructeur avec question
		 */
		public ConsoleInputHandler(String question, ConsoleInputCallback callback) {
			this(question, ConsoleInputMode.NORMAL, true, callback);
		}
		
		/**
		 * Constructeur avec question et flag d'arret
		 */
		public ConsoleInputHandler(String question, boolean isStoppable, ConsoleInputCallback callback) {
			this(question, ConsoleInputMode.NORMAL, isStoppable, callback);
		}

		/**
		 * Constructeur complet
		 */
		public ConsoleInputHandler(String question, ConsoleInputMode mode, boolean isStoppable, ConsoleInputCallback callback) {
			if (callback == null || mode == null) {
				throw new NullPointerException();
			}
			_callback = callback;
			_question = question;
			_mode = mode;
			_stoppable = isStoppable;
		}
		
		public boolean handleInputCommand(String cmd, IConsoleView view, boolean isUserIntent) {
			// On transmet à la callback
			return _callback.handleInputCommand(cmd, view, isUserIntent, _question);
		}
		
		public boolean isStoppable() {
			return _stoppable;
		}
		
		public ConsoleInputMode getInputMode() {
			return _mode;
		}

		public String getQuestionString() {
			return _question;
		}

		public boolean hasQuestionString() {
			return _question != null;
		}

		public String rewriteInputCommand(String cmd) {
			// On transmet à la callback
			return _callback.rewriteInputCommand(cmd);
		}

	}

}
