package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Field;
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

    private Class<?> type = null;
    
    public Param(Class<?> type) {
    	this.type = type;
    	this.value = null;
    }

    public Param(XmlConfig conf, File src, Element param) throws XmlConfigException {

        String typeName = conf.getAttributeValue(param, "type");
        String fieldName = conf.getAttributeValue(param, "name");
        String valueStr = conf.getTextContent(param);
        
        if (typeName == null) {
        	// Get parent bean type name
        	String beanTypeName = conf.getAttributeValue(param.getParentNode(), "class");
        	if (beanTypeName == null) {
        		String beanName = conf.getAttributeValue(param.getParentNode(), "name");
        		if (beanName != null) {
        			beanTypeName = conf.getBean(beanName).getClass().getName();
        		}
        	}
        	if (beanTypeName == null) {
                throw new XmlConfigException(src, "<param> and <attr> elements should have 'type' attribute");
            }
        	// Get type of data (using setter or attribute to infer it)
        	try {
        		Class<?> type = ReflectionUtils.getFieldTypeByAttributeOrGetter(
        				beanTypeName, fieldName);
        		typeName = type.getName();
        	}
        	catch (ClassNotFoundException e) {
        		throw new XmlConfigException(src, e, "Class not found '%s' for <param name='%s'>",
        				beanTypeName, fieldName);
        	}
        	catch (NoSuchFieldException e) {
        		throw new XmlConfigException(src, "Unable to find accessible field or setter in '%s' for node <param name='%s'>",
        				beanTypeName, fieldName);
			}
        }
        
        // Parse the value
        parseStringValue(conf, src, typeName, valueStr, param);
        
    }
    
    protected Param(XmlConfig conf, File src, org.w3c.dom.Attr node, Object beanInstance)
    		throws XmlConfigException {
    	
		try {
			
			String typeName = ReflectionUtils.getFieldTypeByAttributeOrGetter(
					beanInstance.getClass(), node.getNodeName()).getName();
			
			parseStringValue(conf, src, typeName, node.getNodeValue(), null);
			
		}
		catch (NoSuchFieldException e) {
			throw new XmlConfigException(src, "Unable to find accessible field or setter in '%s' for attribute <bean %s='...'>",
					beanInstance.getClass().getName(), node.getNodeName());
		}
    	
    }
    
    /**
     * @param param Is nullable
     */
    protected void parseStringValue(XmlConfig conf, File src, String typeName, String valueStr, Element param)
    		throws XmlConfigException {
        
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
            type = boolean.class;
            break;
            
        case "byte": case "java.lang.Byte":
            value = new Byte(valueStr);
            type = byte.class;
            break;
            
        case "char": case "java.lang.Character":
            if (valueStr.length() != 1) {
            	throw new XmlConfigException(src, "Invalid %s value \"%s\"", "char", valueStr);
            }
            value = new Character(valueStr.charAt(0));
            type = char.class;
            break;
            
        case "double": case "Double": case "java.lang.Double":
            value = new Double(valueStr);
            type = double.class;
            break;
            
        case "float": case "Float": case "java.lang.Float":
            value = new Float(valueStr);
            type = float.class;
            break;
            
        case "int": case "Integer": case "java.lang.Integer":
            value = new Integer(valueStr);
            type = int.class;
            break;
            
        case "long": case "Long": case "java.lang.Long":
            value = new Long(valueStr);
            type = long.class;
            break;
            
        case "short": case "Short": case "java.lang.Short":
            value = new Short(valueStr);
            type = short.class;
            break;
            
        case "string": case "String": case "java.lang.String":
            value = new String(valueStr);
            type = String.class;
            break;
            
        case "bean":
            List<Node> list = XmlUtils.getChildrenByTagName(param, "bean");
            value = conf.handleBean(src, (Element)list.get(0));
            type = value.getClass();
            break;
        
        default:
        	// Access to static fields and enumerations
        	// Ex: <attr name="color">RED</attr>
        	try {
	        	type = Class.forName(typeName);
	        	value = null;
	        	String valueName = param.getFirstChild().getNodeValue().trim();
	    		for (Field field : type.getDeclaredFields()) {
	    			if (!Modifier.isStatic(field.getModifiers())) continue;
	    			if (!field.getName().equals(valueName)) continue;
	    			value = field.get(null);
	    			type = field.getType();
	    			return;
	    		}
        	}
        	catch (Exception ex) {
        		throw new XmlConfigException(src, ex, "invalid type value '%s'", typeName);
        	}
    		// TODO Préciser que l'on se situe dans un param pour le log de l'exception ?
        	throw new XmlConfigException(src, "invalid type value '%s'", typeName);
        }
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getType() {
        return type;
    }

}
