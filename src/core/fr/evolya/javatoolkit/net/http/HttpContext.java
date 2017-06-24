package fr.evolya.javatoolkit.net.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

public interface HttpContext {

	/**
	 * Renvoie la liste des propri�t�s � envoyer avec la requ�te HTTP
	 * pour une URL donn�e.
	 * 
	 * @param url
	 * @return
	 */
	public Map<String, String> getRequestProperties(URL url);

	/**
	 * Mettre � jour le contexte.
	 * 
	 * @param request
	 * @param connection
	 */
	public void update(HttpRequest request, HttpURLConnection connection);
	
	/**
	 * Modifier le HostnameVerifier qui sera utilis� pour valider les
	 * requ�tes HTTP associ�es � ce contexte.
	 */
	public void setHostnameVerifier(HostnameVerifier verifier);
	
	/**
	 * Renvoie le HostnameVerifier utilis� par ce contexte.
	 * @return
	 */
	public HostnameVerifier getHostnameVerifier();

	/**
	 * D�struction de l'objet
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
