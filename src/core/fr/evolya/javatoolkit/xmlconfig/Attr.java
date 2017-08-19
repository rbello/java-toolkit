package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Method;

import org.w3c.dom.Node;

import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

/**
 * An object representing an attribute.
 *
 * @author Antti S. Brax
 * @author R. Bello
 * 
 * @version 2.0
 */
class Attr extends Param {

    protected String name = null;
	private Object bean;

    public Attr(XmlConfig conf, File src, Node attrNode, Object beanInstance) 
        throws XmlConfigException {
        super(conf, src, attrNode);
        bean = beanInstance;
        name = conf.getAttributeValue(attrNode, "name");
        if (name == null) {
            throw new XmlConfigException(src, "<attr> element must have a 'name' attribute");
        }
    }

    public String getName() {
        return name;
    }
    
    public Object getBean() {
    	return bean;
    }

	public Method getSetterMethod() {
		String methodName = ReflectionUtils.getSetterMethodName(name);
		Class<?>[] argumentsTypes = new Class[] { getClazz() };
		return ReflectionUtils.getMethodMatching(bean.getClass(), methodName, argumentsTypes);
	}
	
	public String getSetterMethodName() {
		String methodName = ReflectionUtils.getSetterMethodName(name);
		Class<?>[] argumentsTypes = new Class[] { getClazz() };
		return ReflectionUtils.getMethodSignature(methodName, argumentsTypes);
	}

}
