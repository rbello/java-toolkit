package fr.evolya.javatoolkit.xmlconfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fr.evolya.javatoolkit.code.utils.ReflectionUtils;
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
        String fieldName = conf.getAttributeValue(param, "name", mapProperties);
        String valueStr = conf.getTextContent(param, mapProperties);
        
        if (typeName == null) {
        	// Infer type from parent
        	String beanTypeName = conf.getAttributeValue(param.getParentNode(), "class", mapProperties);
        	if (beanTypeName == null) {
        		String beanName = conf.getAttributeValue(param.getParentNode(), "name", mapProperties);
        		if (beanName != null) {
        			beanTypeName = mapBeans.get(beanName).getClass().getName();
        		}
        	}
        	if (beanTypeName == null) {
                throw new XmlConfigException("param and attr elements should have 'type' attribute");
            }
        	// Get param type from bean's attribute
        	try {
        		Field field = ReflectionUtils.getFieldMatching(Class.forName(beanTypeName), fieldName);
	        	typeName = field.getType().getName();
        	}
        	catch (NoSuchFieldException ex) { }
        	// Get param type from bean' setter method
        	if (typeName == null) {
        		Method m = ReflectionUtils.getMethodMatchingIgnoreCase(Class.forName(beanTypeName), 
        				"set" + fieldName);
        		if (m != null) {
        			typeName = m.getParameterTypes()[0].getName();
        		}
        	}
        	if (typeName == null) {
        		throw new XmlConfigException("Unable to infer type of field " + beanTypeName 
        				+ "." + fieldName);
        	}
        }
        
        switch (typeName) {
        
        case "bool": case "boolean": case "java.lang.Boolean":
            if (valueStr.equalsIgnoreCase("true") || valueStr.equals("1")) {
                value = new Boolean(true);
            }
            else if (valueStr.equalsIgnoreCase("false") || valueStr.equals("0")) {
                value = new Boolean(false);
            } 
            else {
                throw new XmlConfigException(String.format("invalid %s value \"%s\"", "boolean", valueStr));
            }
            clazz = boolean.class;
            break;
            
        case "byte": case "java.lang.Byte":
            value = new Byte(valueStr);
            clazz = byte.class;
            break;
            
        case "char": case "java.lang.Character":
            if (valueStr.length() != 1) {
            	throw new XmlConfigException(String.format("invalid %s value \"%s\"", "char", valueStr));
            }
            value = new Character(valueStr.charAt(0));
            clazz = char.class;
            break;
            
        case "double": case "Double": case "java.lang.Double":
            value = new Double(valueStr);
            clazz = double.class;
            break;
            
        case "float": case "Float": case "java.lang.Float":
            value = new Float(valueStr);
            clazz = float.class;
            break;
            
        case "int": case "Integer": case "java.lang.Integer":
            value = new Integer(valueStr);
            clazz = int.class;
            break;
            
        case "long": case "Long": case "java.lang.Long":
            value = new Long(valueStr);
            clazz = long.class;
            break;
            
        case "short": case "Short": case "java.lang.Short":
            value = new Short(valueStr);
            clazz = short.class;
            break;
            
        case "string": case "String": case "java.lang.String":
            value = new String(valueStr);
            clazz = String.class;
            break;
            
        case "bean":
            Element elem = (Element)param;
            List<Node> list = XmlUtils.getChildrenByTagName(elem, "bean");
            value = XmlConfig.handleBean(conf, (Element)list.get(0), null, mapProperties, mapBeans);
            clazz = value.getClass();
            break;
        
        default:
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
