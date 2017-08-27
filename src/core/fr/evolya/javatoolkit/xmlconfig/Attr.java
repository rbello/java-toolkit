package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Method;

import org.w3c.dom.Element;

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

	/**
	 * Constructor for a node <attr>
	 */
    public Attr(XmlConfig conf, File src, Element attrNode, Object beanInstance) 
        throws XmlConfigException {
        super(conf, src, attrNode);
        bean = beanInstance;
        name = conf.getAttributeValue(attrNode, "name");
        if (name == null) {
            throw new XmlConfigException(src, "<attr> element must have a 'name' attribute");
        }
    }

    /**
     * Constructor for <bean attributeName="attributeValue">
     */
    public Attr(XmlConfig conf, File src, org.w3c.dom.Attr node, Object beanInstance) 
    		throws XmlConfigException {
		super(conf, src, node, beanInstance);
		bean = beanInstance;
		name = node.getNodeName();
	}

	public String getName() {
        return name;
    }
    
    public Object getBean() {
    	return bean;
    }

	public Method getSetterMethod() {
		String methodName = ReflectionUtils.getSetterMethodName(name);
		Class<?>[] argumentsTypes = new Class[] { getType() };
		return ReflectionUtils.getMethodMatching(bean.getClass(), methodName, argumentsTypes);
	}
	
	public String getSetterMethodName() {
		String methodName = ReflectionUtils.getSetterMethodName(name);
		Class<?>[] argumentsTypes = new Class[] { getType() };
		return ReflectionUtils.getMethodSignature(methodName, argumentsTypes);
	}

	public void invoke(File src, String beanName, String descriptor) throws XmlConfigException {
		Method method = getSetterMethod();
		
		if (method == null) {
			throw new XmlConfigException(src, "Setter method '%s::%s' doesn't exists in bean '%s' for " + descriptor,
					bean.getClass().getName(), getSetterMethodName(), beanName, getName());
		}
		try {
			method.invoke(bean, new Object[] { getValue() });
		}
		catch (Exception ex) {
			throw new XmlConfigException(src, ex, "Error while invoking setter '%s::%s' in bean '%s' for " + descriptor,
					bean.getClass().getName(), getSetterMethodName(), beanName, getName());
		}
	}

}
