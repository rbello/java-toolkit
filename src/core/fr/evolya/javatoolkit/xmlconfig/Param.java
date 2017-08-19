package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

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
 * @version 2.0
 */
class Param {

    private Object value = null;

    private Class<?> clazz = null;
    
    public Param(Class<?> type) {
    	this.clazz = type;
    	this.value = null;
    }

    public Param(XmlConfig conf, File src, Node param) throws XmlConfigException {

        String typeName = conf.getAttributeValue(param, "type");
        String valueStr = conf.getTextContent(param);
        String fieldName = conf.getAttributeValue(param, "name");
        
        if (typeName == null) {
        	// Infer type from parent
        	String beanTypeName = conf.getAttributeValue(param.getParentNode(), "class");
        	if (beanTypeName == null) {
        		String beanName = conf.getAttributeValue(param.getParentNode(), "name");
        		if (beanName != null) {
        			beanTypeName = conf.getBean(beanName).getClass().getName();
        		}
        	}
        	if (beanTypeName == null) {
                throw new XmlConfigException(src, "param and attr elements should have 'type' attribute");
            }
        	// Get param type from bean's attribute
        	try {
        		Field field = ReflectionUtils.getFieldMatching(Class.forName(beanTypeName), fieldName);
	        	typeName = field.getType().getName();
        	}
        	catch (ClassNotFoundException e) {
        		throw new XmlConfigException(src, "param and attr elements should have 'type' attribute");
        	}
        	catch (NoSuchFieldException ex) {
        		
        	}
        	// Get param type from bean' setter method
        	if (typeName == null) {
        		Method m = ReflectionUtils.getMethodMatchingIgnoreCase(Class.forName(beanTypeName), 
        				"set" + fieldName);
        		if (m != null) {
        			typeName = m.getParameterTypes()[0].getName();
        		}
        	}
        	if (typeName == null) {
        		throw new XmlConfigException(src, "Unable to infer type of field " + beanTypeName 
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
                throw new XmlConfigException(src, "Invalid %s value \"%s\"", "boolean", valueStr);
            }
            clazz = boolean.class;
            break;
            
        case "byte": case "java.lang.Byte":
            value = new Byte(valueStr);
            clazz = byte.class;
            break;
            
        case "char": case "java.lang.Character":
            if (valueStr.length() != 1) {
            	throw new XmlConfigException(src, "Invalid %s value \"%s\"", "char", valueStr);
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
            value = conf.handleBean(src, (Element)list.get(0), null);
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
    		// TODO Préciser que l'on se situe dans un param pour le log de l'exception ?
            throw new XmlConfigException(src, "invalid type value '%s'", typeName);
        }
    }

//    public void setValue(Object value) {
//        this.value = value;
//    }

    public Object getValue() {
        return value;
    }

    // TODO renommer en getType
    public Class<?> getClazz() {
        return clazz;
    }

}
