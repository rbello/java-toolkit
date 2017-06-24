package fr.evolya.javatoolkit.xmlconfig;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * An object representing an attribute.
 *
 * @author Antti S. Brax
 * @author R. Bello
 * 
 * @version 1.1
 */
class Attr extends Param {

    private String name = null;

    /**
     * @param param an attr or param element.
     */
    public Attr(XmlConfig conf, Node attr, Map<String, String> mapProperties,
    		Map<String, Object> mapBeans) 
        throws Exception, XmlConfigException, ClassNotFoundException {

        super(conf, attr, mapProperties, mapBeans);

        name = conf.getAttributeValue(attr, "name", mapProperties);
        if (name == null) {
            throw new XmlConfigException("attr element must have a name "
                                         + "attribute");
        }

    }

    /**
     * Set the value of name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of name.
     */
    public String getName() {
        return name;
    }

}
