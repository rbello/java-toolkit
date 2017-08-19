package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

/**
 * Searching the GETTER method (get the list, then use the list's add method)
 */
public class ListSetterMethod implements IListHandler {

	private List<Object> list;

	@Override
	public String getMethodName(String attributeName) {
		return ReflectionUtils.getSetterMethodName(attributeName);
	}

	@Override
	public Class<?>[] getMethodParams(Class<?> beanClass, Class<?> listElementClass) {
		return new Class[] { List.class };
	}
	
	@Override
	public boolean checkMethod(File src, Method method, Class<?> beanClass, Object beanInstance,
			String listAttributeName, Class<?> listElementClass) throws Exception {
		
		list = new ArrayList<Object>();
		method.invoke(beanInstance, new Object[] { list });
		return true;
	}

	@Override
	public void invoke(XmlConfig cfg, Method method, File src, Object beanInstance,
			Element childNode, Class<?> listElementClass) throws Exception {
		list.add(cfg.handleListItem(src, childNode, listElementClass));
	}

}
