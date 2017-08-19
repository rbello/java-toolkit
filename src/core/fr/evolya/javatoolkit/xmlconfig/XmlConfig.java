package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;
import fr.evolya.javatoolkit.code.utils.XmlUtils;

/**
 * This class constructs objects from an XML file.
 * 
 * @author Antti S. Brax
 * @author Jeon Jiwon
 * @author R. Bello
 * 
 * @version 2.0
 */
public class XmlConfig {
	
	public static final Logger LOGGER = Logs.getLogger("IOC");

	/**
	 * This attribute contains the current beans.
	 */
	protected Map<String, Object> beans = null;
	
	/**
	 * This attribute contains the current properties.
	 */
	protected Map<String, String> properties = null;

	private boolean replaceVariables;

	/**
	 * Create an empty configuration.
	 */
	public XmlConfig() {
		beans 		= Collections.synchronizedMap(new HashMap<String, Object>());
		properties 	= Collections.synchronizedMap(new HashMap<String, String>());
	}

	/**
	 * Read a configuration from the InputStream.
	 * 
	 * @param in
	 *            the input stream which provides the new configuration.
	 * @exception 
	 *            the configuration process failed.
	 *            
	 * @see #addConfiguration(java.io.InputStream)
	 */
	public XmlConfig(InputStream in) throws XmlConfigException, IOException {
		this();
		addConfiguration(in);
	}

	/**
	 * Read a configuration from the File.
	 * 
	 * @exception 
	 *            the configuration process failed.
	 * 
	 * @see #addConfiguration(java.io.File)
	 */
	public XmlConfig(File file) throws XmlConfigException, IOException {
		this();
		addConfiguration(file);
	}

	// ==================================================================== //

	/**
	 * Add configuration from the specified file.
	 * @throws XmlConfigException 
	 */
	public void addConfiguration(File file) throws IOException, XmlConfigException {
		addConfiguration(file, new FileInputStream(file));
	}

	/**
	 * Add configuration from the specified input stream.
	 * @throws XmlConfigException 
	 */
	public void addConfiguration(InputStream in) throws IOException, XmlConfigException {
		addConfiguration(null, in);
	}
	
	/**
	 * Add configuration from the specified input stream.
	 */
	protected synchronized void addConfiguration(File src, InputStream in)
		throws XmlConfigException, IOException {
		
		// Create document builder factory
		DocumentBuilder db;
		try {
			db = XmlUtils.createDocumentBuilder();
		}
		catch (ParserConfigurationException ex) {
			throw new XmlConfigException("Could not create XML parser", ex);
		}

		// Read and parse document
		Document doc;
		try {
			doc = db.parse(in);
		}
		catch (IOException ex) {
			throw new XmlConfigException(src, "Error while reading configuration. Check the given file is readdable.", ex);
		}
		catch (SAXException ex) {
			throw new XmlConfigException(src, "Could not parse configuration file. Check the file has a well-formed XML format.", ex);
		}
		
		// Log
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, "Parse file: " + src.getAbsolutePath());
		}
		
		// Handle document
		handleConf(src, doc.getDocumentElement());
		
	}

	
	
	/**
	 * Get a string property.
	 * 
	 * @param name
	 *            the name of the property.
	 */
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	/**
	 * Get a named bean.
	 * 
	 * @param name
	 *            the name of the bean.
	 */
	public Object getBean(String name) {
		return beans.get(name);
	}
	
	/**
	 * Get a named bean.
	 * 
	 * @param name
	 *            the name of the bean.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(String name, Class<T> type) {
		if (!beans.containsKey(name)) {
			LOGGER.log(Logs.WARNING, "Bean not found: " + name);
			return null;
		}
		Object bean = beans.get(name);
		if (!type.isInstance(bean)) {
			LOGGER.log(Logs.WARNING, "Bean with name '" + name + "' is not an instance of " + type.getName());
			return null;
		}
		return (T) beans.get(name);
	}
	
	/**
	 * Return the number of properties stored in the current configuration.
	 */
	public int getPropertiesCount() {
		return properties.size();
	}

	/**
	 * Return the number of named beans stored in the current configuration.
	 */
	public int getBeansCount() {
		return beans.size();
	}

	// ==================================================================== //

	protected void handleConf(File src, Element rootElement) throws XmlConfigException {

		// See if the element contains the replacevars attribute.
		replaceVariables = XmlUtils.getBooleanAttribute(rootElement, "replacevars", true);
		
		// Fetch child nodes
		XmlUtils.<XmlConfigException>forEachChildNodes(rootElement, (child) -> {
			switch (child.getNodeName()) {
			// <include>path</include>
			case "include": 
				try {
					handleInclude(src, child);
				} catch (IOException e) {
					throw new XmlConfigException(src, e, "Unable to include: %s", getTextContent(child));
				}
				break;
			// <bean name="Logical name" class="path.Class">
			case "bean": handleBean(src, child, null); break;
			// <property name="Propety name">value</property>
			case "property":
				String name = getAttributeValue(child, "name");
				String value = getTextContent(child);
				if (XmlUtils.getBooleanAttribute(child, "system", false)) {
					System.setProperty(name, value);
				}
				else {
					properties.put(name, value);
				}
				break;
			default:
				throw new XmlConfigException(src, "Unknown element <%s> inside <conf> element", child.getNodeName());
			}
		});
	}

	protected void handleInclude(File src, Element elem)
			throws XmlConfigException, IOException {
		String source = getTextContent(elem);
		if (source.trim().isEmpty()) {
			throw new XmlConfigException(src, "Include target must be in the text content " +
					"of the <include> element");
		}
		addConfiguration(new File(source));
	}

	protected Object handleBean(File src, Element beanNode, Class<?> beanClass)
			throws XmlConfigException {

		// If the bean has a logical name then see if a previously configured
		// instance is available.
		Object beanInstance = null;
		String beanName = getAttributeValue(beanNode, "name");
		if (beanName != null) {
			beanInstance = beans.get(beanName);
			if (beanInstance != null) {
				beanClass = beanInstance.getClass();
			}
		}

		// If there is no previously configured instance available and
		// the class attribute is specified then create a new object.
		if (beanClass == null) {
			String className = getAttributeValue(beanNode, "class");
			if (className == null) {
				throw new XmlConfigException(src, "Element <bean> must have a 'class' attribute");
			}
			try {
				beanClass = Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				throw new XmlConfigException(src, e, "Unable to create <bean> with a not found class: %s", className);
			}
		}
		if (beanInstance == null) {
			beanInstance = createBeanInstance(src, beanClass, beanName, beanNode);
		}
		
		final Object beanInstanceCopy = beanInstance;
		
		// Fetch childs of bean's node
		XmlUtils.<XmlConfigException>forEachChildNodes(beanNode, (child) -> {
			switch (child.getNodeName()) {
			case "attr": handleAttr(src, beanInstanceCopy, beanName, child); break;
			case "call": handleCall(src, beanInstanceCopy, beanName, child); break;
			case "list": handleList(src, beanInstanceCopy, beanName, child); break;
			case "constructor": break; // Already processed
			default:
				throw new XmlConfigException(src, "Unknown element <%s> in <bean>", child.getNodeName());
			}
		});

		// Save the bean if a logical name was given.
		if (beanName != null && !beanName.isEmpty()) {
			beans.put(beanName, beanInstance);
			LOGGER.log(Logs.DEBUG, "Create bean: " + beanName + " (" + beanClass.getName() + ")");
		}
		else {
			LOGGER.log(Logs.DEBUG_FINE, "Create bean: " + beanClass.getName());
		}

		return beanInstance;
	}

	private <T> T createBeanInstance(File src, Class<T> type, String name, Element node)
			throws XmlConfigException {
		// Check if given type is an interface
		if (type.isInterface()) {
			throw new XmlConfigException(src,
					"An interface was defined as bean class (bean %s, interface %s)",
					name, type.getName());
		}
		// Get the defined constructor (if any).
		List<Node> constructors = XmlUtils.getChildrenByTagName(node, "constructor");
		if (constructors.size() > 1) {
			throw new XmlConfigException(src, "Multiple constructors defined for bean '%s'", name);
		}
		try {
			// Initialize with a constructor
			if (constructors.size() > 0) {
				return initializeBean(src, type, constructors.get(0));
			}
			// Initialize without constructor
			return type.newInstance();
		}
		catch (Throwable t) {
			throw new XmlConfigException(src, t, "Unable to create a new instance of " +
					"class %s (bean %s)", type.getName(), name);
		}
//	} else {
//		String msg;
//		if (name != null) {
//			msg = "No class given to a new bean (" + name + ")";
//		} else {
//			msg = "No class given to an unnamed bean";
//		}
//		throw new XmlConfigException(msg);
//	}
//} else if (clazzName != null && clazzName.length() != 0
//		&& clazzName.equals(bean.getClass().getName()) == false) {
//	String msg = "Bean's (" + name + ") class has changed from "
//			+ clazzName + " to " + bean.getClass().getName();
//	throw new XmlConfigException(msg);
//}
	}

	protected void handleAttr(File src, Object bean, String beanName, Node node)
			throws XmlConfigException {
		// Create attribute
		Attr attr = new Attr(this, src, node, bean);
		
		Method method = attr.getSetterMethod();
		
		if (method == null) {
			throw new XmlConfigException(src, "Class %s does not contain method %s",
					bean.getClass().getName(), attr.getSetterMethodName());
		}
		try {
			method.invoke(bean, new Object[] { attr.getValue() });
		}
		catch (Exception ex) {
			throw new XmlConfigException(src, ex, "Error while invoking %s::%s",
					bean.getClass().getName(), attr.getSetterMethodName());
		}
	}

	protected void handleCall(File src, Object bean, String beanName, Node call)
		throws XmlConfigException {

		String methodName = getAttributeValue(call, "name");
		if (methodName == null) {
			throw new XmlConfigException(src, "Missing 'name' attribute in <call>");
		}

		Class<?> beanClass = bean.getClass();
		List<Node> paramsNodes = XmlUtils.getChildrenByTagName((Element) call, "param");

		Class<?>[] paramsClasses = new Class[paramsNodes.size()];
		Object[] paramsValues = new Object[paramsNodes.size()];
		for (int i = 0; i < paramsNodes.size(); i++) {
			Param param = new Param(this, src, paramsNodes.get(i));
			paramsClasses[i] = param.getClazz();
			paramsValues[i] = param.getValue();
		}

		Method method = ReflectionUtils.getMethodMatching(beanClass, methodName, paramsClasses);
		if (method == null) {
			throw new XmlConfigException(src, "Class %s does not contain method %s",
					beanClass.getName(), ReflectionUtils.getMethodSignature(methodName, paramsClasses));
		}

		try {
			method.invoke(bean, paramsValues);
		}
		catch (Exception ex) {
			throw new XmlConfigException(src, ex, "Error while invoking %s::%s",
					beanClass.getName(), ReflectionUtils.getMethodSignature(methodName, paramsClasses));
		}
		
	}

	protected void handleList(File src, Object bean, String beanName, Element list)
			throws XmlConfigException {
		
		if (beanName == null) beanName = bean.getClass().getSimpleName();
		
		String listAttributeName = getAttributeValue(list, "name");
		if (listAttributeName == null) {
			throw new XmlConfigException(src, "Missing 'name' attribute for <list> in bean '%s'",
					beanName);
		}
		
		String listClassName = getAttributeValue(list, "class");
		if (listClassName == null) {
        	throw new XmlConfigException(src, "Missing 'class' attribute for <list name='%s'> in bean '%s'",
        			listAttributeName, beanName);
		}
		
		Class<?> listElementClass;
		try {
			listElementClass = Class.forName(listClassName);
		}
		catch (ClassNotFoundException e) {
			throw new XmlConfigException(src, "Class not found '%s' for <list name='%s'> in bean '%s'",
					listClassName, listAttributeName, beanName);
		}
		Class<?> beanClass = bean.getClass();
		
		IHandler[] handlers = new IHandler[] {
				new ListAdderMethod(), new ListGetterMethod(), new ListSetterMethod()
		};
		
		for (IHandler handler : handlers) {
		
			try {
				
				// Get the right method
				Method method = ReflectionUtils.getMethodMatching(
						beanClass,
						handler.getMethodName(listAttributeName),
						handler.getMethodParams(beanClass, listElementClass));
				
				// No method found, try something else
				if (method == null)
					continue;
			
				// Check if method is available
				if (!handler.checkMethod(src, method, beanClass, bean, listAttributeName,
						listElementClass))
					continue;
				
				// Fetch child nodes
				XmlUtils.forEachChildNodes(list, (child) -> {
					handler.invoke(this, method, src, bean, child, listElementClass);
				});
			
			}
			catch (Exception ex) {
				throw new XmlConfigException(src, ex, "Unable to create <list name='%s' class='%s'> in bean '%s'",
						listAttributeName, listClassName, beanName);
			}
			
			// It's done
			return;
		}

		
		throw new XmlConfigException(src, "No adder/getter/setter method for <list name='%s' class='%s'> in bean '%s'",
				listAttributeName, beanName);
	}

	// ==================================================================== //

	protected <T> T initializeBean(File src, Class<T> clazz, Node node) throws XmlConfigException {

		if (Modifier.isAbstract(clazz.getModifiers())) {
			throw new XmlConfigException(src, "Unable to create instance of abstract class '%s'",
					clazz.getName());
		}
		
		List<Node> params = XmlUtils.getChildrenByTagName((Element) node, "param");
		Class<?>[] p_classes = new Class[params.size()];
		Object[] p_values = new Object[params.size()];
		for (int i = 0; i < params.size(); i++) {
			Param param = new Param(this, src, (Node) params.get(i));
			p_classes[i] = param.getClazz();
			p_values[i] = param.getValue();
		}

		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor(p_classes);
			return (T) constructor.newInstance(p_values);
		}
		catch (Throwable t) {
			throw new XmlConfigException("Failed to invoke method "
					+ clazz.getName() + "."
					+ ReflectionUtils.getMethodSignature("<init>", p_classes), t);
		}
	}

	/**
	 * Replace ${name} type variables from the string.
	 */
	protected String replaceVariables(String orig) {

		if (replaceVariables == false || orig == null) {
			return orig;
		}

		StringBuffer sb = new StringBuffer();
		int start, end, index;

		index = 0;
		while ((start = orig.indexOf("${", index)) != -1) {
			sb.append(orig.substring(index, start));
			index = start;
			if ((end = orig.indexOf("}", index)) != -1) {
				String key = orig.substring(start + 2, end);
				index = end + 1;
				String val = properties.get(key);
				if (val != null) {
					sb.append(val);
				}
			} else {
				break;
			}
		}
		if (index < orig.length()) {
			sb.append(orig.substring(index, orig.length()));
		}
		return sb.toString();
	}

	/**
	 * Get the text content of the specified element as a String.
	 */
	protected String getTextContent(Node elem) {
		Node n = elem.getFirstChild();
		if (n == null) return "";
		return replaceVariables(n.getNodeValue().trim());
	}

	/**
	 * Get the name of an attribute.
	 * 
	 * @param name
	 *            the name of the attribute.
	 */
	protected String getAttributeValue(Node elem, String name) {
		Node node = elem.getAttributes().getNamedItem(name);
		if (node == null) return null;
		return replaceVariables(node.getNodeValue());
	}

}
