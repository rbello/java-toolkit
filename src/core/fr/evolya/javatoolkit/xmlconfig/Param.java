package fr.evolya.javatoolkit.xmlconfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fr.evolya.javatoolkit.code.utils.XmlUtils;

/**
 * An object representing a parameter.
 *
 * @author Antti S. Brax
 * @author R. Bello
 * 
 * @version 1.1
 */
class Param {

    private Object value = null;

    private Class<?> clazz = null;

    // ==================================================================== //

    /**
     * @param param an attr or param element.
     */
    public Param(XmlConfig conf, Node param, Map<String, String> mapProperties,
    		Map<String, Object> mapBeans) throws Exception, XmlConfigException, ClassNotFoundException {

        String typeName = conf.getAttributeValue(param, "type", mapProperties);
        if (typeName == null) {
            throw new XmlConfigException("param and attr elements must have "
                                         + " type attributes");
        }
        String className = conf.getAttributeValue(param, "classcast", mapProperties);

        if (typeName.equals("boolean")) {
            String tmp = conf.getTextContent(param, mapProperties);
            if (tmp.equalsIgnoreCase("true")) {
                value = new Boolean(true);
            } else if (tmp.equalsIgnoreCase("false")) {
                value = new Boolean(false);
            } else {
                String msg = "invalid boolean value \"" + tmp + "\"";
                throw new XmlConfigException(msg);
            }
            clazz = (className == null) ? boolean.class
                : Class.forName(className);
        } else if (typeName.equals("byte")) {
            value = new Byte(conf.getTextContent(param, mapProperties));
            clazz = (className == null) ? byte.class
                : Class.forName(className);
        } else if (typeName.equals("char")) {
            String tmp = conf.getTextContent(param, mapProperties);
            if (tmp.length() != 1) {
                String msg = "invalid char value \"" + tmp + "\"";
                throw new XmlConfigException(msg);
            }
            value = new Character(tmp.charAt(0));
            clazz = (className == null) ? char.class
                : Class.forName(className);
        } else if (typeName.equals("double") || typeName.equals("Double")) {
            value = new Double(conf.getTextContent(param, mapProperties));
            clazz = (className == null) ? double.class
                : Class.forName(className);
        } else if (typeName.equals("float") || typeName.equals("Float")) {
            value = new Float(conf.getTextContent(param, mapProperties));
            clazz = (className == null) ? float.class
                : Class.forName(className);
        } else if (typeName.equals("int") || typeName.equals("Integer")) {
            value = new Integer(conf.getTextContent(param, mapProperties));
            clazz = (className == null) ? int.class
                : Class.forName(className);
        } else if (typeName.equals("long") || typeName.equals("Long")) {
            value = new Long(conf.getTextContent(param, mapProperties));
            clazz = (className == null) ? long.class
                : Class.forName(className);
        } else if (typeName.equals("short") || typeName.equals("Short")) {
            value = new Short(conf.getTextContent(param, mapProperties));
            clazz = (className == null) ? short.class
                : Class.forName(className);
        } else if (typeName.equals("string") || typeName.equals("String")) {
            value = conf.getTextContent(param, mapProperties);
            clazz = (className == null) ? String.class
                : Class.forName(className);
        } else if (typeName.equals("bean")) {
            Element elem = (Element)param;
            List<Node> list = XmlUtils.getChildrenByTagName(elem, "bean");
            value = XmlConfig.handleBean(conf, (Element)list.get(0), null, mapProperties, mapBeans);
            clazz = (className == null) ? value.getClass()
                : Class.forName(className);
        } 
        else {
        	clazz = Class.forName(typeName);
        	value = null;
        	String valueName = param.getFirstChild().getNodeValue().trim();
    		for (Field field : clazz.getDeclaredFields()) {
    			if (!Modifier.isStatic(field.getModifiers())) continue;
    			if (!field.getName().equals(valueName)) continue;
    			value = field.get(null);
    			clazz = field.getType();
    			return;
    		}
			String msg = "invalid type value \"" + typeName + "\"";
            throw new XmlConfigException(msg);
        }
    }

    // ==================================================================== //

    /**
     * Set the value of value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Get the value of value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the value of class.
     */
    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Get the value of class.
     */
    public Class<?> getClazz() {
        return clazz;
    }

}
