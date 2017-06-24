package fr.evolya.javatoolkit.soho.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.net.http.HttpContext;
import fr.evolya.javatoolkit.net.http.HttpException;
import fr.evolya.javatoolkit.net.http.HttpRequest;
import fr.evolya.javatoolkit.net.http.SingleHostnameVerifier;
import fr.evolya.javatoolkit.net.http.StatefullHttpContext;
import fr.evolya.javatoolkit.soho.connector.SohoConnector;

/**
 * Session avec conservation de l'état.
 */
public class SohoSession implements ISohoSession {

	private static final String SESSION_COOKIE_NAME = "PHPSESSID";

	/**
	 * Le connecteur associé à cette session.
	 */
	protected SohoConnector _connector;
	
	/**
	 * Les events de la session.
	 */
	protected EventSource<SohoSessionListener> _eventsSession =
			new EventSource<SohoSessionListener>(SohoSessionListener.class, this);
	
	/**
	 * Le contexte HTTP utilisé pour le dialogue avec le serveur.
	 */
	protected HttpContext _context;
	
	/**
	 * L'identifiant de la session, donn� par le serveur.
	 */
	protected String _sessionID = null;
	
	/**
	 * L'identifiant de l'utilisateur.
	 * Si NULL, alors la session n'est pas authentifi�e.
	 */
	protected String _userID = null;
	
	/**
	 * Le timestamp (en millisecondes) datant la dernière requête
	 * au serveur.
	 */
	protected long _lastUpdate = 0;

	/**
	 * Mémorisation de la dernière requête faite au serveur.
	 */
	protected HttpRequest _lastRequest;

	/**
	 * Constructeur par défaut.
	 * 
	 * @param connector
	 */
	public SohoSession(SohoConnector connector) {
		
		// On enregistre le conntecteur
		_connector = connector;
		
		// On fabrique le contexte HTTP qui sera utilisé avec cette session
		_context = new StatefullHttpContext();
		
		// On associe un HostnameVerifier qui autorise uniquement le domaine de l'hôte
		_context.setHostnameVerifier(new SingleHostnameVerifier(getServerRootURL().getHost()));
		
	}

	@Override
	public synchronized void auth(String userID, String password) throws HttpException, MalformedURLException {
		
		// Déjà authentifié
		if (_userID != null) {
			return;
		}
		
		// Event before
		if (!_eventsSession.trigger("beforeSessionAuthentication", this, userID)) {
			throw new SecurityException("Internal authentication stop");
		}
		
		// On recupère le salt
		String salt = salt();
		
		// Tableau des données
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("login-wgcrt", userID);
		data.put("password-wgcrt", _connector.hash(userID, password, salt));
		data.put("submit-wgcrt", "Send");
		
		// On envoie la connexion en synchrone
		_lastRequest = _connector.post("/ws.php?w=auth", data, _context);
		_lastUpdate = new Date().getTime();
		_sessionID = getSessionID();
		
		// Vérification du code de retour du serveur
		if (_lastRequest.getReturnCode() < 200 || _lastRequest.getReturnCode() >= 300) {
			
			// On construit une exception
			HttpException ex = new HttpException(_lastRequest.getReturnCode(), _lastRequest.getReturnMessage());
			
			// On propage un event failure
			_eventsSession.trigger("onSessionAuthenticationFailure", this, userID, ex);
			
			// Et un event de fin d'authentification
			_eventsSession.trigger("afterSessionAuthentication", this, userID, false);
			
			// Et on lance l'exception
			throw ex;
			
		}
		
		// Event success
		_eventsSession.trigger("onSessionAuthenticationSuccess", this, userID);
		
		// On sauvegarde le nom d'utilisateur
		_userID = userID;
		
		// Event after
		_eventsSession.trigger("afterSessionAuthentication", this, userID, true);
		
	}

	/**
	 * Renvoie le salt utilis� pour l'authentification avec le serveur.
	 * 
	 * @return La cha�ne servant � faire le salt d'un hash de password
	 * @throws MalformedURLException 
	 * @throws IOException
	 */
	protected synchronized String salt() throws HttpException, MalformedURLException {
		
		// Execution de la requ�te
		_lastRequest = _connector.get("/ws.php?w=auth&salt=1", _context);
		_lastUpdate = new Date().getTime();
		_sessionID = getSessionID();
		
		// V�rification du code de retour du serveur
		if (_lastRequest.getReturnCode() < 200 || _lastRequest.getReturnCode() >= 300) {
			throw new HttpException(_lastRequest.getReturnCode(), _lastRequest.getReturnMessage());
		}
		
		// D�codage du JSON
		Map<String, Object> map = _connector.jsonDecode(_lastRequest.getReturnBody());
		if (map == null) {
			throw new HttpException(601, "Invalid Response JSON");
		}
		
		// V�rification des donn�es de retour
		if (!map.containsKey("salt")) {
			throw new HttpException(602, "Invalid Response Array");
		}
		
		// Renvoie du salt
		return (String) map.get("salt");
		
	}

	@Override
	public long getLastUpdateTimestamp() {
		return _lastUpdate;
	}

	@Override
	public String getSessionID() {
		Map<String, Map<String, String>> cookies = _context.getCookies(_connector.getServerRootURL().getHost());
		for (String cookieName : cookies.keySet()) {
			if (cookieName.equals(SESSION_COOKIE_NAME)) {
				//System.out.println(SESSION_COOKIE_NAME+"="+cookies.get(cookieName).get("value"));
				return cookies.get(cookieName).get("value");
			}
		}
		return null;
	}

	@Override
	public String getUserName() {
		return _userID;
	}

	@Override
	public boolean isAlive() {
		return _sessionID != null;
	}

	@Override
	public boolean isLogged() {
		return _userID != null;
	}

	@Override
	public SohoConnector getConnector() {
		return _connector;
	}

	public URL getServerRootURL() {
		return _connector.getServerRootURL();
	}
	
	@Override
	public ServerCommandResult sendCLI(String cmd) throws HttpException, MalformedURLException {
		
		// Arguments � envoyer au serveur
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("c", cmd); // command
		
		// Execution de la requ�te
		_lastRequest = _connector.post("/ws.php?w=exec-cmd", data, _context);
		_lastUpdate = new Date().getTime();
		_sessionID = getSessionID();
		
		// V�rification du code de retour
		if (_lastRequest.getReturnCode() < 200 || _lastRequest.getReturnCode() >= 300) {
			throw new HttpException(_lastRequest.getReturnCode(), _lastRequest.getReturnMessage());
		}
		
		// {"failure": boolean, "returnCode": int, "user": string, "data": string}
		// D�codage du JSON
		Map<String, Object> map = _connector.jsonDecode(_lastRequest.getReturnBody());
		if (map == null) {
			throw new HttpException(601, "Invalid Response JSON");
		}
		
		// V�rification des donn�es de retour
		if (!map.containsKey("failure") || !map.containsKey("returnCode") || !map.containsKey("user") || !map.containsKey("data")) {
			throw new HttpException(602, "Invalid Response Array");
		}
		
		// On renvoie le r�sultat
		return new ServerCommandResult(
			this,
			cmd,
			(Boolean) map.get("failure"),
			(Integer) map.get("returnCode"),
			(String) map.get("user"),
			(String) map.get("data"),
			map.containsKey("concat") ? (String) map.get("concat") : ""
		);
		
	}
	
	public HttpRequest getLastRequest() {
		return _lastRequest;
	}

	@Override
	public HttpContext getContext() {
		return _context;
	}

	@Override
	public synchronized boolean logout(boolean gracefull) {
		
		// D�j� logout
		if (_userID == null) {
			return true;
		}
		
		// Event before
		if (!_eventsSession.trigger("beforeSessionLogout", this, _userID)) {
			return false;
		}
		
		// On fait une copie
		final String uid = _userID;
		
		// Si on n'attends pas la r�ponse du serveur, on supprime directement l'userID,
		// au cas o� des erreurs viendraient tout faire foirer.
		if (!gracefull) {
			_userID = null;
		}
		
		// Indiquer le succ�s du logout serveur
		boolean success = false;
		
		// On tente de se d�connecter aupr�s du serveur
		try {
			
			// On fait la demande au serveur
			_lastRequest = _connector.get("/ws.php?w=auth&logout=1", _context);
			
			// On met � jour le timer
			_lastUpdate = new Date().getTime();
			
			// On sauvegarde l'ID de session
			_sessionID = getSessionID();
			
			// Pour indiquer si le logout est un succ�s
			success = _lastRequest.isSuccess();
			
		} catch (MalformedURLException e) {
			// Ce n'est vraiment pas sens� arriver
		}
		
		// Dans tous les cas, on supprime l'utilisateur maintenant
		_userID = null;
		
		// Event after
		_eventsSession.trigger("afterSessionLogout", this, uid, success);
		
		return success;
	}

	@Override
	public synchronized void close() {
		
		// D�j� close
		if (_sessionID == null) {
			return;
		}
		
		// Event before
		_eventsSession.trigger("beforeSessionClosed", this);
		
		// Logout (si besoin)
		logout(true);
		
		// TODO Envoyer une requ�te pour terminer la session PHP ?
		// Pas impl�ment� c�t� serveur
		
		// Reset
		_lastUpdate = 0;
		_sessionID = null;
		if (_lastRequest != null) {
			_lastRequest.dispose();
			_lastRequest = null;
		}
		
		// Event after
		_eventsSession.trigger("afterSessionClosed", this);
		
	}
	
	@Override
	public synchronized void dispose() {
		close();
		if (_context != null) {
			_context.dispose();
			_context = null;
		}
		_lastUpdate = 0;
		_sessionID = null;
		_userID = null;
		_connector = null;
	}

	@Override
	public String[] getAutoComplete(String inputText) throws HttpException, MalformedURLException {
		
		// Arguments à envoyer au serveur
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("a", inputText); // autocomplete
		
		// Execution de la requête
		_lastRequest = _connector.post("/ws.php?w=exec-cmd", data, _context);
		_lastUpdate = new Date().getTime();
		_sessionID = getSessionID();
		
		// Vérification du code de retour
		if (_lastRequest.getReturnCode() < 200 || _lastRequest.getReturnCode() >= 300) {
			throw new HttpException(_lastRequest.getReturnCode(), _lastRequest.getReturnMessage());
		}
		
		// Lecture de la requ�te
		try {
			return SohoConnector.MAPPER.readValue(_lastRequest.getReturnBody(), String[].class);
		} catch (Exception ex) {
			throw new HttpException(601, "Invalid Response JSON");
		}
		
	}

	@Override
	public EventSource<SohoSessionListener> getEventsSession() {
		return _eventsSession;
	}

	@Override
	public HttpRequest post(String target, Map<String, Object> data) throws MalformedURLException {
		return _connector.post(target, data, _context);
	}

}
