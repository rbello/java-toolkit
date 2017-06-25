package fr.evolya.javatoolkit.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.evolya.javatoolkit.cli.argparser.Args;
import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
import fr.evolya.javatoolkit.code.annotations.ByCopy;
import fr.evolya.javatoolkit.code.annotations.ThreadSafe;
import fr.evolya.javatoolkit.code.funcint.Callback;
import fr.evolya.javatoolkit.code.utils.StringUtils;
import fr.evolya.javatoolkit.code.utils.Utils;

public class CLI {

	/**
	 * Les commandes supportées par cette interface.
	 */
	private HashMap<String, ICLICommand> _commands = new HashMap<String, ICLICommand>();
	
	/**
	 * Le regex utilisé pour parser la ligne de commande.
	 */
	private Pattern _regex = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

	/**
	 * Sortie
	 */
	private CLISessionStream _output;
	
	/**
	 * Sémaphore
	 */
	private Semaphore _lock = new Semaphore(1, true);
	
	/**
	 * Constructeur
	 */
	public CLI(CLISessionStream out) {
		_output = out;
	}
	
	public Handle execute(String commandLine, Option... opts) {
		return execute(commandLine, null, opts); 
	}
	
	public Handle execute(String commandLine, final Callback<Handle, Handle> cb, final Option... opts) {
		Handle handle = executeAsynch(commandLine, cb, opts);
		handle.waitForCompletion();
		return handle;
	}
	
	@ThreadSafe
	@AsynchOperation
	public Handle executeAsynch(String commandLine, final Callback<Handle, Handle> cb, final Option... opts) {
		
		// Méfiance...
		if (commandLine == null) {
			throw new NullPointerException("commandLine is null");
		}
		
		List<Option> opts2 = Arrays.asList(opts);
		
		// On fabrique une réponse
		final Handle handle = parseInput(
				commandLine,
				opts2.contains(Option.USER_INTENT),
				null); // TODO Supporter le user object
		
		// Lancement dans un nouveau thread
		final Thread t = new Thread(new Runnable() {
			public void run() {
				
				// On attend la fin de l'execution de la commande en cours
				try {
					
					// Peut lever une InterruptedException
					_lock.acquire();
					
					// On arrète
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					
					// On vérifie que cette commande existe
					if (!_commands.containsKey(handle.commandName)) {
						handle.returnState = 404;
						handle.returnString = "Error: command not found";
						cb.onFailure(handle);
						return;
					}
					
					// On insert les données dans la commande
					parseCommand(handle);
					
					// Par défaut, la commande a bien été traitée mais il n'y a aucune réponse
					handle.returnState = 204;
					handle.returnString = "OK";
					
					// On execute la requête
					execute(handle);
					handle.executed = true;
					
					// Fin de l'opération
					if (handle.isSuccess())
						cb.onSuccess(handle);
					else
						cb.onFailure(handle);
					
				}
				/*catch (InterruptedException e) {
					// Interruption
					return;
				}*/
				catch (Throwable ex) {
					
					// On enregistre l'exception
					handle.invokeException = ex;
					
					// On modifie l'état de la réponse
					handle.returnState = 500;
					handle.returnString = "Error: internal " + ex.getClass().getSimpleName().replace("Exception", "");
					
					ex.printStackTrace();
					
				}
				finally {
					
					// On indique que la commande a été executée
					handle.executed = true;
					
					// On retire l'association au thread
					handle.thread = null;
					
					// On libère l'execution d'autres commandes
					_lock.release();
					
				}
				
			}
		}, "CLI-" + super.hashCode());
		
		// On donne le thread au handle pour le cancel()
		handle.thread = t;
		
		// On lance le thread
		t.start();
		
		// On renvoie le handle
		return handle;
		
	}


	
	/**
	 * Traiter une commande.
	 * 
	 * @param commandLine		La ligne de commande à traiter en entrée
	 * @param isUserIntent		TRUE si la commande vient directement de l'utilisateur humain.
	 * @param userObject		Object pour identifier l'utilisateur qui a lancé l'action.
	 * @param handleExceptions	Pour que la méthode ne renvoie pas les RuntimeExceptions.
	 * @return Un objet qui contient à la fois la réponse, et tous les éléments de la question,
	 * c-à-d tous les éléments de la ligne de commande et de la commande.
	 */
	@Deprecated
	public Handle input(String commandLine, boolean isUserIntent, Object userObject, boolean handleExceptions) {
		
		// Méfiance...
		if (commandLine == null) {
			throw new NullPointerException("commandLine is null");
		}

		// On fabrique une réponse
		Handle result = parseInput(commandLine, isUserIntent, userObject);
		
		// On execute la requête
		executeQuery(result, handleExceptions);
		
		// Si il faut piper
		if (result.returnState >= 200 && result.returnState < 300 && result.pipeQuery != null) {
			// On renvoie le résultat de la commande pipée
			return pipe(result, isUserIntent, userObject, handleExceptions);
		}
		
		// On renvoie le résultat de cette commande
		return result;
		
	}
	
	private Handle pipe(Handle request, boolean isUserIntent, Object userObject, boolean handleExceptions) {
		
		// On fabrique une réponse à partir des données à piper
		Handle piped = parseInput(request.pipeQuery, isUserIntent, userObject);
		
		// Debug
		//System.out.println("Pipe " + request.commandName + " -> " + piped.stringRaw);
		
		// On ajoute les données pipées aux tokens de la commande pipée
		String[] copy = new String[piped.tokensAll.length + 1];
		for (int i = 0, l = piped.tokensAll.length; i < l; i++)
			copy[i] = piped.tokensAll[i];
		copy[piped.tokensAll.length] = request.returnString;
		piped.tokensAll = copy;
		
		// Et aussi aux arguments
		copy = new String[piped.tokensArgs.length + 1];
		for (int i = 0, l = piped.tokensArgs.length; i < l; i++)
			copy[i] = piped.tokensArgs[i];
		copy[piped.tokensArgs.length] = request.returnString;
		piped.tokensArgs = copy;
		
		// Et aussi dans la ligne de commande brute
		piped.stringRaw += piped.stringRaw.isEmpty() ? "" : " <PipedData>";
		
		// On execute la commande pipée
		executeQuery(piped, handleExceptions);
		
		// Si il faut encore piper
		if (piped.returnState >= 200 && piped.returnState < 300 && piped.pipeQuery != null) {
			// On renvoie le résultat de la commande pipée
			return pipe(piped, isUserIntent, userObject, handleExceptions);
		}
		
		// On renvoie le résultat de cette commande
		return piped;
		
	}

	@Deprecated
	private void executeQuery(Handle result, boolean handleExceptions) {

		// On vérifie que cette commande existe
		if (!_commands.containsKey(result.commandName)) {
			result.returnState = 404;
			result.returnString = "Error: command not found";
			return;
		}
		
		try {
			
			// On parse le résultat
			parseCommand(result);
			
			// Par défaut, la commande a bien été traitée mais il n'y a aucune réponse
			result.returnState = 204;
			
			// On execute la requête
			execute(result);
		}
		catch (Throwable ex) {
			
			// On enregistre l'exception
			result.invokeException = ex;
			
			// On modifie l'état de la réponse
			result.returnState = 500;
			result.returnString = "Internal Error " + ex.getClass().getSimpleName();
			
			// Non-traitement des exceptions silencieuse : propagation des events
			if (!handleExceptions) {
				throw new RuntimeException(ex);
			}
			
		}
		
	}

	private void execute(Handle result) throws Exception {
		
//		try {
			
			// On lance l'appel à la commande, en lui passant l'objet
			// de réponse qui contient aussi toutes les données de la question.
			if (!result.commandObject.execute(result)) {
				
				// En cas d'erreur, si la commande n'a pas changé le status on
				// va le changer maintenant.
				if (result.returnState == 204) {
					result.returnState = 501;
					if (result.returnString == "OK") {
						result.returnString = result.hasPushed ? null : "Error: unknown";
					}
				}
				
			}
		
//			// La commande a renvoyée un succés et un retour en string, mais
//			// n'a pas modifié le code de retour. On change l'état.
//			else if (result.returnState == 204 && result.returnString == null) {
//				result.returnState = 200;
//				result.returnString = "OK";
//			}
			
//		}
//		catch (Throwable ex) {
//			
//			// On enregistre l'exception
//			result.invokeException = ex;
//			
//			// On modifie l'état de la réponse
//			result.returnState = 500;
//			result.returnString = "Internal Error " + ex.getClass().getSimpleName();
//			
//			// Non-traitement des exceptions silencieuse : propagation des events
//			if (!handleExceptions) {
//				throw new RuntimeException(ex);
//			}
//			
//		}
	}

	private void parseCommand(Handle result) throws Exception {
		
		// On recupère la commande en elle-même
		result.commandObject = _commands.get(result.commandName);
		
		// On garde l'objet qui sera utilisé à l'instruction suivante
		// pour contenir les données de la ligne de commande une fois parsée.
		// Dans le contexte du parser, il s'agit du target object, qui va
		// être setté par reflexion.
		result.tokensParsed = result.commandObject.getTargetObject();
		
		// On parse les arguments, et on enregistre le résultat dans
		// le target object du parser. Cette méthode renvoie la liste
		// des tokens qui n'ont pas été parsée car ne correspondant pas
		// au format attendu par la commande.
		if (result.tokensParsed != null) {
//			try {
				result.tokensExtra = Args.parse(result.tokensParsed, result.tokensArgs);
//			}
			// A la moindre erreur inattendue
//			catch (RuntimeException ex) {
//				
//				// On enregistre l'exception
//				result.invokeException = ex;
//				
//				// On modifie l'état de la réponse
//				result.returnState = 500;
//				result.returnString = "Internal Error " + ex.getClass().getSimpleName();
//				
//				// Non-traitement des exceptions silencieuse : propagation des events
//				if (!handleExceptions) {
//					throw ex;
//				}
//				
//			}
		}
		else {
			// Si il n'y a pas d'objets cible pour parser la commande, on considère
			// que tous les tokens sont en plus.
			result.tokensExtra = new ArrayList<String>(Arrays.asList(result.tokensArgs));
		}
		
	}
	
	/*private CLIQueryResponse parseInput(String[] commandLine, boolean isUserIntent, Object userObject) {
		
		// Construction de l'objet qui sera à la fois pour contenir
		// la réponse, mais qui mémorise tous les éléments de la question.
		CLIQueryResponse result = new CLIQueryResponse();
		
		// On enregistre les tokens de la commande
		result.tokensAll = commandLine;
		
		// On extrait les autres informations
		extract(result, isUserIntent, userObject);
		
		// Et on renvoie le tout
		return result;
		
	}*/
	
	private Handle parseInput(String commandLine, boolean isUserIntent, Object userObject) {
		
		// Construction de l'objet qui sera à la fois pour contenir
		// la réponse, mais qui mémorise tous les éléments de la question.
		Handle result = new Handle(_output);
		
		// On parse la ligne de commande en prenant en compte les quotes
		Matcher m = _regex .matcher(commandLine);
		List<String> tokensList = new ArrayList<String>();
		while (m.find()) tokensList .add(m.group(1));
		
		// On a détecté des commandes à passer en pipe, on va les enregistrer
		// dans la réponse aussi.
		commandLine = StringUtils.EMPTYSTRING;
		int i = 0;
		for (String token : new ArrayList<String>(tokensList)) {
			if (result.pipeQuery != null) {
				result.pipeQuery += (result.pipeQuery.isEmpty() ? StringUtils.EMPTYSTRING : StringUtils.WHITESPACE) + token;
				tokensList.remove(i);
			}
			else if (token.equals(StringUtils.PIPE)) {
				result.pipeQuery = StringUtils.EMPTYSTRING;
				tokensList.remove(i);
			}
			else {
				commandLine += (commandLine.isEmpty() ? StringUtils.EMPTYSTRING : StringUtils.WHITESPACE) + token;
				i++;
			}
		}
		
		// On enregistre les tokens de la commande
		result.tokensAll = tokensList.toArray(new String[0]);
		
		// On extrait les autres informations
		extract(result, isUserIntent, userObject);
		
		return result;
		
	}
	
	/**
	 * Extrait les 
	 * 
	 * 
	 */
	private void extract(Handle result, boolean isUserIntent, Object userObject) {
		
		// On enregistre la ligne de commande brute
		result.stringRaw = Utils.implode(StringUtils.WHITESPACE, result.tokensAll);
		
		// On sauvegarde les paramàtres données
		result.isUserIntent = isUserIntent;
		result.userObject = userObject;
		
		// La ligne de commande est vide
		if (result.stringRaw.trim().isEmpty()) {
			result.returnState = 400;
			result.returnString = "Empty";
			return;
		} 
		
		// On recupère le nom de la commande
		result.commandName = result.tokensAll[0].toLowerCase();
		
		// On isole les paramètres (on retire le nom de la commande)
		List<String> args = new ArrayList<String>();
		for (int i = 1, l = result.tokensAll.length; i < l; ++i) {
			args.add(result.tokensAll[i]);
		}
		
		// On sauvegarge les arguments dans l'objet de résultat
		result.tokensArgs = args.toArray(new String[0]);
		
	}
	
	/**
	 * Ajouter une commande disponible 
	 */
	public void addCommand(String commandName, ICLICommand command) {
		_commands.put(commandName, command);
	}

	/**
	 * Renvoie une copie de la liste des commandes.
	 */
	@ByCopy
	public Map<String, ICLICommand> getCommands() {
		return new HashMap<String, ICLICommand>(_commands);
	}

}
