package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.lang.reflect.Method;

import org.w3c.dom.Element;

public interface IHandler {

	String getMethodName(String attributeName);

	Class<?>[] getMethodParams(Class<?> beanClass, Class<?> listElementClass);

	boolean checkMethod(File src, Method method, Class<?> beanClass, Object beanInstance,
			String listAttributeName, Class<?> listElementClass) throws Exception;

	void invoke(XmlConfig cfg, Method method, File src, Object beanInstance, 
			Element childNode, Class<?> listElementClass) throws Exception;

}
