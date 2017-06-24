package fr.evolya.javatoolkit.net.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Code by Andreas Sterbenz
 *  at https://code.google.com/p/java-use-examples/source/browse/trunk/src/com/aw/ad/util/InstallCert.java
 */
public class CertificateTools {
	
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    /**
     * Permet de debugger 
     * @param chain
     * @throws NoSuchAlgorithmException
     * @throws CertificateEncodingException
     */
	public static void debugCertificateChain(X509Certificate[] chain) {
        try {
			System.out.println("Server sent " + chain.length + " certificate(s):");
	        System.out.println();
	        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
	        MessageDigest md5 = MessageDigest.getInstance("MD5");
	        for (int i = 0; i < chain.length; i++) {
	            X509Certificate cert = chain[i];
	            System.out.println
	                    (" " + (i + 1) + " Subject " + cert.getSubjectDN());
	            System.out.println("   Issuer  " + cert.getIssuerDN());
	            sha1.update(cert.getEncoded());
	            System.out.println("   sha1    " + toHexString(sha1.digest()));
	            md5.update(cert.getEncoded());
	            System.out.println("   md5     " + toHexString(md5.digest()));
	            System.out.println();
	        }
        }
        catch (Exception ex) {
        	ex.printStackTrace(System.out);
        }
	}
	
	/**
	 * Ajouter un certificat dans un keystore. 
	 * 
	 * @param certAlias
	 * @param cert
	 * @param keystore
	 * @param file 
	 * @param password
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static void addTrustedCertificate(String certAlias, X509Certificate cert, KeyStore keystore, String keystorePassword, File file) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        keystore.setCertificateEntry(certAlias, cert);
        OutputStream out = new FileOutputStream(file);
        keystore.store(out, keystorePassword.toCharArray());
        out.close();
	}
	
	public static void addTrustedCertificatesChain(String chainAlias, X509Certificate[] cert, KeyStore keystore, String keystorePassword, File file) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();        
        addTrustedCertificatesChain(chainAlias, cert, keystore, keystorePassword, file, privateKey);
	}
	
	public static void addTrustedCertificatesChain(String chainAlias, X509Certificate[] cert, KeyStore keystore, String keystorePassword, File file, PrivateKey privateKey) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        keystore.setKeyEntry(chainAlias, privateKey, keystorePassword.toCharArray(), cert);
        OutputStream out = new FileOutputStream(file);
        keystore.store(out, keystorePassword.toCharArray());
        out.close();
	}
	
	/**
	 * Renvoie la liste des certificats diffusés par un serveur.
	 * 
	 * @param host
	 * @return
	 * @throws Exception
	 */
	public static X509Certificate[] getCertificates(String host) throws Exception {
		return getCertificates(host, 443, "changeit");
	}

	/**
	 * Renvoie la liste des certificats diffusés par un serveur.
	 * 
	 * TODO Faire une gestion plus fine des exceptions
	 * 
	 * @param host
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public static X509Certificate[] getCertificates(String host, int port, String keystorePassword) throws Exception {
		
		File keystoreFile = getKeystoreFile();
		KeyStore ks = createKeystoreInstance(keystoreFile, keystorePassword);
		SavingTrustManager tm = createSavingTrustManager(ks);
		SSLContext context = createSSLContext(tm);
		SSLSocketFactory factory = context.getSocketFactory();
		
        System.out.println("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        
        try {
        	
        	/**
        	* Starts an SSL handshake on this connection. Common reasons include a
        	* need to use new encryption keys, to change cipher suites, or to
        	* initiate a new session. To force complete reauthentication, the
        	* current session could be invalidated before starting this handshake.
        	* If data has already been sent on the connection, it continues to flow
        	* during this handshake. When the handshake completes, this will be
        	* signaled with an event. This method is synchronous for the initial
        	* handshake on a connection and returns when the negotiated handshake
        	* is complete. Some protocols may not support multiple handshakes on an
        	* existing socket and may throw an IOException.
        	*/
        	
            System.out.println("Starting SSL handshake...");
            socket.startHandshake();
            
            /**
            * Retrieve the server's certificate chain
            *
            * Returns the identity of the peer which was established as part of
            * defining the session. Note: This method can be used only when using
            * certificate-based cipher suites; using it with non-certificate-based
            * cipher suites, such as Kerberos, will throw an
            * SSLPeerUnverifiedException.
            *
            *
            * Returns: an ordered array of peer certificates, with the peer's own
            * certificate first followed by any certificate authorities.
            */
            // TODO il semble qu'il soit possible de les récupérer sans passer par le SavingTrustManager
            // Certificate[] serverCerts = socket.getSession().getPeerCertificates();
            
            socket.close();
            System.out.println();
            System.out.println("No errors, certificate is already trusted");
        } catch (SSLException ex) {
            System.out.println();
            ex.printStackTrace(System.out);
        }
        
        return tm.getChain();
		
	}
	
	/**
	 * Construit un contexte SSL et l'associe à un TrustManager.
	 * Le keystore sert à la construction du TrustManager.
	 * 
	 * @param ks
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static SSLContext createSSLContext(TrustManager tm) throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[]{ tm }, null);
		return context;
	}
	
	/**
	 * Construit un manager de certificats de confiance à partir d'un keystore.
	 * 
	 * Ce manager ne peut pas vraiment être utilisé, il va lancer des exceptions,
	 * mais il sert à récupérer les certificats.
	 * 
	 * @param ks
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public static SavingTrustManager createSavingTrustManager(KeyStore ks) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf =
	            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(ks);
	    X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
	    SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
	    return tm;
	}
	
	/**
	 * Recupère l'instance d'un fichier keystore.
	 * 
	 * @param keystoreFile
	 * @param password
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static KeyStore createKeystoreInstance(File keystoreFile, String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        InputStream in = new FileInputStream(keystoreFile);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, password.toCharArray());
        in.close();
        return ks;
	}
	
	/*public static Map<String, X509Certificate> getCertificates(File keystoreFile, String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore ks = createKeystoreInstance(keystoreFile, password);
		Map<String, X509Certificate> map = new HashMap<String, X509Certificate>();
		Enumeration<String> aliases = ks.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			//map.put(alias, ks.get)
		}
	}*/
	
	public void saveCERFile(File cerFile, KeyStore store, String certAlias) throws CertificateException, KeyStoreException, IOException {
		
		InputStream fis = new FileInputStream(cerFile);
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0) {
		    Certificate cert = cf.generateCertificate(bis);
		    store.setCertificateEntry(certAlias + bis.available(), cert);
		}
		
	}
	
	/**
	 * Renvoie un pointeur de fichier vers le keystore
	 */
	public static File getKeystoreFile() {
        final char SEP = File.separatorChar;
        
        final File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
        File file = new File(dir, "jssecacerts");
        
        if (file.isFile() == false) {
            file = new File(dir, "cacerts");
        }
        
        if (file.isFile() == false) {
            file = new File("jssecacerts");
        }
        
        return file;
	}
	
    /**
     * Conversion d'hexa vers string.
     * 
     * @param bytes
     * @return
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }
    
    
    /**
     * TODO
     * 
     * 
     * 
     *             // This class retrieves the most-trusted CAs from the keystore
            PKIXParameters params = new PKIXParameters(keystore);

            // Get the set of trust anchors, which contain the most-trusted CA certificates
            Iterator it = params.getTrustAnchors().iterator();
            while( it.hasNext() ) {
                TrustAnchor ta = (TrustAnchor)it.next();
                // Get certificate
                X509Certificate cert = ta.getTrustedCert();
                System.out.println(cert);
            }
            
            
            
            
            
            ---------------------------------------
            
            
            
            
            
            
            @Test
public void testKeyStore() throws Exception{
        try {
        String storeName =  "/home/grigory/outstore.pkcs12";
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        Certificate trustCert =  createCertificate("CN=CA", "CN=CA", publicKey, privateKey);
        Certificate[] outChain = { createCertificate("CN=Client", "CN=CA", publicKey, privateKey), trustCert };

        KeyStore outStore = KeyStore.getInstance("PKCS12");
        outStore.load(null, "secret".toCharArray());
        outStore.setKeyEntry("mykey", privateKey, "secret".toCharArray(), outChain);
        OutputStream outputStream = new FileOutputStream(storeName);
        outStore.store(outputStream, "secret".toCharArray());
        outputStream.flush();
        outputStream.close();

        KeyStore inStore = KeyStore.getInstance("PKCS12");
        inStore.load(new FileInputStream(storeName), "secret".toCharArray());
        Key key = outStore.getKey("myKey", "secret".toCharArray());
        Assert.assertEquals(privateKey, key);

        Certificate[] inChain = outStore.getCertificateChain("mykey");
        Assert.assertNotNull(inChain);
        Assert.assertEquals(outChain.length, inChain.length);
    } catch (Exception e) {
        e.printStackTrace();
        throw new AssertionError(e.getMessage());
    }
            
            
            
            
            ----------------------------------
            
            TODO
            
            
                        Properties sysProperties = System.getProperties();
	   // change proxy settings if required and enable the below lines
           // sysProperties.put("proxyHost", "proxy.starhub.net.sg");
           // sysProperties.put("proxyPort", "8080");
           // sysProperties.put("proxySet",  "true");
            
            
            ----------------------------------------------
            
            
            
            
		// TODO
		/*if (getKeystoreFile().isFile()) {
			try {
				
				// On recupère le keystore de l'application
				KeyStore ks = CertificateTools.createKeystoreInstance(
					getKeystoreFile().getAbsoluteFile(),
					getKeystorePassword()
				);
				
				// TODO MEGA MEGA MOCHE !!!
				// GROSSE FAILLE DE SECU !! A CORRIGER AU PLUS VITE !
				
				// Si le keystore contient la bonne clé
				if (ks.containsAlias("workshop.evolya.fr")) {
					
					// On fabrique un trust manager
					UnsecuredTrustManager tm = CertificateTools.createUnsecuredTrustManager(
							ks, new X509Certificate[] {
									(X509Certificate) ks.getCertificate("workshop.evolya.fr")
							}
					);
					
					// On fabrique un context SSL à partir du trust manager
					SSLContext ctx = CertificateTools.createSSLContext(tm);

					// On modifie la factory par défaut des SSLSocket
					HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
					
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// TODO Vérifier (ça ne semble pas vraiment marcher)
	    //System.setProperty("javax.net.ssl.keyStore", getKeystoreFile().getAbsolutePath());
	    //System.setProperty("javax.net.ssl.keyStorePassword", getKeystorePassword());
	    
    
    
            */
     
	
}
