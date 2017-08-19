package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;

/**
 * This exception is thrown if the configuration fails because the XML
 * document format is incorrect.
 *
 * @author Antti S. Brax
 * @author R. Bello
 * 
 * @version 2.0
 */
public class XmlConfigException extends Exception {

	private static final long serialVersionUID = 1095874317920314252L;

	public XmlConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }

	public XmlConfigException(File src, String msg) {
		super(msg + "\nFile: " + src.getAbsolutePath());
	}
	
	public XmlConfigException(File src, String msg, Object... args) {
		this(src, String.format(msg, args));
	}
	
	public XmlConfigException(File src, Throwable cause, String msg) {
		super(msg + "\nFile: " + src.getAbsolutePath(), cause);
	}
	
	public XmlConfigException(File src, Throwable cause, String msg, Object... args) {
		this(src, cause, String.format(msg, args));
	}

}
