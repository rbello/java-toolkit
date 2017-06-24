package fr.evolya.javatoolkit.cli;

import java.io.IOException;
import java.util.List;

import fr.evolya.javatoolkit.code.Util;

public class Handle {

	private final CLISessionStream _out;

	public Handle(CLISessionStream out) {
		_out = out;
	}

	/**
	 * L'état de la réponse.
	 * 
	 * 	 0 = Rien, la réponse n'a même pas été traitée
	 * 200 = OK
	 * 204 = OK, mais pas de retour string
	 * 400 = Ligne de commande vide
	 * 404 = Command not found
	 * 500 = Erreur interne, une RuntimeException
	 * 501 = La commande a renvoyée une erreur mais ne l'a pas renseignée
	 */
	public int					returnState			= 0;
	
	/**
	 * TRUE si la demande a été lancée par un utilisateur humain.
	 */
	public boolean				isUserIntent		= false;
	
	/**
	 * L'objet qui identifie l'utilisateur qui a lancé la commande.
	 */
	public Object				userObject			= null;
	
	/**
	 * La ligne de commande brute en entrée.
	 */
	public String				stringRaw			= null;
	
	/**
	 * Les mots qui composents la ligne de commande, séparés par tous les caractères blancs.
	 */
	public String[]				tokensAll			= null;
	
	/**
	 * Idem que tokensAll sans le premier argument, c-à-d le nom de la commande.
	 */
	public String[]				tokensArgs			= null;
	
	/**
	 * L'objet qui contient les données parsées de la ligne de commande.
	 */
	public Object				tokensParsed		= null;
	
	/**
	 * Les tokens qui n'ont pas été parsés de la ligne de commande, car
	 * non pris en compte par l'objet cible.
	 */
	public List<String>			tokensExtra			= null;
	
	/**
	 * Le nom de la commande invoquée.
	 */
	public String				commandName			= null;
	
	/**
	 * La commande elle-même, si elle a été trouvée.
	 */
	public ICLICommand			commandObject		= null;

	/**
	 * Si le CLI a été lancé avec le paramètre handleExceptions à TRUE, les RuntimeException
	 * levées pendant l'invocation de la commande seront enregistrées ici.
	 */
	public Throwable			invokeException		= null;

	/**
	 * Le retour de la commande, sous forme de string
	 */
	public String				returnString		= null;
	
	protected boolean executed = false;

	protected Thread thread = null;

	protected String pipeQuery = null;

	protected boolean hasPushed = false;
	
	public synchronized void write(String string) {
		hasPushed = true;
		try {
			_out.outputWrite(string);
		} catch (IOException e) {
		}
//		if (returnString == null)
//			returnString = string;
//		else
//			returnString += /*"\n" +*/ string;
	}
	
	public synchronized void writeLine(String string) {
		hasPushed = true;
		try {
			_out.outputWriteLine(string);
		} catch (IOException e) {
		}
//		if (returnString == null)
//			returnString = string;
//		else
//			returnString += Util.NL + string;
	}

	public boolean hasPushed() {
		return hasPushed;
	}

	public void waitForCompletion() {
		try{
			while (!executed) {
				Thread.sleep(1);
			}
		}
		catch (InterruptedException ex) {
			return;
		}
	}
	
	public boolean cancel() {
		if (!executed && thread != null) {
			thread.interrupt();
			return true;
		}
		return false;
	}
	
	public boolean isExecuted() {
		return executed;
	}
	
	public boolean isSuccess() {
		return /*executed && */returnState >= 200 && returnState < 300;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(commandName);
		sb.append(' ');
		sb.append(returnState);
		if (isSuccess()) {
			sb.append(" success");
		}
		else {
			sb.append(" failure ");
			sb.append(invokeException);
		}
		sb.append(" ");
		sb.append(returnString);
		return sb.toString();
	}

	public synchronized void writeLine() {
		writeLine(Util.EMPTYSTRING);
	}

	public synchronized void removeLines(int howMany) {
		try {
			_out.outputCleanLines(howMany + 1);
		} catch (IOException e) { }
	}

	public synchronized void replaceLines(int howMany, String msg) {
		removeLines(howMany);
		writeLine(msg);
	}

}