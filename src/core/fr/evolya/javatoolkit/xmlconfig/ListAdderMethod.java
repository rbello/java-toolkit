package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Method;

import org.w3c.dom.Element;

import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

/**
 * Searching the ADDER method (use the add method)
 */
public class ListAdderMethod implements IListHandler {

	@Override
	public String getMethodName(String attributeName) {
		return ReflectionUtils.getAdderMethodName(attributeName);
	}

	@Override
	public Class<?>[] getMethodParams(Class<?> beanClass, Class<?> listElementClass) {
		return new Class[] { listElementClass };
	}

	@Override
	public boolean checkMethod(File src, Method method, Class<?> beanClass, Object beanInstance,
			String listAttributeName, Class<?> listElementClass) throws Exception {
		return true;
	}

	@Override
	public void invoke(XmlConfig cfg, Method method, File src, Object beanInstance,
			Element childNode, Class<?> listElementClass) throws Exception {
		Object[] methodParams = new Object[] { cfg.handleListItem(src, childNode, listElementClass) };
		method.invoke(beanInstance, methodParams);
	}

}
