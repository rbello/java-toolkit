package fr.evolya.javatoolkit.soho.session;

import java.net.MalformedURLException;
import java.util.Map;

import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.net.http.HttpContext;
import fr.evolya.javatoolkit.net.http.HttpException;
import fr.evolya.javatoolkit.net.http.HttpRequest;
import fr.evolya.javatoolkit.soho.connector.SohoConnector;

/**
 * Interface des sessions avec un serveur Soho.
 */
public interface ISohoSession {

	/**
	 * Indique si la session est toujours active.
	 */
	public boolean isAlive();

	/**
	 * Renvoie un timestamp indiquant le moment de la dernière requête
	 * au serveur. C'est utilisé pour la détermination du isAlive.
	 * Si aucune requête n'a été faite au serveur, cette méthode renvoie 0.
	 */
	public long getLastUpdateTimestamp();

	/**
	 * Renvoie l'identifiant de la session donné par le serveur. Si la
	 * connexion n'a jamais été utilisée, cette méthode renvoie NULL.
	 */
	public String getSessionID();

	/**
	 * Indique si la session est authentifée auprès du serveur.
	 */
	public boolean isLogged();
	
	/**
	 * Déconnection de l'authentification
	 */
	public boolean logout(boolean gracefull);

	/**
	 * Renvoie le nom de l'utilisateur actuellement authentifié sur
	 * cette session, ou NULL si la session n'est pas loggée.
	 */
	public String getUserName();
	
	/**
	 * Renvoie le connecteur qui a initialisé cette session.
	 */
	public SohoConnector getConnector();

	/**
	 * Supprimer cette session.
	 */
	public void dispose();
	
	/**
	 * Renvoie la source des events de la session.
	 */
	public EventSource<SohoSessionListener> getEventsSession();

	/**
	 * Authentification de la session.
	 * 
	 * @param user
	 * @param password
	 * @throws HttpError En cas de retour négatif du serveur
	 * @throws HttpException En cas de réponse négative du serveur
	 */
	public void auth(String user, String password) throws HttpException, MalformedURLException;

	/**
	 * Envoyer une commande en CLI au serveur 
	 */
	public ServerCommandResult sendCLI(String cmd) throws HttpException, MalformedURLException;

	/**
	 * Renvoie le contexte HTTP actuel de la sesesion
	 */
	public HttpContext getContext();
	
	/**
	 * Renvoie l'objet de la dernière requête HTTP faite par cette session
	 * @return
	 */
	public HttpRequest getLastRequest();
	
	/**
	 * Envoie une demande d'autocomplete de commande au serveur.
	 * @param inputText
	 * @return
	 */
	public String[] getAutoComplete(String inputText) throws HttpException, MalformedURLException;
	
	/**
	 * Fermer une session, en arretant toute communication avec le serveur.
	 * Ce dernier doit normalement avoir fait le ménage de son côté pour
	 * supprimer la session.
	 */
	public void close();
	
	public HttpRequest post(String url, Map<String, Object> data) throws MalformedURLException;
	
	/**
	 * Le résultat d'une commande CLI
	 */
	public static class ServerCommandResult {

		/**
		 * La session associée.
		 */
		public final ISohoSession session;
		
		/**
		 * La ligne de commande brute.
		 */
		public final String cmd;
		
		/**
		 * Un boolean pour savoir s'il y a eu un problème.
		 */
		public final boolean failure;
		
		/**
		 * Le code de retour, ce n'est pas le HTTP mais le code renvoyé par la commande
		 * côté serveur.
		 */
		public final int returnCode;
		
		/**
		 * L'identifiant de l'utilisateur actuel de la session, overridé par le sudo.
		 */
		public final String userID;
		
		/**
		 * Le résultat de la commande, sous forme de string.
		 */
		public final String data;
		
		/**
		 * En cas d'autocomplete, le fragment à concatener à l'input.
		 */
		public final String concat;

		/**
		 * Constructeur
		 */
		public ServerCommandResult(ISohoSession session, String cmd, boolean failure, int returnCode, String userID, String data, String concat) {
			this.session = session;
			this.cmd = cmd;
			this.failure = failure;
			this.returnCode = returnCode;
			this.userID = userID;
			this.data = data;
			this.concat = concat;
		}
		
		/**
		 * Afficher la réponse sous forme de string.
		 */
		public String toString() {
			return data;
		}
		
	}

}
