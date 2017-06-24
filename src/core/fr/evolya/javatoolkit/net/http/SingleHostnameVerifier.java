package fr.evolya.javatoolkit.net.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class SingleHostnameVerifier implements HostnameVerifier {
	
    protected String hostname;

	public SingleHostnameVerifier(String hostname) {
		this.hostname = hostname;
	}

	public boolean verify(String hostname, SSLSession session)
    {
    	// On ne valide que les l'hôte donné
    	if (hostname.equals(this.hostname))
            return true;

        return false;
    }

}
