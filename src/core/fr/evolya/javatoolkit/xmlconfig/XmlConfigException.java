package fr.evolya.javatoolkit.xmlconfig;

/**
 * This exception is thrown if the configuration fails because the XML
 * document format is incorrect.
 *
 * @author Antti S. Brax
 * @author R. Bello
 * 
 * @version 1.1
 */
public class XmlConfigException extends Exception {

	private static final long serialVersionUID = 1095874317920314252L;

	public XmlConfigException() {
		super();
	}
	
    public XmlConfigException(String msg) {
        super(msg);
    }

	public XmlConfigException(Throwable cause) {
		super(cause);
	}
	
	public XmlConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
