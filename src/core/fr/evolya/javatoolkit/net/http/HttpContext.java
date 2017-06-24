package fr.evolya.javatoolkit.net.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

public interface HttpContext {

	/**
	 * Renvoie la liste des propriétés à envoyer avec la requête HTTP
	 * pour une URL donnée.
	 * 
	 * @param url
	 * @return
	 */
	public Map<String, String> getRequestProperties(URL url);

	/**
	 * Mettre à jour le contexte.
	 * 
	 * @param request
	 * @param connection
	 */
	public void update(HttpRequest request, HttpURLConnection connection);
	
	/**
	 * Modifier le HostnameVerifier qui sera utilisé pour valider les
	 * requêtes HTTP associées à ce contexte.
	 */
	public void setHostnameVerifier(HostnameVerifier verifier);
	
	/**
	 * Renvoie le HostnameVerifier utilisé par ce contexte.
	 * @return
	 */
	public HostnameVerifier getHostnameVerifier();

	/**
	 * Déstruction de l'objet
	 */
	public void dispose();

	/**
	 * cookieName => [ propertyName => propertyValue ]
	 * 
	 * @param host
	 * @return
	 */
	public Map<String, Map<String, String>> getCookies(String host);
	
}
