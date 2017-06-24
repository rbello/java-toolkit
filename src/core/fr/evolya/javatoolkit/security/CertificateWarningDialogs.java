package fr.evolya.javatoolkit.security;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.gui.swing.KeyStoreManagerView;
import fr.evolya.javatoolkit.net.http.CertificateTools;

/**
 * -Djavax.net.ssl.keyStore=./jssecacerts -Djavax.net.ssl.keyStorePassword=changeit
 * 
 * @author life
 *
 */
public class CertificateWarningDialogs {
	
	/**
	 * Les codes de retours possibles de ce processus.
	 */
	public enum ReturnState {
		
		/**
		 * L'opération a été annulée par l'utilisateur.
		 */
		OPERATION_CANCELED,
		
		/**
		 * Le certificat a bien été ajouté dans le keystore, et tout va bien.
		 */
		CERTIFICATE_ADDED,
		
		/**
		 * L'étape de récupération du certificat a foirée.
		 * Une exception est toujours associée à ce type de retour.
		 */
		FAILED_ON_CERTIFICATE,
		
		/**
		 * L'étape d'enregistrement dans le keytore a foirée
		 * Une exception est toujours associée à ce type de retour.
		 */
		FAILED_ON_KEYSTORE,
		
		/**
		 * Le serveur n'a renvoyé aucun certificat au moment de la récupèration,
		 * ce qui est plutôt douteux... mais pas d'exceptions.
		 */
		FAILED_EMPTY_CERTIFICATE,
		
		/**
		 * Si l'utilisateur souhaite souhaite ouvrir le manager de keystore
		 * et le regarder avant de faire le processus
		 */
		OPERATION_PAUSED
	}

	/**
	 * Le logger
	 */
	public static final Logger LOGGER = Logs.getLogger("SSL");
	
	/**
	 * L'exception produite par le processus, en cas d'erreur.
	 */
	protected Exception _ex;
	
	/**
	 * Constructeur
	 * 
	 * @param parent Le composant parent, pour faire des dialogs modaux
	 * @param url L'URL du serveur où obtenir le certificat
	 * @param exMessage Le message de l'exception SSL qui a du être levé avant
	 * @param certAlias L'alias du certificat dans le keystore (en cas de choix de l'utilisateur)
	 * @param chainAlias L'alias de la chain dans la certificat (en cas de choix de l'utilisateur)
	 * @param keystoreFile Chemin vers le keystore
	 * @param keystorePassword Password à utiliser pour le keystore
	 */
	public ReturnState showDialogs(Component parent, URL url, String exMessage, String certAlias, String chainAlias, File keystoreFile, String keystorePassword) {
		
		// On ouvre un popup modal, pour demander � l'utilisateur
		// s'il souhaite ajouter les certificats du serveur dans
		// la liste de confiance, ou non, ou ouvrir la config
		Object[] options = {
			"Trust this host",
            "Discard",
            "Open keystore manager"
        };
		int choice = JOptionPane.showOptionDialog(
			parent,
			"The server " + url + " has the"
			+ " following security issue:\n" + exMessage
			+ "\nDo you want to trust this host?",
			"SSL Error",
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.ERROR_MESSAGE,
			null,
			options,
			options[1]
		);
		
		// Si l'utilisateur a d�clin�, on renvoie le code appropri�
		if (choice == 1) {
			return ReturnState.OPERATION_CANCELED;
		}
		
		// Affichage du 
		if (choice == 2) {
			new KeyStoreManagerView(keystoreFile).setVisible(true);
			return ReturnState.OPERATION_PAUSED;
		}
		
		// La future liste des certificats
		X509Certificate[] cert = null;
		
		try {
			
			// On liste les certificats
			cert = CertificateTools.getCertificates(url.getHost());
			
			// Erreur
			if (cert == null) {
				throw new NullPointerException("Returned certificat list is null");
			}
			
		}
		catch (Exception ex1) {
			
			// Log
			if (LOGGER.isLoggable(Logs.DEBUG)) {
				LOGGER.log(Logs.DEBUG, ex1.getClass().getSimpleName(), ex1);
			}
			
			// Debug
			ex1.printStackTrace();
			
			// On enregistre l'exception
			_ex = ex1;
			
			// Et on renvoie un code d'erreur
			return ReturnState.FAILED_ON_CERTIFICATE;
			
		}
		
		// Log
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, "Host: " + url.getHost() + "  -  Certificates: " + cert.length);
			int i = 1;
			for (X509Certificate c : cert) {
				LOGGER.log(Logs.DEBUG, i++ + ") Subject: " + c.getSubjectDN());
				LOGGER.log(Logs.DEBUG, "   Issuer: " + c.getIssuerDN());
			}
		}
		
		// Erreur interne : no certificates
		// Donc ambiguité avec le fait qu'on vient de dire à l'user
		// qu'il était bloqué par un certificat
		if (cert.length == 0) {
			return ReturnState.FAILED_EMPTY_CERTIFICATE;
		}

		// On ouvre un popup modal, pour donner à l'utilisateur des infos sur
		// le certificat, et lui demander s'il il l'accepte
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder sb = new StringBuilder();
		sb.append("The following certificate will be added:");
		sb.append("\nSubject: ");
		sb.append(cert[0].getSubjectDN());
		sb.append("\nSigned by: ");
		sb.append(cert[0].getIssuerDN());
		sb.append("\nValid: ");
		sb.append(dateFormat.format(cert[0].getNotBefore()));
		sb.append(" to ");
		sb.append(dateFormat.format(cert[0].getNotAfter()));
		try {
			cert[0].checkValidity();
			sb.append(" (valid)");
		} catch (CertificateExpiredException e) {
			sb.append(" (expired)");
		} catch (CertificateNotYetValidException e) {
			sb.append(" (not yet valid)");
		}
		sb.append("\nSaved to: " + keystoreFile.getAbsolutePath());
		if (cert.length > 1) {
			sb.append("\n\nSigned by:");
			for (int i = 1; i < cert.length; i++) {
				sb.append("\n");
				sb.append(i);
				sb.append(") " + cert[i].getIssuerDN());
			}
		}
		sb.append("\n\nDo you want to trust this certifiate?");
		Object[] options1 = {
			"Yes",
            "No"
        };
		int choice1 = JOptionPane.showOptionDialog(
			parent,
			sb.toString(),
			"Confirm certificate trust access",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,
			options1,
			options1[0]
		);
		
		// Si l'utilisateur a décliné, on renvoie le code approprié
		if (choice1 == 1) {
			return ReturnState.OPERATION_CANCELED;
		}
		
		try {
			
			// Log
			if (LOGGER.isLoggable(Logs.DEBUG)) {
				LOGGER.log(Logs.DEBUG, "Saving certificate '" + cert[0].getSubjectDN()
						+ "' to '" + keystoreFile.getAbsolutePath() + "' with password '"
						+ (keystorePassword != null ? "YES" : "NO"));
			}
			
			// On tente d'ouvrir le keystore
			KeyStore keystore = CertificateTools.createKeystoreInstance(keystoreFile, keystorePassword);
			
			// On enregistre le certificat du host
			// Note : tout seul ça ne suffit pas à faire fonctionner le système
			CertificateTools.addTrustedCertificate(certAlias, cert[0], keystore, keystorePassword, keystoreFile);
			
			// Et la keychain
			//CertificateTools.addTrustedCertificatesChain(chainAlias, cert, keystore, password, file);
			
			// Et on renvoie un code positif
			return ReturnState.CERTIFICATE_ADDED;
			
		}
		catch (Exception ex2) {
			
			// Log
			if (LOGGER.isLoggable(Logs.DEBUG)) {
				LOGGER.log(Logs.DEBUG, ex2.getClass().getSimpleName(), ex2);
			}
			
			// On enregistre l'exception
			_ex = ex2;
			
			// Et on renvoie un code d'erreur
			return ReturnState.FAILED_ON_KEYSTORE;
			
		}
		
	}

	/**
	 * Renvoie l'exception produite lors du processus en cas d'erreur
	 */
	public Exception getException() {
		return _ex;
	}

}
