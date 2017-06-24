package fr.evolya.javatoolkit.soho.connector;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.evolya.javatoolkit.code.worker.IWorker;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.net.http.Http;
import fr.evolya.javatoolkit.net.http.HttpContext;
import fr.evolya.javatoolkit.net.http.HttpRequest;
import fr.evolya.javatoolkit.soho.session.ISohoSession;
import fr.evolya.javatoolkit.soho.session.SohoSession;

/**
 * Connecteur à un serveur Soho, permettant d'ouvrir des sessions avec lui.
 */
public class SohoConnector {

	/**
	 * URL racine du serveur Soho.
	 */
	protected URL _serverRootURL;
	
	/**
	 * Liste des session actives maintenues par ce connecteur.
	 */
	protected List<ISohoSession> _activesSessions = new ArrayList<ISohoSession>();
	
	/**
	 * Les events du connecteur.
	 */
	protected EventSource<SohoConnectorListener> _eventsConnector =
			new EventSource<SohoConnectorListener>(SohoConnectorListener.class, this);
	
	/**
	 * Cet objet permet de décoder le JSON.
	 * Il est fourni par la librairie Jackson.
	 */
	public static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * L'encodeur/décodeur de SHA1.
	 */
	protected MessageDigest _sha1;

	/**
	 * Constructeur par défaut.
	 * 
	 * @param serverRootURL Chemin URL vers la racine du serveur Soho.
	 * @throws MalformedURLException Si l'URL est invalide.
	 * @throws NoSuchAlgorithmException Si le SHA1 n'est pas disponible.
	 * @throws SecurityException Si l'URL n'est pas en HTTPS
	 */
	public SohoConnector(String serverRootURL)
		throws MalformedURLException, NoSuchAlgorithmException, SecurityException {
		
		// On fabrique l'URL du serveur
		// Une MalformedURLException peut se lever ici
		_serverRootURL = new URL(serverRootURL);
		
		// On vérifie que le protocole soit bien du HTTPS
		if (!_serverRootURL.getProtocol().toLowerCase().equals("https")) {
			throw new SecurityException("URL with 'https' protocol required");
		}
		
		// Une NoSuchAlgorithmException ou SecurityException peut se lever ici
		_sha1 = MessageDigest.getInstance("SHA1");
		
	}
	
	/**
	 * Les events du connecteur.
	 */
	public EventSource<SohoConnectorListener> getEventsConnector() {
		return _eventsConnector;	
	}
	
	/**
	 * Ouvrir une session anonyme sur le serveur.
	 * 
	 * @return La session
	 */
	public synchronized ISohoSession open() {
		
		// Création de la session
		SohoSession s = new SohoSession(this);
		
		// Enregistrement dans les sessions en cours
		_activesSessions.add(s);
		
		// Event
		_eventsConnector.trigger("onSessionCreated", s, this);
		
		// On renvoie la session
		return s;
		
	}

	/**
	 * Encoder le mot de passe pour l'authentification.
	 * 
	 * @param user
	 * @param password
	 * @param salt
	 * @return Le mot de passe.
	 */
	public String hash(String user, String password, String salt) {
		
		// Vérification des arguments
		if (user == null || password == null || salt == null) {
			throw new NullPointerException();
		}
		
		// On renvoie le hash
		return "b:" + encodeSHA1(salt + ":" + encodeSHA1(user + ":" + password));
		
	}
	
	public String encodeSHA1(String data) {
		return byteArrayToHexString(_sha1.digest(data.getBytes()));
	}
	
	public static String byteArrayToHexString(byte[] b) {
		  String r = "";
		  for (int i = 0; i < b.length; i++) {
			  r += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		  }
		  return r;
		}

	public HttpRequest post(String target, Map<String, Object> data, HttpContext context) throws MalformedURLException {
		return Http.post(_serverRootURL + target, data, context);
	}

	public HttpRequest get(String target, HttpContext context) throws MalformedURLException {
		return Http.get(_serverRootURL + target, context);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> jsonDecode(String data)  {
		try {
			return MAPPER.readValue(data, Map.class);
		}
		catch (Exception ex) {
			return null;
		}
	}

	public URL getServerRootURL() {
		return _serverRootURL;
	}

	public synchronized List<ISohoSession> getActivesSessions() {
		return _activesSessions;
	}
	
	public MessageDigest getSha1Digest() {
		return _sha1;
	}

	public synchronized void dispose() {
		
		// Déjà fait
		if (_activesSessions == null) {
			return;
		}
		
		// On ferme les sessions
		close();
		
		// On propage aux sessions 
		for (ISohoSession s : _activesSessions) {
			s.dispose();
		}
		
		// Disposes
		_eventsConnector.dispose();
		
		// Nettoyage
		_activesSessions.clear();
		_activesSessions = null;
		_serverRootURL = null;
		_eventsConnector = null;
		_sha1 = null;
		
	}
	
	/**
	 * Déconnecter toutes les sessions associées à ce
	 * connecteur. A la fin de l'opération, les ou les serveurs
	 * qui ont été solicités ont normalement reçu un signal
	 * leur indiquant que la session était terminée. 
	 */
	public synchronized void close() {
		
		// Le connecteur n'avait rien d'ouvert
		if (_activesSessions == null || _activesSessions.size() == 0) {
			return;
		}
		
		// Event before
		if (!_eventsConnector.trigger("beforeConnectorClosed", this)) {
			return;
		}

		// On parcours les sessions
		for (ISohoSession s : _activesSessions) {
			
			// On ferme la session
			s.close();
			
		}
		
		// Nettoyage
		_activesSessions.clear();
		
		// Event after
		_eventsConnector.trigger("afterConnectorClosed", this);
		
	}

	public void setWorker(IWorker _worker) {
		// TODO Auto-generated method stub
		
	}

}
