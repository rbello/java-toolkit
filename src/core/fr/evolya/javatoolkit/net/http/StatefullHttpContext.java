package fr.evolya.javatoolkit.net.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;

public class StatefullHttpContext implements HttpContext {
	
	protected static final String SET_COOKIE = "Set-Cookie";
    protected static final String COOKIE_VALUE_DELIMITER = ";";
    protected static final String PATH = "path";
    protected static final String EXPIRES = "expires";
    protected static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
    protected static final String SET_COOKIE_SEPARATOR="; ";
    protected static final String COOKIE = "Cookie";
    protected static final char NAME_VALUE_SEPARATOR = '=';
    protected static final char DOT = '.';

	protected Map<String, String> properties = new HashMap<String, String>();

	// Map<String domain, Map<String cookieName, Map<String propertyName, String propertyValue>>>
	protected Map<String, Map<String, Map<String, String>>> cookies = new HashMap<String, Map<String, Map<String, String>>>();
	
	protected HostnameVerifier hostnameVerifier;
	
	protected static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
	
	@Override
	public Map<String, String> getRequestProperties(URL url) {
		
		// On fait une copie des properties
		Map<String, String> p = new HashMap<String, String>(properties);
		
		// Ecriture des cookies
		if (cookies.size() > 0) {
			
			// Le domaine et le chemin de l'URL de la requête
			String domain = getDomainFromHost(url.getHost());
			String path = url.getPath();
			
			// Aucun cookie pour ce domaine
			if (!cookies.containsKey(domain)) {
				return p;
			}
			
			// On recupère les cookies et un itérateur
			Map<String, Map<String, String>> cookies = this.cookies.get(domain);
			Iterator<String> cookieNames = cookies.keySet().iterator();
			
			// Cet objet permet de construire la string de header
			StringBuffer sb = new StringBuffer();
			
			// Parcours des cookies
			while (cookieNames.hasNext()) {
				
				// Current cookie
				String cookieName = cookieNames.next();
				Map<String, String> cookie = cookies.get(cookieName);
			    
				// Check to ensure path matches
				if (!comparePaths((String) cookie.get(PATH), path)) {
					continue;
				}
				
				// Check to ensure cookie is not expired
				try {
					if (!isNotExpired((String) cookie.get(EXPIRES))) {
						continue;
					}
				}
				catch (Exception ex) {
					continue;
				}
				
				// Cookie is valid, add to header string  
				sb.append(cookieName);
				sb.append("=");
				sb.append((String) cookie.get("value"));
				
				// Add cookie properties
				for (String key : cookie.keySet()) {
					if (key.equals("name") || key.equals("value")) continue;
					sb.append(COOKIE_VALUE_DELIMITER);
					sb.append(key);
					sb.append(NAME_VALUE_SEPARATOR);
					sb.append(cookie.get(key));
				}
				
				// Separator
				if (cookieNames.hasNext()) {
					sb.append(SET_COOKIE_SEPARATOR);
				}
			}
			// Save cookie header
			//System.out.println(sb.toString());
			p.put(COOKIE, sb.toString());
		}
		
		// On renvoie la liste des propriétés
		return p;
	}

    /**
     * Renvoie la liste des cookies.
     */
	public Map<String, Map<String, Map<String, String>>> getCookies() {
		return cookies;
	}
    
	@Override
	public void update(HttpRequest r, HttpURLConnection c) {
		
		// Let's determine the domain from where these cookies are being sent
		String domain = getDomainFromHost(c.getURL().getHost());
		
		// This is where we will store cookies for this domain
		// Map<String cookieName, Map<String propertyName, String propertyValue>>
		Map<String, Map<String, String>> domainStore;
		
		// Now let's check the store to see if we have an entry for this domain
		if (cookies.containsKey(domain)) {
		    // We do, so lets retrieve it from the store
		    domainStore = (Map<String, Map<String, String>>) cookies.get(domain);
		} else {
		    // We don't, so let's create it and put it in the store
		    domainStore = new HashMap<String, Map<String, String>>();
		    cookies.put(domain, domainStore);
		}
		
		// OK, now we are ready to get the cookies out of the request object
		for (String headerName : r.getReturnHeaders().keySet()) {
			
			// Pas un cookie
			if (!headerName.equalsIgnoreCase(SET_COOKIE)) {
				continue;
			}
			
			// Tokenizer
			StringTokenizer st = new StringTokenizer(
				r.getReturnHeaders().get(headerName),
				COOKIE_VALUE_DELIMITER
			);
			
			// Cookie properties
			Map<String, String> cookie = new HashMap<String, String>();
			
			// The specification dictates that the first name/value pair
			// in the string is the cookie name and value, so let's handle
			// them as a special case: 
			
			if (st.hasMoreTokens()) {
			    String token  = st.nextToken();
			    String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
			    String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
			    domainStore.put(name, cookie);
			    cookie.put("name", name.trim());
			    cookie.put("value", value);
			}
	    
			while (st.hasMoreTokens()) {
			    String token = st.nextToken();
			    String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase();
			    String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
			    cookie.put(name.trim().toLowerCase(), value);
			}
			
		}
		
	}
	
	public void debug() {
		System.out.println("--- " + this + " ---");
		System.out.println("Properties: " + properties.size());
		for (String key : properties.keySet()) {
			System.out.println("  " + key + " = " + properties.get(key));
		}
		System.out.println("Cookies: " + cookies.size());
		for (String domain : cookies.keySet()) {
			for (String cookieName : cookies.get(domain).keySet()) {
				Map<String, String> cookie = cookies.get(domain).get(cookieName);
				System.out.println("  Domain: " + domain);
				for (String key : cookie.keySet()) {
					System.out.println("   - " + key + " = " + cookie.get(key));
				}
			}
		}
	}
	
	/**
	 * Déterminer le domaine à partir du hostname.
	 */
    protected static String getDomainFromHost(String host) {
    	if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
    	    return host.substring(host.indexOf(DOT) + 1);
    	} else {
    	    return host;
    	}
    }
    
    /**
     * Déterminer si un cookie est expiré.
     */
    protected static boolean isNotExpired(String cookieExpires) {
    	if (cookieExpires == null) return true;
    	Date now = new Date();
    	try {
    	    return (now.compareTo(dateFormat.parse(cookieExpires))) <= 0;
    	} catch (ParseException pe) {
    	    System.err.println(pe.getMessage() + " (" + pe.getStackTrace()[0] +")");
    	    return false;
    	}
    }

    /**
     * Comparer deux chemins.
     */
    protected static boolean comparePaths(String cookiePath, String targetPath) {
    	if (cookiePath == null) {
    	    return true;
    	}
    	else if (cookiePath.equals("/")) {
    	    return true;
    	}
    	else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) {
    	    return true;
    	}
    	return false;
    }

	@Override
	public HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

	@Override
	public void setHostnameVerifier(HostnameVerifier verifier) {
		hostnameVerifier = verifier;
	}

	@Override
	public void dispose() {
		hostnameVerifier = null;
		cookies.clear();
		cookies = null;
		properties = null;
	}

	@Override
	public Map<String, Map<String, String>> getCookies(String host) {
		host = getDomainFromHost(host);
		if (!cookies.containsKey(host)) {
			return new HashMap<String, Map<String, String>>();
		}
		return cookies.get(host);
	}

}
