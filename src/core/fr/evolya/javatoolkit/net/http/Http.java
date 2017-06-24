package fr.evolya.javatoolkit.net.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import fr.evolya.javatoolkit.code.Logs;

public class Http {
	
	public static final String POST = "POST";
	public static final String GET  = "GET";
	
	/**
	 * Logger
	 */
	public static final Logger LOGGER = Logs.getLogger("HTTP");
	
	/**
	 * Executer une requ�te HTTP � partir d'un contexte en synchrone.
	 * 
	 * C'est la m�thode de base, qui sert � toutes les autres m�thodes.
	 * 
	 * @param request
	 * @param context
	 * @return La requ�te contenant la r�ponse.
	 */
	public static HttpRequest execute(final HttpRequest request, final HttpContext context) {
		
		// V�rification arguments
		if (request == null) {
			throw new NullPointerException();
		}
		
		// Debug
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, "HTTP " + request.getRequestMethod()
					+ " -> " + request.getRequestURL() + " (asynch=off"
					+ (context != null ? "; context=yes": "") + ")");
		}
		
		// La connexion
		HttpURLConnection c = null;
		
		// On tente de cr�er la connexion
		try {
			// HTTPS
			if (request.getRequestURL().getProtocol().equals("https")) {
				c = (HttpsURLConnection) request.getRequestURL().openConnection();
				// Avant d'utiliser de la connexion HTTPS, on regarde si un HostnameVerifier
				// est associ� au contexte
				if (context != null && context.getHostnameVerifier() != null) {
					((HttpsURLConnection) c).setHostnameVerifier(context.getHostnameVerifier());
				}
			}
			// HTTP
			else {
				c = (HttpURLConnection) request.getRequestURL().openConnection();
			}
		}
		catch (IOException ex) {
			// Traitement de l'erreur
			request.failure(ex);
			request.setReturnCode(600);
			request.setReturnMessage("Internal Client Error: create "
					+ request.getRequestURL().getProtocol() + " connection");
			return request;
		}
		
		// On sauvegarde la connexion si c'est demand�
		if (request.isKeepAlive()) {
			request.setConnection(c);
		}
		
		// Configuration
		c.setUseCaches(false);
		c.setDoInput(true);
		c.setDoOutput(true);
		c.setAllowUserInteraction(false);
		
		// On indique la m�thode requ�te
		try {
			
			c.setRequestMethod(request.getRequestMethod());
			
		} catch (ProtocolException ex) {
			// Traitement de l'erreur
			request.failure(ex);
			request.setReturnCode(600);
			request.setReturnMessage("Internal Client Error: protocol exception");
			return request;
		}
		
		// On passe les properties du contexte
		if (context != null) {
			Map<String, String> p = context.getRequestProperties(request.getRequestURL());
			for (String key : p.keySet()) {
				c.setRequestProperty(key, p.get(key));
			}
		}
		
		// On r�cup�re les donn�es de la requ�te
		String body = request.getRequestDataRaw();
		
		// Longueur de la requ�te
		c.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
		
		// Pour le mode POST
		if (request.getRequestMethod().toUpperCase().equals("POST")) {
			c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		}
		
		// Ouverture de la connexion
		try {
			
			c.connect();
			
		} catch (IOException ex) {
			// Traitement de l'erreur
			request.failure(ex);
			request.setReturnCode(600);
			request.setReturnMessage("Internal Client Error: open "
					+ request.getRequestURL().getProtocol() + " connection");
			return request;
		}
		
		// Envoie des donn�es
		DataOutputStream out = null;
		try {
			
			// Ouverture du flux d'�criture
			out = new DataOutputStream(c.getOutputStream());
			
			// Ecriture des donn�es
			out.writeBytes(body);
			body = null;
		    out.flush();

		} catch (IOException ex) {
			// Traitement de l'erreur
			request.failure(ex);
			request.setReturnCode(600);
			request.setReturnMessage("Internal Client Error: write");
			return request;
		}
		finally {
			// Fermeture du flux de d'�criture
		    try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) { }
		}

	    
		// Lecture des headers
		String headerName = null;
		for (int i=1; (headerName = c.getHeaderFieldKey(i)) != null; i++) {
			request.addReturnHeader(headerName, c.getHeaderField(i));
		}
		
		try {
			// Lecture des codes et messages de retour
			request.setReturnCode(c.getResponseCode());
			request.setReturnMessage(c.getResponseMessage());
		}
		catch (IOException ex) {
			// Traitement de l'erreur
			request.failure(ex);
			request.setReturnCode(600);
			request.setReturnMessage("Internal Client Error: read status");
			return request;
		}
		
		// On met � jour le contexte
		if (context != null) {
			context.update(request, c);
		}
		
		// R�ception des donn�es
		BufferedReader rd = null;
		try {
			
			// Ouverture du flux de lecture
			rd = new BufferedReader(new InputStreamReader(c.getInputStream()));
			
			// Lecture du corps de la r�ponse
			String line;
			StringBuffer rs = new StringBuffer(); 
			while((line = rd.readLine()) != null) {
				rs.append(line);
				rs.append('\r');
			}
			request.setReturnBody(rs.toString());
			
		}
		catch (IOException ex) {
			// Traitement de l'erreur
			request.failure(ex);
			return request;
		}
		finally {
			// Fermeture du flux de lecture
			try {
				if (rd != null) {
					rd.close();
				}
			}
			catch (IOException ex) { }
				
		}
		
		// Debug
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, "HTTP " + request.getRequestMethod()
					+ " <- " + request.getRequestURL() + " : " + request.getReturnCode()
					+ " " + request.getReturnMessage());
		}
		
		// On coupe la connexion si on en n'a plus besoin
		if (!request.isKeepAlive()) {
			c.disconnect();
		}
		
		// Et on renvoie la requ�te
		return request;
	}
	
	/**
	 * Executer une requ�te HTTP � partir d'un contexte en asynchrone.
	 * 
	 * @param request
	 * @param context
	 * @param callback
	 */
	public static HttpRequest execute(HttpRequest request, HttpContext context, HttpCallback callback) {
		
		// V�rification arguments
		if (request == null || callback == null) {
			throw new NullPointerException();
		}
		
		// Debug
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, "HTTP " + request.getRequestMethod()
					+ " -> " + request.getRequestURL() + " (asynch=on"
					+ (context != null ? "; context=yes": "") + ")");
		}
		
		// On fabrique un thread d'appel asynchrone
		HttpAsynchThread t = new HttpAsynchThread(request, context, callback);
		
		// On passe le thread � la requete pour permettre l'abort()
		request.setAsynchThread(t);
		
		// On lance le thread
		t.start();
		
		// Et on retourne la requ�te
		return request;
		
		
	}
	
	/**
	 * Executer une requ�te HTTP en synchrone.
	 * 
	 * @param request
	 * @return La requ�te, contenant la r�ponse.
	 */
	public static HttpRequest execute(HttpRequest request) {
		return execute(request, (HttpContext) null);
	}
	
	/**
	 * Executer une requ�te HTTP en asynchrone.
	 * 
	 * @param request
	 * @param callback
	 */
	public static HttpRequest execute(HttpRequest request, HttpCallback callback) {
		return execute(request, (HttpContext) null, callback);
	}
	
	/**
	 * Executer une requ�te HTTP GET en synchrone.
	 * 
	 * @param url
	 * @return La requ�te, contenant la r�ponse.
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url);
		return execute(r);
	}
	
	/**
	 * Executer une requ�te HTTP GET en asynchrone.
	 * 
	 * @param url
	 * @param callback 
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url, HttpCallback callback) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url);
		return execute(r, callback);
	}
	
	/**
	 * Executer une requ�te HTTP GET � partir d'un contexte en synchrone.
	 * 
	 * @param url
	 * @param context
	 * @return La requ�te, contenant la r�ponse.
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url, HttpContext context) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url);
		return execute(r, context);
	}
	
	/**
	 * Executer une requ�te HTTP GET � partir d'un contexte en asynchrone.
	 * 
	 * @param url
	 * @param context
	 * @param callback
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url, HttpContext context, HttpCallback callback) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url);
		return execute(r, context, callback);
	}
	
	/**
	 * Executer une requ�te HTTP GET en synchrone.
	 * 
	 * @param url
	 * @return La requ�te, contenant la r�ponse.
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url, Map<String, String> params) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url, params);
		return execute(r);
	}
	
	/**
	 * Executer une requ�te HTTP GET en asynchrone.
	 * 
	 * @param url
	 * @param callback
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url, Map<String, String> params, HttpCallback callback) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url, params);
		return execute(r, callback);
	}
	
	/**
	 * Executer une requ�te HTTP GET � partir d'un contexte en synchrone.
	 * 
	 * @param url
	 * @param context
	 * @return La requ�te, contenant la r�ponse.
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url, Map<String, String> params, HttpContext context) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url, params);
		return execute(r, context);
	}
	
	/**
	 * Executer une requ�te HTTP GET � partir d'un contexte en asynchrone.
	 * @param url
	 * @param context
	 * @param callback
	 * @throws MalformedURLException 
	 */
	public static HttpRequest get(String url, Map<String, String> params, HttpContext context, HttpCallback callback) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(GET);
		r.setRequestURL(url, params);
		return execute(r, context, callback);
	}

	/**
	 * Executer une requ�te HTTP POST en sychrone.
	 * 
	 * @param url
	 * @param data
	 * @return La requ�te, contenant la r�ponse.
	 * @throws MalformedURLException 
	 */
	public static HttpRequest post(String url, Map<String, Object> data) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(POST);
		r.setRequestURL(url);
		r.setRequestData(data);
		return execute(r);
	}

	/**
	 * Executer une requ�te HTTP POST en asynchrone.
	 * 
	 * @param url
	 * @param callback
	 * @throws MalformedURLException 
	 */	
	public static HttpRequest post(String url, Map<String, Object> data, HttpCallback callback) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(POST);
		r.setRequestURL(url);
		r.setRequestData(data);
		return execute(r, callback);
	}
	
	/**
	 * Executer une requ�te HTTP POST � partir d'un contexte en synchrone.
	 * 
	 * @param url
	 * @param data
	 * @param context
	 * @return La requ�te, contenant la r�ponse.
	 * @throws MalformedURLException 

	 */
	public static HttpRequest post(String url, Map<String, Object> data, HttpContext context) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(POST);
		r.setRequestURL(url);
		r.setRequestData(data);
		return execute(r, context);
	}
	
	/**
	 * Executer une requ�te HTTP POST � partir d'un contexte en asynchrone.
	 * @param url
	 * @param data
	 * @param context
	 * @param callback
	 * @throws MalformedURLException 
	 */
	public static HttpRequest post(String url, Map<String, Object> data, HttpContext context, HttpCallback callback) throws MalformedURLException {
		HttpRequest r = new HttpRequest();
		r.setRequestMethod(POST);
		r.setRequestURL(url);
		r.setRequestData(data);
		return execute(r, context, callback);
	}
	
	/**
	 * Encoder une string pour passer en variable d'URL.
	 * 
	 * @param url
	 * @return La string encod�e
	 */
	@SuppressWarnings("deprecation")
	public static String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(url);
		}
	}

	/**
	 * @deprecated
	 * @param ex
	 * @return
	 */
	public static boolean isHttpException(Exception ex) {
		return ex.getMessage().toLowerCase().contains("http response code:");
	}

	/**
	 * @deprecated
	 * @param ex
	 * @return
	 */
	public static String getErrorMessage(Exception ex) {
		int code = parseErrorCode(ex.getMessage());
		return code + " " + getErrorMessageByCode(code);
	}

	public static String getErrorMessageByCode(int code) {
		switch (code) {
		case 100:	return	"Continue";
		case 101:	return	"Switching Protocols";
		case 102:	return	"Processing";
		case 118:	return	"Connection Timed Out";
		case 200:	return	"OK";
		case 201:	return	"Created";
		case 202:	return 	"Accepted";
		case 203:	return 	"Non-Authoritative Information";
		case 204:	return 	"No Content";
		case 205:	return 	"Reset Content";
		case 206:	return 	"Partial Content";
		case 207:	return 	"Multi-Status";
		case 210:	return 	"Content Different";
		case 226:	return 	"IM Used";
		case 300:	return 	"Multiple Choices";
		case 301:	return 	"Moved Permanently";
		case 302:	return 	"Moved Temporarily";
		case 303:	return 	"See Other";
		case 304:	return 	"Not Modified";
		case 305:	return 	"Use Proxy";
		case 307:	return 	"Temporary Redirect";
		case 310:	return 	"Too Many Redirects";
		case 400:	return 	"Bad Request";
		case 401:	return 	"Unauthorized";
		case 402:	return 	"Payment Required";
		case 403:	return 	"Forbidden";
		case 404:	return 	"Not Found";
		case 405:	return 	"Method Not Allowed";
		case 406:	return 	"Not Acceptable";
		case 407:	return 	"Proxy Authentication Required";
		case 408:	return 	"Request Time-out";
		case 409:	return 	"Conflict";
		case 410:	return 	"Gone";
		case 411:	return 	"Length Required";
		case 412:	return 	"Precondition Failed";
		case 413:	return 	"Request Entity Too Large";
		case 414:	return 	"Request-URI Too Long";
		case 415:	return 	"Unsupported Media Type";
		case 416:	return 	"Requested Range Unsatisfiable";
		case 417:	return 	"Expectation Failed";
		case 418:	return 	"I�m a Teapot";
		case 422:	return 	"Unprocessable Entity";
		case 423:	return 	"Locked";
		case 424:	return 	"Method Failure";
		case 425:	return 	"Unordered Collection";
		case 426:	return 	"Upgrade Required";
		case 449:	return 	"Retry With	Code";
		case 450:	return 	"Blocked by Parental Controls";
		case 456:	return 	"Unrecoverable Error";
		case 499:	return 	"Client Has Closed Connection";
		case 500:	return 	"Internal Server Error";
		case 501:	return 	"Not Implemented";
		case 502:	return 	"Bad Gateway";
		case 503:	return 	"Service Unavailable";
		case 504:	return 	"Gateway Time-out";
		case 505:	return 	"HTTP Version Not Supported";
		case 506:	return 	"Variant Also Negociate";
		case 507:	return 	"Insufficient Storage";
		case 508:	return 	"Loop Detected";
		case 509:	return 	"Bandwidth Limit Exceeded";
		default :
			return "Unknown Error";
		}
	}

	public static int parseErrorCode(String msg) {
		
		//System.out.println("!! " + msg);
		
		// Server returned HTTP response code: 406 for URL:
		
		Pattern pattern = Pattern.compile("(.*?)http response code\\: (.*?) for(.*)");
		
		Matcher matcher = pattern.matcher(msg.toLowerCase());
		
		if (!matcher.matches()) {
			return 0;
		}
		
		return Integer.parseInt(matcher.group(2));
		
	}
	
}
