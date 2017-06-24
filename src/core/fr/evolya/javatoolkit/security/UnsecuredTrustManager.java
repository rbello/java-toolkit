package fr.evolya.javatoolkit.security;


import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import fr.evolya.javatoolkit.code.Logs;

/**
 * Un X509TrustManager qui autorise uniquement certains certificats
 * 
 * Pas utilisé, franchement déconseillé car pas stable du tout
 */
public class UnsecuredTrustManager implements X509TrustManager {
	
	private final X509TrustManager tm;
	private X509Certificate[] acceptedIssuers;

    public UnsecuredTrustManager(X509TrustManager tm, X509Certificate[] acceptedIssuers) {
        this.tm = tm;
        this.acceptedIssuers = acceptedIssuers;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.acceptedIssuers;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    	try {
    		tm.checkClientTrusted(chain, authType);
    	}
    	catch (CertificateException ex) {
    		if (CertificateWarningDialogs.LOGGER.isLoggable(Logs.WARNING)) {
    			CertificateWarningDialogs.LOGGER.log(Logs.WARNING, ex.getClass().getSimpleName(), ex);
    		}
    		throw ex;
    	}
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    	try {
    		tm.checkServerTrusted(chain, authType);
    	}
    	catch (CertificateException ex) {
    		if (CertificateWarningDialogs.LOGGER.isLoggable(Logs.WARNING)) {
    			CertificateWarningDialogs.LOGGER.log(Logs.WARNING, ex.getClass().getSimpleName(), ex);
    		}
    		throw ex;
    	}
    }
    
	/**
	 * Construit un manager de certificats de confiance à partir d'un keystore.
	 * 
	 * Ce manager ne peut pas vraiment être utilis�é il va lancer des exceptions,
	 * mais il sert à récupérer les certificats.
	 * 
	 * @param ks
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public static UnsecuredTrustManager createUnsecuredTrustManager(KeyStore ks, X509Certificate[] acceptedIssuers) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf =
	            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(ks);
	    X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
	    UnsecuredTrustManager tm = new UnsecuredTrustManager(defaultTrustManager, acceptedIssuers);
	    return tm;
	}

}
