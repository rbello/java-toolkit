package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.w3c.dom.Element;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

/**
 * Searching the GETTER method (get the list, then use the list's add method)
 */
public class ListGetterMethod implements IListHandler {

	private List<Object> list;

	@Override
	public String getMethodName(String attributeName) {
		return ReflectionUtils.getGetterMethodName(attributeName);
	}

	@Override
	public Class<?>[] getMethodParams(Class<?> beanClass, Class<?> listElementClass) {
		return new Class[] { };
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkMethod(File src, Method method, Class<?> beanClass, Object beanInstance,
			String listAttributeName, Class<?> listElementClass) throws Exception {
		// The class has a getter, but this method doesn't return a list
		if (!method.getReturnType().equals(List.class)) {
			// Nevermind, the getter method should be for other things
			XmlConfig.LOGGER.log(Logs.WARNING, String.format("Class %s has a getter method for attribute %s but the return type is not List<%s>",
					beanClass.getName(), listAttributeName, listElementClass.getSimpleName()));
		}
		list = (List<Object>) method.invoke(beanInstance, new Object[0]);
		if (list == null) {
			throw new XmlConfigException(src, "Attribute %s::%s of type %s is not initialized",
					beanClass.getName(), listAttributeName, listElementClass.getSimpleName());
			// TODO Else let the setter method search to create a list ?
		}
		return true;
	}

	@Override
	public void invoke(XmlConfig cfg, Method method, File src, Object beanInstance,
			Element childNode, Class<?> listElementClass) throws Exception {
		list.add(cfg.handleListItem(src, childNode, listElementClass));
	}

}
